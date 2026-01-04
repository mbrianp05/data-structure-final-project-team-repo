package effects;

import Tree.BinaryTreeNode;
import entities.Player;
import game.GameController;
import map.MineRoom;
import weapons.Projectile;

import java.awt.*;

// Efectos para explosiones
public class AoEExplosion extends Projectile implements Effect {
    private final float maxRadius;
    private float age = 0f;

    public AoEExplosion(Player owner, BinaryTreeNode<MineRoom> roomNode, float x, float y, float maxRadius, int damage, float lifeSeconds, GameController controller) {
        super(owner, roomNode, x, y, 0f, 0f, lifeSeconds, damage, controller);
        this.maxRadius = maxRadius;
        damageEnemiesInRadius(x, y, maxRadius * 0.6f, damage); // Daña a los enemigos dentro de un área
    }

    @Override
    public void update(float dt, GameController ctx) {
        age += dt;
        boolean debeExpirar;

        if (age >= life || expired) {
            debeExpirar = true;
        }
        else {
            debeExpirar = false;
        }

        if (debeExpirar) {
            expire();
        }
        else {
            float progress = age / life;
            float currentRadius = maxRadius * progress;

            if (age < 0.1f) {
                damageEnemiesInRadius(x, y, currentRadius, damage / 2);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        float progress;

        if (age / life < 1f) {
            progress = age / life;
        }
        else {
            progress = 1f;
        }

        float r = maxRadius * (0.3f + 0.7f * progress);

        int ir;

        if ((int) r > 2) {
            ir = (int) r;
        }
        else {
            ir = 2;
        }

        int alpha1 = (int) (180 * (1f - progress));
        Color col = new Color(255, 140, 40, alpha1);
        g.setColor(col);
        g.fillOval((int) (x - r), (int) (y - r), ir * 2, ir * 2);

        int alpha2 = (int) (220 * (1f - progress));
        g.setColor(new Color(255, 200, 120, alpha2));

        int innerW;

        if ((int) (r * 0.8f) > 4) {
            innerW = (int) (r * 0.8f);
        }
        else {
            innerW = 4;
        }

        int innerH;

        if ((int) (r * 0.8f) > 4) {
            innerH = (int) (r * 0.8f);
        }
        else {
            innerH = 4;
        }

        g.fillOval((int) (x - r * 0.4f), (int) (y - r * 0.4f), innerW, innerH);
    }

    @Override
    public boolean isExpired() {
        boolean resultado;

        if (expired) {
            resultado = true;
        }
        else {
            resultado = false;
        }

        return resultado;
    }
}
