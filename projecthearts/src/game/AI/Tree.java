package game.AI;

/**
 * Represents the tree structure in Monte Carlo Tree Search.
 * This class holds the root of the tree and provides basic operations
 * to access and modify the tree structure.
 */
public class Tree {
    private Node root;

    /**
     * Constructs a tree with a specified root node.
     *
     * @param root The root node of the tree.
     */
    public Tree(Node root) {
        this.root = root;
    }

    /**
     * Returns the root node of the tree.
     *
     * @return The root node.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Sets the root node of the tree.
     *
     * @param root The node to be set as the root.
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * Adds a child node to a specified parent node in the tree.
     * This method can be used to expand the tree during the MCTS process.
     *
     * @param parent The parent node to which the child will be added.
     * @param child The child node to be added to the parent.
     */
    public void addChild(Node parent, Node child) {
        if (parent != null) {
            parent.getChildren().add(child);
            child.setParent(parent);
        }
    }
}
