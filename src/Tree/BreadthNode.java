package Tree;

public class BreadthNode<E> {
    private BinaryTreeNode<E> node;
    private int level;

    public BreadthNode(BinaryTreeNode<E> node) {
        this.node = node;
        this.level = 0;
    }

    public BreadthNode(BinaryTreeNode<E> node, int fatherLevel) {
        this.node = node;
        this.level = fatherLevel + 1;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public BinaryTreeNode<E> getNode() {
        return this.node;
    }

    public void setNode(BinaryTreeNode<E> node) {
        this.node = node;
    }

    public E getInfo() {
        return this.node.getInfo();
    }
}
