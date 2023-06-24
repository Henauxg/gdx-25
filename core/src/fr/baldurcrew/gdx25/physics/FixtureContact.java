package fr.baldurcrew.gdx25.physics;

import com.badlogic.gdx.physics.box2d.Fixture;

public class FixtureContact {

    public final Fixture handledFixture;
    public final Fixture otherFixture;

    public FixtureContact(Fixture handledFixture, Fixture otherFixture) {
        this.handledFixture = handledFixture;
        this.otherFixture = otherFixture;
    }
}