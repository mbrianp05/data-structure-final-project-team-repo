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
import java.util.Random;

public class Player {

    public float x, y;
    public final int w = 20, h = 20;

    public int maxHp = 100;
    public int hp = maxHp;
    public boolean alive = true;

    private float hurtCooldown = 0.8f;
    private float hurtTimer = 0f;

    public float vx = 0f, vy = 0f;

    public float facingX = 0f;
    public float facingY = -1f;

    public void setFacing(float dx, float dy) {
        if (Math.abs(dx) < 1e-4f && Math.abs(dy) < 1e-4f) {
            return;
        }
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len > 1e-4f) {
            this.facingX = dx / len;
            this.facingY = dy / len;
        }
    }

    public int level = 1;
    public int currentXp = 0;
    public int xpToNextLevel = 100;
    public int totalXp = 0;

    public final BinaryTree<Binding> weaponLevelsTree = new BinaryTree<Binding>();
    public final BinaryTree<Binding> passiveStacksTree = new BinaryTree<Binding>();

    public float damageMultiplier = 1f;
    public float attackCooldownMultiplier = 1f;
    public float moveSpeedMultiplier = 1f;
    public float crystalXpMultiplier = 1f;
    public int flatCrystalBonus = 0;
    public float hpRegenPerSec = 0f;

    public final BinaryTree<Binding> weaponTimersTree = new BinaryTree<Binding>();

    private final PerkPool pool;
    private final Random rnd = new Random();

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

        putBinding(weaponLevelsTree, "pico", 1);
        putBinding(weaponTimersTree, "pico", 0);
    }

    private int animTick = 0;
    private boolean facingLeft = false;
    private boolean facingRight = true;

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

    private Binding getBinding(BinaryTree<Binding> tree, String key) {
        Binding result = null;
        if (tree != null && tree.getRoot() != null) {
            PreorderIterator<Binding> it = tree.preOrderIterator();
            while (it.hasNext()) {
                BinaryTreeNode<Binding> node = it.nextNode();
                Binding b = node.getInfo();
                if (b != null && b.key.equals(key)) {
                    result = b;
                    break;
                }
            }
        }
        return result;
    }

    private void putBinding(BinaryTree<Binding> tree, String key, int value) {
        if (tree == null) {
            return;
        }
        Binding existing = getBinding(tree, key);
        if (existing != null) {
            existing.value = value;
        }
        else {
            BinaryTreeNode<Binding> node = new BinaryTreeNode<>(new Binding(key, value));
            if (tree.getRoot() == null) {
                tree.setRoot(node);
            }
            else {
                BinaryTreeNode<Binding> cursor = (BinaryTreeNode<Binding>) tree.getRoot();
                while (cursor.getRight() != null) {
                    cursor = cursor.getRight();
                }
                cursor.setRight(node);
            }
        }
    }

    private int getIntBinding(BinaryTree<Binding> tree, String key, int defaultValue) {
        int result = defaultValue;
        Binding b = getBinding(tree, key);
        if (b != null) {
            result = b.value;
        }
        return result;
    }

    private void incBinding(BinaryTree<Binding> tree, String key, int delta) {
        Binding b = getBinding(tree, key);
        if (b != null) {
            b.value += delta;
        }
        else {
            putBinding(tree, key, delta);
        }
    }

    private void removeBinding(BinaryTree<Binding> tree, String key) {
        if (tree == null || tree.getRoot() == null) {
            return;
        }
        PreorderIterator<Binding> it = tree.preOrderIterator();
        BinaryTreeNode<Binding> found = null;
        while (it.hasNext()) {
            BinaryTreeNode<Binding> n = it.nextNode();
            Binding b = n.getInfo();
            if (b != null && b.key.equals(key)) {
                found = n;
                break;
            }
        }
        if (found != null) {
            tree.deleteNode(found);
        }
    }

    public void addXp(int xp, GameController controller) {
        if (xp > 0) {
            int baseGained = Math.round(xp * crystalXpMultiplier);
            int gained = Math.max(0, baseGained + flatCrystalBonus);
            currentXp += gained;
            totalXp += gained;
            while (currentXp >= xpToNextLevel) {
                currentXp -= xpToNextLevel;
                level++;
                xpToNextLevel = calcXpForLevel(level);
                controller.onPlayerLeveledUp(level);
                break;
            }
        }
    }

    private int calcXpForLevel(int lvl) {
        double base = 80.0;
        double curve = 1.35;
        int calculated = (int)Math.round(base * Math.pow(lvl, curve));
        return Math.max(20, calculated);
    }

    public void applyChoice(Choice c) {
        if (c != null) {
            if (c.kind == Choice.Kind.WEAPON) {
                int prev = getIntBinding(weaponLevelsTree, c.id, 0);
                int next = Math.max(1, prev + 1);
                putBinding(weaponLevelsTree, c.id, next);
                if (getBinding(weaponTimersTree, c.id) == null) {
                    putBinding(weaponTimersTree, c.id, 0);
                }
            }
            else if (c.kind == Choice.Kind.PASSIVE) {
                incBinding(passiveStacksTree, c.id, 1);
                PassiveDef p = pool.getPassive(c.id);
                if (p != null) {
                    p.applyTo(this, 1);
                }
            }
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
                if (b == null) {
                    continue;
                }
                PassiveDef pd = pool.getPassive(b.key);
                if (pd != null) {
                    pd.applyTo(this, b.value);
                }
            }
        }
    }

    public void receiveDamage(int dmg, float kbX, float kbY) {
        if (alive && hurtTimer <= 0f) {
            hp -= dmg;
            if (hp <= 0) {
                hp = 0;
                alive = false;
            }
            vx += kbX;
            vy += kbY;
            hurtTimer = hurtCooldown;
        }
    }

    public void receiveDamage(int dmg) {
        receiveDamage(dmg, 0f, 0f);
    }

    public void applyKnockback(float kx, float ky) {
        this.vx += kx;
        this.vy += ky;
    }

    public void updateWeapons(float dt, List<Enemy> enemies, GameController controller) {
        if (weaponTimersTree.getRoot() != null) {
            List<BinaryTreeNode<Binding>> nodes = new ArrayList<>();
            PreorderIterator<Binding> itTimers = weaponTimersTree.preOrderIterator();
            while (itTimers.hasNext()) {
                nodes.add(itTimers.nextNode());
            }
            for (BinaryTreeNode<Binding> n : nodes) {
                Binding b = n.getInfo();
                if (b == null) {
                    continue;
                }
                int newValue = Math.max(0, b.value - Math.round(dt * 1000));
                b.value = newValue;
            }
        }

        if (weaponLevelsTree.getRoot() != null) {
            PreorderIterator<Binding> it = weaponLevelsTree.preOrderIterator();
            while (it.hasNext()) {
                BinaryTreeNode<Binding> n = it.nextNode();
                Binding b = n.getInfo();
                if (b == null) {
                    continue;
                }
                String wid = b.key;
                int wlvl = b.value;
                WeaponDef def = pool.getWeapon(wid);
                if (def == null) {
                    continue;
                }

                float timer = getWeaponTimerSeconds(wid);
                if (timer <= 0f) {
                    float baseCd = def.baseCooldown * attackCooldownMultiplier;
                    float levelFactor = (float)Math.pow(0.95, wlvl - 1);
                    float effectiveCd = Math.max(0.02f, baseCd * levelFactor);

                    def.fire(this, wlvl, enemies, controller);
                    putBinding(weaponTimersTree, wid, Math.round(effectiveCd * 1000f));
                }
            }
        }
    }

    private float getWeaponTimerSeconds(String wid) {
        float result = 0f;
        Binding t = getBinding(weaponTimersTree, wid);
        if (t != null) {
            result = t.value / 1000f;
        }
        return result;
    }

    public void update(float dt, List<Enemy> enemies, GameController controller) {
        if (alive) {
            if (hurtTimer > 0f) {
                hurtTimer = Math.max(0f, hurtTimer - dt);
            }

            if (hp < maxHp && hpRegenPerSec > 0f) {
                float heal = hpRegenPerSec * dt;
                int healAmount = Math.round(heal);
                int newHp = hp + healAmount;
                hp = Math.min(maxHp, newHp);
            }

            x += vx * dt;
            y += vy * dt;
            float vxDecay = Math.max(0f, 1f - 6f * dt);
            float vyDecay = Math.max(0f, 1f - 6f * dt);
            vx *= vxDecay;
            vy *= vyDecay;

            updateWeapons(dt, enemies, controller);

            animTick++;
        }
    }

    public Rectangle getBounds() {
        int xPos = (int)(x - w/2);
        int yPos = (int)(y - h/2);
        return new Rectangle(xPos, yPos, w, h);
    }

    public int getWeaponLevel(String id) {
        return getIntBinding(weaponLevelsTree, id, 0);
    }

    public int getPassiveStacks(String id) {
        return getIntBinding(passiveStacksTree, id, 0);
    }

    public final java.util.List<String> keysOwned = new java.util.ArrayList<>();

    public void addKey(String keyId) {
        if (keyId != null) {
            boolean found = false;
            for (String k : keysOwned) {
                if (keyId.equals(k)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                keysOwned.add(keyId);
            }
        }
    }

    public boolean hasKey(String keyId) {
        boolean result = false;
        if (keyId != null) {
            for (String k : keysOwned) {
                if (keyId.equals(k)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}