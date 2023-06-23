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
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.physics.ContactHandler;
import fr.baldurcrew.gdx25.physics.FixtureContact;

public class Character extends Actor implements Disposable, ContactHandler { // TODO Remove Actor since unused
    private static final float CHARACTER_HEIGHT = 0.75f;
    private static final float ASPECT_RATIO = 0.76f;
    private static final float CHARACTER_WIDTH = ASPECT_RATIO * CHARACTER_HEIGHT;
    private static final float MAX_X_MOVEMENT_VELOCITY = 5f;

    private final Animation<TextureRegion> animation;
    private boolean touchingBoat;

    private Body body;
    private SpriteBatch spriteBatch;
    private float stateTime;
    private TextureRegion currentFrame;
    private MoveState moveState;
    private Boat boat;
    private Vector2 boatContactPoint;

    public Character(World world, Boat boat, int colorRow, float x, float y, float density, float friction, float restitution) {
        this.boat = boat;
        this.animation = CharacterResources.getInstance().getAnimation(Action.IDLE, colorRow);
        this.moveState = MoveState.IDLE;
        this.body = createBody(world, x, y, density, friction, restitution);

        spriteBatch = new SpriteBatch();
        stateTime = 0f;
        touchingBoat = false;
    }

    private Body createBody(World world, float centerX, float centerY, float density, float friction, float restitution) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);
        bodyDef.linearDamping = 0f;
        //bodyDef.angularDamping = 1f;
        bodyDef.fixedRotation = true;

        final var body = world.createBody(bodyDef);
        body.setUserData(this);
        {
            final var characterPolygon = new PolygonShape();
            final var collider = new FixtureDef();

            characterPolygon.setAsBox(CHARACTER_WIDTH / 2f, CHARACTER_HEIGHT / 2f);

            collider.shape = characterPolygon;
            collider.density = density;
            collider.friction = friction;
            collider.restitution = restitution;

            body.createFixture(collider);
            characterPolygon.dispose();
        }
        {
            final var footSensorPolygon = new PolygonShape();
            final var footSensor = new FixtureDef();

            final var footSensorHeight = CHARACTER_HEIGHT / 10f;
            footSensorPolygon.setAsBox(0.9f * CHARACTER_WIDTH / 2f, footSensorHeight, new Vector2(0, -CHARACTER_HEIGHT / 2f - footSensorHeight / 2f), 0f);

            footSensor.shape = footSensorPolygon;
            footSensor.isSensor = true;

            body.createFixture(footSensor);
            footSensorPolygon.dispose();
        }

        return body;
    }

    public void render(Camera camera) {
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

        final var renderX = bodyX - (CHARACTER_WIDTH / 2f) * Math.cos(body.getAngle()) + (CHARACTER_WIDTH / 2f) * Math.sin(body.getAngle());
        final var renderY = bodyY - (CHARACTER_HEIGHT / 2f) * Math.cos(body.getAngle()) - (CHARACTER_HEIGHT / 2f) * Math.sin(body.getAngle());

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
        var velocity = body.getLinearVelocity(); // TODO Compute the character relative velocity to its environment (boat, ..)
        if (touchingBoat && boatContactPoint != null) {
            final float boatAngularVelocity = boat.getBody().getAngularVelocity();
            Gdx.app.log("Char", "Correcting char velocity by " + boatAngularVelocity);
            if (boatAngularVelocity != 0) {
                final Vector2 contactVector = boatContactPoint.cpy().sub(boat.getBody().getPosition());
                final Vector2 velocityFromBoatAngularVel = new Vector2(-boatAngularVelocity * contactVector.y, boatAngularVelocity * contactVector.x);
                velocity.sub(velocityFromBoatAngularVel);
            }
        }

        float desiredVelX = 0f;
        switch (moveState) {
            case RIGHT: {
                desiredVelX = MAX_X_MOVEMENT_VELOCITY;
            }
            break;
            case LEFT: {
                desiredVelX = -MAX_X_MOVEMENT_VELOCITY;
            }
            break;
            case IDLE: {
                desiredVelX = 0f;
                //desiredVelX = velocity.x * 0.97f;
            }
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

    public void setFriction(float friction) {
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setFriction(friction);
        });
    }

    public void setDensity(float density) {
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setDensity(density);
        });
        this.body.resetMassData();
    }

    public void setRestitution(float restitution) {
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setRestitution(restitution);
        });
    }

    @Override
    public void handleContactBegin(FixtureContact contact) {
        if (contact.otherFixture().getBody().getUserData() == boat) {
            touchingBoat = true;
            Gdx.app.log("Char", "touchingBoat = true");
        }
    }

    @Override
    public void handleContactEnd(FixtureContact contact) {
        if (contact.otherFixture().getBody().getUserData() == boat) {
            touchingBoat = false;
            Gdx.app.log("Char", "touchingBoat = false");
        }
    }

    @Override
    public void handlePreSolve(Contact contact, FixtureContact fixtures) {
        if (fixtures.otherFixture().getBody().getUserData() == boat) {
            if (contact.getWorldManifold().getNumberOfContactPoints() > 0) {
                boatContactPoint = contact.getWorldManifold().getPoints()[0];
            }
        }
    }

    enum AnimationState {
        EnteringIdle, Idle, Walking, Climbing, EnteringJumping, Jumping, Landing, Swimming;
    }

    enum MoveState {
        RIGHT, IDLE, LEFT
    }
}
