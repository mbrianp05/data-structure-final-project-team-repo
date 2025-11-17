package map;

import java.util.*;

import Tree.BinaryTreeNode;

import java.awt.Rectangle;

/**
 * Puerta dentro de una sala que lleva a otra sala (otro nodo).
 * area: rectángulo en coords locales de la sala origen que activa la transición.
 * destino: BinaryTreeNode<map.MineRoom> al que va la puerta.
 * spawnX, spawnY: coordenadas en la sala destino donde aparecerá el jugador.
 */

/**
 * Puerta dentro de una sala que lleva a otra sala (otro nodo) o representa la
 * salida (isWin).
 */
public class Door {
    public final Rectangle area;
    public final BinaryTreeNode<MineRoom> destino;
    public final int spawnX;
    public final int spawnY;
    public boolean locked = false;
    public boolean isWin = false; // true si abrirla gana la partida
    public String label = "";

    // nuevo campo para diferenciar puertas normales
    public int tipo = 0; // 0 = genérica, 1..5 = sprites p1..p5

    public Door(Rectangle area, BinaryTreeNode<MineRoom> destino, int spawnX, int spawnY) {
        this.area = area;
        this.destino = destino;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.isWin = false;
        this.label = destino != null && destino.getInfo() != null ? String.valueOf(destino.getInfo().id) : "";
        this.tipo = new Random().nextInt(5) + 1; // asigna sprite aleatorio p1..p5
    }

    // constructor para puerta de victoria (sin destino)
    public Door(Rectangle area, boolean isWin, String label) {
        this.area = area;
        this.destino = null;
        this.spawnX = -1;
        this.spawnY = -1;
        this.locked = false;
        this.isWin = isWin;
        this.label = label != null ? label : "EXIT";
        this.tipo = 0; // las finales no usan sprite
    }

    // método auxiliar
    public boolean isFinalDoor() {
        return isWin;
    }
}
