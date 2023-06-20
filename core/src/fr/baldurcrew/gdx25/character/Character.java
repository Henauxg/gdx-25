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
import fr.baldurcrew.gdx25.Constants;

public class Character extends Actor implements Disposable {
    private static final float CHARACTER_HEIGHT = 1f;
    private static final float ASPECT_RATIO = 0.76f;
    private static final float CHARACTER_WIDTH = ASPECT_RATIO * CHARACTER_HEIGHT;
    private final Animation<TextureRegion> animation;
    public int MAX_X_MOVEMENT_VELOCITY = 2;
    public int MAX_Y_MOVEMENT_VELOCITY = 2;
    float stateTime;
    float time;
    boolean isFacingRight = true;
    boolean canJump = false;
    float deltaTime = Gdx.graphics.getDeltaTime();
    private int colorRow;
    private float xVelocity = 0f;
    private float yVelocity = 0f;
    private Body body;
    private CharacterAnimator characterAnimator;
    private SpriteBatch spriteBatch;
    private TextureRegion currentFrame;

    public Character(int colorRow, World world) {
        this.colorRow = colorRow;
        this.animation = CharacterResources.getInstance().getAnimation(Action.IDLE, colorRow);

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

        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        //Sprite feinte = animation.getKeyFrame().getTexture();

        System.out.println("rotation : " + rotation + "°");
        System.out.println("angle : " + body.getAngle());
        //TODO: Check if correct when rotated
        final var renderX = bodyX - (CHARACTER_WIDTH / 2f) * Math.cos(body.getAngle()) + (CHARACTER_WIDTH / 2f) * Math.sin(body.getAngle());
        final var renderY = bodyY - (CHARACTER_HEIGHT / 2f) * Math.cos(body.getAngle()) - (CHARACTER_HEIGHT / 2f) * Math.sin(body.getAngle());
        //final var renderX = bodyX;
        //final var renderY = bodyY;

        var affine = new Affine2();
        //TODO: Compute the scale elsewhere for better perf.
        affine.setToTrnRotScl((float) renderX, (float) renderY, rotation, 1, 1);

        spriteBatch.setProjectionMatrix(camera.combined);

        System.out.println("current region height : " + currentFrame.getRegionHeight() + "  current region width : " + currentFrame.getRegionWidth());
        System.out.println("Body center  : " + body.getLocalCenter());

        spriteBatch.begin();
        spriteBatch.draw(currentFrame, CHARACTER_WIDTH, CHARACTER_HEIGHT, affine);
        //spriteBatch.draw(currentFrame, bodyX, bodyY, CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f, CHARACTER_WIDTH, CHARACTER_HEIGHT, 1f, 1f, rotation, false);

        spriteBatch.end();
    }

    public void handleInputs() {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (!currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            body.applyForceToCenter(new Vector2(-MAX_X_MOVEMENT_VELOCITY, 0), true);
            //characterAnimator.
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            if (currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            body.applyForceToCenter(new Vector2(MAX_X_MOVEMENT_VELOCITY, 0), true);
        }

        //TODO: Check wake parameters
//        body.applyForceToCenter(new Vector2(xVelocity, 0), true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }

    enum State {
        EnteringIdle, Idle, Walking, Climbing, EnteringJumping, Jumping, Landing, Swimming;

    }
}
