package game.AI;

import java.util.List;

import game.GameState;

/**
 * Represents a node in a Tree Search.
 * Each node stores the game state at that point, along with its relationship to other nodes in the tree.
 */
public class Node {
    private GameState gameState;
    private Node parent;
    private List<Node> children;
    private double winScore;
    private int visits;
    
    /**
     * Constructs a new Node with the specified game state.
     *
     * @param gameState The game state associated with this node.
     */
    public Node(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Returns the game state associated with this node.
     *
     * @return The current game state.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets the game state for this node.
     *
     * @param gameState The new game state to set.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Returns the parent of this node.
     *
     * @return The parent node, or null if this is the root node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node.
     *
     * @param parent The parent node to set.
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Returns the list of child nodes of this node.
     *
     * @return A list of child nodes.
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Sets the list of child nodes for this node.
     *
     * @param children The list of child nodes to set.
     */
    public void setChildren(List<Node> children) {
        this.children = children;
    }

    /**
     * Returns the number of wins recorded for this node.
     *
     * @return The number of wins.
     */
    public double getWinScore() {
        return winScore;
    }

    /**
     * Sets the number of wins for this node.
     *
     * @param wins The number of wins to set.
     */
    public void setWinScore(double wins) {
        this.winScore = wins;
    }

    /**
     * Returns the number of times this node has been visited.
     *
     * @return The number of visits.
     */
    public int getVisits() {
        return visits;
    }

    /**
     * Sets the number of times this node has been visited.
     *
     * @param visits The number of visits to set.
     */
    public void setVisits(int visits) {
        this.visits = visits;
    }

    // Method for checking if a node is terminal
    public boolean isTerminal() {
        return true;
    }

    // Method for checking if a node is fully extended
    public boolean isFullyExpanded() {
        return true;
    }

    // Method for selecting-strategier (e.g UCT)
    public Node selectChild() {
        return new Node(gameState);
    }
}
