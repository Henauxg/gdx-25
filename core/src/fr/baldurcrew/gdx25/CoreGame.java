package fr.baldurcrew.gdx25;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.character.Character;
import fr.baldurcrew.gdx25.character.CharacterResources;
import fr.baldurcrew.gdx25.character.CharacterSpawner;
import fr.baldurcrew.gdx25.layer.ParallaxLayer;
import fr.baldurcrew.gdx25.physics.WorldContactListener;
import fr.baldurcrew.gdx25.utils.Range;
import fr.baldurcrew.gdx25.utils.Utils;
import fr.baldurcrew.gdx25.water.WaterSimulation;
import fr.baldurcrew.gdx25.water.WaveEmitter;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;


public class CoreGame extends ApplicationAdapter {

    private static final Color CLEAR_COLOR = new Color(0.5f, 0.898f, 1, 1);
    private static final Color DEBUG_CLEAR_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final int INITIAL_CHARACTER_COUNT = 1;
    private static final float DEFAULT_AUDIO_VOLUME = 0.2f;

    public static boolean debugMode = true;
    public static boolean debugClearColor = false;
    public static boolean debugEnableCharacterGeneration = true;
    public static boolean debugEnableWaterRendering = true;
    public static boolean debugEnableBoatRendering = true;
    public static boolean debugEnableWaveGeneration = true;
    public static boolean debugEnableWaterDrag = true;
    public static boolean debugEnableFakeWaterVelocity = true;
    public SpriteBatch spriteBatch;
    private List<ParallaxLayer> parallaxLayers;
    private BitmapFont font;

    private World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;
    private List<Character> characters;
    private float accumulator = 0;

    private WorldContactListener worldContactListener;
    private WaterSimulation water;
    private Boat boat;
    private WaveEmitter waveEmitter;
    private Music waveSounds;
    private Music music;
    private float sailingTime;
    private CharacterSpawner characterSpawner;
    private float characterDensity = 0.6f;
    private float characterFriction = 0.5f;
    private float characterRestitution = 0.2f;
    private Range defaultWavePeriodRange = Range.buildRangeEx(0.5f, 1.5f);
    private Range defaultWaveAmplitudeRange = Range.buildRangeEx(4f, 6.5f);

    private ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private long windowHandle;
    //Character
    private float[] uiCharFriction = new float[1];
    private float[] uiCharDensity = new float[1];
    private float[] uiCharRestitution = new float[1];
    //Boat
    private float[] uiBoatDensity = new float[1];
    private float[] uiBoatRestitution = new float[1];
    private float[] uiBoatFriction = new float[1];
    //Water
    private int[] uiWaterWavesPropagationPasses = new int[1];
    private float[] uiWaterWavesPropagationSpreadFactor = new float[1];
    private float[] uiWaterSpringsStiffness = new float[1];
    private float[] uiWaterSpringsDampeningFactor = new float[1];
    private float[] uiWaterBaseWaterLevel = new float[1];
    private float[] uiWaterDensity = new float[1];
    private float[] uiWaterFakeVelocityX = new float[1];
    private float[] uiWaterFakeVelocityY = new float[1];
    private float[] uiWaveEmitterAmplitudeRange = new float[2];
    private float[] uiWaveEmitterPeriodRange = new float[2];

    @Override
    public void create() {
//        waveSounds = Gdx.audio.newMusic(Gdx.files.internal("DasLiedderSturme.mp3"));
//        waveSounds.setLooping(true);
//        waveSounds.setVolume(DEFAULT_AUDIO_VOLUME);
//        waveSounds.play();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Box2D.init();
        debugRenderer = new Box2DDebugRenderer();
        CharacterResources.getInstance();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont(); // libGDX's default Arial font

        createParallaxLayers();

        createImGui();
        createTestLevel();
    }

    private void createParallaxLayers() {
        parallaxLayers = new ArrayList<>();
//        parallaxLayers[0] = new ParallaxLayer(new Texture("0.png"), 0.1f, true, false);
//        parallaxLayers[1] = new ParallaxLayer(new Texture("1.png"), 0.2f, true, false);
//        parallaxLayers[2] = new ParallaxLayer(new Texture("2.png"), 0.3f, true, false);
//        parallaxLayers[3] = new ParallaxLayer(new Texture("3.png"), 0.5f, true, false);
//        parallaxLayers[4] = new ParallaxLayer(new Texture("4.png"), 0.8f, true, false);
        parallaxLayers.add(new ParallaxLayer(new Texture("5.png"), 1.5f, true, false));
        parallaxLayers.add(new ParallaxLayer(new Texture("6.png"), 1.2f, true, false));
    }

    private void createImGui() {
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();
        io.getFonts().build();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 110");
    }

