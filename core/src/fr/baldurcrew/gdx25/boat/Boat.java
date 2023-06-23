package fr.baldurcrew.gdx25.boat;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.Constants;
import fr.baldurcrew.gdx25.CoreGame;

public class Boat implements Disposable {

    public static final float BOAT_HEIGHT = 5f;
    public static final float ASPECT_RATIO = 1.07f;
    public static final float BOAT_WIDTH = ASPECT_RATIO * BOAT_HEIGHT;
    private Sprite boatSprite;
    private Texture boatTexture;
    private SpriteBatch spriteBatch; // TODO Share a sprite batch in CoreGame
    private Body body;

    // TODO Tweak
    private float density = 0.25f;
    private float restitution = 0.3f;
    private float friction = 0.5f;

    public Boat(World world, float centerX, float centerY) {
        spriteBatch = new SpriteBatch();
        boatTexture = new Texture("blue_boat.png");
        boatSprite = new Sprite(boatTexture);

        boatSprite.setSize(BOAT_WIDTH, BOAT_HEIGHT);
        boatSprite.setOriginCenter();
        boatSprite.setPosition(centerX - BOAT_WIDTH / 2f, centerY - BOAT_HEIGHT / 2f);

        body = createBody(world, centerX, centerY);
    }

    private Body createBody(World world, float centerX, float centerY) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);
        bodyDef.angularDamping = 0;

        final var body = world.createBody(bodyDef);
        body.setUserData(this);

        final var boatPolygon = new PolygonShape();
        float[] vertices = new float[]{
                -BOAT_WIDTH / 2f, 0,
                -BOAT_WIDTH / 3f, -BOAT_HEIGHT / 3f,
                0, -2f,
                BOAT_WIDTH / 3f, -BOAT_HEIGHT / 3f,
                BOAT_WIDTH / 2.2f, 0};
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

    public void render(Camera camera) {
        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        final var renderX = bodyX - BOAT_WIDTH / 2f;
        final var renderY = bodyY - BOAT_HEIGHT / 2.5f; // TODO Adjust to fixture vertices
        boatSprite.setPosition(renderX, renderY);
        boatSprite.setRotation(rotation);

        if (CoreGame.debugEnableBoatRendering) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            boatSprite.draw(spriteBatch);
            // spriteBatch.draw(boatSprite, posX, posY, BOAT_WIDTH, BOAT_HEIGHT);
            spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        boatTexture.dispose();
        spriteBatch.dispose();
    }

    public void update() {
        // Force the boat x position.
        body.setTransform(Constants.VIEWPORT_WIDTH / 2f, body.getPosition().y, body.getAngle()); // TODO Clean
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
}
