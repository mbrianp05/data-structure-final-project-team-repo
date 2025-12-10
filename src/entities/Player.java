package entities;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.PreorderIterator;
import game.GameController;
import menu.Choice;
import menu.PerkPool;
import perks.PassiveDef;
import weapons.WeaponDef;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * entities.Player actualizado con sistema de XP / niveles, armas automáticas y
 * pasivas.
 * Depende de menu.PerkPool, weapons.WeaponDef y perks.PassiveDef que ya tienes
 * en el proyecto.
 */
public class Player {

    // posición y tamaño (centro)
    public float x, y;
    public final int w = 20, h = 20;

    // vida y estado
    public int maxHp = 100;
    public int hp = maxHp;
    public boolean alive = true;

    // Hurt / invulnerabilidad
    private final float hurtCooldown = 0.8f; // segundos de invulnerabilidad después de recibir daño
    private float hurtTimer = 0f;

    // Knockback residual (aplicado por enemigos / proyectiles)
    public float vx = 0f, vy = 0f;

    // Facing vector (para armas / disparos)
    public float facingX = 0f;
    public float facingY = -1f; // por defecto mirando hacia arriba

    /** Establece facing desde componentes y normaliza si es necesario */
    public void setFacing(float dx, float dy) {
        if (Math.abs(dx) < 1e-4f && Math.abs(dy) < 1e-4f)
            return;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 1e-4f) {
            this.facingX = dx / len;
            this.facingY = dy / len;
        }
    }

    // XP / progresión
    public int level = 1;
    public int currentXp = 0;
    public int xpToNextLevel = 100;
    public int totalXp = 0;

    // armas y pasivas almacenadas en árboles de Binding
    public final BinaryTree<Binding> weaponLevelsTree = new BinaryTree<Binding>();
    public final BinaryTree<Binding> passiveStacksTree = new BinaryTree<Binding>();

    // derived multipliers
    public float damageMultiplier = 1f;
    public float attackCooldownMultiplier = 1f;
    public float moveSpeedMultiplier = 1f;
    public float crystalXpMultiplier = 1f;
    public int flatCrystalBonus = 0;
    public float hpRegenPerSec = 0f;

    // cooldown timers por arma
    public final BinaryTree<Binding> weaponTimersTree = new BinaryTree<Binding>();

    // referencia al pool
    private final PerkPool pool;

    /** Binding sencillo para pair (key,value). */
    public static class Binding {
        public String key;
        public int value;

        public Binding(String k, int v) {
            this.key = k;
            this.value = v;
        }
    }

    public Player(float x, float y, PerkPool pool) {
        this.x = x;
        this.y = y;
        this.pool = pool;
        this.xpToNextLevel = calcXpForLevel(this.level);

        // por defecto tiene el pico nivel 1
        putBinding(weaponLevelsTree, "pico", 1);
        putBinding(weaponTimersTree, "pico", 0);
    }

    // -------------------------
    // Animación y dirección
    // -------------------------
    private int animTick = 0;
    private boolean facingLeft = false;
    private boolean facingRight = true; // por defecto mirando a la derecha

    public int getAnimTick() {
        return animTick;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingLeft() {
        facingLeft = true;
        facingRight = false;
    }

    public void setFacingRight() {
        facingRight = true;
        facingLeft = false;
    }

    // -------------------------
    // Operaciones sobre BinaryTree<Binding>
    // -------------------------
    private Binding getBinding(BinaryTree<Binding> tree, String key) {
        if (tree == null || tree.getRoot() == null)
            return null;
        PreorderIterator<Binding> it = tree.preOrderIterator();
        while (it.hasNext()) {
            BinaryTreeNode<Binding> node = it.nextNode();
            Binding b = node.getInfo();
            if (b != null && b.key.equals(key))
                return b;
        }
        return null;
    }

    private void putBinding(BinaryTree<Binding> tree, String key, int value) {
        if (tree == null)
            return;
        Binding existing = getBinding(tree, key);
        if (existing != null) {
            existing.value = value;
            return;
        }
        BinaryTreeNode<Binding> node = new BinaryTreeNode<>(new Binding(key, value));
        if (tree.getRoot() == null) {
            tree.setRoot(node);
        } else {
            BinaryTreeNode<Binding> cursor = (BinaryTreeNode<Binding>) tree.getRoot();
            while (cursor.getRight() != null)
                cursor = cursor.getRight();
            cursor.setRight(node);
        }
    }

    private int getIntBinding(BinaryTree<Binding> tree, String key, int defaultValue) {
        Binding b = getBinding(tree, key);
        return b != null ? b.value : defaultValue;
    }

    private void incBinding(BinaryTree<Binding> tree, String key, int delta) {
        Binding b = getBinding(tree, key);
        if (b != null) {
            b.value += delta;
        } else {
            putBinding(tree, key, delta);
        }
    }

    // -------------------------
    // XP y nivelación
    // -------------------------
    public void addXp(int xp, GameController controller) {
        if (xp <= 0)
            return;
        int gained = Math.max(0, Math.round(xp * crystalXpMultiplier) + flatCrystalBonus);
        currentXp += gained;
        totalXp += gained;
        while (currentXp >= xpToNextLevel) {
            currentXp -= xpToNextLevel;
            level++;
            xpToNextLevel = calcXpForLevel(level);
            controller.onPlayerLeveledUp(level);
            break; // pausa para modal
        }
    }

    private int calcXpForLevel(int lvl) {
        double base = 80.0;
        double curve = 1.35;
        return Math.max(20, (int) Math.round(base * Math.pow(lvl, curve)));
    }

    public void applyChoice(Choice c) {
        if (c == null)
            return;
        if (c.kind == Choice.Kind.WEAPON) {
            int prev = getIntBinding(weaponLevelsTree, c.id, 0);
            int next = Math.max(1, prev + 1);
            putBinding(weaponLevelsTree, c.id, next);
            if (getBinding(weaponTimersTree, c.id) == null)
                putBinding(weaponTimersTree, c.id, 0);
        } else if (c.kind == Choice.Kind.PASSIVE) {
            incBinding(passiveStacksTree, c.id, 1);
            PassiveDef p = pool.getPassive(c.id);
            if (p != null)
                p.applyTo(this, 1);
        }
    }

    public void recomputeDerivedStats() {
        damageMultiplier = 1f;
        attackCooldownMultiplier = 1f;
        moveSpeedMultiplier = 1f;
        crystalXpMultiplier = 1f;
        flatCrystalBonus = 0;
        hpRegenPerSec = 0f;
        if (passiveStacksTree != null && passiveStacksTree.getRoot() != null) {
            PreorderIterator<Binding> it = passiveStacksTree.preOrderIterator();
            while (it.hasNext()) {
                BinaryTreeNode<Binding> n = it.nextNode();
                Binding b = n.getInfo();
                if (b == null)
                    continue;
                PassiveDef pd = pool.getPassive(b.key);
                if (pd != null)
                    pd.applyTo(this, b.value);
            }
        }
    }

    // -------------------------
    // Recepción de daño / knockback
    // -------------------------
    public void receiveDamage(int dmg, float kbX, float kbY) {
        if (!alive)
            return;
        if (hurtTimer > 0f)
            return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
        vx += kbX;
        vy += kbY;
        hurtTimer = hurtCooldown;
    }

    public void receiveDamage(int dmg) {
        receiveDamage(dmg, 0f, 0f);
    }

    public void applyKnockback(float kx, float ky) {
        this.vx += kx;
        this.vy += ky;
    }

    // -------------------------
    // Armamento automático
    // -------------------------
    public void updateWeapons(float dt, List<Enemy> enemies, GameController controller) {
        if (weaponTimersTree.getRoot() != null) {
            List<BinaryTreeNode<Binding>> nodes = new ArrayList<>();
            PreorderIterator<Binding> itTimers = weaponTimersTree.preOrderIterator();
            while (itTimers.hasNext())
                nodes.add(itTimers.nextNode());
            for (BinaryTreeNode<Binding> n : nodes) {
                Binding b = n.getInfo();
                if (b == null)
                    continue;
                b.value = Math.max(0, b.value - Math.round(dt * 1000));
            }
        }

        if (weaponLevelsTree.getRoot() == null)
            return;
        PreorderIterator<Binding> it = weaponLevelsTree.preOrderIterator();
        while (it.hasNext()) {
            BinaryTreeNode<Binding> n = it.nextNode();
            Binding b = n.getInfo();
            if (b == null)
                continue;
            String wid = b.key;
            int wlvl = b.value;
            WeaponDef def = pool.getWeapon(wid);
            if (def == null)
                continue;

            float timer = getWeaponTimerSeconds(wid);
            if (timer > 0f)
                continue;

            float baseCd = def.baseCooldown * attackCooldownMultiplier;
            float effectiveCd = Math.max(0.02f, baseCd * (float) Math.pow(0.95, wlvl - 1));

            def.fire(this, wlvl, enemies, controller);
            putBinding(weaponTimersTree, wid, Math.round(effectiveCd * 1000f));
        }
    }

    private float getWeaponTimerSeconds(String wid) {
        Binding t = getBinding(weaponTimersTree, wid);
        if (t == null)
            return 0f;
        return t.value / 1000f;
    }

    // -------------------------
    // Update general
    // -------------------------
    public void update(float dt, List<Enemy> enemies, GameController controller) {
        if (!alive)
            return;

        if (hurtTimer > 0f)
            hurtTimer = Math.max(0f, hurtTimer - dt);

        if (hp < maxHp && hpRegenPerSec > 0f) {
            float heal = hpRegenPerSec * dt;
            hp = Math.min(maxHp, hp + Math.round(heal));
        }

        x += vx * dt;
        y += vy * dt;
        vx *= Math.max(0f, 1f - 6f * dt);
        vy *= Math.max(0f, 1f - 6f * dt);

        updateWeapons(dt, enemies, controller);

        // --- Animación ---
        animTick++;
    }

    // -------------------------
    // Utilities / getters
    // -------------------------
    public Rectangle getBounds() {
        return new Rectangle((int) (x - w / 2), (int) (y - h / 2), w, h);
    }

    public int getWeaponLevel(String id) {
        return getIntBinding(weaponLevelsTree, id, 0);
    }

    public int getPassiveStacks(String id) {
        return getIntBinding(passiveStacksTree, id, 0);
    }

    // Inventario simple de llaves
    public final java.util.List<String> keysOwned = new java.util.ArrayList<>();

    public void addKey(String keyId) {
        if (keyId == null)
            return;
        for (String k : keysOwned)
            if (keyId.equals(k))
                return;
        keysOwned.add(keyId);
    }

    public boolean hasKey(String keyId) {
        if (keyId == null)
            return false;
        for (String k : keysOwned)
            if (keyId.equals(k))
                return true;
        return false;
    }
}
