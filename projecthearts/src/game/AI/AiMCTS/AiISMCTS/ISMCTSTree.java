package game.AI.AiMCTS.AiISMCTS;

import java.util.Collections;
import java.util.Comparator;

/**
 * Represents a tree in the ISMCTS (Information Set Monte Carlo Tree Search) algorithm.
 * Each tree has a root node.
 */
public class ISMCTSTree {
    private ISMCTSNode root;

    // Getters & Setters
    public ISMCTSNode getRoot() {return root;}
    public void setRoot(ISMCTSNode root) {this.root = root;}

    /**
     * Constructs a new ISMCTSTree object.
     * 
     * @param root The root node.
     */
    public ISMCTSTree(ISMCTSNode root) {
        this.root = root;
    }

    /**
     * Selects the best child node of the root node, 
     * by getting the child node with the highest number of visits.
     * 
     * @return The best child node of the root node.
     */
    public ISMCTSNode selectBestNode() {
        return Collections.max(root.getChildren(),
                        Comparator.comparingDouble(ISMCTSNode::getVisits));
    }
    
    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ISMCTSTree: \n");
        sb.append(root.toString());
        
        for (ISMCTSNode child : root.getChildren()) {
            sb.append(child.toString());
        }
        return sb.toString();
    }
}
