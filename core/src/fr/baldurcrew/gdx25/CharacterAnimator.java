package fr.baldurcrew.gdx25;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CharacterAnimator {

    // Row & columns of the sprite sheet
    private static final int FRAME_COLS = 11, FRAME_ROWS = 5;

    Animation<TextureRegion> walkAnimation;
    Texture charactersSheet;
    SpriteBatch spriteBatch;
    float stateTime;

    public void initialize() {

        //load sheet
        charactersSheet = new Texture(Gdx.files.internal("characters_sheet.png"));

        //split sheet
        TextureRegion[][] frames = TextureRegion.split(charactersSheet,
                charactersSheet.getWidth() / FRAME_COLS,
                charactersSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] walkingFrames = new TextureRegion[2];
        walkingFrames[0] = frames[0][9];
        walkingFrames[1] = frames[0][10];

        walkAnimation = new Animation<TextureRegion>(0.125f, walkingFrames);

        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    public void render(){
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen
            stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

            // Get current frame of animation for the current stateTime
            TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);
            spriteBatch.begin();
            spriteBatch.draw(currentFrame, 50, 50); // Draw current frame at (50, 50)
            spriteBatch.end();
    }
}
