package fr.baldurcrew.gdx25.character.ai;

import fr.baldurcrew.gdx25.character.Character;

public class BellyDancerAiController extends AiController {
    public BellyDancerAiController(CharacterAiType type) {
        super(type);
    }

    @Override
    public Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently) {
        // TODO Implement
        return Character.MoveState.IDLE;
    }
}
