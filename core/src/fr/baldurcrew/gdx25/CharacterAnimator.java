package fr.baldurcrew.gdx25;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.stream.Collectors;


public class CharacterAnimator {


    // Row & columns of the sprite sheet
    private static final int FRAME_COLS = 11, FRAME_ROWS = 5;

    private static final TextureRegion[][] ALL_TEXTURE_REGIONS = loadAndSplitSpriteSheet("characters_sheet.png", FRAME_COLS, FRAME_ROWS);

    Animation<TextureRegion> walkAnimation;
    SpriteBatch spriteBatch;
    float stateTime;

    EnumMap<Action, TextureRegion[]> mapCharacterTextureRegion;


    public CharacterAnimator(int colorRow) {
        initialize(colorRow);
    }

    public void initialize(int colorRow) {

        mapCharacterTextureRegion = getCharTextureRegions(colorRow);

    }

    private EnumMap<Action, TextureRegion[]> getCharTextureRegions(int charRow) {
        var charTextureRegions = ALL_TEXTURE_REGIONS[charRow];
        var mapCharacterTextureRegion = Arrays.stream(Action.values()).collect(Collectors.toMap(Function.identity(), action -> getCharActionTextureRegions(action, charTextureRegions)));

        return new EnumMap<>(mapCharacterTextureRegion);
    }

    private TextureRegion[] getCharActionTextureRegions(Action action, TextureRegion[] charTextureRegions) {
        int framesCount = action.getFramesCount();
        var actionTextureRegions = new TextureRegion[framesCount];
        for (int i = 0; i < framesCount; i++) {
            actionTextureRegions[i] = charTextureRegions[action.getFrame(i)];
        }
        return actionTextureRegions;
    }

    private static TextureRegion[][] loadAndSplitSpriteSheet(String imagePath, int frameCols, int frameRows) {
        //load sheet
        var charactersSheet = new Texture(Gdx.files.internal(imagePath));

        //split sheet
        TextureRegion[][] frames = TextureRegion.split(charactersSheet, charactersSheet.getWidth() / frameCols, charactersSheet.getHeight() / frameRows);
        return frames;
    }
}
