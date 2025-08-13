package game.AI.AiMCTS.AiMCTSBasic;

import game.Difficulty;
import game.GameState;
import game.AI.AiMCTS.UCB1;

/**
 * This AI class works with MCTS, where perfect information about the (actually unknown) playable cards 
 * in the opponents' hand is available in the expansion phase and 
 * UCB1 is used in the selection phase. The simulation phase lasts until the end of the round.
 * It can be referred to the following literature:
 * "Cheating UCT": https://eprints.whiterose.ac.uk/75048/1/CowlingPowleyWhitehouse2012.pdf S. 125/126 IV. C
 * Cowling, Peter I. orcid.org/0000-0003-1310-6683, Powley, Edward J. and Whitehouse,
 * Daniel (2012) Information Set Monte Carlo Tree Search. Computational Intelligence and AI
 * in Games, IEEE Transactions on. 6203567. pp. 120-143. ISSN 1943-068X 
 */
public class AiMCTSCheatingPIRoundSimUcb1 extends AiMCTSCheatingPIRoundSim {

    public AiMCTSCheatingPIRoundSimUcb1(){
        super();
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {
        return super.bestPass(gameState, playerNo, receiverNo);
    }
    
    /** 
     * Method that sets the difficulty for bestPass-MCTS as the number of iterations.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    @Override
    protected void setDifficultyForBestPass(GameState gameState, int playerNo) {
        this.numberOfIterations = Difficulty.determineNumberOfIterationsVariant2(gameState, playerNo);        
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    protected void setMemoryForBestPass(GameState gameState, int playerNo) {
        //Memory not applicable for bestPass.
        ; 
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public int bestMove(GameState g, int playerNo) {
        return super.bestMove(g, playerNo);
    }
    
    /** 
     * Method that sets the difficulty for bestMove-MCTS as the number of iterations.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    @Override
    protected void setDifficultyForBestMove(GameState gameState, int playerNo) {
        this.numberOfIterations = Difficulty.determineNumberOfIterationsVariant1(gameState, playerNo);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    protected void setMemoryForBestMove(GameState gameState, int playerNo) {
        //Memory is not applicable for bestMove of this Ai, as it is all-knowing.
        ;   
    }
    
    /**
     * Method that selects the child node for a parent node according to a specific selection strategy, UCB1 here.   
     * @param parentNode The parent node.
     * @return MCTSNode The selected child node.
     */ 
    @Override
    protected MCTSNode select(MCTSNode parentNode) {
        MCTSNode childNode = (MCTSNode) UCB1.findBestNodeWithUCB1(parentNode);
        return childNode;
    }
    
}
