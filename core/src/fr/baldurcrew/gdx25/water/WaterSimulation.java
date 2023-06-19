package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class WaterSimulation {
    private final float fromX, toX;
    private final ArrayList<Spring> springs;
    private final ShapeRenderer shapeBatch;
    private float wavesPropagationPasses = 4; // 8
    private float wavesPropagationSpreadFactor = 0.2f; // TODO Tweak
    private float springsStiffness = 0.025f;
    private float springsDampeningFactor = 0.025f;
    private float baseWaterLevel = 5f;

    public WaterSimulation(int springsCount, float fromX, float toX) {
        this.fromX = fromX;
        this.toX = toX;
        this.springs = new ArrayList<>(springsCount);

        final var totalLength = toX - fromX;
        final var springPlacementStep = totalLength / (springsCount - 1);
        for (int i = 0; i < springsCount; i++) {
            final float x = fromX + i * springPlacementStep;
            springs.add(new Spring(x));
        }

        shapeBatch = new ShapeRenderer();
        shapeBatch.setColor(0, 0.5f, 1, 1);
    }

    public void update() {
        for (var spring : this.springs) {
            spring.update(springsStiffness, springsDampeningFactor, baseWaterLevel);
        }

        float[] leftDeltas = new float[springs.size()];
        float[] rightDeltas = new float[springs.size()];

        for (int j = 0; j < wavesPropagationPasses; j++) {
            for (int i = 0; i < springs.size(); i++) {
                final var spring = springs.get(i);
                if (i > 0) {
                    final var leftSpring = springs.get(i - 1);
                    leftDeltas[i] = wavesPropagationSpreadFactor * (spring.getHeight() - leftSpring.getHeight());
                    leftSpring.addVelocity(leftDeltas[i]);
                }
                if (i < springs.size() - 1) {
                    final var rightSpring = springs.get(i + 1);
                    rightDeltas[i] = wavesPropagationSpreadFactor * (spring.getHeight() - rightSpring.getHeight());
                    rightSpring.addVelocity(rightDeltas[i]);
                }
            }

            for (int i = 0; i < springs.size(); i++) {
                if (i > 0) springs.get(i - 1).addHeight(leftDeltas[i]);

                if (i < springs.size() - 1) springs.get(i + 1).addHeight(rightDeltas[i]);
            }
        }
    }

    public void disturbWater(int index, float speed) {
        if (index >= 0 && index < springs.size()) {
            springs.get(index).addVelocity(speed);
        }
    }


    public void render(OrthographicCamera camera) {
        shapeBatch.setProjectionMatrix(camera.combined);

        for (int i = 0; i < springs.size() - 1; i++) {
            final var leftSpring = springs.get(i);
            final var rightSpring = springs.get(i + 1);

            shapeBatch.begin(ShapeRenderer.ShapeType.Line);
            shapeBatch.line(new Vector2(leftSpring.getX(), 0f), new Vector2(leftSpring.getX(), leftSpring.getHeight()));
            shapeBatch.end();
        }
    }

    public void handleInput(float xWorld) {
        if (xWorld >= fromX && xWorld <= toX) {
            final float index = this.springs.size() * (xWorld - fromX) / (toX - fromX);
            this.disturbWater(Math.round(index), 5f);
        }
    }
}
