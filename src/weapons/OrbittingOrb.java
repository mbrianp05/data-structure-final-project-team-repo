package weapons;

import Tree.BinaryTreeNode;
import effects.Effect;
import entities.Enemy;
import entities.Player;
import game.GameController;
import map.MineRoom;
import utils.ResourceManager; // importa ResourceManager para acceder a p3

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

// Orbes que orbitan alrededor del Player
public class OrbittingOrb extends Projectile implements Effect {
    public final Player ownerPlayer;
    private final float orbitRadius;
    private final float angularSpeed;
    private float angle;
    private final float damageInterval;
    private float tickTimer = 0f;

    public OrbittingOrb(Player owner, BinaryTreeNode<MineRoom> roomNode, float initialAngle,
                        float orbitRadius, float angularSpeed, float lifeSeconds,
                        int damage, GameController controller) {
        super(owner, roomNode, owner.x, owner.y, 0f, 0f, lifeSeconds, damage, controller);
        this.ownerPlayer = owner;
        this.angle = initialAngle;
        this.orbitRadius = orbitRadius;
        this.angularSpeed = angularSpeed;
        this.damageInterval = 0.12f;
    }

    @Override
    public void update(float dt, GameController ctx) {
        if (expired) return;

        angle += angularSpeed * dt;
        x = ownerPlayer.x + (float) Math.cos(angle) * orbitRadius;
        y = ownerPlayer.y + (float) Math.sin(angle) * orbitRadius;

        tickTimer -= dt;
        if (tickTimer <= 0f) {
            if (roomNode != null) {
                List<Enemy> enemies = controller.enemyManager.getEnemiesAt(roomNode);
                float r2 = 12f * 12f;
                for (Enemy e : enemies) {
                    if (!e.isAlive()) continue;
                    float dx = e.getX() - x;
                    float dy = e.getY() - y;
                    float distanceSquared = dx * dx + dy * dy;
                    if (distanceSquared <= r2) {
                        e.damage(damage, controller);
                        float dist = (float) Math.sqrt(distanceSquared);
                        float nx = 0f, ny = -1f;
                        if (dist > 0.001f) {
                            nx = dx / dist;
                            ny = dy / dist;
                        }
                        e.applyKnockback(nx * 30f, ny * 30f);
                    }
                }
            }
            tickTimer = damageInterval;
        }

        life -= dt;
        if (life <= 0f) expire();
    }

    @Override
    public void render(Graphics2D g) {
        BufferedImage sprite = ResourceManager.pw3; // sprite del orbe de carbón
        if (sprite != null) {
            int drawW = sprite.getWidth();
            int drawH = sprite.getHeight();
            int dx = (int) (x - drawW / 2f);
            int dy = (int) (y - drawH / 2f);
            g.drawImage(sprite, dx, dy, drawW, drawH, null);
        } else {
            // fallback si no hay sprite cargado
            int s = 10;
            g.setColor(new Color(180, 200, 255));
            g.fillOval((int) (x - s / 2), (int) (y - s / 2), s, s);
            g.setColor(new Color(160, 180, 255, 120));
            g.drawOval((int) (x - s), (int) (y - s), s * 2, s * 2);
        }
    }

    @Override
    public boolean isExpired() {
        return expired;
    }
}
