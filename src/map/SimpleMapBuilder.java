package map;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.TreeNode;

public class SimpleMapBuilder {

    public static BinaryTree<MineRoom> buildProceduralBinaryMap(int height, int roomW, int roomH) {
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        int totalLevels = height + 1;
        int totalNodes = (1 << totalLevels) - 1;
        @SuppressWarnings("unchecked")
        BinaryTreeNode<MineRoom>[] nodes = (BinaryTreeNode<MineRoom>[]) new BinaryTreeNode[totalNodes + 1];

        for (int level = 0; level <= height; level++) {
            int count = 1 << level;
            for (int i = 0; i < count; i++) {
                int index = (1 << level) + i;
                int id = level;
                MineRoom room = new MineRoom(id, roomW, roomH);
                nodes[index] = new BinaryTreeNode<>(room);
            }
        }

        for (int idx = 1; idx <= totalNodes; idx++) {
            BinaryTreeNode<MineRoom> node = nodes[idx];
            if (node == null) {
                continue;
            }
            int leftIdx = idx * 2;
            int rightIdx = idx * 2 + 1;
            if (leftIdx <= totalNodes) {
                node.setLeft(nodes[leftIdx]);
            }
            if (rightIdx <= totalNodes) {
                node.setRight(nodes[rightIdx]);
            }
        }
        BinaryTree<MineRoom> map = new BinaryTree<>(nodes[1]);

        for (int idx = 1; idx <= totalNodes; idx++) {
            BinaryTreeNode<MineRoom> parent = nodes[idx];
            if (parent == null) {
                continue;
            }
            int left = idx * 2;
            int right = idx * 2 + 1;
            if (left <= totalNodes && nodes[left] != null) {
                addParentChildDoors(parent, nodes[left]);
            }
            if (right <= totalNodes && nodes[right] != null) {
                addParentChildDoors(parent, nodes[right]);
            }
        }

        BinaryTreeNode<MineRoom> rootNode = nodes[1];
        if (rootNode != null) {
            MineRoom rootRoom = rootNode.getInfo();
            java.awt.Rectangle winArea = new java.awt.Rectangle(rootRoom.width - 110, 10, 100, 32);
            Door win = new Door(winArea, true, "Exit");
            win.locked = true;
            rootRoom.doors.add(win);
        }

        return map;
    }

    private static void addParentChildDoors(BinaryTreeNode<MineRoom> parent, BinaryTreeNode<MineRoom> child) {
        MineRoom pRoom = parent.getInfo();
        MineRoom cRoom = child.getInfo();

        java.awt.Rectangle childDoor = new java.awt.Rectangle(cRoom.width / 2 - 40, 10, 80, 28);
        int parentSpawnX = pRoom.width / 2;
        int parentSpawnY = pRoom.height - 80;
        Door childToParent = new Door(childDoor, parent, parentSpawnX, parentSpawnY);
        if (childToParent.destino != null && childToParent.destino.getInfo() != null) {
            childToParent.label = String.valueOf(childToParent.destino.getInfo().id);
        }
        else {
            childToParent.label = "";
        }
        cRoom.doors.add(childToParent);

        boolean isLeftChild = false;
        if (parent.getLeft() != null && parent.getLeft().equals(child)) {
            isLeftChild = true;
        }
        int offset = 0;
        if (isLeftChild) {
            offset = -80;
        }
        else {
            offset = 80;
        }
        int pxMin = 60;
        int pxMax = pRoom.width - 60;
        int pxCenter = pRoom.width / 2 + offset;
        int px = Math.max(pxMin, Math.min(pxMax, pxCenter));
        java.awt.Rectangle parentDoor = new java.awt.Rectangle(px - 40, pRoom.height - 38, 80, 28);
        int childSpawnX = cRoom.width / 2;
        int childSpawnY = 80;
        Door parentToChild = new Door(parentDoor, child, childSpawnX, childSpawnY);
        if (parentToChild.destino != null && parentToChild.destino.getInfo() != null) {
            parentToChild.label = String.valueOf(parentToChild.destino.getInfo().id);
        }
        else {
            parentToChild.label = "";
        }
        pRoom.doors.add(parentToChild);
    }

    public static BinaryTreeNode<MineRoom> pickRandomNonRootNode(BinaryTree<MineRoom> map) {
        BinaryTreeNode<MineRoom> result = null;
        if (map != null) {
            TreeNode<MineRoom> rootT = map.getRoot();
            if (rootT instanceof BinaryTreeNode) {
                BinaryTreeNode<MineRoom> root = (BinaryTreeNode<MineRoom>) rootT;
                java.util.List<BinaryTreeNode<MineRoom>> candidates = new java.util.ArrayList<>();
                java.util.Queue<BinaryTreeNode<MineRoom>> q = new java.util.ArrayDeque<>();
                q.add(root);

                while (!q.isEmpty()) {
                    BinaryTreeNode<MineRoom> cur = q.poll();
                    if (cur != null) {
                        try {
                            int nodeLevel = map.nodeLevel(cur);
                            if (nodeLevel != 0) {
                                candidates.add(cur);
                            }
                        } catch (Exception ex) {
                            candidates.add(cur);
                        }
                        if (cur.getLeft() != null) {
                            q.add(cur.getLeft());
                        }
                        if (cur.getRight() != null) {
                            q.add(cur.getRight());
                        }
                    }
                }

                if (!candidates.isEmpty()) {
                    java.util.Random random = new java.util.Random();
                    int randomIndex = random.nextInt(candidates.size());
                    result = candidates.get(randomIndex);
                }
            }
        }
        return result;
    }

    public static BinaryTreeNode<MineRoom> pickRandomLeaf(BinaryTree<MineRoom> map) {
        BinaryTreeNode<MineRoom> result = null;
        java.util.List<TreeNode<MineRoom>> leaves = map.getLeaves();
        if (!leaves.isEmpty()) {
            java.util.Random random = new java.util.Random();
            int randomIndex = random.nextInt(leaves.size());
            TreeNode<MineRoom> selected = leaves.get(randomIndex);
            if (selected instanceof BinaryTreeNode) {
                result = (BinaryTreeNode<MineRoom>) selected;
            }
        }
        return result;
    }
}