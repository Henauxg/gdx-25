package fr.baldurcrew.gdx25.boat;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class Boat implements Disposable {

    private static final float BOAT_HEIGHT = 5f;
    private static final float ASPECT_RATIO = 1.07f;
    private static final float BOAT_WIDTH = ASPECT_RATIO * BOAT_HEIGHT;
    private Texture boatTexture;
    private SpriteBatch spriteBatch; // TODO Share a sprite batch in CoreGame
    private float x, y;

    public Boat(float x, float y) {
        this.x = x - BOAT_WIDTH / 2f;
        this.y = y - BOAT_HEIGHT / 2f;

        spriteBatch = new SpriteBatch();
        this.boatTexture = new Texture("blue_boat.png");
    }

    public void render(Camera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(boatTexture, x, y, BOAT_WIDTH, BOAT_HEIGHT);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        boatTexture.dispose();
        spriteBatch.dispose();
    }
}
