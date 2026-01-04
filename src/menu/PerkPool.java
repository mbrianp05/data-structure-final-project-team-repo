package menu;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.PreorderIterator;
import entities.Player;
import perks.*;
import weapons.*;

import java.util.*;

public class PerkPool {
    private final BinaryTree<WeaponDef> weaponsTree = new BinaryTree<WeaponDef>();
    private final BinaryTree<PassiveDef> passivesTree = new BinaryTree<PassiveDef>();
    private final Random rnd = new Random();

    public PerkPool() {
        registerWeapon(new PickaxeWeapon());
        registerWeapon(new DynamiteWeapon());
        registerWeapon(new CoalOrbWeapon());
        registerWeapon(new LanternWeapon());

        registerPassive(new DwarvenEndurance());
        registerPassive(new MinerGreed());
        registerPassive(new ForgeTempering());
        registerPassive(new HearthRegeneration());
    }

    public void registerWeapon(WeaponDef w) {
        if (w == null) {
            return;
        }
        BinaryTreeNode<WeaponDef> node = new BinaryTreeNode<>(w);
        if (weaponsTree.getRoot() == null) {
            weaponsTree.setRoot(node);
        }
        else {
            BinaryTreeNode<WeaponDef> cursor = (BinaryTreeNode<WeaponDef>) weaponsTree.getRoot();
            while (cursor.getRight() != null) {
                cursor = cursor.getRight();
            }
            cursor.setRight(node);
        }
    }

    public void registerPassive(PassiveDef p) {
        if (p == null) {
            return;
        }
        BinaryTreeNode<PassiveDef> node = new BinaryTreeNode<>(p);
        if (passivesTree.getRoot() == null) {
            passivesTree.setRoot(node);
        }
        else {
            BinaryTreeNode<PassiveDef> cursor = (BinaryTreeNode<PassiveDef>) passivesTree.getRoot();
            while (cursor.getRight() != null) {
                cursor = cursor.getRight();
            }
            cursor.setRight(node);
        }
    }

    private List<WeaponDef> allWeapons() {
        List<WeaponDef> out = new ArrayList<>();
        if (weaponsTree.getRoot() == null) {
            return out;
        }
        PreorderIterator<WeaponDef> it = weaponsTree.preOrderIterator();
        while (it.hasNext()) {
            WeaponDef w = it.nextNode().getInfo();
            if (w != null) {
                out.add(w);
            }
        }
        return out;
    }

    private List<PassiveDef> allPassives() {
        List<PassiveDef> out = new ArrayList<>();
        if (passivesTree.getRoot() == null) {
            return out;
        }
        PreorderIterator<PassiveDef> it = passivesTree.preOrderIterator();
        while (it.hasNext()) {
            PassiveDef p = it.nextNode().getInfo();
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }

    public WeaponDef getWeapon(String id) {
        WeaponDef result = null;
        List<WeaponDef> weapons = allWeapons();
        for (WeaponDef w : weapons) {
            if (w.id.equals(id)) {
                result = w;
                break;
            }
        }
        return result;
    }

    public PassiveDef getPassive(String id) {
        PassiveDef result = null;
        List<PassiveDef> passives = allPassives();
        for (PassiveDef p : passives) {
            if (p.id.equals(id)) {
                result = p;
                break;
            }
        }
        return result;
    }

    public List<Choice> pickOptions(int n, Player player) {
        List<Choice> out = new ArrayList<>();
        List<WeaponDef> wList = allWeapons();
        List<PassiveDef> pList = allPassives();

        if (wList.isEmpty() && pList.isEmpty()) {
            return out;
        }

        List<WeaponDef> wPool = new ArrayList<>(wList);
        List<PassiveDef> pPool = new ArrayList<>(pList);

        while (out.size() < n && (!wPool.isEmpty() || !pPool.isEmpty())) {
            boolean pickWeapon = rnd.nextBoolean();
            if (pickWeapon && !wPool.isEmpty()) {
                WeaponDef chosen = wPool.remove(rnd.nextInt(wPool.size()));
                boolean alreadyExists = false;
                for (Choice c : out) {
                    if (c.id.equals(chosen.id)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    out.add(new Choice(Choice.Kind.WEAPON, chosen.id, chosen.name, chosen.description));
                }
            }
            else if (!pPool.isEmpty()) {
                PassiveDef chosen = pPool.remove(rnd.nextInt(pPool.size()));
                boolean alreadyExists = false;
                for (Choice c : out) {
                    if (c.id.equals(chosen.id)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    out.add(new Choice(Choice.Kind.PASSIVE, chosen.id, chosen.name, chosen.description));
                }
            }
            else if (!wPool.isEmpty()) {
                WeaponDef chosen = wPool.remove(rnd.nextInt(wPool.size()));
                boolean alreadyExists = false;
                for (Choice c : out) {
                    if (c.id.equals(chosen.id)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    out.add(new Choice(Choice.Kind.WEAPON, chosen.id, chosen.name, chosen.description));
                }
            }
        }

        return out;
    }
}