package fr.baldurcrew.gdx25.water;

public class Spring {

    /// Horizontal position
    private final float x;
    private float height;
    private float velocity;

    public Spring(float x, float initialHeight) {
        this.x = x;
        height = initialHeight;
    }

    public void update(float stiffness, float dampening, float baseLevel) {
        float heightDelta = baseLevel - height;
        float acceleration = stiffness * heightDelta - dampening * velocity;

        // Euler
        velocity += acceleration;
        height += velocity; // TODO Check: Updated verticalVelocity before position
    }

    public void addVelocity(float velocity) {

        this.velocity += velocity;
    }

    public void addHeight(float height) {
        this.height += height;
    }

    public float getHeight() {
        return height;
    }

    public float getVelocity() {
        return velocity;
    }

    public float getX() {
        return x;
    }
}
