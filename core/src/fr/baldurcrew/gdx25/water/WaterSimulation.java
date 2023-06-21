package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.CoreGame;
import fr.baldurcrew.gdx25.Utils;
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

    private Body createWaterBody(World world, float fromX, float toX) {
        final var halfWidth = (toX - fromX) / 2f;
        final var halfHeight = baseWaterLevel / 2f;
        final var centerX = fromX + halfWidth;
        final var centerY = halfHeight;

        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody; // TODO Might just be static if not using fixtures for waves
        bodyDef.position.set(centerX, centerY);
        bodyDef.gravityScale = 0;

        final var body = world.createBody(bodyDef);
        body.setUserData(this);

        final var waterPolygon = new PolygonShape();
        // TODO generate multiple fixtures for the water body
        // TODO Optimization: Create water fixtures for waves only under the boat
        waterPolygon.setAsBox(halfWidth, halfHeight);

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = waterPolygon;
        fixtureDef.isSensor = true;
        fixtureDef.density = 1.0f; // TODO Tweak ?

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
        // TODO Should depend on Constants.TIME_STEP
        updateSprings();
        updateImmersedFixtures();
    }

    // TODO Check: Should update fixtureContacts before this
    private void updateImmersedFixtures() {
        this.fixtureContacts.forEach(contact -> {
            final var fixtureA = contact.fixtureA();
            final var fixtureB = contact.fixtureB();

            float fluidDensity = fixtureA.getDensity();

            final var intersection = Utils.getIntersection(fixtureA, fixtureB);
            if (intersection != null && !intersection.isEmpty()) {
                final var world = fixtureB.getBody().getWorld();

                final var polygon = Utils.getPolygon(intersection);
                final float area = polygon.area();
                final var centroid = GeometryUtils.polygonCentroid(polygon.getVertices(), 0, polygon.getVertices().length, new Vector2());

                // Apply buoyancy
                final var displacedMass = fluidDensity * area;
                Vector2 buoyancyForce = new Vector2(displacedMass * -world.getGravity().x,
                        displacedMass * -world.getGravity().y);
                fixtureB.getBody().applyForce(buoyancyForce, centroid, true);
            }
        });
    }

    private void updateSprings() {
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
        if (CoreGame.debugMode) {
            if (xWorld >= fromX && xWorld <= toX) {
                final float index = this.springs.size() * (xWorld - fromX) / (toX - fromX);
                this.disturbWater(Math.round(index), 5f);
            }
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