    public void createTestLevel() {
        waveSounds = Gdx.audio.newMusic(Gdx.files.internal("nice_waves.mp3"));
        waveSounds.setLooping(true);
        waveSounds.setVolume(DEFAULT_AUDIO_VOLUME);
        waveSounds.play();

        world = new World(new Vector2(0, Constants.GRAVITY_VALUE), true);
        worldContactListener = new WorldContactListener();
        world.setContactListener(worldContactListener);


        final Range waterSimulationRange = Range.buildRangeEx(-0.25f * Constants.VIEWPORT_WIDTH, 1.25f * Constants.VIEWPORT_WIDTH);
        // Only simulate physics under the boat
        final float physicSimulationMargin = Boat.BOAT_WIDTH * 0.2f;
        final Range waterPhysicsSimulationRange = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f - physicSimulationMargin, Boat.BOAT_WIDTH + 2 * physicSimulationMargin);

        water = new WaterSimulation(world, 80, waterSimulationRange, waterPhysicsSimulationRange);
        worldContactListener.addListener(water);
        boat = new Boat(world, Constants.VIEWPORT_WIDTH / 2f, water.getWaterLevel() + 1f);
        waveEmitter = new WaveEmitter(water, defaultWavePeriodRange, defaultWaveAmplitudeRange); // TODO Evolve over time to increase the difficulty

