package fr.baldurcrew.gdx25.layer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

    private final TextureRegion textureRegion;
    private final Texture texture;
    private final float wiggleAmplitude;

    private float textureAspectRatio;
    private float scrollSpeedFactor;
    private float heightScalingRatio;
    private float yOffsetPositionFactor;
    private boolean isWrapHorizontally, iswWrapVertically;
    private float wiggleSpeed;
    private float animationTimer;
    private int textureRegionOffsetX;
    private int textureRegionOffsetY;


    public ParallaxLayer(Texture texture, float scrollSpeedFactor, float heightScalingRatio, float yOffsetPositionFactor, boolean isWrapHorizontally, boolean iswWrapVertically, float wiggleSpeed, float wiggleAmplitude) {
        this.texture = texture;
        this.scrollSpeedFactor = -scrollSpeedFactor;
        this.isWrapHorizontally = isWrapHorizontally;
        this.iswWrapVertically = iswWrapVertically;
        this.wiggleSpeed = wiggleSpeed;
        this.wiggleAmplitude = wiggleAmplitude;
        this.heightScalingRatio = heightScalingRatio;
        this.yOffsetPositionFactor = yOffsetPositionFactor;
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

        if (iswWrapVertically) {
            textureRegionOffsetY = ((int) (100 * scrollSpeedFactor * animationTimer)) % textureRegion.getRegionHeight();
            if (textureRegionOffsetY == 0) animationTimer = 0;
            textureRegion.setRegionY(textureRegionOffsetY);
            textureRegion.setRegionHeight(texture.getHeight());
        }
        var yOffset = camera.viewportHeight * yOffsetPositionFactor;

        if (wiggleSpeed > 0) {
            yOffset += (float) Math.cos(2 * Math.PI * textureRegionOffsetX / textureRegion.getRegionWidth() * wiggleSpeed) * wiggleAmplitude;
        }

        final var layerHeight = camera.viewportWidth / textureAspectRatio * heightScalingRatio;
        spriteBatch.draw(textureRegion, 0, camera.viewportHeight - layerHeight + yOffset, camera.viewportWidth, layerHeight);
    }
}
