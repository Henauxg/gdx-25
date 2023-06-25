package fr.baldurcrew.gdx25.character.ai;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import fr.baldurcrew.gdx25.character.Character;

public class StalkerAiController extends AiController {

    private static final float RANDOM_SEEK_AREA = 0.5f;

    private State state;
    private float targetX;
    private float waitingTimer;
    private float waitDuration;

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
                    moveState = Character.MoveState.IDLE;
                    if (Math.abs(playerX - posX) > RANDOM_SEEK_AREA) {
                        state = State.ChoosingTargetPosition;
                    } else {
                        state = State.Waiting;
                        waitingTimer = 0f;
                        waitDuration = MathUtils.random(0.2f, 0.5f);
                    }
                } else {
                    if (targetX > posX) {
                        moveState = Character.MoveState.RIGHT;
                    } else {
                        moveState = Character.MoveState.LEFT;
                    }
                }
            }
            break;
            case Waiting: {
                moveState = Character.MoveState.IDLE;
                waitingTimer += Gdx.graphics.getDeltaTime();
                if (waitingTimer >= waitDuration) {
                    state = State.ChoosingTargetPosition;
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
        RunningToPos, Waiting, ChoosingTargetPosition
    }
}
