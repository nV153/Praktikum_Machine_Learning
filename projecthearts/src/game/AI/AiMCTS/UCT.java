package game.AI.AiMCTS;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.Card;
import game.AI.Node;
import game.AI.AiMCTS.AiISMCTS.ISMCTSNode;

/**
 * Utility class for calculating UCT (Upper Confidence Bound applied to Trees) values,
 * primarily used in the selection phase of Monte Carlo Tree Search (MCTS and ISMCTS) AI algorithms.
 */
public class UCT {

    private static final double EXPLORATION_PARAMETER = 1.5;

    /**
     * Calculates the UCT value for a node.
     *
     * @param parentVisit The total number of visits to the parent node.
     * @param nodeWinScore The win score of the node.
     * @param nodeVisit The number of visits to the node.
     * @return The UCT value of the node.
     */
    public static double calculateUCTValue(int parentVisit, double nodeWinScore, int nodeVisit) {
        return calculateWinScoreRatio(nodeWinScore, nodeVisit) 
            + ((EXPLORATION_PARAMETER) * calculateExplorationLogTerm(parentVisit, nodeVisit));
    }

    /**
     * Finds the best node to explore next using the UCT algorithm.
     *
     * @param node The node whose children are considered for exploration.
     * @return The best child node based on UCT value.
     */
    public static Node findBestNodeWithUCT(Node node) {
        int parentVisit = node.getVisits();
        return Collections.max(
            node.getChildren(),
            Comparator.comparing(child -> calculateUCTValue(parentVisit, child.getWinScore(), child.getVisits())));
    }

    /**
     * Finds the best ISMCTSNode to explore next using the UCT algorithm. The UCT value is calculated for each child node
     * of the given node that has a played card in the list of possible moves. The availability of each such child node is
     * incremented. The child node with the highest UCT value is selected as the best child.
     *
     * @param node The ISMCTSnode whose children are considered for exploration.
     * @param possibleMoves The list of cards that are possible moves.
     * @return The best child node based on UCT value. If no child node has a played card in the list of possible moves,
     *         returns null.
     */
    public static ISMCTSNode findBestNodeWithUCT(ISMCTSNode node, List<Card> possibleMoves) {
        ISMCTSNode bestChild = null;
        double maxUCTValue = Double.NEGATIVE_INFINITY;
    
        for (ISMCTSNode child : node.getChildren()) {
            if (possibleMoves.contains(child.getPlayedCard())) {
                child.incrementAvailability();
                double uctValue = calculateUCTValue(child.getAvailability(), child.getWinScore(), child.getVisits());
                if (uctValue > maxUCTValue) {
                    maxUCTValue = uctValue;
                    bestChild = child;
                }
            }
        }
        return bestChild;
    }

    /**
     * Finds the best ISMCTSNode to explore next using the UCT algorithm. The UCT value is calculated for each child node
     * of the given node. The child node with the highest UCT value is selected as the best child.
     * This Method is used for the pass phase of the ISMCTS algorithm.
     *
     * @param selectedNode The ISMCTSnode whose children are considered for exploration.
     * @return The best child node based on UCT value.
     */
    public static ISMCTSNode findBestNodeWithUCTForPass(ISMCTSNode selectedNode) {
        int parentVisit = selectedNode.getVisits();
        return Collections.max(
            selectedNode.getChildren(),
            Comparator.comparingDouble(child -> calculateUCTValue(parentVisit, child.getWinScore(), child.getVisits())));
    }

    private static double calculateWinScoreRatio(double nodeWinScore, double nodeVisit) {
        return nodeWinScore / nodeVisit;
    }

    private static double calculateExplorationLogTerm(double parentVisit, double nodeVisit) {
        return Math.sqrt(Math.log(parentVisit) / nodeVisit);
    }
}