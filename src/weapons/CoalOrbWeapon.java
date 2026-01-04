package weapons;

import entities.Enemy;
import entities.Player;
import game.GameController;

import java.util.List;

public class CoalOrbWeapon extends WeaponDef {
    public CoalOrbWeapon() {
        super("orbe_carbon", "Orbe Mágico", "Orbes que orbitan alrededor del jugador y dañan.", 0.9f);
    }

    @Override
    public void fire(Player player, int level, List<Enemy> enemies, GameController controller) {
        int base = 1;
        int extra = (level - 1) / 2;
        int totalDesired = base + extra;

        int existing = controller.countOrbitingOrbsForPlayer(player);

        int toSpawn = Math.max(0, totalDesired - existing);
        if (toSpawn <= 0) {
            return;
        }

        float orbitRadius = 24f + Math.min(40f, level * 3f);
        float angularSpeed = 3.0f + level * 0.25f;
        int dmg = 10 + level * 4;
        float life = 6f + level * 1.2f;

        int startIndex = existing;
        int finalTotal = existing + toSpawn;
        for (int i = 0; i < toSpawn; i++) {
            float divisor = Math.max(1, finalTotal);
            float angleFactor = (float) (Math.PI * 2) / divisor;
            float ang = (float) ((startIndex + i) * angleFactor);
            OrbittingOrb orb = new OrbittingOrb(player, controller.nodoActual, ang, orbitRadius, angularSpeed, life, dmg, controller);
            controller.spawnEffect(orb);
        }
    }
}