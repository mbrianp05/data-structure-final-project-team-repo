package map;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * Datos de una sala (un nodo del árbol). Contiene puertas, drops y dimensiones
 * locales.
 */
public class MineRoom {
    public int id;
    public int width;
    public int height;
    public boolean colapsado = false;
    public List<Door> doors = new ArrayList<>();

    // drops (crystals) en la sala
    public List<Crystal> drops = new ArrayList<>();

    // keys físicas que se spawnean en la sala (objetos map.Key con posición)
    public List<Key> keys = new ArrayList<>();

    // (opcional) ids lógicas de llaves requeridas o posesión
    public List<String> keysInRoom = new ArrayList<>();
    public List<String> requiredKeys = new ArrayList<>();

    // cuántas veces se limpió esta sala (usado para escalar spawn y dropeos)
    public int clearCount = 0;

    // flag temporal para evitar incrementar clearCount múltiples veces en el mismo
    // frame
    public boolean wasCleared = false;

    // Nuevo: tipo de piso asignado a la sala (1–5)
    public int floorType;

    public MineRoom(int id, int width, int height) {
        this.id = id;
        this.width = 950;
        this.height = 650;

        // Asignar un tipo de piso aleatorio al crear la sala
        this.floorType = 1 + new Random().nextInt(5);
    }

    public Rectangle getBounds() {
        return new Rectangle(0, 0, width, height);
    }

    // ---- Helpers para keys (compatibles con versiones previas) ----

    /** Añade un objeto map.Key (con posición) a la sala */
    public void addKeyObject(Key k) {
        if (k == null)
            return;
        keys.add(k);
    }

    /** Añade sólo el id lógico de la llave (sin posición) */
    public void addKeyToRoom(String keyId) {
        if (keyId == null)
            return;
        if (!hasKeyInRoom(keyId))
            keysInRoom.add(keyId);
    }

    /** Comprueba si la sala contiene el id de llave (lógica) */
    public boolean hasKeyInRoom(String keyId) {
        if (keyId == null)
            return false;
        for (String k : keysInRoom)
            if (k.equals(keyId))
                return true;
        return false;
    }

    /**
     * Elimina la llave física (objeto map.Key) de la sala por id; devuelve true si
     * se eliminó
     */
    public boolean removeKeyObjectById(String keyId) {
        if (keyId == null)
            return false;
        for (int i = 0; i < keys.size(); i++) {
            Key k = keys.get(i);
            if (k != null && keyId.equals(k.id)) {
                keys.remove(i);
                return true;
            }
        }
        return false;
    }
}
