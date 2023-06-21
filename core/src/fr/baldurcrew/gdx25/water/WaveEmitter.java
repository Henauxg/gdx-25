package fr.baldurcrew.gdx25.water;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import fr.baldurcrew.gdx25.Constants;

public class WaveEmitter {

    private WaterSimulation water;
    private Vector2 wavePeriodRange;
    private Vector2 waveAmplitudeRange;
    private float emitTimer;
    private float nextWaveWaitTime;
    private float nextWaveAmplitude;

    public WaveEmitter(WaterSimulation water, Vector2 periodRange, Vector2 amplitudeRange) {
        this.water = water;
        this.wavePeriodRange = periodRange;
        this.waveAmplitudeRange = amplitudeRange;

        emitTimer = 0f;
        this.nextWaveWaitTime = MathUtils.random(wavePeriodRange.x, wavePeriodRange.y);
        this.nextWaveAmplitude = MathUtils.random(waveAmplitudeRange.x, waveAmplitudeRange.y);
    }


    public void update() {
        emitTimer += Constants.TIME_STEP;
        if (emitTimer >= nextWaveWaitTime) {

            //TODO Index
            water.disturbWater(0, nextWaveAmplitude);

            emitTimer = 0f;
            this.nextWaveWaitTime = MathUtils.random(wavePeriodRange.x, wavePeriodRange.y);
            this.nextWaveAmplitude = MathUtils.random(waveAmplitudeRange.x, waveAmplitudeRange.y);
        }
    }

}
