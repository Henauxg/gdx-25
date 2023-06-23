package fr.baldurcrew.gdx25.character.ai;

import fr.baldurcrew.gdx25.Constants;
import fr.baldurcrew.gdx25.character.Character;

import static fr.baldurcrew.gdx25.boat.Boat.BOAT_WIDTH;

public class MadRunnerAiController extends AiController {

    private static final float BOAT_PADDING = 0.7f;

    public MadRunnerAiController(CharacterAiType type) {
        super(type);
    }

    @Override
    public Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently) {
        if (touchedBoatRecently) {
            if (currentDirection == Character.MoveState.RIGHT) {
                if (posX > Constants.VIEWPORT_WIDTH / 2f + BOAT_WIDTH / 2f - BOAT_PADDING) {
                    currentDirection = Character.MoveState.LEFT;
                }
            } else if (currentDirection == Character.MoveState.LEFT) {
                if (posX < Constants.VIEWPORT_WIDTH / 2f - BOAT_WIDTH / 2f + BOAT_PADDING) {
                    currentDirection = Character.MoveState.RIGHT;
                }
            }
        } else {
            return Character.MoveState.IDLE;
        }

        return currentDirection;
    }

    enum State {
        RunningRight, RunningLeft
    }
}
