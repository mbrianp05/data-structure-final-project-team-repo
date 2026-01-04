package map;

import java.util.*;

import Tree.BinaryTreeNode;

import java.awt.Rectangle;

//Puerta dentro de una sala que lleva a otra sala (otro nodo).

public class Door {
    public final Rectangle area;
    public final BinaryTreeNode<MineRoom> destino;
    public final int spawnX;
    public final int spawnY;
    public boolean locked = false;
    public boolean isWin = false; // true si abrirla gana la partida
    public String label = "";

    // nuevo campo para diferenciar puertas normales
    public int tipo = 0;

    public Door(Rectangle area, BinaryTreeNode<MineRoom> destino, int spawnX, int spawnY) {
        this.area = area;
        this.destino = destino;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.isWin = false;
        if (destino != null && destino.getInfo() != null) {
            this.label = String.valueOf(destino.getInfo().id);
        }
        else {
            this.label = "";
        }
        this.tipo = new Random().nextInt(5) + 1; // asigna sprite aleatorio p1..p5
    }

    // constructor para puerta de victoria
    public Door(Rectangle area, boolean isWin, String label) {
        this.area = area;
        this.destino = null;
        this.spawnX = -1;
        this.spawnY = -1;
        this.locked = false;
        this.isWin = isWin;
        if (label != null) {
            this.label = label;
        }
        else {
            this.label = "EXIT";
        }
        this.tipo = 0;
    }

    // método auxiliar
    public boolean isFinalDoor() {
        return isWin;
    }
}