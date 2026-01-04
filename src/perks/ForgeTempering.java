package perks;

import entities.Player;


public class ForgeTempering extends PassiveDef {
    public ForgeTempering() {
        super("temple_forja", "Ira", "+10% daño cuerpo a cuerpo por nivel");
    }
    @Override
    public void applyTo(Player player, int stacks) {
        player.damageMultiplier *= Math.pow(1.10, stacks);
    }
}