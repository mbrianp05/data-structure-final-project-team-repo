package Tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BinaryTree<E> extends Tree<E> implements Serializable {
    private static final long serialVersionUID = 1L;

    public BinaryTree() {
    }

    public BinaryTree(TreeNode<E> root) {
        super(root);
    }

    public BinaryTree(BinaryTreeNode<E> root) {
        this.root = root;
    }

    protected int level(BinaryTreeNode<E> cursor) {
        if (cursor != null) {
            int levelLST = this.level(cursor.getLeft());
            int levelRST = this.level(cursor.getRight());
            return (levelLST >= levelRST ? levelLST : levelRST) + 1;
        } else {
            return -1;
        }
    }

    public int treeLevel() {
        int level = -1;
        if (this.root != null) {
            level = 0;
        }

        return level;
    }

    public int nodeLevel(TreeNode<E> node) {
        if (node != null) {
            return node.equals(this.root) ? 0 : this.nodeLevel(this.getFather((BinaryTreeNode)node)) + 1;
        } else {
            return -1;
        }
    }

    public E deleteNode(BinaryTreeNode<E> node) {
        if (node == null) {
            return null;
        } else {
            if (this.root != null && this.root.equals(node)) {
                this.root = null;
            } else {
                BinaryTreeNode<E> father = this.getFather(node);
                this.deleteNotRoot(node, father);
            }

            return node.getInfo();
        }
    }

    private void deleteNotRoot(BinaryTreeNode<E> node, BinaryTreeNode<E> father) {
        if (node != null && father != null) {
            if (father.getLeft() != null && father.getLeft().equals(node)) {
                father.setLeft(null);
            } else if (father.getRight() != null && father.getRight().equals(node)) {
                father.setRight(null);
            }
        }

    }

    public int nodeDegree(TreeNode<E> node) {
        int degree = 0;
        if (((BinaryTreeNode)node).getLeft() != null) {
            ++degree;
        }

        if (((BinaryTreeNode)node).getRight() != null) {
            ++degree;
        }

        return degree;
    }

    public BinaryTreeNode<E> getFather(BinaryTreeNode<E> node) {
        BinaryTreeNode<E> returnNode = null;
        if (node != null && !node.equals(this.root)) {
            PreorderIterator<E> iterator = this.preOrderIterator();
            boolean stop = false;

            while(iterator.hasNext() && !stop) {
                BinaryTreeNode<E> iterNode = iterator.nextNode();
                if (node.equals(iterNode.getLeft()) || node.equals(iterNode.getRight())) {
                    stop = true;
                    returnNode = iterNode;
                }
            }
        }

        return returnNode;
    }

    public List<TreeNode<E>> getLeaves() {
        List<TreeNode<E>> leavesList = new ArrayList();
        PreorderIterator<E> iterator = this.preOrderIterator();

        while(iterator.hasNext()) {
            BinaryTreeNode<E> node = iterator.nextNode();
            if (node.getLeft() == null && node.getRight() == null) {
                leavesList.add(node);
            }
        }

        return leavesList;
    }

    private void getNodeSubTree(BinaryTreeNode<E> root, BinaryTreeNode<E> node, BinaryTree<E> tree) {
        if (root != null && !root.equals(node)) {
            BinaryTreeNode<E> cursor = new BinaryTreeNode(root.getInfo());
            if (root.getLeft() != null && !root.getLeft().equals(node)) {
                this.getNodeSubTree(root.getLeft(), node, tree);
                cursor.setLeft((BinaryTreeNode)tree.getRoot());
            } else {
                cursor.setLeft(null);
            }

            if (root.getRight() != null && root.getRight().equals(node)) {
                this.getNodeSubTree(root.getRight(), node, tree);
                cursor.setRight((BinaryTreeNode)tree.getRoot());
            } else {
                cursor.setRight(null);
            }

            tree.setRoot(cursor);
        }

    }

    public List<BinaryTreeNode<E>> getSons(BinaryTreeNode<E> node) {
        List<BinaryTreeNode<E>> sons = new ArrayList();
        if (node != null) {
            if (node.getLeft() != null) {
                sons.add(node.getLeft());
            }

            if (node.getRight() != null) {
                sons.add(node.getRight());
            }
        }

        return sons;
    }

    public BinaryTree<E> getSubTree(BinaryTreeNode<E> node) {
        BinaryTree<E> tree = null;
        if (node != null) {
            PreorderIterator<E> iter = this.preOrderIterator();
            boolean found = false;

            while(iter.hasNext() && !found) {
                BinaryTreeNode<E> cursor = iter.nextNode();
                if (cursor.equals(node)) {
                    found = true;
                    BinaryTreeNode<E> newRoot = new BinaryTreeNode(node.getInfo());
                    this.buildSubTree(node, newRoot);
                    tree = new BinaryTree<E>(newRoot);
                }
            }
        }

        return tree;
    }

    private void buildSubTree(BinaryTreeNode<E> srcFather, BinaryTreeNode<E> newFather) {
        if (srcFather.getLeft() != null) {
            BinaryTreeNode<E> newLeft = new BinaryTreeNode(srcFather.getLeft().getInfo());
            newFather.setLeft(newLeft);
            this.buildSubTree(srcFather.getLeft(), newFather.getLeft());
        }

        if (srcFather.getRight() != null) {
            BinaryTreeNode<E> newRight = new BinaryTreeNode(srcFather.getRight().getInfo());
            newFather.setRight(newRight);
            this.buildSubTree(srcFather.getRight(), newFather.getRight());
        }

    }

    public boolean insertNode(BinaryTreeNode<E> node, char type, BinaryTreeNode<E> father) {
        boolean inserted = false;
        if (node != null) {
            if (type == 'R' && father == null) {
                if (this.isEmpty()) {
                    this.setRoot(node);
                } else {
                    node.setLeft((BinaryTreeNode)this.root);
                    this.setRoot(node);
                }

                inserted = true;
            } else {
                PreorderIterator<E> iterator = this.preOrderIterator();
                boolean existsFather = false;

                while(iterator.hasNext() && !existsFather) {
                    BinaryTreeNode<E> currentNode = iterator.nextNode();
                    if (currentNode.equals(father)) {
                        existsFather = true;
                    }
                }

                if (existsFather) {
                    if (type == 'L') {
                        node.setLeft(father.getLeft());
                        father.setLeft(node);
                    } else {
                        node.setRight(father.getRight());
                        father.setRight(node);
                    }

                    inserted = true;
                }
            }
        }

        return inserted;
    }

    public int totalNodes() {
        int count = 0;

        for(PreorderIterator<E> iterator = this.preOrderIterator(); iterator.hasNext(); ++count) {
            iterator.next();
        }

        return count;
    }

    public TreeNode<E> getRoot() {
        return this.root;
    }

    public PreorderIterator<E> preOrderIterator() {
        return new PreorderIterator(this);
    }

    public SymmetricIterator<E> symmetricIterator() {
        return new SymmetricIterator(this);
    }

    public PosOrderIterator<E> posOrderIterator() {
        return new PosOrderIterator(this);
    }

    public boolean nodeIsLeaf(TreeNode<E> node) {
        return ((BinaryTreeNode)node).getLeft() == null && ((BinaryTreeNode)node).getRight() == null;
    }

    public int treeHeight() {
        return this.level((BinaryTreeNode)this.root);
    }
}