        final float charsSpawnPadding = Boat.BOAT_WIDTH * 0.2f;
        final Range spawnRangeX = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f + charsSpawnPadding, Boat.BOAT_WIDTH - 2 * charsSpawnPadding);
        final Range spawnRangeY = Range.buildRangeEx(water.getWaterLevel() + Boat.BOAT_HEIGHT / 2f, water.getWaterLevel() + Boat.BOAT_HEIGHT * 1.5f);

        characters = new ArrayList<>();
        for (int i = 0; i < INITIAL_CHARACTER_COUNT; i++) {
            this.spawnCharacter(CharacterResources.getRandomCharacterIndex(), false, spawnRangeX.getRandom(), spawnRangeY.getRandom());
        }
        characterSpawner = new CharacterSpawner(this, spawnRangeX, spawnRangeY, Range.buildRangeEx(2.5f, 6f));

        sailingTime = 0;

        initTweakingUIValues();
    }

    private void initTweakingUIValues() {
        uiCharFriction[0] = characterFriction;
        uiCharDensity[0] = characterDensity;
        uiCharRestitution[0] = characterRestitution;
        //Boat
        uiBoatDensity[0] = boat.getDensity();
        uiBoatRestitution[0] = boat.getRestitution();
        uiBoatFriction[0] = boat.getFriction();
        //Water
        uiWaterWavesPropagationPasses[0] = water.getWavesPropagationPasses();
        uiWaterWavesPropagationSpreadFactor[0] = water.getWavesPropagationSpreadFactor();
        uiWaterSpringsStiffness[0] = water.getSpringsStiffness();
        uiWaterSpringsDampeningFactor[0] = water.getSpringsDampening();
        uiWaterBaseWaterLevel[0] = water.getWaterLevel();
        uiWaterDensity[0] = water.getDensity();
        uiWaterFakeVelocityX[0] = water.getFakeWaterVelocityX();
        uiWaterFakeVelocityY[0] = water.getFakeWaterVelocityY();
        uiWaveEmitterAmplitudeRange[0] = defaultWaveAmplitudeRange.from;
        uiWaveEmitterAmplitudeRange[1] = defaultWaveAmplitudeRange.to;
        uiWaveEmitterPeriodRange[0] = defaultWavePeriodRange.from;
        uiWaveEmitterPeriodRange[1] = defaultWavePeriodRange.to;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        sailingTime += deltaTime;

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

        spriteBatch.begin();

        Matrix4 originalMatrix = spriteBatch.getProjectionMatrix().cpy();
        spriteBatch.setProjectionMatrix(camera.combined);
        parallaxLayers.forEach(l -> l.render(camera, spriteBatch, deltaTime));

        boat.render(camera);
        water.render(camera);
        characters.forEach(character -> character.render(camera));

        spriteBatch.setProjectionMatrix(originalMatrix);
        font.draw(spriteBatch, Utils.secondsToDisplayString(sailingTime), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 10, 0, Align.center, false);

        spriteBatch.end();

        renderImGui();
    }

    private void renderImGui() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        // --- ImGUI draw commands go here ---
        drawUI();
        // ---
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void drawUI() {
        // Limit the width of the widgets
        ImGui.pushItemWidth(ImGui.getWindowWidth() * 0.35f);

        ImGui.text("Characters");
        if (ImGui.checkbox("AI Characters Generation", debugEnableCharacterGeneration)) {
            debugEnableCharacterGeneration = !debugEnableCharacterGeneration;
        }
        if (ImGui.sliderFloat("Char Friction", uiCharFriction, 0, 1)) {
            characterFriction = uiCharFriction[0];
            characters.forEach(c -> c.setFriction(uiCharFriction[0]));
        }
        if (ImGui.sliderFloat("Char Density", uiCharDensity, 0, 30f)) {
            characterDensity = uiCharDensity[0];
            characters.forEach(c -> c.setDensity(uiCharDensity[0]));
        }
        if (ImGui.sliderFloat("Char Restitution", uiCharRestitution, 0, 1)) {
            characterRestitution = uiCharRestitution[0];
            characters.forEach(c -> c.setRestitution(uiCharRestitution[0]));
        }
        ImGui.separator();
        ImGui.text("Boat");
        if (ImGui.checkbox("Boat rendering", debugEnableBoatRendering)) {
            debugEnableBoatRendering = !debugEnableBoatRendering;
        }
        if (ImGui.sliderFloat("Boat Density", uiBoatDensity, 0.25f, 1)) {
            boat.setDensity(uiBoatDensity[0]);
        }
        if (ImGui.sliderFloat("Boat Restitution", uiBoatRestitution, 0, 1)) {
            boat.setRestitution(uiBoatRestitution[0]);
        }
        if (ImGui.sliderFloat("Boat Friction", uiBoatFriction, 0, 1)) {
            boat.setFriction(uiBoatFriction[0]);
        }
        ImGui.separator();
        ImGui.text("Water");
        if (ImGui.checkbox("Water rendering", debugEnableWaterRendering)) {
            debugEnableWaterRendering = !debugEnableWaterRendering;
        }
        if (ImGui.checkbox("Wave Generation", debugEnableWaveGeneration)) {
            debugEnableWaveGeneration = !debugEnableWaveGeneration;
        }
        if (ImGui.checkbox("Wave Drag", debugEnableWaterDrag)) {
            debugEnableWaterDrag = !debugEnableWaterDrag;
        }
        if (ImGui.checkbox("Fake water velocity", debugEnableFakeWaterVelocity)) {
            debugEnableFakeWaterVelocity = !debugEnableFakeWaterVelocity;
        }
        if (ImGui.sliderFloat("Water Density", uiWaterDensity, 0, 1)) {
            water.setDensity(uiWaterDensity[0]);
        }
        if (ImGui.sliderFloat("Base Level", uiWaterBaseWaterLevel, 0.5f, Constants.VIEWPORT_HEIGHT)) {
            water.setBaseWaterLevel(uiWaterBaseWaterLevel[0]);
        }
        ImGui.text("Water waves");
        if (ImGui.dragInt("Waves Propagation", uiWaterWavesPropagationPasses, 1, 1, 10)) {
            water.setWavesPropagationPasses(uiWaterWavesPropagationPasses[0]);
        }
        if (ImGui.sliderFloat("Waves Propagation Spread Factor", uiWaterWavesPropagationSpreadFactor, 0f, 0.4f)) {
            water.setWavesPropagationSpreadFactor(uiWaterWavesPropagationSpreadFactor[0]);
        }
        if (ImGui.sliderFloat("Springs Stiffness", uiWaterSpringsStiffness, 0f, 0.1f)) {
            water.setSpringsStiffness(uiWaterSpringsStiffness[0]);
        }
        if (ImGui.sliderFloat("Springs Dampening", uiWaterSpringsDampeningFactor, 0f, 0.2f)) {
            water.setSpringsDampeningFactor(uiWaterSpringsDampeningFactor[0]);
        }
        if (ImGui.sliderFloat("Fake water velocity X", uiWaterFakeVelocityX, 0f, 25f)) {
            water.setFakeWaterVelocityX(uiWaterFakeVelocityX[0]);
        }

        if (ImGui.sliderFloat("Fake water velocity Y", uiWaterFakeVelocityY, -10f, 10f)) {
            water.setFakeWaterVelocityY(uiWaterFakeVelocityY[0]);
        }
        if (ImGui.sliderFloat2("Wave emitter amplitude range", uiWaveEmitterAmplitudeRange, 0f, 25f)) {
            waveEmitter.setAmplitudeRange(Range.buildRange(uiWaveEmitterAmplitudeRange[0], uiWaveEmitterAmplitudeRange[1]));
        }
        if (ImGui.sliderFloat2("Wave emitter period range", uiWaveEmitterPeriodRange, 0f, 6f)) {
            waveEmitter.setPeriodRange(Range.buildRange(uiWaveEmitterPeriodRange[0], uiWaveEmitterPeriodRange[1]));
        }

        ImGui.separator();
        ImGui.text("Debug");
        if (ImGui.checkbox("Debug Mode", debugMode)) {
            debugMode = !debugMode;
        }
        if (ImGui.checkbox("White Clear Color", debugClearColor)) {
            debugClearColor = !debugClearColor;
        }
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
                debugEnableCharacterGeneration = !debugEnableCharacterGeneration;
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
        float frameTime = Math.min(deltaTime, Constants.MIN_TIME_STEP);
        accumulator += frameTime;
        while (accumulator >= Constants.TIME_STEP) {
            characters.forEach(c -> c.update());
            waveEmitter.update();
            characterSpawner.update();
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
        waveSounds.dispose();
    }

    @Override
    public void dispose() {
        debugRenderer.dispose();
        disposeCurrentLevel();
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void spawnCharacter(int charIndex, boolean aiControlled, float x, float y) {
        final var spawned = new Character(world, boat, charIndex, aiControlled, x, y, characterDensity, characterFriction, characterRestitution);
        characters.add(spawned);
        worldContactListener.addListener(spawned);
        CharacterResources.getInstance().getRandomSpawnSound().play(DEFAULT_AUDIO_VOLUME);
    }
}
