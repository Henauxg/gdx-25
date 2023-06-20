package fr.baldurcrew.gdx25;

import com.badlogic.gdx.physics.box2d.*;

import java.util.HashMap;


public class WorldContactListener implements ContactListener {
    private HashMap<Object, ContactHandler> contactHandlers = new HashMap<>();

    public void addListener(ContactHandler listener) {
        contactHandlers.put(listener, listener);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        final var handlerA = contactHandlers.get(fixtureA.getUserData());
        if (handlerA != null) {
            handlerA.handleContact(new ContactUpdate(fixtureA, fixtureB, ContactStatus.Begin));
        }

        final var handlerB = contactHandlers.get(fixtureB.getUserData());
        if (handlerB != null) {
            handlerB.handleContact(new ContactUpdate(fixtureB, fixtureA, ContactStatus.Begin));
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        final var handlerA = contactHandlers.get(fixtureA.getUserData());
        if (handlerA != null) {
            handlerA.handleContact(new ContactUpdate(fixtureA, fixtureB, ContactStatus.End));
        }

        final var handlerB = contactHandlers.get(fixtureB.getUserData());
        if (handlerB != null) {
            handlerB.handleContact(new ContactUpdate(fixtureB, fixtureA, ContactStatus.End));
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public enum ContactStatus {
        Begin, End,
    }

    public interface ContactHandler {
        public void handleContact(WorldContactListener.ContactUpdate contact);
    }

    public record ContactUpdate(Fixture fixtureA, Fixture fixtureB, ContactStatus status) {
    }
}
