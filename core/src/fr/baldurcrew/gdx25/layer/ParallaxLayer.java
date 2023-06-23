package fr.baldurcrew.gdx25.layer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

    private final TextureRegion textureRegion;
    private final Texture texture;

    private float textureAspectRatio;
    private float scrollSpeedFactor;
    private boolean isWrapHorizontally, iswWrapVertically;
    private float animationTimer;
    private int textureRegionOffsetX;

    public ParallaxLayer(Texture texture, float scrollSpeedFactor, boolean isWrapHorizontally, boolean iswWrapVertically) {
        this.texture = texture;
        this.scrollSpeedFactor = -scrollSpeedFactor;
        this.isWrapHorizontally = isWrapHorizontally;
        this.iswWrapVertically = iswWrapVertically;
        this.texture.setWrap(
                this.isWrapHorizontally ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge,
                this.iswWrapVertically ? Texture.TextureWrap.Repeat : Texture.TextureWrap.ClampToEdge
        );
        textureRegion = new TextureRegion(texture);
        textureAspectRatio = texture.getWidth() / texture.getHeight();
    }

    public void render(Camera camera, SpriteBatch spriteBatch, float deltaTime) {
        animationTimer += deltaTime;

        textureRegionOffsetX = ((int) (100 * scrollSpeedFactor * animationTimer)) % textureRegion.getRegionWidth();
        if (textureRegionOffsetX == 0) animationTimer = 0;
        textureRegion.setRegionX(textureRegionOffsetX);
        textureRegion.setRegionWidth(texture.getWidth());

        final var heightScalingRatio = 0.9f;
        final var yOffset = camera.viewportHeight * 0.12f;
        final var layerHeight = camera.viewportWidth / textureAspectRatio * heightScalingRatio;
        spriteBatch.draw(textureRegion, 0, camera.viewportHeight - layerHeight + yOffset, camera.viewportWidth, layerHeight);
    }
}
