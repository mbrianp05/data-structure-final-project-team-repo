package effects;

import Tree.BinaryTreeNode;
import entities.Enemy;
import entities.Player;
import game.GameController;
import map.MineRoom;
import weapons.Projectile;
import utils.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.util.List;

public class SlashEffect extends Projectile implements Effect {
    private final float originX, originY;
    private final float dirX, dirY;
    private final float range;
    private final float angle;
    private final float lifeTotal;
    private float age = 0f;
    private boolean damaged = false;

    public SlashEffect(Player owner, BinaryTreeNode<MineRoom> roomNode,
                       float originX, float originY,
                       float dirX, float dirY,
                       float range, float angle,
                       int damage, float lifeSeconds,
                       GameController controller) {
        super(owner, roomNode, originX, originY, 0f, 0f, lifeSeconds, damage, controller);
        this.originX = originX;
        this.originY = originY;
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (len < 1e-6f) {
            this.dirX = 1f;
            this.dirY = 0f;
        } else {
            this.dirX = dirX / len;
            this.dirY = dirY / len;
        }

        this.range = range;
        this.angle = angle;
        this.lifeTotal = lifeSeconds;
    }

    @Override
    public void update(float dt, GameController ctx) {
        age += dt;

        if (!damaged) {
            applySlashDamage();
            damaged = true;
        }

        if (age >= lifeTotal) {
            expire();
        }
    }

    private void applySlashDamage() {
        if (roomNode == null) return;

        List<Enemy> enemies = controller.enemyManager.getEnemiesAt(roomNode);
        float coneCos = (float) Math.cos(angle * 0.5f);

        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;

            float dx = e.getX() - originX;
            float dy = e.getY() - originY;
            float d2 = dx * dx + dy * dy;

            if (d2 > range * range) continue;

            float d = (float) Math.sqrt(d2);
            float nx = dx / (d + 1e-6f);
            float ny = dy / (d + 1e-6f);
            float dot = nx * dirX + ny * dirY;

            if (dot >= coneCos) {
                e.damage(damage, controller);
                e.applyKnockback(nx * 60f, ny * 60f);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        BufferedImage sprite = ResourceManager.pw4; // sprite del slash
        if (sprite != null) {
            // calcular ángulo de rotación a partir de la dirección del jugador
            double angleRad = Math.atan2(dirY, dirX);

            // calcular posición más alejada del jugador (más lejos que antes)
            float offset = range * 1.2f; // empuja más lejos
            float cx = originX + dirX * offset;
            float cy = originY + dirY * offset;

            int drawW = sprite.getWidth();
            int drawH = sprite.getHeight();

            // crear transformación: trasladar, rotar y escalar al doble
            AffineTransform transform = new AffineTransform();
            transform.translate(cx - drawW, cy - drawH); // ajustar para centro
            transform.rotate(angleRad, drawW, drawH);
            transform.scale(2.0, 2.0); // doble tamaño

            g.drawImage(sprite, transform, null);
        } else {
            // fallback visual si no hay sprite cargado
            float progress = Math.min(1f, age / lifeTotal);
            float alpha = 1f - progress;
            int r = (int) range;

            g.setColor(new Color(220, 220, 80, (int) (180 * alpha)));
            g.fillOval((int) (originX - r), (int) (originY - r), r * 2, r * 2);

            g.setColor(new Color(255, 255, 200, (int) (200 * alpha)));
            int lx = (int) (originX + dirX * (r * 0.6f));
            int ly = (int) (originY + dirY * (r * 0.6f));
            g.fillOval(lx - 12, ly - 12, 24, 24); // también más grande
        }
    }

    @Override
    public boolean isExpired() {
        return expired;
    }
}
