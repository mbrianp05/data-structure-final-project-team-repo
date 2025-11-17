package map;

/**
 * Recompensa dejada por enemigos al morir.
 */
public class Crystal {
    public float x, y;
    public int value;
    public boolean collected = false;

    public Crystal(float x, float y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}
