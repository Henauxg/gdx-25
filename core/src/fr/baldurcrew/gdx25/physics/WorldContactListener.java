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

        final ContactHandler handlerA = contactHandlers.get(fixtureA.getBody().getUserData());
        if (handlerA != null) {
            handlerA.handleContactBegin(new FixtureContact(fixtureA, fixtureB));
        }

        final ContactHandler handlerB = contactHandlers.get(fixtureB.getBody().getUserData());
        if (handlerB != null) {
            handlerB.handleContactBegin(new FixtureContact(fixtureB, fixtureA));
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        final ContactHandler handlerA = contactHandlers.get(fixtureA.getBody().getUserData());
        if (handlerA != null) {
            handlerA.handleContactEnd(new FixtureContact(fixtureA, fixtureB));
        }

        final ContactHandler handlerB = contactHandlers.get(fixtureB.getBody().getUserData());
        if (handlerB != null) {
            handlerB.handleContactEnd(new FixtureContact(fixtureB, fixtureA));
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        final ContactHandler handlerA = contactHandlers.get(fixtureA.getBody().getUserData());
        if (handlerA != null) {
            handlerA.handlePreSolve(contact, new FixtureContact(fixtureA, fixtureB));
        }
        final ContactHandler handlerB = contactHandlers.get(fixtureB.getBody().getUserData());
        if (handlerB != null) {
            handlerB.handlePreSolve(contact, new FixtureContact(fixtureB, fixtureA));
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }


}

