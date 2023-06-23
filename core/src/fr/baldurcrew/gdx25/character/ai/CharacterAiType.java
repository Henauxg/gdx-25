package fr.baldurcrew.gdx25.character.ai;

import com.badlogic.gdx.math.MathUtils;

public enum CharacterAiType {
    Stalker,
    Darwin,
    MadRunner,
    BellyDancer;

    public static CharacterAiType getRandomType() {
        CharacterAiType[] types = values();
        return types[MathUtils.random(0, types.length - 1)];
    }
}

