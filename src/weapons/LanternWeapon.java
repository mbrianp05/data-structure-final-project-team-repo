package weapons;

import entities.Enemy;
import entities.Player;
import game.GameController;

import java.util.List;

// Farol Mágico (proyectil que persigue a enemigo cercano)
public class LanternWeapon extends WeaponDef {
    public LanternWeapon() {
        super("farol_magico", "Farol Mágico", "Dispara un farol que persigue al enemigo más cercano.", 1.2f);
        // baseCooldown aumentado de 0.7f a 1.2f para disparar más lento por defecto
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        float best2 = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive())
                continue;
            float dx = e.getX() - player.x, dy = e.getY() - player.y;
            float d2 = dx * dx + dy * dy;
            if (d2 < best2) {
                best2 = d2;
            }
        }

        int dmg = 14 + level * 6;   // damage reducido para los niveles iniciales
        float speed = 160f + level * 6f; // proyectil más lento que antes (antes 220)
        float life = 4f;

        HomingProjectile p = new HomingProjectile(player, controller.nodoActual, player.x, player.y, speed, life, dmg,
                controller);

        controller.spawnProjectile(p);

    }
}
