package fr.baldurcrew.gdx25.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Logger;
import fr.baldurcrew.gdx25.Constants;

import static com.badlogic.gdx.utils.Align.left;
import static com.badlogic.gdx.utils.Align.right;

public class Character extends Actor implements Disposable {
    private static final float CHARACTER_HEIGHT = 1f;
    private static final float ASPECT_RATIO = 0.76f;
    private static final float CHARACTER_WIDTH = ASPECT_RATIO * CHARACTER_HEIGHT;
    private final Animation<TextureRegion> animation;
    public float MAX_X_MOVEMENT_VELOCITY = 5f;
    public float MAX_Y_MOVEMENT_VELOCITY = 2;
    float stateTime;
    //    float time;
//    boolean isFacingRight = true;
//    boolean canJump = false;
//    float deltaTime = Gdx.graphics.getDeltaTime();
    private int colorRow;
    private float xVelocity = 0f;
    private float yVelocity = 0f;
    private Body body;
    private CharacterAnimator characterAnimator;
    private SpriteBatch spriteBatch;
    private TextureRegion currentFrame;
    private MoveState moveState;

    public Character(int colorRow, World world) {
        this.colorRow = colorRow;
        this.animation = CharacterResources.getInstance().getAnimation(Action.IDLE, colorRow);
        this.moveState = MoveState.IDLE;
        this.body = createBody(world, Constants.VIEWPORT_WIDTH / 2, Constants.VIEWPORT_HEIGHT / 2 + 5);

        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    private Body createBody(World world, float centerX, float centerY) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);
        bodyDef.linearDamping = 0f;
        //bodyDef.angularDamping = 1f;
        bodyDef.fixedRotation = true;

        final var body = world.createBody(bodyDef);
        final var characterPolygon = new PolygonShape();
        final var fixtureDef = new FixtureDef();

        characterPolygon.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

        fixtureDef.shape = characterPolygon;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);

        characterPolygon.dispose();

        return body;
    }

    public void render(Camera camera) {
        //TODO: create animation only once
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time
        var isFlipped = false;
        if (currentFrame != null) {
            isFlipped = currentFrame.isFlipX();
        }
        currentFrame = animation.getKeyFrame(stateTime, true);

        if (isFlipped && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        var affine = new Affine2();
        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        //Sprite feinte = animation.getKeyFrame().getTexture();

        //TODO: Check if correct when rotated
        final var renderX = bodyX - (CHARACTER_WIDTH / 2f) * Math.cos(body.getAngle()) + (CHARACTER_WIDTH / 2f) * Math.sin(body.getAngle());
        final var renderY = bodyY - (CHARACTER_HEIGHT / 2f) * Math.cos(body.getAngle()) - (CHARACTER_HEIGHT / 2f) * Math.sin(body.getAngle());

        //TODO: Compute the scale elsewhere for better perf.
        affine.setToTrnRotScl((float) renderX, (float) renderY, rotation, 1, 1);

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        spriteBatch.draw(currentFrame, CHARACTER_WIDTH, CHARACTER_HEIGHT, affine);

        spriteBatch.end();
    }

    public void handleInputs() {
        moveState = MoveState.IDLE;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (!currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            moveState = MoveState.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            moveState = MoveState.RIGHT;
        }
    }

    public void update() {
        var velocity = body.getLinearVelocity(); // TODO

        float desiredVelX = 0f;
        switch (moveState) {
            case RIGHT:
                desiredVelX = MAX_X_MOVEMENT_VELOCITY;
                break;
            case LEFT:
                desiredVelX = -MAX_X_MOVEMENT_VELOCITY;
                break;
            case IDLE:
                desiredVelX = 0f;
                break;
            default:
                System.out.println("(╯°□°）╯︵ ┻━┻ ");
        }

        var deltaVelX = desiredVelX - velocity.x;
        float impulseX = body.getMass() * deltaVelX;

        body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }

    enum AnimationState {
        EnteringIdle, Idle, Walking, Climbing, EnteringJumping, Jumping, Landing, Swimming;

    }

    enum MoveState {
        RIGHT, IDLE, LEFT
    }
}
