package fr.baldurcrew.gdx25;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.physics.WorldContactListener;
import fr.baldurcrew.gdx25.water.WaterSimulation;


public class CoreGame extends ApplicationAdapter {

    private static final Color CLEAR_COLOR = new Color(0.5f, 0.898f, 1, 1);
    public static boolean debugMode = false;

    World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;
    private Character character;
    private float accumulator = 0;

    private WaterSimulation water;
    private Boat boat;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Box2D.init();
        world = new World(new Vector2(0, Constants.GRAVITY_VALUE), true);
        final var contactListener = new WorldContactListener();
        world.setContactListener(contactListener);
        debugRenderer = new Box2DDebugRenderer();

        character = new Character(2);
        water = new WaterSimulation(world, 80, -0.25f * Constants.VIEWPORT_WIDTH, 1.25f * Constants.VIEWPORT_WIDTH);
        boat = new Boat(world, Constants.VIEWPORT_WIDTH / 2f, water.getWaterLevel() + 1f);

        contactListener.addListener(water);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInputs(camera);

        doPhysicsStep(deltaTime);

        camera.update();

        ScreenUtils.clear(CLEAR_COLOR);

        if (debugMode) {
            debugRenderer.render(world, camera.combined);
        }

        boat.render(camera);
        water.render(camera);
        character.render();
    }

    public void handleInputs(OrthographicCamera camera) {
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
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            water.update();
            accumulator -= Constants.TIME_STEP;
        }
    }

    @Override
    public void dispose() {
        water.dispose();
        boat.dispose();
    }
}
