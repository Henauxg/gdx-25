package fr.baldurcrew.gdx25;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.character.Character;
import fr.baldurcrew.gdx25.character.CharacterResources;
import fr.baldurcrew.gdx25.physics.WorldContactListener;
import fr.baldurcrew.gdx25.utils.Range;
import fr.baldurcrew.gdx25.water.WaterSimulation;
import fr.baldurcrew.gdx25.water.WaveEmitter;

import java.util.ArrayList;
import java.util.List;


public class CoreGame extends ApplicationAdapter {

    private static final Color CLEAR_COLOR = new Color(0.5f, 0.898f, 1, 1);
    private static final Color DEBUG_CLEAR_COLOR = new Color(1f, 1f, 1f, 1f);
    public static boolean debugMode = true;
    public static boolean debugClearColor = false;
    public static boolean debugEnableWaterRendering = true;
    public static boolean debugEnableBoatRendering = true;
    public static boolean debugEnableWaveGeneration = true;
    public static boolean debugEnableWaterDrag = true;

    World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;
    private List<Character> characters;
    private float accumulator = 0;

    private WaterSimulation water;
    private Boat boat;
    private WaveEmitter waveEmitter;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Box2D.init();
        debugRenderer = new Box2DDebugRenderer();
        CharacterResources.getInstance();

        createTestLevel();
    }

    public void createTestLevel() {
        world = new World(new Vector2(0, Constants.GRAVITY_VALUE), true);
        final var contactListener = new WorldContactListener();
        world.setContactListener(contactListener);

        characters = new ArrayList<>();
        characters.add(new Character(CharacterResources.GREEN, world));
        characters.add(new Character(CharacterResources.BEIGE, world));
//        characters.add(new Character(CharacterResources.BLUE, world));
//        characters.add(new Character(CharacterResources.PINK, world));
//        characters.add(new Character(CharacterResources.YELLOW, world));

        final var waterSimulationRange = Range.buildRange(-0.25f * Constants.VIEWPORT_WIDTH, 1.25f * Constants.VIEWPORT_WIDTH);
        // Only simulate physics under the boat
        final float physicSimualtinMargin = Boat.BOAT_WIDTH * 0.2f;
        final var waterPhysicsSimulationRange = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f - physicSimualtinMargin, Boat.BOAT_WIDTH + 2 * physicSimualtinMargin);
        water = new WaterSimulation(world, 80, waterSimulationRange, waterPhysicsSimulationRange);
        boat = new Boat(world, Constants.VIEWPORT_WIDTH / 2f, water.getWaterLevel() + 1f);
        waveEmitter = new WaveEmitter(water, new Vector2(0.5f, 1.5f), new Vector2(4f, 6.5f)); // TODO Evolve over time to increase the difficulty

        contactListener.addListener(water);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInputs(camera);

        doPhysicsStep(deltaTime);

        camera.update();

        if (debugClearColor) {
            ScreenUtils.clear(DEBUG_CLEAR_COLOR);
        } else {
            ScreenUtils.clear(CLEAR_COLOR);
        }


        if (debugMode) {
            debugRenderer.render(world, camera.combined);
        }

        boat.render(camera);
        water.render(camera);
        characters.forEach(character -> character.render(camera));
    }

    public void handleInputs(OrthographicCamera camera) {
        characters.forEach(character -> character.handleInputs());

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }
        if (debugMode) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                debugEnableWaterRendering = !debugEnableWaterRendering;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                debugEnableBoatRendering = !debugEnableBoatRendering;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                debugClearColor = !debugClearColor;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
                debugEnableWaveGeneration = !debugEnableWaveGeneration;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
                debugEnableWaterDrag = !debugEnableWaterDrag;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
                disposeCurrentLevel();
                createTestLevel();
            }
        }

        if (Gdx.input.justTouched()) {
            float xViewportPercent = (float) Gdx.input.getX() / (float) Gdx.graphics.getWidth();
            float xWorld = xViewportPercent * Constants.VIEWPORT_WIDTH;

            water.handleInput(xWorld);
        }
    }

    private void doPhysicsStep(float deltaTime) {
        // Fixed time step.
        // Max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= Constants.TIME_STEP) {
            characters.forEach(c -> c.update());
            waveEmitter.update();
            water.update();
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            boat.update();
            accumulator -= Constants.TIME_STEP;
        }
    }

    private void disposeCurrentLevel() {
        water.dispose();
        boat.dispose();
        characters.forEach(c -> c.dispose());
    }

    @Override
    public void dispose() {
        debugRenderer.dispose();
        disposeCurrentLevel();
    }
}
