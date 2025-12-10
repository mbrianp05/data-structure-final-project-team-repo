package Tree;

import java.io.Serializable;

public class BinaryTreeNode<E> extends TreeNode<E> implements Serializable {
    private static final long serialVersionUID = 1L;
    protected BinaryTreeNode<E> left;
    protected BinaryTreeNode<E> right;

    public BinaryTreeNode() {
        this.info = null;
        this.left = null;
        this.right = null;
    }

    public BinaryTreeNode(E info) {
        this.info = info;
        this.left = null;
        this.right = null;
    }

    public E getInfo() {
        return this.info;
    }

    public void setInfo(E info) {
        this.info = info;
    }

    public BinaryTreeNode<E> getLeft() {
        return this.left;
    }

    public void setLeft(BinaryTreeNode<E> left) {
        this.left = left;
    }

    public BinaryTreeNode<E> getRight() {
        return this.right;
    }

    public void setRight(BinaryTreeNode<E> right) {
        this.right = right;
    }
}

