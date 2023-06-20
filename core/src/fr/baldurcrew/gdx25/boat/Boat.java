package fr.baldurcrew.gdx25.boat;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;

public class Boat implements Disposable {

    private static final float BOAT_HEIGHT = 5f;
    private static final float ASPECT_RATIO = 1.07f;
    private static final float BOAT_WIDTH = ASPECT_RATIO * BOAT_HEIGHT;
    private Sprite boatSprite;
    private Texture boatTexture;
    private SpriteBatch spriteBatch; // TODO Share a sprite batch in CoreGame
    //    private float centerX, centerY, renderX, renderY;
    private Body body;

    public Boat(World world, float centerX, float centerY) {
//        this.centerX = centerX;
//        this.centerY = centerY;
//        this.renderX = centerX - BOAT_WIDTH / 2f;
//        this.renderY = centerY - BOAT_HEIGHT / 2f;

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

        final var body = world.createBody(bodyDef);

        final var boatPolygon = new PolygonShape();
        // TODO generate vertices for the boat body
        boatPolygon.setAsBox(BOAT_WIDTH / 2f, BOAT_HEIGHT / 5f); // TODO Adjust

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = boatPolygon;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);

        boatPolygon.dispose();

        return body;
    }

    public void render(Camera camera) {
        float bodyX = body.getPosition().x;
        float bodyY = body.getPosition().y;
        float rotation = (float) Math.toDegrees(body.getAngle());

        final var renderX = bodyX - BOAT_WIDTH / 2f;
        final var renderY = bodyY - BOAT_HEIGHT / 5f; // TODO Adjust
        boatSprite.setPosition(renderX, renderY);
        boatSprite.setRotation(rotation);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        boatSprite.draw(spriteBatch);
//        spriteBatch.draw(boatSprite, posX, posY, BOAT_WIDTH, BOAT_HEIGHT);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        boatTexture.dispose();
        spriteBatch.dispose();
    }
}
