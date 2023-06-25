package fr.baldurcrew.gdx25.monster;

public interface Eatable {

    float getX();

    void freezeY(float y);

    void prepareToBeEaten(float yToBeEaten);

    float getMealSizeFactor();

}
