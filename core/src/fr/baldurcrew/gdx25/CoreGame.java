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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.character.Character;
import fr.baldurcrew.gdx25.character.CharacterResources;
import fr.baldurcrew.gdx25.character.CharacterSpawner;
import fr.baldurcrew.gdx25.layer.ParallaxLayer;
import fr.baldurcrew.gdx25.monster.Monster;
import fr.baldurcrew.gdx25.physics.WorldContactListener;
import fr.baldurcrew.gdx25.utils.NumericRenderer;
import fr.baldurcrew.gdx25.utils.Range;
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
    public static final float DEFAULT_AUDIO_VOLUME = 0.2f;
    public static final String LAYER_00 = "sky.png";
    public static final String LAYER_01 = "cloud_01.png";
    public static final String LAYER_02 = "cloud_02.png";
    public static final String LAYER_03 = "island.png";
    public static final String LAYER_04 = "ocean.png";
    public static final String LAYER_05 = "waves-trans.png";
    public static final String LAYER_06 = "creek.png";
    public static final String LAYER_07 = "waves.png";
    public static final String LAYER_08 = "rock_03.png";
    public static final String LAYER_09 = "waves-2.png";
    public static final String LAYER_10 = "waves-3.png";
    public static final String LAYER_11 = "rock_reef.png";
    public static final String LAYER_12 = "waves-4.png";
    public static final String LAYER_13 = "groundswell.png";
    public static final String LAYER_14 = "bedrock.png";
    private static final Color CLEAR_COLOR = new Color(0.5f, 0.898f, 1, 1);
    private static final Color DEBUG_CLEAR_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final int INITIAL_CHARACTER_COUNT = 1;
    public static boolean debugMode = false;
    public static boolean debugClearColor = false;
    public static boolean debugEnableCharacterGeneration = true;
    public static boolean debugEnableWaterRendering = true;
    public static boolean debugEnableBoatRendering = true;
    public static boolean debugEnableWaveGeneration = true;
    public static boolean debugEnableWaterDrag = true;
    public static boolean debugEnableFakeWaterVelocity = true;
    public static boolean debugEnableLiftForce = true;
    public static boolean debugEnableImGui = false;
    public static boolean debugEnableDifficultyScaling = true;
    public static boolean debugEnableTouchControl = true;
    public SpriteBatch spriteBatch;
    private List<ParallaxLayer> backgroundLayers;
    private List<ParallaxLayer> foregroundLayers;

    private BitmapFont font;
    private World world;
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;
    private Texture lostText;
    private Texture beginText;
    private NumericRenderer timerRenderer;
    private List<Character> characters;
    private float accumulator = 0;
    private WorldContactListener worldContactListener;
    private WaterSimulation water;
    private Boat boat;
    private Monster monster;
    private Character playerCharacter;
    private WaveEmitter waveEmitter;
    private Music waveSounds;
    private Music music;
    private float sailingTime;
    private float difficultyFactor;
    //    private boolean gameOver;
    private CharacterSpawner characterSpawner;
    private float characterDensity = 3.0f;
    private float characterFriction = 0.25f;
    private float characterRestitution = 0.2f;
    private Range defaultWavePeriodRange = Range.buildRangeEx(0.5f, 1.5f);
    private Range defaultWaveAmplitudeRange = Range.buildRangeEx(Difficulty.MIN_WAVES_AMPLITUDE_AT_MIN_SCALING, Difficulty.MAX_WAVES_AMPLITUDE_AT_MIN_SCALING);
    private Range charactersSpawnRangeX;
    private Range charactersSpawnRangeY;
    private ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    //Character
    private float[] uiCharFriction = new float[1];
    private float[] uiCharDensity = new float[1];
    private float[] uiCharRestitution = new float[1];
    //Boat
    private float[] uiBoatDensity = new float[1];
    private float[] uiBoatRestitution = new float[1];
    private float[] uiBoatFriction = new float[1];
    private float[] uiBoatAngularDamping = new float[1];
    //Water
    private int[] uiWaterWavesPropagationPasses = new int[1];
    private float[] uiWaterWavesPropagationSpreadFactor = new float[1];
    private float[] uiWaterSpringsStiffness = new float[1];
    private float[] uiWaterSpringsDampeningFactor = new float[1];
    //    private float[] uiWaterBaseWaterLevel = new float[1];
    private float[] uiWaterDensity = new float[1];
    private float[] uiWaterFakeVelocityX = new float[1];
    private float[] uiWaterFakeVelocityY = new float[1];
    private float[] uiWaveEmitterAmplitudeRange = new float[2];
    private float[] uiWaveEmitterPeriodRange = new float[2];
    private float[] uiSailingTime = new float[1];
    private GameState gameState;


    @Override
    public void create() {
        gameState = GameState.WaitingToStart;

        waveSounds = Gdx.audio.newMusic(Gdx.files.internal("nice_waves.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("DasLiedderSturme.mp3"));
        lostText = new Texture("lost.png");
        beginText = new Texture("begin.png");
        timerRenderer = new NumericRenderer();

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

    private void playMusic(Music music) {
        music.setLooping(true);
        music.setVolume(DEFAULT_AUDIO_VOLUME);
        music.play();
    }

    private void createParallaxLayers() {
        backgroundLayers = new ArrayList<>();
        foregroundLayers = new ArrayList<>();

        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_00), 0.3f, 0.8f, 0.12f, true, true, 0, 0)); // far cloud
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_01), 0.6f, 0.9f, 0.11f, true, false, 0, 0)); // cloud
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_03), 0.2f, 0.8f, -0.36f, true, false, 0, 0)); // far island
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_04), 0.4f, 0.9f, -0.40f, true, false, 1.2f, 0.15f)); // ocean
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_05), 0.4f, 0.4f, -0.22f, true, false, 3, 0.25f)); //sea
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_06), 0.7f, 0.9f, -0.34f, true, false, 0.5f, 0.01f)); // far island
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_07), 0.82f, 0.4f, -0.29f, true, false, 5, 0.2f)); //sea
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_08), 0.9f, 0.85f, -0.32f, true, false, 3, 0.05f)); //rock reef
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_09), 1f, 0.4f, -0.30f, true, false, 2, 0.1f)); //sea
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_10), 1.2f, 0.4f, -0.31f, true, false, 1, 0.2f)); //sea
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_11), 1.4f, 0.5f, -0.42f, true, false, 3, 0.05f));
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_12), 1.5f, 0.4f, -0.32f, true, false, 3, 0.25f)); //sea
        backgroundLayers.add(new ParallaxLayer(new Texture(LAYER_13), 1.8f, 0.9f, -0.52f, true, false, 0, 0)); //rock reef

        foregroundLayers.add(new ParallaxLayer(new Texture(LAYER_02), 0.8f, 0.9f, 0.12f, true, false, 0, 0)); // cloud
        foregroundLayers.add(new ParallaxLayer(new Texture(LAYER_14), 2f, 0.3f, -0.96f, true, false, 0, 0)); //rock bottom
    }

    private void createImGui() {
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.getFonts().addFontDefault();
        io.getFonts().build();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 110");
    }

    public void createTestLevel() {
        playMusic(waveSounds);
        playMusic(music);

        world = new World(new Vector2(0, Constants.GRAVITY_VALUE), true);
        worldContactListener = new WorldContactListener();
        world.setContactListener(worldContactListener);

        final Range waterSimulationRange = Range.buildRangeEx(-0.25f * Constants.VIEWPORT_WIDTH, 1.25f * Constants.VIEWPORT_WIDTH);
        // Only simulate physics under the boat
        final float physicSimulationMargin = Boat.BOAT_WIDTH * 0.2f;
//        final Range waterPhysicsSimulationRange = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f - physicSimulationMargin, Boat.BOAT_WIDTH + 2 * physicSimulationMargin);
        final Range waterPhysicsSimulationRange = waterSimulationRange.buildSubRange(0.25f * Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_WIDTH);

        water = new WaterSimulation(world, 80, waterSimulationRange, waterPhysicsSimulationRange);
        worldContactListener.addListener(water);
        boat = new Boat(world, Constants.VIEWPORT_WIDTH / 2f, water.getWaterLevel() + 1f);
        waveEmitter = new WaveEmitter(water, defaultWavePeriodRange, defaultWaveAmplitudeRange); // TODO Evolve over time to increase the difficulty

        final float charsSpawnPadding = Boat.BOAT_WIDTH * 0.2f;
        charactersSpawnRangeX = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f + charsSpawnPadding, Boat.BOAT_WIDTH - 2 * charsSpawnPadding);
