package fr.baldurcrew.gdx25;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private static final int INITIAL_CHARACTER_COUNT = 3;
    private static final float DEFAULT_AUDIO_VOLUME = 0.2f;

    public static boolean debugMode = true;
    public static boolean debugClearColor = false;
    public static boolean debugEnableCharacterGeneration = true;
    public static boolean debugEnableWaterRendering = true;
    public static boolean debugEnableBoatRendering = true;
    public static boolean debugEnableWaveGeneration = true;
    public static boolean debugEnableWaterDrag = true;

    public SpriteBatch spriteBatch;
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
    private float sailingTime;
    private CharacterSpawner characterSpawner;

    private ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private long windowHandle;
    //Character
    private float[] charFriction = new float[1];
    private float[] charDensity = new float[1];
    private float[] charRestitution = new float[1];
    //Boat
    private float[] boatDensity = new float[1];
    private float[] boatRestitution = new float[1];
    private float[] boatFriction = new float[1];
    //Water
    private int[] waterWavesPropagationPasses = new int[1];
    private float[] waterWavesPropagationSpreadFactor = new float[1];
    private float[] waterSpringsStiffness = new float[1];
    private float[] waterSpringsDampeningFactor = new float[1];
    private float[] waterBaseWaterLevel = new float[1];
    private float[] waterDensity = new float[1];


    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Box2D.init();
        debugRenderer = new Box2DDebugRenderer();
        CharacterResources.getInstance();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont(); // libGDX's default Arial font

        createImGui();
        createTestLevel();
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


        final Range waterSimulationRange = Range.buildRange(-0.25f * Constants.VIEWPORT_WIDTH, 1.25f * Constants.VIEWPORT_WIDTH);
        // Only simulate physics under the boat
        final float physicSimulationMargin = Boat.BOAT_WIDTH * 0.2f;
        final Range waterPhysicsSimulationRange = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f - physicSimulationMargin, Boat.BOAT_WIDTH + 2 * physicSimulationMargin);

        water = new WaterSimulation(world, 80, waterSimulationRange, waterPhysicsSimulationRange);
        worldContactListener.addListener(water);
        boat = new Boat(world, Constants.VIEWPORT_WIDTH / 2f, water.getWaterLevel() + 1f);
        waveEmitter = new WaveEmitter(water, Range.buildRange(0.5f, 1.5f), Range.buildRange(4f, 6.5f)); // TODO Evolve over time to increase the difficulty

        final float charsSpawnPadding = Boat.BOAT_WIDTH * 0.2f;
        final Range spawnRangeX = waterSimulationRange.buildSubRange(waterSimulationRange.halfExtent - Boat.BOAT_WIDTH / 2f + charsSpawnPadding, Boat.BOAT_WIDTH - 2 * charsSpawnPadding);
        final Range spawnRangeY = Range.buildRange(water.getWaterLevel() + Boat.BOAT_HEIGHT / 2f, water.getWaterLevel() + Boat.BOAT_HEIGHT * 1.5f);

        characters = new ArrayList<>();
        for (int i = 0; i < INITIAL_CHARACTER_COUNT; i++) {
            this.spawnCharacter(CharacterResources.getRandomCharacterIndex(), spawnRangeX.getRandom(), spawnRangeY.getRandom());
        }
        characterSpawner = new CharacterSpawner(this, spawnRangeX, spawnRangeY, Range.buildRange(2.5f, 6f));

        sailingTime = 0;

        initTweakingUIValues();
    }

    private void initTweakingUIValues() {
        charFriction[0] = characters.get(0).getFriction();
        charDensity[0] = characters.get(0).getDensity();
        charRestitution[0] = characters.get(0).getRestitution();
        //Boat
        boatDensity[0] = boat.getDensity();
        boatRestitution[0] = boat.getRestitution();
        boatFriction[0] = boat.getFriction();
        //Water
        waterWavesPropagationPasses[0] = water.getWavesPropagationPasses();
        waterWavesPropagationSpreadFactor[0] = water.getWavesPropagationSpreadFactor();
        waterSpringsStiffness[0] = water.getSpringsStiffness();
        waterSpringsDampeningFactor[0] = water.getSpringsDampening();
        waterBaseWaterLevel[0] = water.getWaterLevel();
        waterDensity[0] = water.getDensity();
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


        boat.render(camera);
        water.render(camera);
        characters.forEach(character -> character.render(camera));

        spriteBatch.begin();
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
        ImGui.text("Characters");
        if (ImGui.checkbox("Character Generation", debugEnableCharacterGeneration)) {
            debugEnableCharacterGeneration = !debugEnableCharacterGeneration;
        }
        if (ImGui.sliderFloat("Char Friction", charFriction, 0, 1)) {
            characters.forEach(c -> c.setFriction(charFriction[0]));
        }
        if (ImGui.sliderFloat("Char Density", charDensity, 0, 30f)) {
            characters.forEach(c -> c.setDensity(charDensity[0]));
        }
        if (ImGui.sliderFloat("Char Restitution", charRestitution, 0, 1)) {
            characters.forEach(c -> c.setRestitution(charRestitution[0]));
        }
        ImGui.separator();
        ImGui.text("Boat");
        if (ImGui.checkbox("Boat rendering", debugEnableBoatRendering)) {
            debugEnableBoatRendering = !debugEnableBoatRendering;
        }
        if (ImGui.sliderFloat("Boat Density", boatDensity, 0, 1)) {
            boat.setDensity(boatDensity[0]);
        }
        if (ImGui.sliderFloat("Boat Restitution", boatRestitution, 0, 1)) {
            boat.setRestitution(charRestitution[0]);
        }
        if (ImGui.sliderFloat("Boat Friction", boatFriction, 0, 1)) {
            boat.setFriction(boatFriction[0]);
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
        if (ImGui.sliderFloat("Water Density", waterDensity, 0, 1)) {
            water.setDensity(waterDensity[0]);
        }
        if (ImGui.sliderFloat("Base Level", waterBaseWaterLevel, 0.5f, Constants.VIEWPORT_HEIGHT)) {
            water.setBaseWaterLevel(waterBaseWaterLevel[0]);
        }
        ImGui.text("Water waves");
        if (ImGui.dragInt("Waves Propagation", waterWavesPropagationPasses, 1, 1, 10)) {
            water.setWavesPropagationPasses(waterWavesPropagationPasses[0]);
        }
        if (ImGui.sliderFloat("Waves Propagation Spread Factor", waterWavesPropagationSpreadFactor, 0f, 0.4f)) {
            water.setWavesPropagationSpreadFactor(waterWavesPropagationSpreadFactor[0]);
        }
        if (ImGui.sliderFloat("Springs Stiffness", waterSpringsStiffness, 0f, 0.1f)) {
            water.setSpringsStiffness(waterSpringsStiffness[0]);
        }
        if (ImGui.sliderFloat("Springs Dampening", waterSpringsDampeningFactor, 0f, 0.2f)) {
            water.setSpringsDampeningFactor(waterSpringsDampeningFactor[0]);
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
        float frameTime = Math.min(deltaTime, 0.25f);
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

    public void spawnCharacter(int charIndex, float x, float y) {
        final var spawned = new Character(world, charIndex, x, y);
        characters.add(spawned);
        worldContactListener.addListener(spawned);
        CharacterResources.getInstance().getRandomSpawnSound().play(DEFAULT_AUDIO_VOLUME);
    }
}
