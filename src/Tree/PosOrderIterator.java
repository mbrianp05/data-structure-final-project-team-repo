package Tree;

import java.util.ArrayDeque;

public class PosOrderIterator<E> implements ITreeIterator<E> {
    private StackNode<E> nextNode;
    private BinaryTreeNode<E> currentNode;
    private Tree<E> tree;
    private final ArrayDeque<StackNode<E>> stack;

    public PosOrderIterator(Tree<E> tree) {
        this.tree = tree;
        this.stack = new ArrayDeque();
        this.currentNode = null;
        this.nextNode = null;
        if (!tree.isEmpty()) {
            this.nextNode = new StackNode(this.moveCursorToLastLeftOrRightNode((BinaryTreeNode)tree.getRoot()));
        }

        this.tree = tree;
    }

    public BinaryTreeNode<E> nextNode() {
        this.currentNode = null;
        if (this.nextNode != null) {
            this.currentNode = this.nextNode.getNode();
            if (this.nextNode.getRight() != null && this.nextNode.getCount() != 2) {
                this.nextNode.incrementCount();
                this.nextNode.incrementCount();
                this.stack.push(this.nextNode);
                this.nextNode = new StackNode(this.moveCursorToLastLeftOrRightNode(this.nextNode.getRight()));
            } else {
                this.nextNode = null;
                if (!this.stack.isEmpty()) {
                    StackNode<E> father = this.stack.pop();
                    this.nextNode = father;
                    if (father.getCount() == 1 && father.getRight() != null) {
                        father.incrementCount();
                        this.stack.push(father);
                        this.nextNode = new StackNode(this.moveCursorToLastLeftOrRightNode(father.getRight()));
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

    private BinaryTreeNode<E> moveCursorToLastLeftOrRightNode(BinaryTreeNode<E> initialNode) {
        BinaryTreeNode<E> cursor;
        for(cursor = initialNode; cursor.getLeft() != null; cursor = cursor.getLeft()) {
            StackNode<E> node = new StackNode(cursor);
            node.incrementCount();
            this.stack.push(node);
        }

        if (cursor.getRight() != null) {
            StackNode<E> stackNode = new StackNode(cursor);
            stackNode.incrementCount();
            stackNode.incrementCount();
            this.stack.push(stackNode);
            cursor = this.moveCursorToLastLeftOrRightNode(cursor.getRight());
        }

        return cursor;
    }
}
