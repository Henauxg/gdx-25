package fr.baldurcrew.gdx25.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CharacterResources {
    public static final int BEIGE = 0;
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int PINK = 3;
    public static final int YELLOW = 4;
    public static final int SPAWN_SOUNDS_COUNT = 4;
    public static final float CHARACTER_HEIGHT = 0.75f;
    public static final float ASPECT_RATIO = 0.76f;
    public static final float CHARACTER_WIDTH = ASPECT_RATIO * CHARACTER_HEIGHT;
    private static final int FRAME_COLS = 11, FRAME_ROWS = 5;
    private static final TextureRegion[][] allTextureRegions = loadAndSplitSpriteSheet("chars_sheet_trim.png", FRAME_COLS, FRAME_ROWS);
    private static CharacterResources instance;
    private final Map<Integer, Map<Action, Animation<TextureRegion>>> characterAnimations;
    private Sound[] spawnSounds;

    private CharacterResources() {
        this.characterAnimations = new HashMap<>();

        Stream.of(BEIGE, BLUE, GREEN, PINK, YELLOW).forEach(color -> {
            final var actionMap = new HashMap<Action, Animation<TextureRegion>>();
            characterAnimations.put(color, actionMap);
            Stream.of(Action.values())
                    .forEach(action -> {
                        var charTextureRegions = allTextureRegions[color];
                        var animation = new Animation<TextureRegion>(0.250f, getCharActionTextureRegions(action, charTextureRegions));
                        actionMap.put(action, animation);
                    });
        });

        spawnSounds = new Sound[SPAWN_SOUNDS_COUNT];
        for (int i = 0; i < 4; i++) {
            spawnSounds[i] = Gdx.audio.newSound(Gdx.files.internal("weee_" + i + ".mp3"));
        }
    }

    public static CharacterResources getInstance() {
        if (instance == null) {
            instance = new CharacterResources();
        }

        return instance;
    }

    private static TextureRegion[][] loadAndSplitSpriteSheet(String imagePath, int frameCols, int frameRows) {
        //load sheet
        var charactersSheet = new Texture(Gdx.files.internal(imagePath));

        //split sheet
        TextureRegion[][] frames = TextureRegion.split(charactersSheet, charactersSheet.getWidth() / frameCols, charactersSheet.getHeight() / frameRows);
        return frames;
    }

    public static int getRandomCharacterIndex() {
        return MathUtils.random(BEIGE, PINK);
    }

    public static int getPlayerCharacterIndex() {
        return YELLOW;
    }

    public Sound getRandomSpawnSound() {
        return spawnSounds[MathUtils.random(0, SPAWN_SOUNDS_COUNT - 1)];
    }

    public Animation<TextureRegion> getAnimation(Action action, int rowColor) {
        //TODO: check if rowColor exist
        return characterAnimations.get(rowColor).get(action);

    }

    private TextureRegion[] getCharActionTextureRegions(Action action, TextureRegion[] charTextureRegions) {
        int framesCount = action.getFramesCount();
        var actionTextureRegions = new TextureRegion[framesCount];
        for (int i = 0; i < framesCount; i++) {
            actionTextureRegions[i] = charTextureRegions[action.getFrame(i)];
        }
        return actionTextureRegions;
    }
}
