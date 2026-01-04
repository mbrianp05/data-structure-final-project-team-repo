package map;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

//Datos de una sala (un nodo del árbol). Contiene puertas, drops y enemigos
public class MineRoom {
    public int id;
    public int width;
    public int height;

    public boolean colapsado = false;
    public List<Door> doors = new ArrayList<>();

    // drops (crystales) en la sala
    public List<Crystal> drops = new ArrayList<>();

    // keys físicas que se spawnean en la sala
    public List<Key> keys = new ArrayList<>();

    //ids lógicas de llaves requeridas o posesión
    public List<String> keysInRoom = new ArrayList<>();
    public List<String> requiredKeys = new ArrayList<>();

    // cuántas veces se limpió esta sala
    public int clearCount = 0;

    public boolean wasCleared = false;

    // flag para indicar si un boss ha sido spawneado en esta sala
    public boolean bossSpawned = false;

    public int floorType;

    public MineRoom(int id, int width, int height) {
        this.id = id;
        this.width = 890;
        this.height = 570;

        this.floorType = 1 + new Random().nextInt(5);
    }

    public Rectangle getBounds() {
        return new Rectangle(0, 0, width, height);
    }

    //Helpers para keys

    public void addKeyObject(Key k) {
        if (k != null) {
            keys.add(k);
        }
    }

    public void addKeyToRoom(String keyId) {
        if (keyId != null) {
            if (!hasKeyInRoom(keyId)) {
                keysInRoom.add(keyId);
            }
        }
    }

    // Comprueba si la sala contiene el id de llave
    public boolean hasKeyInRoom(String keyId) {
        boolean result = false;
        if (keyId != null) {
            for (String k : keysInRoom) {
                if (k.equals(keyId)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean removeKeyObjectById(String keyId) {
        boolean result = false;
        if (keyId != null) {
            for (int i = 0; i < keys.size(); i++) {
                Key k = keys.get(i);
                if (k != null && keyId.equals(k.id)) {
                    keys.remove(i);
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}