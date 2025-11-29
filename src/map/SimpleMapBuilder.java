package map;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.TreeNode;

public class SimpleMapBuilder {

    public static BinaryTree<MineRoom> buildProceduralBinaryMap(int height, int roomW, int roomH) {
        if (height < 0)
            throw new IllegalArgumentException("height must be >= 0");
        int totalLevels = height + 1;
        int totalNodes = (1 << totalLevels) - 1;
        @SuppressWarnings("unchecked")
        BinaryTreeNode<MineRoom>[] nodes = (BinaryTreeNode<MineRoom>[]) new BinaryTreeNode[totalNodes + 1];

        // crear nodos: id = nivel
        for (int level = 0; level <= height; level++) {
            int count = 1 << level;
            for (int i = 0; i < count; i++) {
                int index = (1 << level) + i;
                int id = level;
                MineRoom room = new MineRoom(id, roomW, roomH);
                nodes[index] = new BinaryTreeNode<>(room);
            }
        }

        // enlazar padre-hijo (estructura completa)
        for (int idx = 1; idx <= totalNodes; idx++) {
            BinaryTreeNode<MineRoom> node = nodes[idx];
            if (node == null)
                continue;
            int leftIdx = idx * 2;
            int rightIdx = idx * 2 + 1;
            if (leftIdx <= totalNodes)
                node.setLeft(nodes[leftIdx]);
            if (rightIdx <= totalNodes)
                node.setRight(nodes[rightIdx]);
        }
        BinaryTree<MineRoom> map = new BinaryTree<>(nodes[1]);
        // crear puertas padre<->hijo: en child una única puerta superior hacia el
        // padre,
        // en parent una puerta inferior hacia el child (una por hijo).
        for (int idx = 1; idx <= totalNodes; idx++) {
            BinaryTreeNode<MineRoom> parent = nodes[idx];
            if (parent == null)
                continue;
            int left = idx * 2;
            int right = idx * 2 + 1;
            if (left <= totalNodes && nodes[left] != null)
                addParentChildDoors(parent, nodes[left]);
            if (right <= totalNodes && nodes[right] != null)
                addParentChildDoors(parent, nodes[right]);
        }

        // añadir puerta de victoria en la raíz y marcarla cerrada por defecto
        BinaryTreeNode<MineRoom> rootNode = nodes[1];
        if (rootNode != null) {
            MineRoom rootRoom = rootNode.getInfo();
            java.awt.Rectangle winArea = new java.awt.Rectangle(rootRoom.width - 110, 10, 100, 32);
            Door win = new Door(winArea, true, "Exit");
            win.locked = true;
            rootRoom.doors.add(win);
        }

        // La llave dorada ya NO se coloca al inicio del juego
        // Se spawneará cuando el jugador derrote al jefe final
        return map;
    }

    private static void addParentChildDoors(BinaryTreeNode<MineRoom> parent, BinaryTreeNode<MineRoom> child) {
        MineRoom pRoom = parent.getInfo();
        MineRoom cRoom = child.getInfo();

        // En child: puerta superior (única) hacia parent
        java.awt.Rectangle childDoor = new java.awt.Rectangle(cRoom.width / 2 - 40, 10, 80, 28);
        int parentSpawnX = pRoom.width / 2;
        int parentSpawnY = pRoom.height - 80;
        Door childToParent = new Door(childDoor, parent, parentSpawnX, parentSpawnY);
        childToParent.label = childToParent.destino != null && childToParent.destino.getInfo() != null
                ? String.valueOf(childToParent.destino.getInfo().id)
                : "";
        cRoom.doors.add(childToParent);

        // En parent: puerta inferior hacia child (izq/der)
        boolean isLeftChild = parent.getLeft() != null && parent.getLeft().equals(child);
        int offset = isLeftChild ? -80 : 80;
        int px = Math.max(60, Math.min(pRoom.width - 60, pRoom.width / 2 + offset));
        java.awt.Rectangle parentDoor = new java.awt.Rectangle(px - 40, pRoom.height - 38, 80, 28);
        int childSpawnX = cRoom.width / 2;
        int childSpawnY = 80;
        Door parentToChild = new Door(parentDoor, child, childSpawnX, childSpawnY);
        parentToChild.label = parentToChild.destino != null && parentToChild.destino.getInfo() != null
                ? String.valueOf(parentToChild.destino.getInfo().id)
                : "";
        pRoom.doors.add(parentToChild);
    }

    /**
     * Elige una sala aleatoria que no sea la raíz recorriendo el árbol desde la
     * raíz.
     */
    public static BinaryTreeNode<MineRoom> pickRandomNonRootNode(BinaryTree<MineRoom> map) {
        if (map == null)
            return null;
        TreeNode<MineRoom> rootT = map.getRoot();
        if (!(rootT instanceof BinaryTreeNode))
            return null;
        BinaryTreeNode<MineRoom> root = (BinaryTreeNode<MineRoom>) rootT;
        java.util.List<BinaryTreeNode<MineRoom>> candidates = new java.util.ArrayList<>();
        java.util.Queue<BinaryTreeNode<MineRoom>> q = new java.util.ArrayDeque<>();
        q.add(root);

        while (!q.isEmpty()) {
            BinaryTreeNode<MineRoom> cur = q.poll();
            if (cur == null)
                continue;
            // excluir root nivel 0
            try {
                if (map.nodeLevel(cur) != 0)
                    candidates.add(cur);
            } catch (Exception ex) {
                // si nodeLevel no funciona por alguna razón, añadir (se filtrará luego)
                candidates.add(cur);
            }
            if (cur.getLeft() != null)
                q.add(cur.getLeft());
            if (cur.getRight() != null)
                q.add(cur.getRight());
        }

        // si por alguna razón sólo tenemos la raíz, devolver null
        if (candidates.isEmpty())
            return null;
        return candidates.get(new java.util.Random().nextInt(candidates.size()));
    }

    public static BinaryTreeNode<MineRoom> pickRandomLeaf(BinaryTree<MineRoom> map) {
        java.util.List<TreeNode<MineRoom>> leaves = map.getLeaves();
        if (leaves.isEmpty())
            return null;
        return (BinaryTreeNode<MineRoom>) leaves.get(new java.util.Random().nextInt(leaves.size()));
    }
}