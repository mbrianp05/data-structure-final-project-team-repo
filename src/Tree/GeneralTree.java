package Tree;// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decomp
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class GeneralTree<E> extends Tree<E> implements Serializable {
    private static final long serialVersionUID = 1L;

    public GeneralTree() {
    }

    public GeneralTree(BinaryTreeNode<E> root) {
        super(root);
    }

    public GeneralTree(List<Tree<E>> trees) {
        if (trees != null && !trees.isEmpty()) {
            this.root = ((Tree)trees.get(0)).getRoot();
            if (trees.size() > 1) {
                BinaryTreeNode<E> convert = (BinaryTreeNode)this.root;

                for(int i = 0; i < trees.size(); ++i) {
                    BinaryTreeNode<E> treeRoot = (BinaryTreeNode)((Tree)trees.get(i)).getRoot();
                    convert.setRight(treeRoot);
                    convert = convert.getRight();
                }
            }
        }

    }

    public int totalNodes() {
        int count = 0;

        for(InDepthIterator<E> iterator = this.inDepthIterator(); iterator.hasNext(); ++count) {
            iterator.next();
        }

        return count;
    }

    public E deleteNode(BinaryTreeNode<E> node) {
        E info = null;
        if (node != null) {
            if (node.equals(this.root)) {
                this.root = null;
            } else {
                InDepthIterator<E> iterator = this.inDepthIterator();
                boolean foundedNode = false;

                while(iterator.hasNext() && !foundedNode) {
                    BinaryTreeNode<E> father = iterator.nextNode();
                    if (father.getLeft() != null) {
                        if (father.getLeft().equals(node)) {
                            foundedNode = true;
                            father.setLeft(node.getRight());
                        } else {
                            BinaryTreeNode<E> prev = father.getLeft();
                            BinaryTreeNode<E> cursor = prev.getRight();

                            while(cursor != null && !foundedNode) {
                                if (cursor.equals(node)) {
                                    foundedNode = true;
                                    prev.setRight(cursor.getRight());
                                } else {
                                    prev = cursor;
                                    cursor = cursor.getRight();
                                }
                            }
                        }
                    }
                }

                if (foundedNode) {
                    info = node.getInfo();
                }
            }
        }

        return info;
    }

    public BinaryTreeNode<E> getFather(BinaryTreeNode<E> node) {
        BinaryTreeNode<E> father = null;
        if (node != null && !this.isEmpty() || !this.root.equals(node)) {
            InDepthIterator<E> iterator = this.inDepthIterator();
            boolean foundedNode = false;

            while(iterator.hasNext() && !foundedNode) {
                BinaryTreeNode<E> cursor = iterator.nextNode();
                if (node.equals(cursor.getLeft())) {
                    father = cursor;
                    foundedNode = true;
                } else if (cursor.getLeft() != null) {
                    BinaryTreeNode<E> aux = cursor.getLeft();

                    while(aux.getRight() != null && !foundedNode) {
                        aux = aux.getRight();
                        if (aux.equals(node)) {
                            foundedNode = true;
                            father = cursor;
                        }
                    }
                }
            }
        }

        return father;
    }

    public List<TreeNode<E>> getLeaves() {
        ArrayList<TreeNode<E>> leavesList = new ArrayList();
        if (!this.isEmpty()) {
            InDepthIterator<E> iterator = this.inDepthIterator();

            while(iterator.hasNext()) {
                BinaryTreeNode<E> node = iterator.nextNode();
                if (node.getLeft() == null) {
                    leavesList.add(node);
                }
            }
        }

        return leavesList;
    }

    public List<BinaryTreeNode<E>> getSons(BinaryTreeNode<E> node) {
        List<BinaryTreeNode<E>> sonsList = new ArrayList();
        if (node != null && node.getLeft() != null) {
            sonsList.add(node.getLeft());
            if (node.getLeft().getRight() != null) {
                for(BinaryTreeNode<E> var3 = node.getLeft(); var3.getRight() != null; var3 = var3.getRight()) {
                    sonsList.add(var3.getRight());
                }
            }
        }

        return sonsList;
    }

    public List<E> getSonsInfo(BinaryTreeNode<E> node) {
        List<E> sonsInfoList = new ArrayList();
        if (node != null && node.getLeft() != null) {
            sonsInfoList.add(node.getLeft().getInfo());
            if (node.getLeft().getRight() != null) {
                for(BinaryTreeNode<E> var3 = node.getLeft(); var3.getRight() != null; var3 = var3.getRight()) {
                    sonsInfoList.add(var3.getRight().getInfo());
                }
            }
        }

        return sonsInfoList;
    }

    public boolean insertNode(BinaryTreeNode<E> node, BinaryTreeNode<E> father) {
        boolean inserted = false;
        if (node != null) {
            if (this.isEmpty()) {
                if (father == null) {
                    this.setRoot(node);
                    inserted = true;
                }
            } else if (father != null) {
                InDepthIterator<E> iterator = this.inDepthIterator();

                for(boolean stop = false; iterator.hasNext() && !stop; inserted = true) {
                    BinaryTreeNode<E> iterNode = iterator.nextNode();
                    if (iterNode.equals(father)) {
                        stop = true;
                        BinaryTreeNode<E> cursor = father.getLeft();
                        if (cursor == null) {
                            father.setLeft(node);
                        } else {
                            while(cursor.getRight() != null) {
                                cursor = cursor.getRight();
                            }

                            cursor.setRight(node);
                        }
                    }
                }
            } else {
                if (((BinaryTreeNode)this.root).getRight() == null) {
                    ((BinaryTreeNode)this.root).setRight(node);
                } else {
                    BinaryTreeNode<E> cursor;
                    for(cursor = ((BinaryTreeNode)this.root).getRight(); cursor.getRight() != null; cursor = cursor.getRight()) {
                    }

                    cursor.setRight(node);
                }

                inserted = true;
            }
        }

        return inserted;
    }

    public boolean insertAsFirstSon(BinaryTreeNode<E> node, BinaryTreeNode<E> father) {
        boolean founded = false;
        if (node != null && father != null) {
            InDepthIterator<E> iter = this.inDepthIterator();

            while(iter.hasNext() && !founded) {
                BinaryTreeNode<E> elem = iter.nextNode();
                if (father.equals(elem)) {
                    founded = true;
                    if (father.getLeft() == null) {
                        father.setLeft(node);
                    } else {
                        BinaryTreeNode<E> h = father.getLeft();
                        node.setRight(h);
                        father.setLeft(node);
                    }
                }
            }
        }

        return founded;
    }

    public int nodeLevel(TreeNode<E> node) {
        int level = -1;
        if (node != null) {
            if (node.equals(this.root)) {
                level = 0;
            } else {
                InBreadthIteratorWithLevels<E> iter = this.inBreadthIteratorWithLevels();
                boolean found = false;

                while(iter.hasNext() && !found) {
                    BreadthNode<E> cursor = iter.nextNodeWithLevel();
                    if (cursor.getNode().equals(node)) {
                        found = true;
                        level = cursor.getLevel();
                    }
                }
            }
        }

        return level;
    }

    public int treeLevel() {
        return this.nodeLevel(this.root);
    }

    public boolean nodeIsLeaf(TreeNode<E> node) {
        if (node != null) {
            return ((BinaryTreeNode)node).getLeft() == null;
        } else {
            return false;
        }
    }

    public int nodeDegree(TreeNode<E> node) {
        int degree = -1;
        if (node != null) {
            degree = 0;
            if (((BinaryTreeNode)node).getLeft() != null) {
                degree = 1 + this.rightBrotherCount(((BinaryTreeNode)node).getLeft());
            }
        }

        return degree;
    }

    private int rightBrotherCount(BinaryTreeNode<E> node) {
        int brother = 0;
        if (node.getRight() != null) {
            brother = 1 + this.rightBrotherCount(node.getRight());
        }

        return brother;
    }

    public InDepthIterator<E> inDepthIterator() {
        return new InDepthIterator(this);
    }

    public InBreadthIterator<E> inBreadthIterator() {
        return new InBreadthIterator(this);
    }

    public InBreadthIteratorWithLevels<E> inBreadthIteratorWithLevels() {
        return new InBreadthIteratorWithLevels(this);
    }

    public int treeHeight() {
        int height = -1;
        InBreadthIteratorWithLevels<E> iter = this.inBreadthIteratorWithLevels();

        BreadthNode<E> lastNode;
        for(lastNode = null; iter.hasNext(); lastNode = iter.nextNodeWithLevel()) {
        }

        if (lastNode != null) {
            height = lastNode.getLevel();
        }

        return height;
    }
}
