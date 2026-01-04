package weapons;

import entities.Enemy;
import entities.Player;
import game.GameController;

import java.util.List;

// Farol Mágico (proyectil que persigue a enemigo cercano)
public class LanternWeapon extends WeaponDef {
    public LanternWeapon() {
        super("farol_magico", "Varita Mágica", "Dispara un orbe que persigue al enemigo más cercano.", 1.2f);
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        Enemy target = null;
        float best2 = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                continue;
            }
            float dx = e.getX() - player.x;
            float dy = e.getY() - player.y;
            float d2 = dx * dx + dy * dy;
            if (d2 < best2) {
                best2 = d2;
                target = e;
            }
        }

        int dmg = 18 + level * 6;
        float speed = 160f + level * 6f;
        float life = 4f;

        HomingProjectile p = new HomingProjectile(player, controller.nodoActual, player.x, player.y, speed, life, dmg,
                controller);

        controller.spawnProjectile(p);
    }
}