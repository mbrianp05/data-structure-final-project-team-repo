package weapons;

import Tree.BinaryTreeNode;
import entities.Enemy;
import entities.Player;
import game.GameController;
import map.MineRoom;
import utils.ResourceManager; // importa ResourceManager para acceder a pw4

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class HomingProjectile extends Projectile {
    private Enemy target;
    private final float speed;
    private final float turnSpeed;

    public HomingProjectile(Player owner, BinaryTreeNode<MineRoom> roomNode,
                            float x, float y, float speed,
                            float lifeSeconds, int damage,
                            GameController controller) {
        super(owner, roomNode, x, y, 0f, 0f, lifeSeconds, damage, controller);
        this.speed = speed;
        this.turnSpeed = 8f;
    }

    @Override
    public void update(float dt, GameController ctx) {
        if (expired) return;

        if (target == null || !target.isAlive()) {
            target = findNearestEnemy();
        }

        if (target != null) {
            float dx = target.getX() - x;
            float dy = target.getY() - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < 2f) {
                target.damage(damage, ctx);
                float nx = 0f, ny = -1f;
                if (dist > 0.001f) {
                    nx = dx / dist;
                    ny = dy / dist;
                }
                target.applyKnockback(nx * 40f, ny * 40f);
                expire();
                return;
            }
            float dxn = dx / (dist + 1e-6f);
            float dyn = dy / (dist + 1e-6f);
            float vxAdjust = (dxn * speed - vx) * Math.min(1f, turnSpeed * dt);
            float vyAdjust = (dyn * speed - vy) * Math.min(1f, turnSpeed * dt);
            vx += vxAdjust;
            vy += vyAdjust;
            float vmag = (float) Math.sqrt(vx * vx + vy * vy);
            if (vmag > 0.001f) {
                vx = vx / vmag * speed;
                vy = vy / vmag * speed;
            }
        } else {
            if (vx == 0f && vy == 0f) {
                expire();
                return;
            }
        }
        super.update(dt, ctx);
    }

    @Override
    public void render(Graphics2D g) {
        BufferedImage sprite = ResourceManager.pw1; // sprite del farol mágico
        if (sprite != null) {
            int drawW = sprite.getWidth();
            int drawH = sprite.getHeight();
            int dx = (int) (x - drawW / 2f);
            int dy = (int) (y - drawH / 2f);
            g.drawImage(sprite, dx, dy, drawW, drawH, null);
        } else {
            // fallback si no hay sprite cargado
            int s = 8;
            g.setColor(new Color(255, 220, 80));
            g.fillOval((int) (x - s / 2), (int) (y - s / 2), s, s);
            g.setColor(new Color(255, 200, 40, 100));
            g.fillOval((int) (x - s), (int) (y - s), s * 2, s * 2);
        }
    }

    private Enemy findNearestEnemy() {
        Enemy result = null;
        if (roomNode != null) {
            List<Enemy> list = controller.enemyManager.getEnemiesAt(roomNode);
            float best2 = Float.MAX_VALUE;
            for (Enemy e : list) {
                if (!e.isAlive()) continue;
                float dx = e.getX() - x;
                float dy = e.getY() - y;
                float d2 = dx * dx + dy * dy;
                if (d2 < best2) {
                    best2 = d2;
                    result = e;
                }
            }
        }
        return result;
    }
}
