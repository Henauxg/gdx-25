package fr.baldurcrew.gdx25.character;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

    /*

    Animation = Nb frames

    Walk      = 2     frames 9, 10
    Climb     = 2     frames 0, 1
    Swim      = 2     frames 7, 8
    Jump      = 3     frames 6, 5, 2

     */


public enum Action {

    IDLE(4),//1

    CLIMB(3, 2),

    JUMP(1, 7, 5, 0),

    SWIM(10, 8),

    WALK(9, 6);

    private int[] frames;

    Action(int... frames) {
        this.frames = frames;
    }

    public int getFrame(int index) {
        return frames[index];
    }

    public int getFramesCount() {
        return frames.length;
    }

    public int[] getFrames() {
        return frames;
    }
}
