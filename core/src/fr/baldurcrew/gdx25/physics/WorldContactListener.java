package fr.baldurcrew.gdx25.physics;

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
        final var status = ContactStatus.Begin;

        final var handlerA = contactHandlers.get(fixtureA.getBody().getUserData());
        if (handlerA != null) {
            handlerA.handleContact(status, new FixtureContact(fixtureA, fixtureB));
        }

        final var handlerB = contactHandlers.get(fixtureB.getBody().getUserData());
        if (handlerB != null) {
            handlerB.handleContact(status, new FixtureContact(fixtureB, fixtureA));
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        final var status = ContactStatus.End;

        final var handlerA = contactHandlers.get(fixtureA.getBody().getUserData());
        if (handlerA != null) {
            handlerA.handleContact(status, new FixtureContact(fixtureA, fixtureB));
        }

        final var handlerB = contactHandlers.get(fixtureB.getBody().getUserData());
        if (handlerB != null) {
            handlerB.handleContact(status, new FixtureContact(fixtureB, fixtureA));
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }


}

