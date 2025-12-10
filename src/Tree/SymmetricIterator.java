package Tree;

import java.util.ArrayDeque;

public class SymmetricIterator<E> implements ITreeIterator<E> {
    private BinaryTreeNode<E> nextNode;
    private BinaryTreeNode<E> currentNode;
    private Tree<E> tree;
    private final ArrayDeque<StackNode<E>> stack;

    public SymmetricIterator(Tree<E> tree) {
        this.tree = tree;
        this.stack = new ArrayDeque();
        this.currentNode = null;
        this.nextNode = this.moveCursorToLastLeftNode((BinaryTreeNode)tree.getRoot());
        this.tree = tree;
    }

    public BinaryTreeNode<E> nextNode() {
        this.currentNode = this.nextNode;
        if (this.currentNode != null) {
            if (this.currentNode.getRight() != null) {
                StackNode<E> node = new StackNode(this.currentNode);
                node.incrementCount();
                node.incrementCount();
                this.stack.push(node);
                this.nextNode = this.moveCursorToLastLeftNode(this.currentNode.getRight());
            } else {
                this.nextNode = null;
                if (!this.stack.isEmpty()) {
                    boolean foundedNextNode = false;

                    while(!this.stack.isEmpty() && !foundedNextNode) {
                        StackNode<E> father = this.stack.pop();
                        if (father.getCount() == 1) {
                            foundedNextNode = true;
                            this.nextNode = father.getNode();
                        }
                    }
                }
            }
        }

        return this.currentNode;
    }

    public boolean hasNext() {
        return this.nextNode != null;
    }

    public E next() {
        E currentInfo = null;
        BinaryTreeNode<E> current = this.nextNode();
        if (current != null) {
            currentInfo = current.getInfo();
        }

        return currentInfo;
    }

    public void remove() {
        this.tree.deleteNode(this.currentNode);
    }

    private BinaryTreeNode<E> moveCursorToLastLeftNode(BinaryTreeNode<E> initialNode) {
        BinaryTreeNode<E> cursor = null;
        if (initialNode != null) {
            for(cursor = initialNode; cursor.getLeft() != null; cursor = cursor.getLeft()) {
                StackNode<E> node = new StackNode(cursor);
                node.incrementCount();
                this.stack.push(node);
            }
        }

        return cursor;
    }
}
