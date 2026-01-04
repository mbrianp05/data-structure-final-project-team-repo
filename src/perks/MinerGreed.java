package perks;

import entities.Player;


public class MinerGreed extends PassiveDef {
    public MinerGreed() {
        super("codicia_minera", "Codicia", "+10% XP de monedas por nivel");
    }
    @Override
    public void applyTo(Player player, int stacks) {
        player.crystalXpMultiplier *= Math.pow(1.10, stacks);
    }
}