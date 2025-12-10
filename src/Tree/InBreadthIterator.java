package Tree;

import java.util.ArrayDeque;

public class InBreadthIterator<E> implements ITreeIterator<E> {
    private final ArrayDeque<BinaryTreeNode<E>> deque;
    private BinaryTreeNode<E> currentNode;
    private BinaryTreeNode<E> nextNode;
    GeneralTree<E> tree;

    public InBreadthIterator(GeneralTree<E> tree) {
        this.tree = tree;
        this.currentNode = null;
        this.nextNode = (BinaryTreeNode)tree.getRoot();
        this.deque = new ArrayDeque();
        if (this.nextNode != null) {
            this.deque.addAll(tree.getSons(this.nextNode));
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
                if (!this.tree.nodeIsLeaf(this.nextNode)) {
                    this.deque.addAll(this.tree.getSons(this.nextNode));
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
                if (!this.tree.nodeIsLeaf(this.nextNode)) {
                    this.deque.addAll(this.tree.getSons(this.nextNode));
                }
            }
        }

        return this.currentNode;
    }

    public void remove() {
        this.tree.deleteNode(this.currentNode);
    }
}
