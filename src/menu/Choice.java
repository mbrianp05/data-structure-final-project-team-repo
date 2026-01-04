package menu;

import java.io.Serializable;

public class Choice implements Serializable {
    public enum Kind { WEAPON, PASSIVE }
    public final Kind kind;
    public final String id;
    public final String name;
    public final String description;

    public Choice(Kind kind, String id, String name, String description) {
        this.kind = kind;
        this.id = id;
        this.name = name;
        this.description = description;
    }
}