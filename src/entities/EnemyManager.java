package entities;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.PreorderIterator;
import map.MineRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnemyManager {
    public static class EnemyListBinding {
        public BinaryTreeNode<MineRoom> roomNode;
        public final List<Enemy> enemies = new ArrayList<>();

        public EnemyListBinding(BinaryTreeNode<MineRoom> roomNode) {
            this.roomNode = roomNode;
        }
    }

    private final BinaryTree<EnemyListBinding> bindingsTree = new BinaryTree<EnemyListBinding>();
    private final Random rnd = new Random();

    public EnemyManager(BinaryTree<MineRoom> map) {
    }

    private EnemyListBinding findBinding(BinaryTreeNode<MineRoom> node) {
        if (node == null || bindingsTree.getRoot() == null) {
            return null;
        }
        PreorderIterator<EnemyListBinding> it = bindingsTree.preOrderIterator();
        while (it.hasNext()) {
            BinaryTreeNode<EnemyListBinding> bnode = it.nextNode();
            EnemyListBinding b = bnode.getInfo();
            if (b != null && b.roomNode != null && b.roomNode.equals(node)) {
                return b;
            }
        }
        return null;
    }

    private BinaryTreeNode<EnemyListBinding> findBindingNode(BinaryTreeNode<MineRoom> node) {
        if (node == null || bindingsTree.getRoot() == null) {
            return null;
        }
        PreorderIterator<EnemyListBinding> it = bindingsTree.preOrderIterator();
        while (it.hasNext()) {
            BinaryTreeNode<EnemyListBinding> bnode = it.nextNode();
            EnemyListBinding b = bnode.getInfo();
            if (b != null && b.roomNode != null && b.roomNode.equals(node)) {
                return bnode;
            }
        }
        return null;
    }

    private BinaryTreeNode<EnemyListBinding> createBindingNode(BinaryTreeNode<MineRoom> node) {
        EnemyListBinding b = new EnemyListBinding(node);
        BinaryTreeNode<EnemyListBinding> newNode = new BinaryTreeNode<>(b);
        if (bindingsTree.getRoot() == null) {
            bindingsTree.setRoot(newNode);
        }
        else {
            BinaryTreeNode<EnemyListBinding> cursor = (BinaryTreeNode<EnemyListBinding>) bindingsTree.getRoot();
            while (cursor.getRight() != null) {
                cursor = cursor.getRight();
            }
            cursor.setRight(newNode);
        }
        return newNode;
    }

    public void addEnemyAt(BinaryTreeNode<MineRoom> node, Enemy e) {
        if (node == null || e == null) {
            return;
        }
        EnemyListBinding b = findBinding(node);
        if (b == null) {
            BinaryTreeNode<EnemyListBinding> bn = createBindingNode(node);
            b = bn.getInfo();
        }
        b.enemies.add(e);
    }

    public void removeEnemy(Enemy e) {
        if (e == null) {
            return;
        }
        if (bindingsTree.getRoot() == null) {
            return;
        }
        PreorderIterator<EnemyListBinding> it = bindingsTree.preOrderIterator();
        BinaryTreeNode<EnemyListBinding> targetBindingNode = null;
        EnemyListBinding targetBinding = null;
        while (it.hasNext()) {
            BinaryTreeNode<EnemyListBinding> bnode = it.nextNode();
            EnemyListBinding b = bnode.getInfo();
            if (b != null && b.enemies.contains(e)) {
                targetBindingNode = bnode;
                targetBinding = b;
                break;
            }
        }
        if (targetBinding != null) {
            targetBinding.enemies.remove(e);
            if (targetBinding.enemies.isEmpty() && targetBindingNode != null) {
                bindingsTree.deleteNode(targetBindingNode);
            }
        }
    }

    public List<Enemy> getEnemiesAt(BinaryTreeNode<MineRoom> node) {
        if (node == null) {
            return Collections.emptyList();
        }
        EnemyListBinding b = findBinding(node);
        if (b == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(b.enemies);
    }

    public boolean isCleared(BinaryTreeNode<MineRoom> node) {
        EnemyListBinding b = findBinding(node);
        if (b == null) {
            return true;
        }
        for (Enemy e : b.enemies) {
            if (e != null && e.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public void removeAllAt(BinaryTreeNode<MineRoom> node) {
        if (node == null) {
            return;
        }
        EnemyListBinding b = findBinding(node);
        if (b == null) {
            return;
        }
        for (Enemy e : new ArrayList<>(b.enemies)) {
            try {
                e.damage(Integer.MAX_VALUE, null);
            } catch (Exception ex) {
            }
        }
        b.enemies.clear();
        BinaryTreeNode<EnemyListBinding> bn = findBindingNode(node);
        if (bn != null) {
            bindingsTree.deleteNode(bn);
        }
    }

    public void spawnHordeAt(BinaryTreeNode<MineRoom> node, int amount, float spawnDistance) {
        if (node == null || amount <= 0) {
            return;
        }
        MineRoom r = node.getInfo();
        if (r == null) {
            return;
        }

        float cx = r.width / 2f;
        float cy = r.height / 2f;
        float dist = Math.max(48f, Math.min(spawnDistance, Math.max(r.width, r.height)));

        List<Enemy> spawned = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2.0;
            double rad = dist * (0.8 + rnd.nextDouble() * 0.4);
            float sx = cx + (float) Math.cos(ang) * (float) rad;
            float sy = cy + (float) Math.sin(ang) * (float) rad;

            if (sx >= 0 && sx <= r.width && sy >= 0 && sy <= r.height) {
                float dl = Math.abs(sx - 0);
                float dr = Math.abs(sx - r.width);
                float dt = Math.abs(sy - 0);
                float db = Math.abs(sy - r.height);
                float min = Math.min(Math.min(dl, dr), Math.min(dt, db));
                float pad = Math.max(40f, Math.min(120f, dist * 0.4f));
                if (min == dl) {
                    sx = -pad;
                }
                else if (min == dr) {
                    sx = r.width + pad;
                }
                else if (min == dt) {
                    sy = -pad;
                }
                else {
                    sy = r.height + pad;
                }
            }

            int level = 1 + rnd.nextInt(2);
            int hp = 18 + level * 8;
            float speed = 18f + level * 3f;
            Enemy e = new Enemy(node, sx, sy, hp, speed, level);
            spawned.add(e);
        }

        for (Enemy e : spawned) {
            addEnemyAt(node, e);
        }
    }

    public List<Enemy> allEnemies() {
        List<Enemy> out = new ArrayList<>();
        if (bindingsTree.getRoot() == null) {
            return out;
        }
        PreorderIterator<EnemyListBinding> it = bindingsTree.preOrderIterator();
        while (it.hasNext()) {
            BinaryTreeNode<EnemyListBinding> n = it.nextNode();
            EnemyListBinding b = n.getInfo();
            if (b != null && !b.enemies.isEmpty()) {
                out.addAll(b.enemies);
            }
        }
        return out;
    }
}
