package game.AI.AiMCTS.AiISMCTS;

import java.util.List;
import game.Card;
import game.Difficulty;
import game.GameMode;
import game.GameState;
import game.HandCards;
import game.AI.AiMCTS.UCT;

/**
 * This class represents a Single Observer Information Set Monte Carlo Tree Search (SO-ISMCTS).
 * It extends the generic ISMCTS class with specific implementations for the game at hand.
 * The SO-ISMCTS algorithm is a variant of the ISMCTS algorithm that works with a single tree (Observant).
 */
public class SO_ISMCTS extends ISMCTS<ISMCTSNode, ISMCTSTree>{

    public SO_ISMCTS() {}

    /**
     * {@inheritDoc}
     * @see ISMTCS#bestPass(GameState, int, int)
     */
    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {
        // Set observer and determine number of iterations
        observer = playerNo;
        iterations = Difficulty.determineNumberOfIterationsVariant2(gameState, observer);

        // Create root node and tree
        ISMCTSNode rootNode = new ISMCTSNode(observer, null, null);
        ISMCTSTree tree = new ISMCTSTree(rootNode);

        // Generate possible pass combinations
        HandCards handcards = gameState.getPlayers()[playerNo].getHandCards();
        possiblePassCombinations = handcards.generatePossiblePassCombinations();

        // Run ISMCTS pass and return the best pass
        int[] bestPass = runISMCTSPass(tree, gameState);
        return bestPass;
    }
    
    /**
     * Runs the ISMCTS algorithm for a given tree and game state to determine the best pass.
     * @param tree The ISMCTS tree to use.
     * @param originalState A copy of the current game state.
     * @return An array of integers representing the best pass.
     */
    protected int[] runISMCTSPass(ISMCTSTree tree, GameState originalState) {
        for (int i = 0; i < iterations; i++) {
            //Copy, Determinization & get possible moves
            currentState = originalState.deepCopy();
            currentState.determinization(observer);
            ISMCTSNode selectedNode = tree.getRoot();

            // ISMCTS 4 phases: Selection or Expansion, Simulation, Backpropagation
            if (!possiblePassCombinations.isEmpty()) {
                selectedNode = expansionPhaseForPass(selectedNode);
            } else {
                selectedNode = selectionPhaseForPass(selectedNode);
            }
            simulationPhase();
            backpropagationPhase(selectedNode);
        }
        return tree.selectBestNode().getPassedCards();
    }

    /**
     * {@inheritDoc}
     * @see ISMTCS#bestMove(GameState, int)
     */
    @Override
    public int bestMove(GameState gameState, int playerNo) {
        observer = playerNo;
        iterations = Difficulty.determineNumberOfIterationsVariant1(gameState, observer);
        startInMillis = System.currentTimeMillis();

        // Create root node and tree
        ISMCTSNode rootNode = new ISMCTSNode(observer, null, null);
        ISMCTSTree tree = new ISMCTSTree(rootNode);

        // Run ISMCTS and get the best node
        ISMCTSNode bestNode = runISMCTS(tree, gameState);
        return bestNode.getPlayedCard().getId();
    }
    
    /**
     * Runs the ISMCTS algorithm for a given tree and game state to determine the best move.
     * @param tree The ISMCTS tree to use.
     * @param originalState The original game state.
     * @return The best node (and therefore the best card/move to play).
     */
    protected ISMCTSNode runISMCTS(ISMCTSTree tree, GameState originalState) {
        for (int i = 0; i < iterations; i++) {
            if (originalState.getGameMode() == GameMode.HUMAN && System.currentTimeMillis() - startInMillis > MAX_ALLOWED_TIME) {
                break;
            }
            // Copy, Determinization & get possible moves
            currentState = originalState.deepCopy();
            currentState.determinization(observer);
            possibleCardsToMove = currentState.determineListOfPlayableCards(observer);
        
            // ISMCTS 4 phases: Selection, Expansion, Simulation, Backpropagation
            ISMCTSNode selectedNode = selectionPhase(tree.getRoot());
            ISMCTSNode expandedNode = expansionPhase(selectedNode);
            simulationPhase();
            backpropagationPhase(expandedNode);
        }
        return tree.selectBestNode();
    }
    
    /**
     * Performs the selection phase of the ISMCTS algorithm for a pass.
     * @param selectedNode The node selected in the previous phase.
     * @return The best node to select.
     */
    protected ISMCTSNode selectionPhaseForPass(ISMCTSNode selectedNode) {
        ISMCTSNode bestNode = UCT.findBestNodeWithUCTForPass(selectedNode);
        currentState.doPass(observer, bestNode.getPassedCards());
        return bestNode;
    }

    /**
     * Performs the selection phase of the ISMCTS algorithm.
     * @param currentNode The current node.
     * @return The best Node to select.
     */
    protected ISMCTSNode selectionPhase(ISMCTSNode currentNode) {
        while (!isTerminal() && currentNode.isFullyExpanded(possibleCardsToMove)) {
            currentNode = UCT.findBestNodeWithUCT(currentNode, possibleCardsToMove);
            playCard(currentNode.getPlayedCard());
        }
        return currentNode;
    }

    /**
     * Performs the expansion phase of the ISMCTS algorithm for a pass.
     * @param selectedNode The node selected in the previous phase.
     * @return The child-node added during the expansion phase.
     */
    protected ISMCTSNode expansionPhaseForPass(ISMCTSNode selectedNode) {
        int[] passCombination = possiblePassCombinations.remove(0);
        currentState.doPass(observer, passCombination);
        selectedNode = selectedNode.addChildForPass(passCombination, observer);
        return selectedNode;
    }

    /**
     * Performs the expansion phase of the ISMCTS algorithm.
     * @param selectedNode The node selected in the previous phase.
     * @return The child-node added during the expansion phase.
     */
    protected ISMCTSNode expansionPhase(ISMCTSNode selectedNode) {
        List<Card> untriedMoves = selectedNode.getUntriedMoves(possibleCardsToMove);
        
        if (!untriedMoves.isEmpty()) {
            Card randomPlayableCard = untriedMoves.get(random.nextInt(untriedMoves.size()));
            int currentPlayer = currentState.getPlayerToMove();
            playCard(randomPlayableCard);
            
            return selectedNode.addChild(randomPlayableCard, currentPlayer);
        }
        return selectedNode;
    }
}

