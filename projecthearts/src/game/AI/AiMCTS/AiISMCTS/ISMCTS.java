package game.AI.AiMCTS.AiISMCTS;

import java.util.List;
import java.util.Random;

import game.Card;
import game.GameState;
import game.AI.AiInterface;

/**
 * This class represents a generic Information Set Monte Carlo Tree Search (ISMCTS).
 * It is an abstract class that is extended by the SO-ISMCTS and MO-ISMCTS classes.
 * The ISMCTS algorithm is a variant of the MCTS algorithm that works with information sets.
 * The ISMCTS algorithm is used to determine the best move or pass for a given game state.
 * Every ISMCTS algorithm consists of 4 phases: Selection, Expansion, Simulation and Backpropagation.
 * The Generic ISMCTS class contains the generic implementations for these phases.
 * The specific implementations for the game at hand are implemented in the SO-ISMCTS and MO-ISMCTS classes.
 */
public abstract class ISMCTS<T, R> implements AiInterface{
    protected static final int MAX_ALLOWED_TIME = 10000;

    protected static int iterations;
    protected GameState currentState;
    protected List<Card> possibleCardsToMove;
    protected List<int[]> possiblePassCombinations;
    protected Random random;
    protected int observer;
    protected double startInMillis;

    public ISMCTS() {
        random = new Random();
    }

    /**
     * Runs the ISMCTS algorithm for a given game state to determine the best move.
     * @param tree The ISMCTS tree to use.
     * @param originalState A copy of the current game state.
     * @return The best move.
     */
    protected abstract ISMCTSNode runISMCTS(R tree, GameState originalState);

    /**
     * Performs the selection phase of the ISMCTS algorithm.
     * @param currentNode The current node(s) of the tree(s).
     * @return The selected node(s).
     */
    protected abstract T selectionPhase(T currentNode);
    
    /**
     * Performs the expansion phase of the ISMCTS algorithm.
     * @param selectedNode The selected node(s).
     * @return The expanded node(s).
     */
    protected abstract T expansionPhase(T selectedNode);

    /**
     * Performs the simulation phase of the ISMCTS algorithm.
     * It plays random moves until a terminal state is reached.
     */
    protected void simulationPhase() {
        possibleCardsToMove = currentState.determineListOfPlayableCards(currentState.getPlayerToMove());
        
        while (!isTerminal()) {
            Card randomPlayableCard = possibleCardsToMove.get(random.nextInt(possibleCardsToMove.size()));
            playCard(randomPlayableCard);
        }
    }
    
    /**
     * Performs the backpropagation phase of the ISMCTS algorithm.
     * It updates the nodes from the expanded node to the root node.
     * @param expandedNode The expanded node.
     */
    protected void backpropagationPhase(ISMCTSNode expandedNode) {
        while (expandedNode != null) {
            expandedNode.update(currentState);
            expandedNode = expandedNode.getParent();
        }
    }

    /**
     * Plays a card in the current gamestate (deepCopy variant).
     * It updates the current game state and the list of possible cards to move.
     * @param card The card to play.
     */
    protected void playCard(Card card) {
        currentState.doMove(card.getId());
        possibleCardsToMove = currentState.determineListOfPlayableCards(currentState.getPlayerToMove());
    }

    /**
     * checks if the current state is terminal.
     * @return true if the current state is terminal, false otherwise.
     */
    protected boolean isTerminal(){
        return currentState.isRoundOver() && possibleCardsToMove.isEmpty();
    }
}


