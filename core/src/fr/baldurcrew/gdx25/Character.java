package fr.baldurcrew.gdx25;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;

public class Character implements Disposable {
    public final int BEIGE = 0;
    public final int BLUE = 1;
    public final int GREEN = 2;
    public final int PINK = 3;
    public final int YELLOW = 4;

    private static final float CHARACTER_HEIGHT = 1f;
    private static final float ASPECT_RATIO = 0.5f;
    private static final float CHARACTER_WIDTH = ASPECT_RATIO * CHARACTER_HEIGHT;

    private int colorRow;
    float stateTime;
    float time;

    private float xVelocity = 0f;
    private float yVelocity = 0f;

    boolean isFacingRight = true;
    boolean canJump = false;

    private Body body;
    private CharacterAnimator characterAnimator;
    private SpriteBatch spriteBatch;
    float deltaTime = Gdx.graphics.getDeltaTime();

    enum State {
        EnteringIdle, Idle, Walking, Climbing, EnteringJumping, Jumping, Landing, Swimming;

    }

    public Character(int colorRow, World world) {
        this.colorRow = colorRow;
        this.characterAnimator = new CharacterAnimator(colorRow);

        this.body = createBody(world, Constants.VIEWPORT_WIDTH / 2, Constants.VIEWPORT_HEIGHT / 2 + 3);

        // Instantiate a SpriteBatch for drawing and reset the elapsed animation
        // time to 0
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    private Body createBody(World world, float centerX, float centerY) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);

        final var body = world.createBody(bodyDef);

        final var characterPolygon = new PolygonShape();
        characterPolygon.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = characterPolygon;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);

        characterPolygon.dispose();

        return body;
    }

    public void render(Camera camera) {
        var frames = this.characterAnimator.mapCharacterTextureRegion.get(Action.CLIMB);
        //TODO: create animation only once
        var animation = new Animation<TextureRegion>(0.250f, frames);
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        //TODO: Check if correct when rotated
        final var renderX = bodyX - CHARACTER_WIDTH / 2f;
        final var renderY = bodyY - CHARACTER_HEIGHT / 2f;

        var affine = new Affine2();
        //TODO: Compute the scale elsewhere for better perf.
        affine.setToTrnRotScl(renderX, renderY, rotation, 1, 1);
        //CHARACTER_WIDTH/currentFrame.getRegionWidth(),
        //CHARACTER_HEIGHT/currentFrame.getRegionHeight());

//        time = time + deltaTime;
//        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//            if (canJump) {
//                yVelocity = yVelocity + Constants.MOVE_VELOCITY * Math.abs(Constants.GRAVITY_VALUE);
//                canJump = false;
//            }
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//            xVelocity = -1 * Constants.MOVE_VELOCITY;
//        }
//
//        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//            xVelocity = Constants.MOVE_VELOCITY;
//        }
//
//        yVelocity = yVelocity + Constants.GRAVITY_VALUE;
//        //float posX = this.getX();
//        //float posY = this.getY();
//        float changeX = xVelocity * deltaTime;
//        float changeY = yVelocity * deltaTime;

        // Get current frame of animation for the current stateTime

        System.out.println("RenderX" + renderX + "   RenderY:" + renderY);

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        spriteBatch.draw(currentFrame, CHARACTER_WIDTH, CHARACTER_HEIGHT, affine);

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }

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
