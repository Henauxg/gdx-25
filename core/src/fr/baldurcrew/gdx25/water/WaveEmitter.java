package fr.baldurcrew.gdx25.water;

import fr.baldurcrew.gdx25.Constants;
import fr.baldurcrew.gdx25.CoreGame;
import fr.baldurcrew.gdx25.utils.Range;

public class WaveEmitter {

    private WaterSimulation water;
    private Range wavePeriodRange;
    private Range waveAmplitudeRange;
    private float emitTimer;
    private float nextWaveWaitTime;

    public WaveEmitter(WaterSimulation water, Range periodRange, Range amplitudeRange) {
        this.water = water;
        this.wavePeriodRange = periodRange;
        this.waveAmplitudeRange = amplitudeRange;

        emitTimer = 0f;
        this.nextWaveWaitTime = wavePeriodRange.getRandom();
    }

    public void update() {
        if (!CoreGame.debugEnableWaveGeneration) return;

        emitTimer += Constants.TIME_STEP;
        if (emitTimer >= nextWaveWaitTime) {
            emitTimer = 0f;

            //TODO Index
            water.disturbWater(0, waveAmplitudeRange.getRandom());
            this.nextWaveWaitTime = wavePeriodRange.getRandom();
        }
    }

    public void setAmplitudeRange(Range amplitudeRange) {
        this.waveAmplitudeRange = amplitudeRange;
    }

    public void setPeriodRange(Range range) {
        this.wavePeriodRange = range;
    }

    public float getAmplitudeRangeMin() {
        return this.waveAmplitudeRange.from;
    }

    public float getAmplitudeRangeMax() {
        return this.waveAmplitudeRange.to;
    }
}
