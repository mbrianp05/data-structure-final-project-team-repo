package weapons;

import effects.SlashEffect;
import entities.Enemy;
import entities.Player;
import game.GameController;

import java.util.List;

public class PickaxeWeapon extends WeaponDef {
    public PickaxeWeapon() {
        super("pico", "Espada", "Ataque cuerpo a cuerpo frontal del jugador.", 0.16f);
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        float dirX = player.facingX;
        float dirY = player.facingY;

        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len < 1e-4f) {
            dirX = player.vx;
            dirY = player.vy;
            len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        }
        if (len < 1e-4f) {
            dirX = 0f;
            dirY = -1f;
            len = 1f;
        }
        dirX /= len;
        dirY /= len;

        float range = 48f + (level - 1) * 6f;
        float angle = (float) Math.toRadians(70);
        int baseDamage = 8 + (level - 1) * 3;
        int dmg = Math.round(baseDamage * player.damageMultiplier);

        SlashEffect slash = new SlashEffect(player, controller.nodoActual, player.x, player.y, dirX, dirY, range, angle, dmg, 0.12f, controller);
        controller.spawnEffect(slash);
    }
}