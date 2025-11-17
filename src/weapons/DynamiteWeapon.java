package weapons;

import effects.AoEExplosion;
import entities.Enemy;
import entities.Player;
import game.GameController;

import java.awt.*;
import java.util.List;

public class DynamiteWeapon extends WeaponDef {
    public DynamiteWeapon() {
        super("dinamita", "Dinamita", "Lanza cargas que explotan con AoE; proyecto lento.", 1.6f);
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        // objetivo: posición del enemigo más cercano si existe, sino adelantado
        Enemy target = null;
        float best2 = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive())
                continue;
            float dx = e.getX() - player.x, dy = e.getY() - player.y;
            float d2 = dx * dx + dy * dy;
            if (d2 < best2) {
                best2 = d2;
                target = e;
            }
        }

        final float speed = 120f; // px/s
        final int damage = (int) (60 + level * 18 * player.damageMultiplier);
        final float life = 4f;

        // spawn a simple slow projectile that moves toward target position and explodes
        // on impact or end of life
        Enemy finalTarget = target;
        Projectile p = new Projectile(player, controller.nodoActual, player.x, player.y, 0f, 0f, life, 0, controller) {
            float vxLocal = 0f, vyLocal = 0f;
            boolean initialized = false;

            @Override
            public void update(float dt, GameController ctx) {
                if (!initialized) {
                    float tx = player.x, ty = player.y;
                    if (finalTarget != null && finalTarget.isAlive()) {
                        tx = finalTarget.getX();
                        ty = finalTarget.getY();
                    }
                    float dx = tx - x, dy = ty - y;
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
                // move
                x += vxLocal * dt;
                y += vyLocal * dt;

                // check collision with enemies
                if (roomNode != null) {
                    List<Enemy> list = controller.enemyManager.getEnemiesAt(roomNode);
                    for (Enemy en : list) {
                        if (!en.isAlive())
                            continue;
                        float dx = en.getX() - x, dy = en.getY() - y;
                        if (dx * dx + dy * dy <= 14f * 14f) {
                            // explode
                            ctx.spawnEffect(
                                    new AoEExplosion(player, roomNode, x, y, 48f + level * 6f, damage, 0.6f, ctx));
                            expire();
                            return;
                        }
                    }
                }

                // life countdown
                life -= dt;
                if (life <= 0f) {
                    controller.spawnEffect(
                            new AoEExplosion(player, roomNode, x, y, 48f + level * 6f, damage, 0.8f, controller));
                    expire();
                }
            }

            @Override
            public void render(Graphics2D g) {
                int s = 10;
                g.setColor(new Color(200, 80, 40));
                g.fillOval((int) (x - s / 2), (int) (y - s / 2), s, s);
                g.setColor(new Color(255, 160, 80, 120));
                g.drawOval((int) (x - s), (int) (y - s), s * 2, s * 2);
            }
        };

        controller.spawnProjectile(p);
    }
}
