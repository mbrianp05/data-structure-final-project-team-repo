package Tree;

import java.util.ArrayDeque;

public class PreorderIterator<E> implements ITreeIterator<E> {
    private BinaryTreeNode<E> nextNode;
    private BinaryTreeNode<E> currentNode = null;
    private final Tree<E> tree;
    private final ArrayDeque<StackNode<E>> stack = new ArrayDeque();

    public PreorderIterator(Tree<E> tree) {
        this.nextNode = (BinaryTreeNode)tree.getRoot();
        this.tree = tree;
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
        if (this.currentNode != null) {
            this.tree.deleteNode(this.currentNode);
        }

    }

    public BinaryTreeNode<E> nextNode() {
        BinaryTreeNode<E> returnNode = this.nextNode;
        this.currentNode = this.nextNode;
        if (this.nextNode != null) {
            if (this.nextNode.getLeft() != null) {
                StackNode<E> newStackNode = new StackNode(this.nextNode);
                newStackNode.incrementCount();
                this.stack.push(newStackNode);
                this.nextNode = this.nextNode.getLeft();
            } else if (this.nextNode.getRight() != null) {
                StackNode<E> newStackNode = new StackNode(this.nextNode);
                newStackNode.incrementCount();
                this.stack.push(newStackNode);
                StackNode<E> node = this.stack.pop();
                node.incrementCount();
                this.stack.push(node);
                this.nextNode = this.nextNode.getRight();
            } else {
                boolean foundedNextNode = false;

                while(!this.stack.isEmpty() && !foundedNextNode) {
                    StackNode<E> father = this.stack.pop();
                    if (father.getRight() != null && father.getCount() == 1) {
                        foundedNextNode = true;
                        this.nextNode = father.getRight();
                        father.incrementCount();
                        this.stack.push(father);
                    }
                }

                if (!foundedNextNode) {
                    this.nextNode = null;
                }
            }
        }

        return returnNode;
    }
}
