package fr.baldurcrew.gdx25.physics;

public interface ContactHandler {
    public void handleContact(ContactStatus status, FixtureContact contact);
}
