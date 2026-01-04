package perks;

import entities.Player;


public abstract class PassiveDef {
    public final String id;
    public final String name;
    public final String description;

    public PassiveDef(String id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    public abstract void applyTo(Player player, int stacks);
}