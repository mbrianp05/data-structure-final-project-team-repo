package weapons;

import effects.AoEExplosion;
import entities.Enemy;
import entities.Player;
import game.GameController;
import utils.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class DynamiteWeapon extends WeaponDef {
    public DynamiteWeapon() {
        super("dinamita", "Bomba", "Lanza bombas que explotan.", 1.6f);
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        Enemy target = null;
        float best2 = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            float dx = e.getX() - player.x;
            float dy = e.getY() - player.y;
            float d2 = dx * dx + dy * dy;
            if (d2 < best2) {
                best2 = d2;
                target = e;
            }
        }

        final float speed = 120f;
        final int damage = (int) (60 + level * 18 * player.damageMultiplier);
        final float life = 4f;

        Enemy finalTarget = target;
        Projectile p = new Projectile(player, controller.nodoActual, player.x, player.y, 0f, 0f, life, damage, controller) {
            float vxLocal = 0f;
            float vyLocal = 0f;
            boolean initialized = false;

            @Override
            public void update(float dt, GameController ctx) {
                if (!initialized) {
                    float tx = player.x;
                    float ty = player.y;
                    if (finalTarget != null && finalTarget.isAlive()) {
                        tx = finalTarget.getX();
                        ty = finalTarget.getY();
                    }
                    float dx = tx - x;
                    float dy = ty - y;
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d > 0.001f) {
                        vxLocal = dx / d * speed;
                        vyLocal = dy / d * speed;
                    } else {
                        vxLocal = 0;
                        vyLocal = 0;
                    }
                    initialized = true;
                }
                x += vxLocal * dt;
                y += vyLocal * dt;

                if (roomNode != null) {
                    List<Enemy> list = controller.enemyManager.getEnemiesAt(roomNode);
                    for (Enemy en : list) {
                        if (!en.isAlive()) continue;
                        float dx = en.getX() - x;
                        float dy = en.getY() - y;
                        float distanceSquared = dx * dx + dy * dy;
                        float collisionRadius = 14f * 14f;
                        if (distanceSquared <= collisionRadius) {
                            ctx.spawnEffect(
                                    new AoEExplosion(player, roomNode, x, y, 48f + level * 6f, damage, 0.6f, ctx));
                            expire();
                            return;
                        }
                    }
                }

                life -= dt;
                if (life <= 0f) {
                    controller.spawnEffect(
                            new AoEExplosion(player, roomNode, x, y, 48f + level * 6f, damage, 0.8f, controller));
                    expire();
                }
            }

            @Override
            public void render(Graphics2D g) {
                BufferedImage sprite = ResourceManager.pw2; // sprite de dinamita
                if (sprite != null) {
                    int drawW = sprite.getWidth();
                    int drawH = sprite.getHeight();
                    int dx = (int) (x - drawW / 2f);
                    int dy = (int) (y - drawH / 2f);
                    g.drawImage(sprite, dx, dy, drawW, drawH, null);
                } else {
                    // fallback si no hay sprite cargado
                    g.setColor(new Color(200, 80, 40));
                    g.fillOval((int) (x - 5), (int) (y - 5), 10, 10);
                }
            }
        };

        controller.spawnProjectile(p);
    }
}
