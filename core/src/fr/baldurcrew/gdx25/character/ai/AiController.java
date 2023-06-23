package fr.baldurcrew.gdx25.character.ai;

import com.badlogic.gdx.math.MathUtils;
import fr.baldurcrew.gdx25.character.Character;

public abstract class AiController {
    private final CharacterAiType aiType;

    protected Character.MoveState currentDirection;

    public AiController(CharacterAiType aiType) {
        this.aiType = aiType;

        currentDirection = getRandomDirection();
    }

    public static AiController getRandomAiController() {
        var type = CharacterAiType.getRandomType();
        switch (type) {
            case Stalker -> {
                return new StalkerAiController(type);
            }
            case Darwin -> {
                return new DarwinAiController(type);
            }
            case MadRunner -> {
                return new RunnerAiController(type);
            }
            case BellyDancer -> {
                return new BellyDancerAiController(type);
            }
        }
        return new DarwinAiController(type);
    }

    private Character.MoveState getRandomDirection() {
        if (MathUtils.random(0, 1) == 0) {
            return Character.MoveState.LEFT;
        }
        return Character.MoveState.RIGHT;
    }

    public abstract Character.MoveState computeMoves(float playerX, float posX, boolean touchedBoatRecently);
}
