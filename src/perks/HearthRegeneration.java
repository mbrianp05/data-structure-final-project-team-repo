package perks;

import entities.Player;


public class HearthRegeneration extends PassiveDef {
    public HearthRegeneration() {
        super("hearth_regen", "Regeneración", "+1 HP/s por nivel");
    }
    @Override
    public void applyTo(Player player, int stacks) {
        player.hpRegenPerSec += 1f * stacks;
    }
}