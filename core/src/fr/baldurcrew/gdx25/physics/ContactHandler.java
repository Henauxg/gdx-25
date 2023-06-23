package fr.baldurcrew.gdx25.physics;

public interface ContactHandler {
    void handleContactBegin(FixtureContact contact);

    void handleContactEnd(FixtureContact contact);

    void handlePreSolve(FixtureContact contact);
}
