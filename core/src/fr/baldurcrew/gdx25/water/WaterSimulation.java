package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import fr.baldurcrew.gdx25.CoreGame;

import java.util.ArrayList;

public class WaterSimulation implements Disposable {

    private final static String PROJECTION_MATRIX_UNIFORM_NAME = "u_projTrans";
    private final float fromX, toX;
    private final ArrayList<Spring> springs;
    private final ShapeRenderer debugShapeBatch;
    private final ShaderProgram waterShaderProgram;
    private final MeshAndBuffers waterMeshAndBuffers;
    private final Body waterBody;
    private float wavesPropagationPasses = 4; // 8
    private float wavesPropagationSpreadFactor = 0.2f; // TODO Tweak
    private float springsStiffness = 0.025f;
    private float springsDampeningFactor = 0.025f;
    private float baseWaterLevel = 5f;
    private Color topWaterColor = new Color(0f, 1f, 0.8f, 0.7f);
    private Color bottomWaterColor = new Color(0f, 0f, 0.4f, 1f);

    public WaterSimulation(World world, int springsCount, float fromX, float toX) {
        this.fromX = fromX;
        this.toX = toX;
        this.springs = new ArrayList<>(springsCount);

        final var totalLength = toX - fromX;
        final var springPlacementStep = totalLength / (springsCount - 1);
        for (int i = 0; i < springsCount; i++) {
            final float x = fromX + i * springPlacementStep;
            springs.add(new Spring(x, baseWaterLevel));
        }

        debugShapeBatch = new ShapeRenderer();
        debugShapeBatch.setColor(0, 0.5f, 1, 1);

        waterShaderProgram = createWaterShader();
        waterMeshAndBuffers = createWaterMeshAndBuffers();

        waterBody = createWaterBody(world, fromX, toX);
    }

    private ShaderProgram createWaterShader() {
        String vertexShader = "attribute vec4 a_position;" + "\n" + "attribute vec4 a_color;" + "\n" + "varying vec4 v_color;" + "\n" + "uniform mat4 " + PROJECTION_MATRIX_UNIFORM_NAME + ";" + "\n" + "void main()" + "\n" + "{" + "\n" + "   v_color = a_color;" + "\n" + "   gl_Position =  u_projTrans * vec4(a_position.xy, 0.0, 1.0);" + "\n" + "}" + "\n";
        String fragmentShader = "#ifdef GL_ES" + "\n" + "precision mediump float;" + "\n" + "#endif" + "\n" + "varying vec4 v_color;" + "\n" + "void main()                                  " + "\n" + "{                                            " + "\n" + "  gl_FragColor = v_color;" + "\n" + "}";

        return new ShaderProgram(vertexShader, fragmentShader);
    }

    private MeshAndBuffers createWaterMeshAndBuffers() {
        final int rectCount = springs.size() - 1;
        final int verticesCount = 4 * rectCount;
        final int indicesCount = 6 * rectCount;
        final var vertexPositionAttribute = VertexAttribute.Position();
        final var vertexColorAttribute = VertexAttribute.ColorUnpacked();

        final var waterMesh = new Mesh(false, true, verticesCount, indicesCount, new VertexAttributes(vertexPositionAttribute, vertexColorAttribute));

        final int valuesPerVertex = vertexPositionAttribute.numComponents + vertexColorAttribute.numComponents;
        final var waterVertexIndices = new short[indicesCount];
        final var waterVerticesWithColor = new float[verticesCount * valuesPerVertex];

        for (int i = 0; i < rectCount; i++) {
            waterVertexIndices[i * 6 + 0] = (short) (i * 4 + 0);
            waterVertexIndices[i * 6 + 1] = (short) (i * 4 + 1);
            waterVertexIndices[i * 6 + 2] = (short) (i * 4 + 2);
            waterVertexIndices[i * 6 + 3] = (short) (i * 4 + 2);
            waterVertexIndices[i * 6 + 4] = (short) (i * 4 + 3);
            waterVertexIndices[i * 6 + 5] = (short) (i * 4 + 0);
        }
        waterMesh.setIndices(waterVertexIndices);

        return new MeshAndBuffers(waterMesh, valuesPerVertex, waterVertexIndices, waterVerticesWithColor);
    }

