package Tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class InBreadthIteratorWithLevels<E> implements ITreeIterator<E> {
    private final ArrayDeque<BreadthNode<E>> deque;
    private BreadthNode<E> currentNode;
    private BreadthNode<E> nextNode;
    GeneralTree<E> tree;

    public InBreadthIteratorWithLevels(GeneralTree<E> tree) {
        this.tree = tree;
        this.currentNode = null;
        this.nextNode = new BreadthNode((BinaryTreeNode)tree.getRoot());
        this.deque = new ArrayDeque();
        if (this.nextNode != null) {
            ArrayList<BreadthNode<E>> sons = this.getSonsWithLevels(tree.getSons(this.nextNode.getNode()), this.nextNode.getLevel());
            this.deque.addAll(sons);
        }

    }

    public boolean hasNext() {
        return this.nextNode != null;
    }

    public E next() {
        E returnInfo = null;
        this.currentNode = this.nextNode;
        if (this.nextNode != null) {
            returnInfo = this.nextNode.getInfo();
            if (this.deque.isEmpty()) {
                this.nextNode = null;
            } else {
                this.nextNode = this.deque.poll();
                if (!this.tree.nodeIsLeaf(this.nextNode.getNode())) {
                    ArrayList<BreadthNode<E>> sons = this.getSonsWithLevels(this.tree.getSons(this.nextNode.getNode()), this.nextNode.getLevel());
                    this.deque.addAll(sons);
                }
            }
        }

        return returnInfo;
    }

    public BinaryTreeNode<E> nextNode() {
        this.currentNode = this.nextNode;
        if (this.nextNode != null) {
            if (this.deque.isEmpty()) {
                this.nextNode = null;
            } else {
                this.nextNode = this.deque.poll();
                if (!this.tree.nodeIsLeaf(this.nextNode.getNode())) {
                    ArrayList<BreadthNode<E>> sons = this.getSonsWithLevels(this.tree.getSons(this.nextNode.getNode()), this.nextNode.getLevel());
                    this.deque.addAll(sons);
                }
            }
        }

        return this.currentNode.getNode();
    }

    public BreadthNode<E> nextNodeWithLevel() {
        this.currentNode = this.nextNode;
        if (this.nextNode != null) {
            if (this.deque.isEmpty()) {
                this.nextNode = null;
            } else {
                this.nextNode = this.deque.poll();
                if (!this.tree.nodeIsLeaf(this.nextNode.getNode())) {
                    ArrayList<BreadthNode<E>> sons = this.getSonsWithLevels(this.tree.getSons(this.nextNode.getNode()), this.nextNode.getLevel());
                    this.deque.addAll(sons);
                }
            }
        }

        return this.currentNode;
    }

    public void remove() {
        this.tree.deleteNode(this.currentNode.getNode());
    }

    public ArrayList<BreadthNode<E>> getSonsWithLevels(List<BinaryTreeNode<E>> sons, int fatherLevel) {
        ArrayList<BreadthNode<E>> list = new ArrayList(sons.size());

        for(BinaryTreeNode<E> node : sons) {
            list.add(new BreadthNode(node, fatherLevel));
        }

        return list;
    }
}
