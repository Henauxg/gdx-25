package fr.baldurcrew.gdx25.monster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import static fr.baldurcrew.gdx25.CoreGame.DEFAULT_AUDIO_VOLUME;

public class Monster {

    public static final float UP_TRANSLATION_ANIMATION_DURATION = 2f;
    public static final float DOWN_TRANSLATION_ANIMATION_DURATION = 2f;
    private static final int FRAME_COLS = 7, FRAME_ROWS = 1;
    private static final String imagePath = "tentacle.png";
    private static final int SPAWN_SOUNDS_COUNT = 2;
    private static final float TENTACLE_WIDTH = 4f;
    private static final float TENTACLE_HEIGHT = 5f;
    private static final float TENTACLE_HIGH_POSITION_Y = -1f;
    private static final float TENTACLE_LOW_POSITION_Y = -TENTACLE_HEIGHT;
    private static final float TENTACLE_GRAB_Y_FACTOR = 0.85f;
    Animation<TextureRegion> tentacleAnimation;
    Texture tentacleSheet;
    float animationTime;
    private Sound[] spawnSounds;
    private AnimationState state;
    private TextureRegion idleTentacleFrame;
    private float xTentacle;
    private float yTentacle = TENTACLE_HIGH_POSITION_Y;
    private Eatable currentMeal;
    private float mealSizeFactor;


    public Monster() {
        tentacleSheet = new Texture(Gdx.files.internal(imagePath));

        TextureRegion[][] tentacleDef = TextureRegion.split(tentacleSheet,
                tentacleSheet.getWidth() / FRAME_COLS,
                tentacleSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] tentacleFrames = new TextureRegion[FRAME_COLS];

        for (int j = 0; j < FRAME_COLS; j++) {
            tentacleFrames[j] = tentacleDef[0][j];
        }

        tentacleAnimation = new Animation<>(0.175f, tentacleFrames);
        animationTime = 0f;

        spawnSounds = new Sound[SPAWN_SOUNDS_COUNT];
        for (int i = 0; i < SPAWN_SOUNDS_COUNT; i++) {
            spawnSounds[i] = Gdx.audio.newSound(Gdx.files.internal("growl_" + i + ".mp3"));
        }

        state = AnimationState.IDLE;
    }

    public Sound getRandomSpawnSound() {
        return spawnSounds[MathUtils.random(0, SPAWN_SOUNDS_COUNT - 1)];
    }

    public void render(Camera camera, SpriteBatch spriteBatch) {
        TextureRegion frame = idleTentacleFrame;
        switch (state) {

            case IDLE: {
            }
            break;
            case UP_TRANSLATION: {
                animationTime += Gdx.graphics.getDeltaTime();
                yTentacle = TENTACLE_LOW_POSITION_Y + animationTime * (TENTACLE_HIGH_POSITION_Y - TENTACLE_LOW_POSITION_Y) / UP_TRANSLATION_ANIMATION_DURATION;
                if (animationTime >= UP_TRANSLATION_ANIMATION_DURATION) {
                    state = AnimationState.ANIMATION;
                    animationTime = 0;
                }
            }
            break;
            case ANIMATION: {
                animationTime += Gdx.graphics.getDeltaTime();
                frame = tentacleAnimation.getKeyFrame(animationTime, false);
                if (tentacleAnimation.isAnimationFinished(animationTime)) {
                    //TODO: Control of character body
                    state = AnimationState.DOWN_TRANSLATION;
                    animationTime = 0;
                }

            }
            break;
            case DOWN_TRANSLATION: {
                animationTime += Gdx.graphics.getDeltaTime();
                yTentacle = TENTACLE_HIGH_POSITION_Y + animationTime * (mealSizeFactor * TENTACLE_LOW_POSITION_Y - TENTACLE_HIGH_POSITION_Y) / DOWN_TRANSLATION_ANIMATION_DURATION;
                currentMeal.freezeY(yTentacle + TENTACLE_HEIGHT * TENTACLE_GRAB_Y_FACTOR);
                if (animationTime >= DOWN_TRANSLATION_ANIMATION_DURATION) {
                    state = AnimationState.IDLE;
                    animationTime = 0;
                }
            }
            break;
        }
        animationTime += Gdx.graphics.getDeltaTime();
        if (state != AnimationState.IDLE) {
            spriteBatch.draw(frame, xTentacle, yTentacle, TENTACLE_WIDTH * mealSizeFactor, TENTACLE_HEIGHT * mealSizeFactor);
        }
    }

    public boolean eat(Eatable eatable) {
        if (state != AnimationState.IDLE) {
            return false;
        }
        currentMeal = eatable;
        mealSizeFactor = eatable.getMealSizeFactor();
        eatable.prepareToBeEaten(TENTACLE_GRAB_Y_FACTOR * TENTACLE_HEIGHT + TENTACLE_HIGH_POSITION_Y);
        getRandomSpawnSound().play(DEFAULT_AUDIO_VOLUME);
        state = AnimationState.UP_TRANSLATION;
        idleTentacleFrame = tentacleAnimation.getKeyFrames()[0];
        xTentacle = eatable.getX() - mealSizeFactor * TENTACLE_WIDTH / 2f;
        yTentacle = -TENTACLE_HEIGHT;
        animationTime = 0;
        return true;
    }

    public void setxTentacle(float xTentacle) {
        this.xTentacle = xTentacle;
    }

    public void setyTentacle(float yTentacle) {
        this.yTentacle = yTentacle;
    }

    enum AnimationState {
        IDLE, UP_TRANSLATION, ANIMATION, DOWN_TRANSLATION;
    }
}
