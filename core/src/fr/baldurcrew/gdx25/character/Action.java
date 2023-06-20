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

    IDLE(3),
    CLIMB(0,1),
    JUMP(6,5,2),
    SWIM(7,8),
    WALK(9,10);

    private int[] frames;

    Action(int... frames) {
        this.frames = frames;
    }

    public int getFrame(int index){
        return frames[index];
    }

    public int getFramesCount() {
        return frames.length;
    }

    public int[] getFrames() {
        return frames;
    }
}
