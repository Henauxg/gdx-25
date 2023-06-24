package fr.baldurcrew.gdx25.character.ai;

import fr.baldurcrew.gdx25.character.Character;

public class DarwinAiController extends AiController {
    public DarwinAiController(CharacterAiType type) {
        super(type);
    }

    @Override
    public Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently) {
        Character.MoveState moveState = Character.MoveState.IDLE;
        if (touchedBoatRecently) {
            moveState = super.currentDirection;
        }
        return moveState;
    }
}
