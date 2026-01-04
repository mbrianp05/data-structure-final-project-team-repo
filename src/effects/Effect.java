package effects;

import game.GameController;

import java.awt.*;


// interfaz para efectos visuales/temporales que viven en una sala.

public interface Effect {
    void update(float dt, GameController controller);
    void render(Graphics2D g);
    boolean isExpired();
}
