package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.CoreGame;
import fr.baldurcrew.gdx25.physics.ContactHandler;
import fr.baldurcrew.gdx25.physics.FixtureContact;
import fr.baldurcrew.gdx25.utils.Range;
import fr.baldurcrew.gdx25.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaterSimulation implements Disposable, ContactHandler {

    /**
     * All the springs simulating the water surface. Fixed size and content during execution.
     */
    private final ArrayList<Spring> springs;
    /**
     * Sublist view of the springs list, containing the springs that are used for the physical simulation.
     */
    private final List<Spring> physicalSpringsSublist;
    private final Body waterBody;
    private final WaterRenderer renderer;
    private final Range waterRange;
    private final float springsSpacing;
    /**
     * Dynamic set of fixtures in contact with the water fixtures.
     */
    private final Set<FixtureContact> fixtureContacts;
    /**
     * Current physical representation of the water. Fixed size during execution.
     */
    private final Fixture[] waterFixtures;

    // TODO Tweak
    private int wavesPropagationPasses = 4; // 8
    private float wavesPropagationSpreadFactor = 0.2f;
    private float springsStiffness = 0.005f; // TODO Reduce
    private float springsDampeningFactor = 0.025f;
    private float baseWaterLevel = 5f;
    private float waterDensity = 1.0f;
    private float fakeWaterVelocityX = 15;
    private float fakeWaterVelocityY = 0;


    /**
     * @param world                  Physic world
     * @param springsCount           How many springs in the simulation
     * @param simulationRangeX       Range of the water simulation, on the X axis
     * @param physicSimulationRangeX Sub-range of the water simulation where physic is simulated (buoyancy, drag, waves, ...), on the X axis
     */
    public WaterSimulation(World world, int springsCount, Range simulationRangeX, Range physicSimulationRangeX) {
        this.waterRange = simulationRangeX;
        this.springs = new ArrayList<>(springsCount);
        this.fixtureContacts = new HashSet<>();

        springsSpacing = waterRange.extent / (springsCount - 1);
        for (int i = 0; i < springsCount; i++) {
            final float x = waterRange.from + i * springsSpacing;
            springs.add(new Spring(x, baseWaterLevel));
        }

        renderer = new WaterRenderer(springsCount - 1);
        waterBody = createWaterBody(world, new Vector2(simulationRangeX.getCenter(), baseWaterLevel / 2f));
        final int startIndex = Math.round(springs.size() * waterRange.percentage(physicSimulationRangeX.from));
        final int endIndex = Math.round(springs.size() * waterRange.percentage(physicSimulationRangeX.to));
        physicalSpringsSublist = springs.subList(startIndex, endIndex);
        waterFixtures = new Fixture[(physicalSpringsSublist.size())];
        generateFixtures(waterFixtures, waterBody, physicalSpringsSublist, springsSpacing);
    }

    private Body createWaterBody(World world, Vector2 center) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(center);

        final var body = world.createBody(bodyDef);
        body.setUserData(this);

        return body;
    }

    private void destroyFixtures() {
        for (Fixture f : this.waterFixtures) {
            waterBody.destroyFixture(f);
        }
    }

    private void generateFixtures(Fixture[] waterFixtures, Body body, List<Spring> springs, float springsSpacing) {
        for (int i = 0; i < springs.size(); i++) {
            final var waterPolygon = new PolygonShape();

            // TODO If needed, could group multiple springs together to form 1 fixture to reduce physics simulation stress
            final var spring = springs.get(i);
            final var halfSpringSpacing = springsSpacing / 2f;
            final var halfSpringHeight = spring.getHeight() / 2f;
            waterPolygon.setAsBox(halfSpringSpacing, halfSpringHeight, new Vector2(spring.getX() + halfSpringSpacing - body.getWorldCenter().x, halfSpringHeight - body.getWorldCenter().y), 0);

            final var fixtureDef = new FixtureDef();
            fixtureDef.shape = waterPolygon;
            fixtureDef.isSensor = true;
            fixtureDef.density = waterDensity;
            waterFixtures[i] = body.createFixture(fixtureDef);

            waterPolygon.dispose();
        }
    }

    public void handleContactBegin(FixtureContact contact) {
        fixtureContacts.add(contact);
    }

    public void handleContactEnd(FixtureContact contact) {
        fixtureContacts.remove(contact);
    }

    @Override
    public void handlePreSolve(Contact contact, FixtureContact fixtures) {
        // Nothing to do here
    }

    public void update() {
        // TODO Should depend on Constants.TIME_STEP
        updateSprings();
        updateImmersedFixtures();

        fixtureContacts.clear();
        destroyFixtures();
        generateFixtures(waterFixtures, waterBody, physicalSpringsSublist, springsSpacing);
    }

    private void updateImmersedFixtures() {
        this.fixtureContacts.forEach(contact -> {
            final var waterFixture = contact.handledFixture();
            final var immersedFixture = contact.otherFixture();

            final float fluidDensity = waterFixture.getDensity();

            final var intersectionVertices = Utils.getIntersection(waterFixture, immersedFixture);
            if (intersectionVertices != null && !intersectionVertices.isEmpty()) {
                final var world = immersedFixture.getBody().getWorld();

                final var intersectionPolygon = Utils.getPolygon(intersectionVertices);
                final float intersectionArea = intersectionPolygon.area();
                final var intersectionCentroid = GeometryUtils.polygonCentroid(intersectionPolygon.getVertices(), 0, intersectionPolygon.getVertices().length, new Vector2());

                // Apply buoyancy
                final var displacedMass = fluidDensity * intersectionArea;
                Vector2 buoyancyForce = new Vector2(displacedMass * -world.getGravity().x, displacedMass * -world.getGravity().y);
                immersedFixture.getBody().applyForce(buoyancyForce, intersectionCentroid, true);


                // Apply drag separately for each polygon edge
                for (int i = 0; i < intersectionVertices.size(); i++) {
                    final Vector2 v0 = intersectionVertices.get(i);
                    final Vector2 v1 = intersectionVertices.get((i + 1) % intersectionVertices.size());
                    final Vector2 midPoint = v0.cpy().add(v1).scl(0.5f);

                    // Find relative velocity between object and fluid at edge midpoint
                    Vector2 velDir;
                    if (CoreGame.debugEnableFakeWaterVelocity) {
                        velDir = immersedFixture.getBody().getLinearVelocityFromWorldPoint(midPoint).sub(new Vector2(fakeWaterVelocityX, fakeWaterVelocityY));
                    } else {
                        velDir = immersedFixture.getBody().getLinearVelocityFromWorldPoint(midPoint).sub(waterFixture.getBody().getLinearVelocityFromWorldPoint(midPoint));
                    }

                    final float vel = velDir.len();
                    velDir.nor();

                    final Vector2 edge = v1.cpy().sub(v0);
                    final float edgeLength = edge.len();
                    edge.nor();

                    final Vector2 normal = new Vector2(edge.y, -edge.x);
                    final float dragDot = normal.dot(velDir);

                    if (dragDot >= 0f) {
                        // Drag force
                        final float dragMag = dragDot * edgeLength * fluidDensity * vel * vel;
                        Vector2 dragForce = velDir.cpy().scl(-dragMag);
                        if (CoreGame.debugEnableWaterDrag) {
                            immersedFixture.getBody().applyForce(dragForce, midPoint, true);
                        }
                        // Lift force
                        final float liftDot = edge.dot(velDir);
                        final float liftMag = dragMag * liftDot;
                        final Vector2 liftDir = new Vector2(-velDir.y, velDir.x);
                        final Vector2 liftForce = liftDir.scl(liftMag);
                        if (CoreGame.debugEnableLiftForce) {
                            immersedFixture.getBody().applyForce(liftForce, midPoint, true);
                        }
                    }
                }


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
            final float index = this.springs.size() * waterRange.clampedPercentage(xWorld);
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

    public void setSpringsDampeningFactor(float springsDampeningFactor) {
        this.springsDampeningFactor = springsDampeningFactor;
    }

    public void setBaseWaterLevel(float baseWaterLevel) {
        // TODO Need to recreate springs
//        this.baseWaterLevel = baseWaterLevel;
    }

    public int getWavesPropagationPasses() {
        return wavesPropagationPasses;
    }

    public void setWavesPropagationPasses(int wavesPropagationPasses) {
        this.wavesPropagationPasses = wavesPropagationPasses;
    }

    public float getWavesPropagationSpreadFactor() {
        return wavesPropagationSpreadFactor;
    }

    public void setWavesPropagationSpreadFactor(float wavesPropagationSpreadFactor) {
        this.wavesPropagationSpreadFactor = wavesPropagationSpreadFactor;
    }

    public float getSpringsStiffness() {
        return springsStiffness;
    }

    public void setSpringsStiffness(float springsStiffness) {
        this.springsStiffness = springsStiffness;
    }

    public float getSpringsDampening() {
        return springsDampeningFactor;
    }

    public float getDensity() {
        return waterDensity;
    }

    public void setDensity(float density) {
        this.waterDensity = density;
        this.waterBody.getFixtureList().forEach(fixture -> {
            fixture.setDensity(density);
        });
    }

    public float getFakeWaterVelocityX() {
        return fakeWaterVelocityX;
    }

    public void setFakeWaterVelocityX(float value) {
        this.fakeWaterVelocityX = value;
    }

    public float getFakeWaterVelocityY() {
        return fakeWaterVelocityY;
    }

    public void setFakeWaterVelocityY(float value) {
        this.fakeWaterVelocityY = value;
    }
}