    private Body createWaterBody(World world, float fromX, float toX) {
        final var halfWidth = (toX - fromX) / 2f;
        final var halfHeight = baseWaterLevel / 2f;
        final var centerX = fromX + halfWidth;
        final var centerY = halfHeight;

        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(centerX, centerY);

        final var body = world.createBody(bodyDef);
        // TODO May need user data

        final var waterPolygon = new PolygonShape();
        // TODO generate vertices for the water body
        waterPolygon.setAsBox(halfWidth, halfHeight);

        final var fixtureDef = new FixtureDef();
        fixtureDef.shape = waterPolygon;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        waterPolygon.dispose();

        return body;
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
        if (CoreGame.debugMode) {
            debugShapeBatch.setProjectionMatrix(camera.combined);
            debugShapeBatch.begin(ShapeRenderer.ShapeType.Line);
            for (int i = 0; i < springs.size() - 1; i++) {
                final var leftSpring = springs.get(i);
                final var rightSpring = springs.get(i + 1);
                debugShapeBatch.line(new Vector2(leftSpring.getX(), 0f), new Vector2(leftSpring.getX(), leftSpring.getHeight()));
            }
            debugShapeBatch.end();
        }

        generateWaterMesh(waterMeshAndBuffers);

        Gdx.gl20.glEnable(GL20.GL_BLEND);
        waterShaderProgram.bind();
        waterShaderProgram.setUniformMatrix(PROJECTION_MATRIX_UNIFORM_NAME, camera.combined);
        waterMeshAndBuffers.mesh.render(waterShaderProgram, GL30.GL_TRIANGLES);
        Gdx.gl20.glDisable(GL20.GL_BLEND);
    }

    private void generateWaterMesh(MeshAndBuffers waterMeshAndBuffers) {
        final var verticesWithColor = waterMeshAndBuffers.verticesWithColor;
        final var valuesPerVertex = waterMeshAndBuffers.valuesPerVertex;

        // TODO Cull springs outside of the viewport
        for (int i = 0; i < springs.size() - 1; i++) {
            final var leftSpring = springs.get(i);
            final var rightSpring = springs.get(i + 1);

            int rectangleOffsetInArray = i * waterMeshAndBuffers.valuesPerVertex * 4;

            // 0 1 2  2 3 0
            //2-1       2
            // \|  and  |\
            //  0       3-0
            fillVertexArray(verticesWithColor, bottomWaterColor, rightSpring.getX(), -1f, rectangleOffsetInArray);
            fillVertexArray(verticesWithColor, topWaterColor, rightSpring.getX(), rightSpring.getHeight(), rectangleOffsetInArray + valuesPerVertex);
            fillVertexArray(verticesWithColor, topWaterColor, leftSpring.getX(), leftSpring.getHeight(), rectangleOffsetInArray + valuesPerVertex * 2);
            fillVertexArray(verticesWithColor, bottomWaterColor, leftSpring.getX(), -1f, rectangleOffsetInArray + valuesPerVertex * 3);
        }
        waterMeshAndBuffers.mesh.setVertices(verticesWithColor);
    }

    private void fillVertexArray(float[] vertexArray, Color color, float x, float y, int vertexOffsetInArray) {
        vertexArray[vertexOffsetInArray] = x;
        vertexArray[vertexOffsetInArray + 1] = y;
        vertexArray[vertexOffsetInArray + 2] = 0; // z position (screen coordinates)
        vertexArray[vertexOffsetInArray + 3] = color.r;
        vertexArray[vertexOffsetInArray + 4] = color.g;
        vertexArray[vertexOffsetInArray + 5] = color.b;
        vertexArray[vertexOffsetInArray + 6] = color.a;
    }

    public void handleInput(float xWorld) {
        if (xWorld >= fromX && xWorld <= toX) {
            final float index = this.springs.size() * (xWorld - fromX) / (toX - fromX);
            this.disturbWater(Math.round(index), 5f);
        }
    }

    @Override
    public void dispose() {
        waterMeshAndBuffers.mesh.dispose();
        waterShaderProgram.dispose();
    }

    public float getWaterLevel() {
        return this.baseWaterLevel;
    }

    private record MeshAndBuffers(Mesh mesh,
                                  int valuesPerVertex,
                                  short[] vertexIndices,
                                  float[] verticesWithColor) {
    }
}