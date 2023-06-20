package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.physics.ContactHandler;
import fr.baldurcrew.gdx25.physics.ContactStatus;
import fr.baldurcrew.gdx25.physics.FixtureContact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WaterSimulation implements Disposable, ContactHandler {

    private final float fromX, toX;
    private final ArrayList<Spring> springs;
    private final Set<FixtureContact> fixtureContacts;

    private final Body waterBody;
    private final WaterRenderer renderer;

    // TODO Tweak
    private float wavesPropagationPasses = 4; // 8
    private float wavesPropagationSpreadFactor = 0.2f;
    private float springsStiffness = 0.025f;
    private float springsDampeningFactor = 0.025f;
    private float baseWaterLevel = 5f;

    public WaterSimulation(World world, int springsCount, float fromX, float toX) {
        this.fromX = fromX;
        this.toX = toX;
        this.springs = new ArrayList<>(springsCount);
        this.fixtureContacts = new HashSet<>();

        final var totalLength = toX - fromX;
        final var springPlacementStep = totalLength / (springsCount - 1);
        for (int i = 0; i < springsCount; i++) {
            final float x = fromX + i * springPlacementStep;
            springs.add(new Spring(x, baseWaterLevel));
        }

        renderer = new WaterRenderer(springsCount - 1);
        waterBody = createWaterBody(world, fromX, toX);
    }

    // TODO Optimization: Create water fixtures for waves only under the boat
    private Body createWaterBody(World world, float fromX, float toX) {
        final var halfWidth = (toX - fromX) / 2f;
        final var halfHeight = baseWaterLevel / 2f;
        final var centerX = fromX + halfWidth;
        final var centerY = halfHeight;

        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);

        final var body = world.createBody(bodyDef);
        // TODO May need user data

        final var waterPolygon = new PolygonShape();
        // TODO generate vertices for the water body
        waterPolygon.setAsBox(halfWidth, halfHeight);

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = waterPolygon;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        waterPolygon.dispose();

        return body;
    }

    public void handleContact(ContactStatus status, FixtureContact contact) {
        switch (status) {
            case Begin -> {
                fixtureContacts.add(contact);
            }
            case End -> {
                fixtureContacts.remove(contact);
            }
        }
    }

    public void update() {
        for (var spring : this.springs) {
            spring.update(springsStiffness, springsDampeningFactor, baseWaterLevel);
        }

        float[] leftDeltas = new float[springs.size()];
        float[] rightDeltas = new float[springs.size()];

        for (int j = 0; j < wavesPropagationPasses; j++) {
            for (int i = 0; i < springs.size(); i++) {
                final var spring = springs.get(i);
                if (i > 0) {
                    final var leftSpring = springs.get(i - 1);
                    leftDeltas[i] = wavesPropagationSpreadFactor * (spring.getHeight() - leftSpring.getHeight());
                    leftSpring.addVelocity(leftDeltas[i]);
                }
                if (i < springs.size() - 1) {
                    final var rightSpring = springs.get(i + 1);
                    rightDeltas[i] = wavesPropagationSpreadFactor * (spring.getHeight() - rightSpring.getHeight());
                    rightSpring.addVelocity(rightDeltas[i]);
                }
            }

            for (int i = 0; i < springs.size(); i++) {
                if (i > 0) springs.get(i - 1).addHeight(leftDeltas[i]);

                if (i < springs.size() - 1) springs.get(i + 1).addHeight(rightDeltas[i]);
            }
        }
    }

    public void disturbWater(int index, float speed) {
        if (index >= 0 && index < springs.size()) {
            springs.get(index).addVelocity(speed);
        }
    }

    public void render(OrthographicCamera camera) {
        renderer.render(camera, springs);
    }

    public void handleInput(float xWorld) {
        if (xWorld >= fromX && xWorld <= toX) {
            // TODO Debug only
            final float index = this.springs.size() * (xWorld - fromX) / (toX - fromX);
            this.disturbWater(Math.round(index), 5f);
        }
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    public float getWaterLevel() {
        return this.baseWaterLevel;
    }
}
