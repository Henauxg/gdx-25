package fr.baldurcrew.gdx25.character.ai;

import fr.baldurcrew.gdx25.character.Character;

public class StalkerAiController extends AiController {


    public StalkerAiController(CharacterAiType aiType) {
        super(aiType);
    }

    @Override
    public Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently) {
        var moveState = Character.MoveState.IDLE;
        if (playerX > posX) {
            moveState = Character.MoveState.RIGHT;
        } else {
            moveState = Character.MoveState.LEFT;
        }
        return moveState;
    }


}
