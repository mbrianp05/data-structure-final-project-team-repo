package weapons;

import entities.Enemy;
import entities.Player;
import game.GameController;

import java.util.List;

public abstract class WeaponDef {
    public final String id;
    public final String name;
    public final String description;
    public final float baseCooldown;

    public WeaponDef(String id, String name, String description, float baseCooldown) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseCooldown = baseCooldown;
    }

    public abstract void fire(Player player, int level, List<Enemy> enemies, GameController controller);
}