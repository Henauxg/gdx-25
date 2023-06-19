package fr.baldurcrew.gdx25;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;


public class CoreGame extends ApplicationAdapter {

    World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;

    private float accumulator = 0;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Box2D.init();
        world = new World(new Vector2(0, -Constants.GRAVITY_VALUE), true);
        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(1, 0, 0, 1);

        debugRenderer.render(world, camera.combined);
        doPhysicsStep(deltaTime);
    }

    private void doPhysicsStep(float deltaTime) {
        // Fixed time step.
        // Max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= Constants.TIME_STEP) {
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            accumulator -= Constants.TIME_STEP;
        }
    }

    @Override
    public void dispose() {

    }
}
