package fr.baldurcrew.gdx25.character;

import fr.baldurcrew.gdx25.Constants;
import fr.baldurcrew.gdx25.CoreGame;
import fr.baldurcrew.gdx25.utils.Range;

public class CharacterSpawner {

    private CoreGame game;

    private Range spawnRangeX;
    private Range spawnRangeY;
    private Range spawnPeriodRange;
    private float spawnTimer;
    private float nextSpawnWaitTime;

    public CharacterSpawner(CoreGame game, Range spawnRangeX, Range spawnRangeY, Range spawnPeriodRange) {
        this.game = game;
        this.spawnRangeX = spawnRangeX;
        this.spawnRangeY = spawnRangeY;
        this.spawnPeriodRange = spawnPeriodRange;

        spawnTimer = 0f;
        this.nextSpawnWaitTime = spawnPeriodRange.getRandom();
    }

    public void update() {
        if (!CoreGame.debugEnableCharacterGeneration) return;

        spawnTimer += Constants.TIME_STEP;
        if (spawnTimer >= nextSpawnWaitTime) {
            spawnTimer = 0f;

            final int charIndex = CharacterResources.getRandomCharacterIndex();
            game.spawnCharacter(charIndex, true, spawnRangeX.getRandom(), spawnRangeY.getRandom());
            this.nextSpawnWaitTime = spawnPeriodRange.getRandom();
        }
    }
}
