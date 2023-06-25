package fr.baldurcrew.gdx25.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Quick & dirty, to render timer from pre-generated textures
 */
public class NumericRenderer implements Disposable {

    private static final int TEXTURES_COUNT = 10;
    private final Texture[] numbers = new Texture[TEXTURES_COUNT];
    private final Texture separator;

    public NumericRenderer() {
        for (int i = 0; i < TEXTURES_COUNT; i++) {
            numbers[i] = new Texture("number_" + i + ".png");
        }
        separator = new Texture("number_separator.png");
    }

    public void renderTimer(SpriteBatch spriteBatch, float x, float y, float renderWidth, float inputSeconds) {
        final int minutes = (int) Math.round(Math.floor(inputSeconds % 3600 / 60));
        final int seconds = (int) Math.round(Math.floor(inputSeconds % 60));

        int texturesCount;
        float widthPerTexture, heightPerTexture;
        float renderX = x - renderWidth / 2f;
        ;
        if (inputSeconds > 3600) {
            final int hours = (int) Math.round(Math.floor(inputSeconds / 3600));
            texturesCount = 8;
            widthPerTexture = renderWidth / texturesCount;
            heightPerTexture = widthPerTexture * 2f; // Assumes height = width * 2
            spriteBatch.draw(numbers[hours / 10], renderX, y, widthPerTexture, heightPerTexture);
            renderX += widthPerTexture;
            spriteBatch.draw(numbers[hours % 10], renderX, y, widthPerTexture, heightPerTexture);
            renderX += widthPerTexture;

            spriteBatch.draw(separator, renderX, y, widthPerTexture, heightPerTexture);
            renderX += widthPerTexture;
        } else {
            texturesCount = 6;
            widthPerTexture = renderWidth / texturesCount;
            heightPerTexture = widthPerTexture * 2f;
        }

        spriteBatch.draw(numbers[minutes / 10], renderX, y, widthPerTexture, heightPerTexture);
        renderX += widthPerTexture;
        spriteBatch.draw(numbers[minutes % 10], renderX, y, widthPerTexture, heightPerTexture);
        renderX += widthPerTexture;
        spriteBatch.draw(separator, renderX, y, widthPerTexture, heightPerTexture);
        renderX += widthPerTexture;
        spriteBatch.draw(numbers[seconds / 10], renderX, y, widthPerTexture, heightPerTexture);
        renderX += widthPerTexture;
        spriteBatch.draw(numbers[seconds % 10], renderX, y, widthPerTexture, heightPerTexture);
    }

    @Override
    public void dispose() {
        separator.dispose();
        for (Texture texture : numbers) {
            texture.dispose();
        }
    }
}