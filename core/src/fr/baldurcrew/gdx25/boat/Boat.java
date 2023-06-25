package fr.baldurcrew.gdx25.boat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.Constants;
import fr.baldurcrew.gdx25.CoreGame;
import fr.baldurcrew.gdx25.monster.Eatable;
import fr.baldurcrew.gdx25.monster.Monster;

public class Boat implements Disposable, Eatable {

    public static final float BOAT_HEIGHT = 5f;
    public static final float ASPECT_RATIO = 1.07f;
    public static final float BOAT_WIDTH = ASPECT_RATIO * BOAT_HEIGHT;
    private static final float DEFAULT_BOAT_ANGULAR_DAMPING = 0;
    private static final double MAX_ALLOWED_BOAT_ANGLE = 120;
    private static final float BOAT_UPSIDE_DOWN_TIMER = 1.5f;

    private Sprite boatSprite;
    private Texture boatTexture;
    private Body body;
    private float upsideDownTimer;
    private boolean upsideDown;

    // TODO Tweak
    private float density = 0.25f;
    private float restitution = 0.3f;
    private float friction = 0.25f;

    private boolean freezeY;
    private float freezeToY;
    private boolean eaten;
    private float startingY;
    private float yToBeEaten;
    private float deathByKrakenTranslationTimer;

    public Boat(World world, float centerX, float centerY) {
        boatTexture = new Texture("blue_boat.png");
        boatSprite = new Sprite(boatTexture);

        boatSprite.setSize(BOAT_WIDTH, BOAT_HEIGHT);
        boatSprite.setOriginCenter();
        boatSprite.setPosition(centerX - BOAT_WIDTH / 2f, centerY - BOAT_HEIGHT / 2f);

        body = createBody(world, centerX, centerY);
        upsideDownTimer = 0f;
        upsideDown = false;
        freezeY = false;
        eaten = false;
    }

    private Body createBody(World world, float centerX, float centerY) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);
        bodyDef.angularDamping = DEFAULT_BOAT_ANGULAR_DAMPING;

        final var body = world.createBody(bodyDef);
        body.setUserData(this);

        final var boatPolygon = new PolygonShape();
        float[] vertices = new float[]{-BOAT_WIDTH / 2f, 0, -BOAT_WIDTH / 3f, -BOAT_HEIGHT / 3f, 0, -2f, BOAT_WIDTH / 3f, -BOAT_HEIGHT / 3f, BOAT_WIDTH / 2.2f, 0};
        boatPolygon.set(vertices);

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = boatPolygon;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        body.createFixture(fixtureDef);

        boatPolygon.dispose();

        return body;
    }

    public void render(Camera camera, SpriteBatch spriteBatch) {
        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        final var renderX = bodyX - BOAT_WIDTH / 2f;
        final var renderY = bodyY - BOAT_HEIGHT / 2.5f; // TODO Adjust to fixture vertices
        boatSprite.setPosition(renderX, renderY);
        boatSprite.setRotation(rotation);

        if (CoreGame.debugEnableBoatRendering) {
            spriteBatch.setProjectionMatrix(camera.combined);
            boatSprite.draw(spriteBatch);
            // spriteBatch.draw(boatSprite, posX, posY, BOAT_WIDTH, BOAT_HEIGHT);
        }
    }

    @Override
    public void dispose() {
        boatTexture.dispose();
    }

    public boolean update() {
        // Force the boat x position.
        body.setTransform(Constants.VIEWPORT_WIDTH / 2f, body.getPosition().y, body.getAngle()); // TODO Clean

        if (eaten) {
            deathByKrakenTranslationTimer += Gdx.graphics.getDeltaTime();
            var y = startingY + ((yToBeEaten - startingY) * (deathByKrakenTranslationTimer / Monster.UP_TRANSLATION_ANIMATION_DURATION));
            body.setTransform(Constants.VIEWPORT_WIDTH / 2f, y, body.getAngle());
        }

        if (freezeY) {
            body.setTransform(Constants.VIEWPORT_WIDTH / 2f, freezeToY, body.getAngle());
        }

        // Check for Upside down boat
        final var clampedAngle = Math.abs(Math.toDegrees(body.getAngle()) % 360);
        if (clampedAngle >= MAX_ALLOWED_BOAT_ANGLE) {
            upsideDownTimer += Constants.TIME_STEP;
            if (upsideDownTimer >= BOAT_UPSIDE_DOWN_TIMER) {
                upsideDown = true;
            }
        } else {
            upsideDownTimer = 0;
        }

        return upsideDown;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setDensity(density);
        });
        this.body.resetMassData();
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setRestitution(restitution);
        });
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
        this.body.getFixtureList().forEach(fixture -> {
            fixture.setFriction(friction);
        });
    }

    public Body getBody() {
        return body;
    }

    public void setAngularDamping(float v) {
        this.body.setAngularDamping(v);
    }

    @Override
    public float getX() {
        return body.getPosition().x;
    }

    @Override
    public void freezeY(float y) {
        this.freezeY = true;
        this.freezeToY = y;
    }

    @Override
    public void prepareToBeEaten(float yToBeEaten) {
        this.eaten = true;
        this.startingY = body.getPosition().y;
        this.yToBeEaten = yToBeEaten;
        this.deathByKrakenTranslationTimer = 0f;
    }

    @Override
    public float getMealSizeFactor() {
        return 1.5f;
    }

    public boolean isEaten() {
        return eaten;
    }
}