//        charactersSpawnRangeY = Range.buildRangeEx(water.getWaterLevel() + Boat.BOAT_HEIGHT / 2f, water.getWaterLevel() + Boat.BOAT_HEIGHT * 1.5f);
        charactersSpawnRangeY = Range.buildRangeEx(Constants.VIEWPORT_HEIGHT * 0.9f, Constants.VIEWPORT_HEIGHT);


        characters = new ArrayList<>();
        characterSpawner = new CharacterSpawner(this, charactersSpawnRangeX, charactersSpawnRangeY, Range.buildRangeEx(Difficulty.MIN_AI_SPAWN_PERIOD_AT_MIN_SCALING, Difficulty.MAX_AI_SPAWN_PERIOD_AT_MIN_SCALING));

        monster = new Monster();

        sailingTime = 0;
        difficultyFactor = 1;

        initTweakingUIValues();
    }

    private void initTweakingUIValues() {
        // Char
        uiCharFriction[0] = characterFriction;
        uiCharDensity[0] = characterDensity;
        uiCharRestitution[0] = characterRestitution;
        // Boat
        uiBoatDensity[0] = boat.getDensity();
        uiBoatRestitution[0] = boat.getRestitution();
        uiBoatFriction[0] = boat.getFriction();
        uiBoatAngularDamping[0] = boat.getBody().getAngularDamping();
        // Water
        uiWaterWavesPropagationPasses[0] = water.getWavesPropagationPasses();
        uiWaterWavesPropagationSpreadFactor[0] = water.getWavesPropagationSpreadFactor();
        uiWaterSpringsStiffness[0] = water.getSpringsStiffness();
        uiWaterSpringsDampeningFactor[0] = water.getSpringsDampening();
//        uiWaterBaseWaterLevel[0] = water.getWaterLevel();
        uiWaterDensity[0] = water.getDensity();
        uiWaterFakeVelocityX[0] = water.getFakeWaterVelocityX();
        uiWaterFakeVelocityY[0] = water.getFakeWaterVelocityY();
        uiWaveEmitterAmplitudeRange[0] = defaultWaveAmplitudeRange.from;
        uiWaveEmitterAmplitudeRange[1] = defaultWaveAmplitudeRange.to;
        uiWaveEmitterPeriodRange[0] = defaultWavePeriodRange.from;
        uiWaveEmitterPeriodRange[1] = defaultWavePeriodRange.to;
        // Others
        uiSailingTime[0] = sailingTime;
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        if (gameState == GameState.Playing) {
            sailingTime += deltaTime;
            updateDifficulty(sailingTime);
        }

        handleInputs(camera);
        handleDebugInputs(camera);

        doPhysicsStep(deltaTime);

        camera.update();

        if (debugClearColor) {
            ScreenUtils.clear(DEBUG_CLEAR_COLOR);
        } else {
            ScreenUtils.clear(CLEAR_COLOR);
        }

        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);
        backgroundLayers.forEach(l -> l.render(camera, spriteBatch, deltaTime));
        boat.render(camera, spriteBatch);
        characters.forEach(character -> character.render(camera, spriteBatch));
        monster.render(camera, spriteBatch);
        foregroundLayers.forEach(l -> l.render(camera, spriteBatch, deltaTime));
        spriteBatch.end();

        water.render(camera);

        spriteBatch.begin();
        var textAsTextureRenderWidth = 10f;
        var textAsTextureRenderHeight = textAsTextureRenderWidth / 4f;
        switch (gameState) {
            case WaitingToStart -> {
                spriteBatch.draw(beginText, camera.viewportWidth / 2f - textAsTextureRenderWidth / 2f, camera.viewportHeight / 4f, textAsTextureRenderWidth, textAsTextureRenderHeight);
            }
            case Playing -> {
                timerRenderer.renderTimer(spriteBatch, camera.viewportWidth / 2f, camera.viewportHeight * 0.9f, 1.5f, sailingTime);
            }
            case GameOver -> {
                timerRenderer.renderTimer(spriteBatch, camera.viewportWidth / 2f, camera.viewportHeight * 0.9f, 1.5f, sailingTime);
                spriteBatch.draw(lostText, camera.viewportWidth / 2f - textAsTextureRenderWidth / 2f, camera.viewportHeight / 4f, textAsTextureRenderWidth, textAsTextureRenderHeight);
            }
        }
        spriteBatch.end();

        if (debugMode) {
            debugRenderer.render(world, camera.combined);
        }

        renderImGui();
    }

    private void renderImGui() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        // --- ImGUI draw commands go here ---
        if (debugEnableImGui) {
            drawUI();
        }
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
        if (ImGui.sliderFloat("Boat Angular Damping", uiBoatAngularDamping, 0, 1)) {
            boat.setAngularDamping(uiBoatAngularDamping[0]);
        }
        ImGui.separator();
        ImGui.text("Water");
        if (ImGui.checkbox("Water rendering", debugEnableWaterRendering)) {
            debugEnableWaterRendering = !debugEnableWaterRendering;
        }
        if (ImGui.checkbox("Wave Generation", debugEnableWaveGeneration)) {
            debugEnableWaveGeneration = !debugEnableWaveGeneration;
        }
        if (ImGui.checkbox("Water Drag", debugEnableWaterDrag)) {
            debugEnableWaterDrag = !debugEnableWaterDrag;
        }
        if (ImGui.checkbox("Lift force", debugEnableLiftForce)) {
            debugEnableLiftForce = !debugEnableLiftForce;
        }
        if (ImGui.checkbox("Fake water velocity", debugEnableFakeWaterVelocity)) {
            debugEnableFakeWaterVelocity = !debugEnableFakeWaterVelocity;
        }
        if (ImGui.sliderFloat("Water Density", uiWaterDensity, 0, 1)) {
            water.setDensity(uiWaterDensity[0]);
        }
