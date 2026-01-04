package perks;

import entities.Player;

public class DwarvenEndurance extends PassiveDef {
    private final int hpPerStack;

    public DwarvenEndurance() {
        super("salud_dwarven", "Firmeza", "+20 de vida máxima por nivel");
        hpPerStack = 20;
    }

    @Override
    public void applyTo(Player player, int stacks) {
        int added = hpPerStack * stacks;
        player.maxHp += added;
        player.hp += added;
    }
}