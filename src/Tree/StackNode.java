package Tree;

public class StackNode<E> {
    private final BinaryTreeNode<E> node;
    private int count;

    public StackNode(BinaryTreeNode<E> node) {
        this.node = node;
        this.count = 0;
    }

    public int getCount() {
        return this.count;
    }

    public void incrementCount() {
        ++this.count;
    }

    public BinaryTreeNode<E> getRight() {
        return this.node.getRight();
    }

    public BinaryTreeNode<E> getLeft() {
        return this.node.getLeft();
    }

    public BinaryTreeNode<E> getNode() {
        return this.node;
    }
}
