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
import fr.baldurcrew.gdx25.boat.Boat;
import fr.baldurcrew.gdx25.physics.ContactHandler;
import fr.baldurcrew.gdx25.physics.FixtureContact;

public class Character extends Actor implements Disposable, ContactHandler { // TODO Remove Actor since unused
    private static final float MAX_X_MOVEMENT_VELOCITY = 5f;
    private static final float MAX_TIME_RECENT_BOAT_TOUCH = 2f;
    private final boolean aiControlled;
    private final int charIndex;

    private boolean touchingBoat;
    private boolean touchedBoatRecently;
    private float lastBoatTouchTimer;
    private Body body;
    private float animationTimer;
    private Animation<TextureRegion> animation;
    private boolean shouldFlipX;
    private TextureRegion currentFrame;
    private MoveState moveState;
    private MoveState previousMoveState;
    private Boat boat;
    private Vector2 boatContactPoint;

    public Character(World world, Boat boat, int charIndex, boolean aiControlled, float x, float y, float density, float friction, float restitution) {
        this.boat = boat;
        this.charIndex = charIndex;
        this.aiControlled = aiControlled;
        this.animation = CharacterResources.getInstance().getAnimation(Action.IDLE, charIndex);
        this.previousMoveState = MoveState.IDLE;
        this.moveState = MoveState.IDLE;
        this.shouldFlipX = false;
        this.body = createBody(world, x, y, density, friction, restitution);

        animationTimer = 0f;
        touchingBoat = false;
        touchedBoatRecently = false;
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

            characterPolygon.setAsBox(CharacterResources.CHARACTER_WIDTH / 2f, CharacterResources.CHARACTER_HEIGHT / 2f);

            collider.shape = characterPolygon;
            collider.density = density;
            collider.friction = friction;
            collider.restitution = restitution;
            // Quick & dirty characters do not collide with each others
            collider.filter.categoryBits = 2;
            collider.filter.maskBits = 1;

            body.createFixture(collider);
            characterPolygon.dispose();
        }
        {
            final var footSensorPolygon = new PolygonShape();
            final var footSensor = new FixtureDef();

            final var footSensorHeight = CharacterResources.CHARACTER_HEIGHT / 10f;
            footSensorPolygon.setAsBox(0.9f * CharacterResources.CHARACTER_WIDTH / 2f, footSensorHeight, new Vector2(0, -CharacterResources.CHARACTER_HEIGHT / 2f - footSensorHeight / 2f), 0f);

            footSensor.shape = footSensorPolygon;
            footSensor.isSensor = true;

            body.createFixture(footSensor);
            footSensorPolygon.dispose();
        }

        return body;
    }

    public void render(Camera camera, SpriteBatch spriteBatch) {
        animationTimer += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

        // Update the animation
        if (moveState == MoveState.LEFT && previousMoveState != MoveState.LEFT) {
            animation = CharacterResources.getInstance().getAnimation(Action.WALK, charIndex);
            animationTimer = 0f;
            shouldFlipX = true;
        } else if (moveState == MoveState.RIGHT && previousMoveState != MoveState.RIGHT) {
            animation = CharacterResources.getInstance().getAnimation(Action.WALK, charIndex);
            animationTimer = 0f;
            shouldFlipX = false;
        } else if (moveState == MoveState.IDLE && previousMoveState != MoveState.IDLE) {
            animation = CharacterResources.getInstance().getAnimation(Action.IDLE, charIndex);
            animationTimer = 0f;
            shouldFlipX = false;
        }
        previousMoveState = moveState;

        currentFrame = animation.getKeyFrame(animationTimer, true);
        if ((shouldFlipX && !currentFrame.isFlipX() || (!shouldFlipX && currentFrame.isFlipX()))) {
            currentFrame.flip(true, false);
        }

        var affine = new Affine2();
        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        final var renderX = bodyX - (CharacterResources.CHARACTER_WIDTH / 2f) * Math.cos(body.getAngle()) + (CharacterResources.CHARACTER_WIDTH / 2f) * Math.sin(body.getAngle());
        final var renderY = bodyY - (CharacterResources.CHARACTER_HEIGHT / 2f) * Math.cos(body.getAngle()) - (CharacterResources.CHARACTER_HEIGHT / 2f) * Math.sin(body.getAngle());

        affine.setToTrnRotScl((float) renderX, (float) renderY, rotation, 1, 1);

        spriteBatch.draw(currentFrame, CharacterResources.CHARACTER_WIDTH, CharacterResources.CHARACTER_HEIGHT, affine);
    }

    public void handleInputs() {
        if (aiControlled) {
            if (touchedBoatRecently) {
                moveState = MoveState.LEFT;
            } else {
                moveState = MoveState.IDLE;
            }
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                moveState = MoveState.LEFT;
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                moveState = MoveState.RIGHT;
            } else {
                moveState = MoveState.IDLE;
            }
        }
    }

    public void update() {
        if (touchedBoatRecently && !touchingBoat) {
            lastBoatTouchTimer += Constants.TIME_STEP;
            if (lastBoatTouchTimer >= MAX_TIME_RECENT_BOAT_TOUCH) {
                lastBoatTouchTimer = 0;
                touchedBoatRecently = false;
            }
        }

        var velocity = body.getLinearVelocity();
        // Here, compute the character relative velocity to its environment (boat, ..)
        if (touchingBoat && boatContactPoint != null) {
            final float boatAngularVelocity = boat.getBody().getAngularVelocity();
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
            touchedBoatRecently = true;
            lastBoatTouchTimer = 0;
        }
    }

    @Override
    public void handleContactEnd(FixtureContact contact) {
        if (contact.otherFixture().getBody().getUserData() == boat) {
            touchingBoat = false;
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
