package fr.baldurcrew.gdx25;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Character {
    public final int BEIGE = 0;
    public final int BLUE = 1;
    public final int GREEN = 2;
    public final int PINK = 3;
    public final int YELLOW = 4;

    private int colorRow;
    float stateTime;
    float delta;

    boolean isFacingRight = true;
    boolean canJump = false;
    private CharacterAnimator characterAnimator;
    private SpriteBatch spriteBatch;

    enum State {
        EnteringIdle, Idle, Walking, Climbing, EnteringJumping, Jumping, Landing, Swimming;
    }

    public Character(int colorRow) {
        this.colorRow = colorRow;
        this.characterAnimator = new CharacterAnimator(colorRow);

        // Instantiate a SpriteBatch for drawing and reset the elapsed animation
        // time to 0
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    public void move(float delta) {

    }

    public void render() {
        var frames = this.characterAnimator.mapCharacterTextureRegion.get(Action.CLIMB);
        var animation = new Animation<TextureRegion>(0.250f, frames);

        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

        // Get current frame of animation for the current stateTime
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        spriteBatch.begin();
        spriteBatch.draw(currentFrame, 50, 50); // Draw current frame at (50, 50)
        spriteBatch.end();
    }
    float deltaTime = Gdx.graphics.getDeltaTime();

    public int getBEIGE() {
        return BEIGE;
    }
    public int getBLUE() {
        return BLUE;
    }
    public int getGREEN() {
        return GREEN;
    }
    public int getPINK() {
        return PINK;
    }
    public int getYELLOW() {
        return YELLOW;
    }
}
