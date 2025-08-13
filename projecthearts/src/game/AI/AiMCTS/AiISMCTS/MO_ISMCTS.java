package game.AI.AiMCTS.AiISMCTS;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import game.Card;
import game.Difficulty;
import game.GameMode;
import game.GameState;
import game.AI.AiMCTS.UCT;

/**
 * This class represents a Multiple Observer Information Set Monte Carlo Tree Search (MO-ISMCTS).
 * It extends the generic ISMCTS class with specific implementations for the game at hand.
 * The MO-ISMCTS algorithm is a variant of the ISMCTS algorithm that works with multiple trees (Multiple-Observer).
 */
public class MO_ISMCTS extends ISMCTS<Map <Integer, ISMCTSNode>, List<ISMCTSTree>> {

    public MO_ISMCTS() {}

    /**
     * {@inheritDoc}
     * @see ISMCTS#bestPass(GameState, int, int)
     */
    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {
        int [] bestPass = new int[3];
        ISMCTS soISMCTS = new SO_ISMCTS();
        bestPass = soISMCTS.bestPass(gameState, playerNo, receiverNo);

        return bestPass;
    }

    /**
     * {@inheritDoc}
     * @see ISMCTS#bestMove(GameState, int)
     */
    @Override
    public int bestMove(GameState gameState, int playertoMove) {
        observer = playertoMove;
        iterations = Difficulty.determineNumberOfIterationsVariant1(gameState, playertoMove);
        startInMillis = System.currentTimeMillis();

        List<ISMCTSTree> trees = createISMCTSTrees(4);
        ISMCTSNode bestNode = runISMCTS(trees, gameState);

        return bestNode.getPlayedCard().getId();
    }

    /**
     * Runs the ISMCTS algorithm for a given list of trees and game state to determine the best move.
     * @param trees The list of ISMCTS trees to use (Every Player = 1 Tree).
     * @param originalState The current Game State.
     * @return The best node.
     */
    @Override
    protected ISMCTSNode runISMCTS(List<ISMCTSTree> trees, GameState originalState) {
        for(int i = 0; i < iterations; i++) {
            if (originalState.getGameMode() == GameMode.HUMAN && System.currentTimeMillis() - startInMillis > MAX_ALLOWED_TIME) {
                break;
            }
            // Copy, Determinization & initialize nodes 
            currentState = originalState.deepCopy();
            currentState.determinization(observer);
            Map<Integer, ISMCTSNode> nodes = initializeNodes(trees);
        
            // ISMCTS 4 phases: Selection, Expansion, Simulation, Backpropagation
            Map<Integer, ISMCTSNode> selectedNodes = selectionPhase(nodes);
            Map<Integer, ISMCTSNode> expandedNodes = expansionPhase(selectedNodes);
            simulationPhase();
            backpropagateEveryTree(expandedNodes);
        }
        return trees.get(observer).selectBestNode();
    }
    
    /**
     * Performs the selection phase of the ISMCTS algorithm.
     * @param nodes The list of nodes from all trees.
     * @return The selected node.
     */
    @Override
    protected Map<Integer, ISMCTSNode> selectionPhase(Map<Integer, ISMCTSNode> nodes) {
        int playerToMove = currentState.getPlayerToMove();
        possibleCardsToMove = currentState.determineListOfPlayableCards(playerToMove);
        ISMCTSNode currentNode = nodes.get(playerToMove);
        
        if (!isTerminal() && currentNode.isFullyExpanded(possibleCardsToMove)) {
            currentNode = UCT.findBestNodeWithUCT(currentNode, possibleCardsToMove);
            Card card = currentNode.getPlayedCard();
            
            nodes = updateNodesWithChild(nodes, card);
            currentState.doMove(currentNode.getPlayedCard().getId());
            return selectionPhase(nodes);
        }
        return nodes;
    }

    /**
     * Performs the expansion phase of the ISMCTS algorithm.
     * @param nodes The list of nodes from all trees.
     * @return The expanded node.
     */
    @Override
    protected Map<Integer, ISMCTSNode> expansionPhase(Map<Integer, ISMCTSNode> nodes) {
        int playerToMove = currentState.getPlayerToMove();
        ISMCTSNode selectedNode = nodes.get(playerToMove);
        possibleCardsToMove = currentState.determineListOfPlayableCards(playerToMove);
        List<Card> untriedMoves = selectedNode.getUntriedMoves(possibleCardsToMove);
        
        if (!untriedMoves.isEmpty()) {
            Card randomPlayableCard = untriedMoves.get(random.nextInt(untriedMoves.size()));
            selectedNode = selectedNode.addChild(randomPlayableCard, playerToMove);
            
            nodes = updateNodesWithChild(nodes, randomPlayableCard);
            currentState.doMove(randomPlayableCard.getId());
        }
        return nodes;
    }

    /**
     * Performs the backpropagation phase of the ISMCTS algorithm for every tree.
     * @param nodes The list of nodes from all trees.
     */
    protected void backpropagateEveryTree(Map<Integer, ISMCTSNode> nodes) {
        for (Map.Entry<Integer, ISMCTSNode> entry : nodes.entrySet()) {
            ISMCTSNode node = entry.getValue();
            backpropagationPhase(node);
        }   
    }

    // Updates the given nodes by finding or creating a child node for each node using the given card.
    private Map<Integer, ISMCTSNode> updateNodesWithChild(Map<Integer, ISMCTSNode> nodes, Card card) {
        for (Map.Entry<Integer, ISMCTSNode> entry : nodes.entrySet()) {
            ISMCTSNode node = entry.getValue().findOrCreateChild(card, entry.getKey());
            nodes.replace(entry.getKey(), node);
        }
        return nodes;
    }

    // Creates a list of ISMCTS trees for the given number of players.
    private List<ISMCTSTree> createISMCTSTrees(int numberOfPlayers) {
        List<ISMCTSTree> trees = new ArrayList<>();

        for (int playerNr = 0; playerNr < numberOfPlayers; playerNr++) {
            ISMCTSNode rootNode = new ISMCTSNode(playerNr, null, null);
            ISMCTSTree tree = new ISMCTSTree(rootNode);
            trees.add(tree);
        }
        return trees;
    }

    // Initializes a map of nodes from the given list of ISMCTS trees.
    private Map<Integer, ISMCTSNode> initializeNodes(List<ISMCTSTree> trees) {
        Map<Integer, ISMCTSNode> nodes = new HashMap<>();
        
        for (ISMCTSTree tree : trees) {
            nodes.put(tree.getRoot().getPlayerNr(), tree.getRoot());
        }
        return nodes;
    }
}
