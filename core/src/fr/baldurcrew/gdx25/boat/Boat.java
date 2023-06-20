package fr.baldurcrew.gdx25.boat;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class Boat implements Disposable {
    private Texture boatTexture;
    private SpriteBatch spriteBatch; // TODO Share a sprite batch in CoreGame
    private float x, y;

    public Boat(Texture boatTexture, float x, float y) {
        this.x = x;
        this.y = y;
        spriteBatch = new SpriteBatch();
//        this.boatTexture = new Texture("blue_boat.png");
        this.boatTexture = boatTexture;
    }

    public void render(Camera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(boatTexture, 50, 50);
        spriteBatch.draw(boatTexture, x, y);
        spriteBatch.draw(boatTexture, 50, -50);
        spriteBatch.end();
    }


    @Override
    public void dispose() {
        boatTexture.dispose();
        spriteBatch.dispose();
    }
}