//        if (ImGui.sliderFloat("Base Level", uiWaterBaseWaterLevel, 0.5f, Constants.VIEWPORT_HEIGHT)) {
//            water.setBaseWaterLevel(uiWaterBaseWaterLevel[0]);
//        }
        ImGui.text("Water waves");
        if (ImGui.dragInt("Waves Propagation", uiWaterWavesPropagationPasses, 1, 1, 10)) {
            water.setWavesPropagationPasses(uiWaterWavesPropagationPasses[0]);
        }
        uiWaterWavesPropagationSpreadFactor[0] = water.getWavesPropagationSpreadFactor();
        if (ImGui.sliderFloat("Waves Propagation Spread Factor", uiWaterWavesPropagationSpreadFactor, 0f, 0.4f)) {
            water.setWavesPropagationSpreadFactor(uiWaterWavesPropagationSpreadFactor[0]);
        }
        if (ImGui.sliderFloat("Springs Stiffness", uiWaterSpringsStiffness, 0f, 0.1f)) {
            water.setSpringsStiffness(uiWaterSpringsStiffness[0]);
        }
        if (ImGui.sliderFloat("Springs Dampening", uiWaterSpringsDampeningFactor, 0f, 0.2f)) {
            water.setSpringsDampeningFactor(uiWaterSpringsDampeningFactor[0]);
        }
        uiWaterFakeVelocityX[0] = water.getFakeWaterVelocityX();
        if (ImGui.sliderFloat("Fake water velocity X", uiWaterFakeVelocityX, 0f, 25f)) {
            water.setFakeWaterVelocityX(uiWaterFakeVelocityX[0]);
        }
        uiWaterFakeVelocityY[0] = water.getFakeWaterVelocityY();
        if (ImGui.sliderFloat("Fake water velocity Y", uiWaterFakeVelocityY, -10f, 10f)) {
            water.setFakeWaterVelocityY(uiWaterFakeVelocityY[0]);
        }
        uiWaveEmitterAmplitudeRange[0] = waveEmitter.getAmplitudeRangeMin();
        uiWaveEmitterAmplitudeRange[1] = waveEmitter.getAmplitudeRangeMax();
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
        if (ImGui.checkbox("Difficulty scaling", debugEnableDifficultyScaling)) {
            debugEnableDifficultyScaling = !debugEnableDifficultyScaling;
        }
        if (ImGui.checkbox("Touch control", debugEnableTouchControl)) {
            debugEnableTouchControl = !debugEnableTouchControl;
        }
        uiSailingTime[0] = sailingTime;
        if (ImGui.sliderFloat("Sailing time", uiSailingTime, 0, Difficulty.MAX_SAILING_TIME_SCALING)) {
            this.setSailingTime(uiSailingTime[0]);
        }
        ImGui.textDisabled("Difficulty factor " + difficultyFactor);
    }

    private void setSailingTime(float time) {
        this.sailingTime = time;
        updateDifficulty(sailingTime);
    }

    private void updateDifficulty(float sailingTime) {
        if (!debugEnableDifficultyScaling) return;

        // Difficulty could be clamped at MAX_DIFFICULTY_FACTOR. But maybe not ?
        difficultyFactor = Difficulty.STARTING_DIFFICULTY_FACTOR + (Difficulty.MAX_DIFFICULTY_FACTOR - Difficulty.STARTING_DIFFICULTY_FACTOR) * sailingTime / Difficulty.MAX_SAILING_TIME_SCALING;
        var difficultyMultiplier = (difficultyFactor - Difficulty.STARTING_DIFFICULTY_FACTOR) / (Difficulty.MAX_DIFFICULTY_FACTOR - Difficulty.STARTING_DIFFICULTY_FACTOR);
        var fakeWaterVelocityX = Difficulty.FAKE_WATER_VELOCITY_X_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.FAKE_WATER_VELOCITY_X_AT_MAX_SCALING - Difficulty.FAKE_WATER_VELOCITY_X_AT_MIN_SCALING);
        var fakeWaterVelocityY = Difficulty.FAKE_WATER_VELOCITY_Y_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.FAKE_WATER_VELOCITY_Y_AT_MAX_SCALING - Difficulty.FAKE_WATER_VELOCITY_Y_AT_MIN_SCALING);
        var waveSpreadFactor = Difficulty.WAVE_SPREAD_FACTOR_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.WAVE_SPREAD_FACTOR_AT_MAX_SCALING - Difficulty.WAVE_SPREAD_FACTOR_AT_MIN_SCALING);
        // Wave period from (0.5f, 1.5f) to ? Do not increase
        var minWaveAmplitude = Difficulty.MIN_WAVES_AMPLITUDE_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.MIN_WAVES_AMPLITUDE_AT_MAX_SCALING - Difficulty.MIN_WAVES_AMPLITUDE_AT_MIN_SCALING);
        var maxWaveAmplitude = Difficulty.MAX_WAVES_AMPLITUDE_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.MAX_WAVES_AMPLITUDE_AT_MAX_SCALING - Difficulty.MAX_WAVES_AMPLITUDE_AT_MIN_SCALING);

        var minAiSpawnPeriod = Difficulty.MIN_AI_SPAWN_PERIOD_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.MIN_AI_SPAWN_PERIOD_AT_MAX_SCALING - Difficulty.MIN_AI_SPAWN_PERIOD_AT_MIN_SCALING);
        var maxAiSpawnPeriod = Difficulty.MAX_AI_SPAWN_PERIOD_AT_MIN_SCALING + difficultyMultiplier * (Difficulty.MAX_AI_SPAWN_PERIOD_AT_MAX_SCALING - Difficulty.MAX_AI_SPAWN_PERIOD_AT_MIN_SCALING);

        water.setFakeWaterVelocityX(fakeWaterVelocityX);
        water.setFakeWaterVelocityY(fakeWaterVelocityY);
        water.setWavesPropagationSpreadFactor(waveSpreadFactor);
        waveEmitter.setAmplitudeRange(Range.buildRange(minWaveAmplitude, maxWaveAmplitude));
        characterSpawner.setSpawnPeriod(Range.buildRange(minAiSpawnPeriod, maxAiSpawnPeriod));
    }

    public void handleInputs(OrthographicCamera camera) {
        switch (gameState) {
            case WaitingToStart -> {
                if (Gdx.input.justTouched()) {
                    gameState = GameState.Playing;
                    playerCharacter = this.spawnCharacter(CharacterResources.getPlayerCharacterIndex(), false, charactersSpawnRangeX.getRandom(), charactersSpawnRangeY.getRandom());
                }
            }
            case Playing -> {
                characters.forEach(character -> character.handleInputs(playerCharacter.getX()));
            }
            case GameOver -> {
                if (Gdx.input.justTouched()) {
                    gameState = GameState.Playing;
                    disposeCurrentLevel();
                    createTestLevel();
                    playerCharacter = this.spawnCharacter(CharacterResources.getPlayerCharacterIndex(), false, charactersSpawnRangeX.getRandom(), charactersSpawnRangeY.getRandom());
                }
            }
        }
    }

    public void handleDebugInputs(OrthographicCamera camera) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            debugMode = !debugMode;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            debugEnableImGui = !debugEnableImGui;
            if (debugEnableImGui)
                debugEnableTouchControl = false; // Disable touch controls when enabling ImGui since touch events are not captured.
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            disposeCurrentLevel();
            createTestLevel();
            gameState = GameState.WaitingToStart;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            playerCharacter = this.spawnCharacter(CharacterResources.getPlayerCharacterIndex(), false, charactersSpawnRangeX.getRandom(), charactersSpawnRangeY.getRandom());
        }
    }

    private void doPhysicsStep(float deltaTime) {
        // Fixed time step.
        // Max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, Constants.MIN_TIME_STEP);
        accumulator += frameTime;
        while (accumulator >= Constants.TIME_STEP) {
            characters.forEach(c -> c.update());
            if (gameState == GameState.Playing || gameState == GameState.GameOver) {
                waveEmitter.update();
            }
            if (gameState == GameState.Playing) {
                characterSpawner.update();
            }
            water.update();
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            final boolean upsideDownBoat = boat.update();
            if (upsideDownBoat && !boat.isEaten()) {
                monster.eat(boat);
                gameState = GameState.GameOver;
            } else if (gameState == GameState.GameOver && !boat.isEaten()) {
                monster.eat(boat);
            }
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
        timerRenderer.dispose();
        beginText.dispose();
        lostText.dispose();
        disposeCurrentLevel();
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        waveSounds.dispose();
        music.dispose();
        ImGui.destroyContext();
    }

    public Character spawnCharacter(int charIndex, boolean aiControlled, float x, float y) {
        final var spawned = new Character(world, this, boat, water, charIndex, aiControlled, x, y, characterDensity, characterFriction, characterRestitution);
        characters.add(spawned);
        worldContactListener.addListener(spawned);
        CharacterResources.getInstance().getRandomSpawnSound().play(DEFAULT_AUDIO_VOLUME);

        return spawned;
    }

    public void playerDied() {
        monster.eat(playerCharacter);
        gameState = GameState.GameOver;
    }

    public void aiCharacterDied(Character character) {
        if (!monster.eat(character)) {
            // TODO Faudrait les despawn un jour :)
        }
    }

    enum GameState {
        WaitingToStart,
        Playing,
        GameOver
    }
}
