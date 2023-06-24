package fr.baldurcrew.gdx25.character.ai;

import com.badlogic.gdx.math.MathUtils;
import fr.baldurcrew.gdx25.character.Character;

public class StalkerAiController extends AiController {

    private static final float RANDOM_SEEK_AREA = 0.6f;

    private State state;
    private float targetX;

    public StalkerAiController(CharacterAiType aiType) {
        super(aiType);

        state = State.ChoosingTargetPosition;
    }

    @Override
    public Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently) {
        Character.MoveState moveState = Character.MoveState.IDLE;
        switch (state) {
            case RunningToPos: {
                if ((currentDirection == Character.MoveState.RIGHT && posX > targetX) || (currentDirection == Character.MoveState.LEFT && posX < targetX)) {
                    state = State.ChoosingTargetPosition;
                    moveState = Character.MoveState.IDLE;
                } else {
                    if (targetX > posX) {
                        moveState = Character.MoveState.RIGHT;
                    } else {
                        moveState = Character.MoveState.LEFT;
                    }
                }
            }
            break;
            case ChoosingTargetPosition: {
                targetX = MathUtils.random(playerX - RANDOM_SEEK_AREA, playerX + RANDOM_SEEK_AREA);
                if (targetX > posX) {
                    currentDirection = Character.MoveState.RIGHT;
                } else {
                    currentDirection = Character.MoveState.LEFT;
                }
                state = State.RunningToPos;
            }
            break;
        }
        return moveState;
    }

    enum State {
        RunningToPos,
        ChoosingTargetPosition
    }
}
