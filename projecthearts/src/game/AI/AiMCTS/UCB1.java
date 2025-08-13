package game.AI.AiMCTS;

import java.util.Collections;
import java.util.Comparator;

import game.AI.AiMCTS.AiMCTSBasic.AiMCTSBasic.MCTSNode;

/**
 * Auxiliary class for calculating UCB1 values, which are used in the selection phase of some MCTS-based AIs.
 */
public class UCB1 {
    
    /**
     * Finds the best node to explore next using the UCB1 formula.
     * For UCB1, see:
     * https://link.springer.com/article/10.1023/A:1013689704352
     * Auer, Peter; Cesa-Bianchi, Nicolò; Fischer, Paul (2002). "Finite-time Analysis of the Multiarmed Bandit Problem". 
     * Machine Learning. 47 (2/3): 235–256. doi:10.1023/a:1013689704352
     * https://link.springer.com/content/pdf/10.1023/A:1013689704352.pdf
     * Figure 1
     * 
     * @param node The node whose children are considered for exploration.
     * @return The best child node based on UCB1 value.
     */
    public static MCTSNode findBestNodeWithUCB1(MCTSNode node) {
        return (MCTSNode) Collections.max(
            node.getChildren(),
            Comparator.comparing(child -> calculateUCB1Value(((MCTSNode) child).getAvgPayout(),child.getVisits(),node.getVisits())));
    }

    /**
     * Calculates the UCB1 value for a node.
     * 
     * @param avgRewardObtained The average reward ("payout") obtained from the node.
     * @param nodeVisit The number of visits to the node.
     * @param parentVisit The total number of visits to the parent node.
     * @return The UCB1 value of the node.
     */
    public static double calculateUCB1Value(double avgRewardObtained, int nodeVisit, int parentVisit) {
        return avgRewardObtained + Math.sqrt((2*Math.log(parentVisit)) / nodeVisit);     
    }

}
