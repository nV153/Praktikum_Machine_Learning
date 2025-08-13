package game.AI.AiMCTS.AiMCTSBasic;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import game.Card;
import game.GameController;
import game.GameMode;
import game.GameState;
import game.Player;
import game.AI.AiInterface;
import game.AI.Node;
import game.AI.Tree;

/**
 * This class provides basic functionalities for MCTS-based AI.
 */
public abstract class AiMCTSBasic implements AiInterface{
    protected MCTSTree tree; //The game tree.
    protected MCTSNode root;
    protected int numberOfIterations;
    protected int expansionDepth;
    protected int maxNumberOfPossiblePasses;

    public AiMCTSBasic(){
        this.tree = null;
        this.root = null;
        this.numberOfIterations = 0;
        this.expansionDepth = 0;
        this.maxNumberOfPossiblePasses = 0;
    }
    
    /** 
     * This method determines the three cards to be passed at the start of a round for a gamestate, player and receiver.
     * @param gameState The gamestate.
     * @param playerNo The player's number.
     * @param receiverNo The receiver's number.
     * @return int[] The numbers of the three cards to be passed.
     */
    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo){

        setDifficultyForBestPass(gameState, playerNo);

        setMemoryForBestPass(gameState, playerNo);

        //Create a new game tree.
        this.root = new MCTSNode(gameState, 0);
        this.root.setPlayerNo(playerNo);
        this.root.setActionsPlayableCards(this.root.getGameState().determineListOfPlayableCards(this.root.getPlayerNo()));        
        this.tree = new MCTSTree(root);
        this.expansionDepth = 1;
        if (gameState.getDeckSize()==52){ 
            //The maximum of possible different passes is the binomial coefficient (13,3), 13 over 3 = 286.
            int binomialCoefficient = 286;
            this.maxNumberOfPossiblePasses = binomialCoefficient;            
        } else{ //Decksize 32
            //The maximum of possible different passes is the binomial coefficient (8,3), 8 over 3 = 56.
            int binomialCoefficient = 56;
            this.maxNumberOfPossiblePasses = binomialCoefficient;                       
        }
        
        //MCTS algorithm run from the root of the tree with return of the most visited child node.
        MCTSNode bestActionMCTSNode = this.runBestPassMCTS(this.tree);

        //Return the action from the most visited child node of the root, i.e. the action most frequently selected in the root (3 card numbers)
        int[] bestCardNumbersToPass = new int[3];
        bestCardNumbersToPass[0] = bestActionMCTSNode.getActionPassedCards()[0].getId();
        bestCardNumbersToPass[1] = bestActionMCTSNode.getActionPassedCards()[1].getId();
        bestCardNumbersToPass[2] = bestActionMCTSNode.getActionPassedCards()[2].getId();
        return bestCardNumbersToPass;
    }


    
    /** 
     * Method that runs the MCTS algorithm for bestPass from the root of the tree with return of the most visited child node.
     * @param tree The game tree.
     * @return MCTSNode The root's most visited child node.
     */
    protected MCTSNode runBestPassMCTS(MCTSTree tree) {

        long start = System.currentTimeMillis();
        long end = start + 5 * 1000; //End after 5 seconds.
        GameMode gameMode = tree.getRoot().getGameState().getGameMode();

        for (int i = 0; i < this.numberOfIterations; i++) {                
            MCTSNode selectedNode = selectionPhase((MCTSNode) tree.getRoot());
            MCTSNode expandedNode = expansionPhaseForBestPass(selectedNode);
            List<Double> gameResultList = simulationPhaseForBestPass(expandedNode, ((MCTSNode) tree.getRoot()).getPlayerNo());
            backpropagationPhase(expandedNode, gameResultList);

            //MCTS algorithm is aborted if 5 seconds are exceeded (only in human mode).
            //For performance improvement, the time query is only carried out every 100 iterations.
            if (gameMode == GameMode.HUMAN) {
                if (i % 100 == 0){
                    if(System.currentTimeMillis() > end){
                        break;
                    }
                }
            }

        }

        return tree.selectBestNode();
    }
    

    
    /** 
     * This method determines the card to move for a gamestate and player.
     * @param g The game state.
     * @param playerNo The player's number.
     * @return int The number of the card to move.
     */
    @Override
    public int bestMove(GameState g, int playerNo) {      
        
        setDifficultyForBestMove(g, playerNo);
        
        setMemoryForBestMove(g, playerNo);
        
        //Create a new game tree. 
        this.root = new MCTSNode(g, 0);
        this.root.setPlayerNo(playerNo);
        this.root.setActionsPlayableCards(this.root.getGameState().determineListOfPlayableCards(this.root.getPlayerNo()));        
        this.tree = new MCTSTree(root);
        this.maxNumberOfPossiblePasses = 0;
        //To reduce memory requirements, a fixed limit on the expansion depth can be selected here. 
        //Since expansion only takes place up to the end of the round and no memory problems have yet occurred, it is not explicitly required here.
        this.expansionDepth = Integer.MAX_VALUE;
        
        //MCTS algorithm run from the root of the tree with return of the most visited child node.
        MCTSNode bestActionMCTSNode = this.runBestMoveMCTS(this.tree);

       //Return the action from the most visited child node of the root, i.e. the action most frequently selected in the root (one card number)
        int bestCardNumberToMove = bestActionMCTSNode.getActionPlayedCard().getId();
        return bestCardNumberToMove;
    }

    
    /** 
     * Method that runs the MCTS algorithm for bestMove from the root of the tree with return of the most visited child node.
     * @param tree The game tree.
     * @return MCTSNode The root's most visited child node.
     */
    protected MCTSNode runBestMoveMCTS(MCTSTree tree) {

        long start = System.currentTimeMillis();
        long end = start + 5 * 1000; //End after 5 seconds.
        GameMode gameMode = tree.getRoot().getGameState().getGameMode();

        for (int i = 0; i < this.numberOfIterations; i++) {        
            MCTSNode selectedNode = selectionPhase((MCTSNode) tree.getRoot());
            MCTSNode expandedNode = expansionPhase(selectedNode);
            List<Double> gameResultList = simulationPhase(expandedNode, ((MCTSNode) tree.getRoot()).getPlayerNo());
            backpropagationPhase(expandedNode, gameResultList);

            //MCTS algorithm is aborted if 5 seconds are exceeded (only in human mode).
            //For performance improvement, the time query is only carried out every 100 iterations.
            if (gameMode == GameMode.HUMAN) {
                if (i % 100 == 0){
                    if(System.currentTimeMillis() > end){
                        break;
                    }
                }
            }

        }

        return tree.selectBestNode();
    }

    /** 
     * Method that sets the difficulty for bestMove.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    protected abstract void setDifficultyForBestMove(GameState gameState, int playerNo);

    /** 
     * Method that sets the difficulty for bestPass.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    protected abstract void setDifficultyForBestPass(GameState gameState, int playerNo);

    /** 
     * Method that sets the memory for bestMove.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    protected abstract void setMemoryForBestMove(GameState gameState, int playerNo);

    /** 
     * Method that sets the memory for bestPass.
     * @param gameState The game state.
     * @param playerNo The player's number.
     */
    protected abstract void setMemoryForBestPass(GameState gameState, int playerNo);

    
    /** 
     * Method that carries out the selection phase of the MCTS algorithm.
     * According to the slides of the intermediate presentation:
     * 1. Choose child nodes among those not yet visited in a uniformly distributed manner, i.e. at random.
     * 2. As soon as each child node is visited, select according to selection strategy, e.g. UCT or UCB1, depending on the subclass.
     * According to https://mcts.ai/about/index.html :
     * Starting at root node R, recursively select optimal child nodes until a leaf node L is reached.    
     * @param root The game tree's root.
     * @return MCTSNode The selected node.
     */
    protected MCTSNode selectionPhase(MCTSNode root) {   
        MCTSNode cur = root;
        while (!cur.isLeaf()) {
            //Determine child nodes not yet visited
            List<Node> notVisitedChildren = new LinkedList<Node>();
            for (Node childNode : cur.getChildren()){
                if (childNode.getVisits() == 0){
                    notVisitedChildren.add(childNode);
                }
            }
            if (notVisitedChildren.size() > 0){
                //1.: Equally distributed choice
                Random rng = new Random();
                int index = rng.nextInt(notVisitedChildren.size());
                cur = (MCTSNode) notVisitedChildren.get(index);
            } else{
                //2.: Choice according to selection strategy 
                // (after "Play each machine once", see literature https://link.springer.com/article/10.1023/A:1013689704352 and slides intermediate presentation
                // , i.e. after no more child nodes are unvisited, i.e. the else-branch here).
                cur = select(cur);
            }           
        }
        return cur;
    }


    /** 
     * Method that carries out the expansion phase of the MCTS algorithm for bestMove.  
     * @param selectedNode The selected node from the selection phase.
     * @return MCTSNode The expanded node.
     */
    protected abstract MCTSNode expansionPhase(MCTSNode selectedNode);

    /** 
     * Method that carries out the expansion phase of the MCTS algorithm for bestPass.  
     * @param selectedNode The selected node from the selection phase.
     * @return MCTSNode The expanded node.
     */
    protected abstract MCTSNode expansionPhaseForBestPass(MCTSNode selectedNode);

    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestMove.  
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove was called.
     * @return List<Double> The round's or game's result that is determined at the end of the simulation of the remaining round or game, respectively and dependent on the subclass.
     */
    protected abstract List<Double> simulationPhase(MCTSNode expandedNode, int rootPlayerNo);

    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestPass.  
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove was called.
     * @return List<Double> The round's or game's result that is determined at the end of the simulation of the remaining round or game, respectively and dependent on the subclass.
     */
    protected abstract List<Double> simulationPhaseForBestPass(MCTSNode expandedNode, int rootPlayerNo);
    
   /** 
    * Method that carries out the backpropagation phase of the MCTS algorithm.  
    * It walks up the path from the expandedNodeSimulated node to the root along the respective parent and increments visits and updates winScore and avgPayout.
    * According to https://mcts.ai/about/index.html : Update the current move sequence with the simulation result.
     * @param expandedNodeSimulated The expanded node from the expansion phase, from which the round or game, respectively, was simulated to its end.
     * @param gameResultList The round's or, respectively, game's result that was determined in the simulation phase.
     */  
    protected void backpropagationPhase(MCTSNode expandedNodeSimulated, List<Double> gameResultList) {        
        MCTSNode cur = expandedNodeSimulated;
        while (cur.getParent() != null){ //Parent is null only for the root.            
            cur.setVisits(cur.getVisits()+1);
            //cur.setWinScore(cur.getWinScore() + gameResultList.get(1).doubleValue());
            cur.setWinScore(cur.getWinScore() + gameResultList.get(2).doubleValue());
            double newAveragePayout = (((double) cur.getVisits()-1)/((double) cur.getVisits()))*cur.avgPayout
                                    + (((double) 1)/((double) cur.getVisits()))* gameResultList.get(0).doubleValue() ;            
            cur.setAvgPayout(newAveragePayout);
            cur = (MCTSNode) cur.getParent();
        }
        //cur is now the root and this must also be updated.
        cur.setVisits(cur.getVisits()+1);
        //cur.setWinScore(cur.getWinScore() + gameResultList.get(1).doubleValue());
        cur.setWinScore(cur.getWinScore() + gameResultList.get(2).doubleValue());
        double newAveragePayout = (((double) cur.getVisits()-1)/((double) cur.getVisits()))*cur.avgPayout
                                + (((double) 1)/((double) cur.getVisits()))* gameResultList.get(0).doubleValue();        
        cur.setAvgPayout(newAveragePayout);
    }   
    
    /** 
     * Method that selects the child node for a parent node according to a specific selection strategy, e.g. UCT or UCB1.   
     * @param parentNode The parent node.
     * @return MCTSNode The selected child node.
     */    
    protected abstract MCTSNode select(MCTSNode parentNode);

    
    
    /** 
     * Method that performs a one-time transition simulation from a gameState copy of the parent 
     * to the game state of the node, i.e. the node of the player whose turn it is next. 
     * @param gameStateCopyOfParent The game state copy of the parent node.
     * @param playedCard The played card, i.e. action, from the parent to the node.
     * @param playerNumberWhoPlayedCard The number of the player/opponent who has just played the card playedCard.
     * @param nextPlayerNoListResult The number of the next player/opponent is stored in List<Integer> nextPlayerNoListResult and returned via the reference.
     * @return GameState The determined game state of the player/opponent for the node, up to/before the move of the player/opponent or until the end of the round or the end of the game.
     */
    protected GameState determineNextGameStateOfNextPlayerOrNode(GameState gameStateCopyOfParent, Card playedCard, int playerNumberWhoPlayedCard,List<Integer> nextPlayerNoListResult){       
        //One could also work directly with the variable gameStateCopyOfParent in the following (call by reference), but "easier to read" with a different identifier.
        //Execute operations on nextGameState, i.e. change it.
        GameState nextGameState = gameStateCopyOfParent;
        //New game controller to have access to GameController methods:
        GameController gameControllerHelper = new GameController(nextGameState);
        //int variable to determine the next player/opponent or his number and to record it since it must also be returned/passed back later.
        int nextPlayerNo = -1;

        //Determine the card number selected by the player as card number 1-52.
        Player player = nextGameState.getPlayers()[playerNumberWhoPlayedCard];
        int cardNumberIdToMove = 0; //1-52
        cardNumberIdToMove = playedCard.getId();
        //Remove this card from the player's hand and add it to the trick.
        nextGameState.playCard(cardNumberIdToMove, player.getPlayerNo());
        //The player has played his card up to this point.
        //Check whether the trick must be completed and evaluated.
        if (nextGameState.getNewestTrick().getNumPlayedCards() == 4){ //Trick end.
            //Evaluate trick.
            //   - If heart has not yet been broken/played in current round, but has been played in this trick, then set isHeartBroken true.
            gameControllerHelper.updateHeartsBroken();
            //   - Then complete the trick: Determine the winner, count the points of the trick, credit them to the winner's round account and return the winner.
            Player currentTrickWinner = gameControllerHelper.completeTrick(); 
            //Trick evaluation finished.                       
            //Check whether the round is already over.
            if ((nextGameState.getDeckSize() == 52 && nextGameState.getTricksPlayed() == 13)
                || (nextGameState.getDeckSize() == 32 && nextGameState.getTricksPlayed() == 8)){                
            //  If yes, 
            //    then end the round, i.e. credit round points to game accounts.
                gameControllerHelper.completeRound(); 
                nextPlayerNo = -1; //The next player does not currently exist, as there is no expansion into a new round.
                //Since there is no expansion into the next round, the nextGameState is a terminal state at this point.
            } else{
            // Else, (round not yet finished)
            //   Set next trick number: Increase number of tricksPlayed by 1.
                nextGameState.setTricksPlayed(nextGameState.getTricksPlayed()+1);                
            //   Determine the starter of the new trick. The starter is the winner of the last trick.
                Player starterOfTrick = currentTrickWinner;
                nextPlayerNo = starterOfTrick.getPlayerNo();
            //   Initialize the new trick:
                nextGameState.initTrick(starterOfTrick.getPlayerNo());
            //      Determine playable hand cards for the starter according to the rules.
                nextGameState.setPlayableCardsForStarterInTrick2orLater(starterOfTrick);                               
            }
        } else{ //The trick is not yet finished. The next player must play his card.            
            //Determine the next player.
            nextPlayerNo = (player.getPlayerNo()+1) % 4;
            //For Trick 2 or Later: Set the playable hand cards for the next player according to the rules.
            // For Trick 1 or 0 (Passing) they have already been set in the above if statement for the next game state.
            if (nextGameState.getTricksPlayed() >= 2){
                nextGameState.setPlayableCardsForNonStarterInTrick2orLater(nextGameState.getPlayers()[nextPlayerNo], nextGameState.getNewestTrick().getStartingCard());
            }
        }
        //The next player has been selected.
        nextPlayerNoListResult.add(Integer.valueOf(nextPlayerNo));
        
        //Here the next game state is ready and can now be returned.
        return nextGameState;
    }

    /**
     * This inner class extends the Nodes class with attributes and methods that are specifically 
     * required for the MCTS implementations here.
     */
    public class MCTSNode extends Node{        
        private Card actionPlayedCard; //The card with coding Id 1-52, which was played (moved) in this node/game state by its corresponding player.
        private List<Card> actionsPlayableCards; //Later contains the cards that can be played (moved) in this game state, i.e. the possible actions.
        private int playerNo;
        private double avgPayout;
        private int depth;
        private Card[] actionPassedCards; //The cards with coding Id 1-52, which were played (passed) in this node/game state by its corresponding player.

        public MCTSNode(GameState g, int depth){
            super(g);
            setParent(null);
            setChildren(null);
            setWinScore(0);
            setVisits(0);
            this.actionPlayedCard = null;
            this.actionsPlayableCards = null;
            this.playerNo = -1;
            this.avgPayout = 0;
            this.depth = depth;
            this.actionPassedCards = null;
        }

        public Card getActionPlayedCard() {
            return actionPlayedCard;
        }

        protected void setActionPlayedCard(Card actionPlayedCard) {
            this.actionPlayedCard = actionPlayedCard;
        }

        public List<Card> getActionsPlayableCards() {
            return actionsPlayableCards;
        }

        protected void setActionsPlayableCards(List<Card> actionsPlayableCards) {
            this.actionsPlayableCards = actionsPlayableCards;
        }

        public int getPlayerNo() {
            return playerNo;
        }

        protected void setPlayerNo(int playerNo) {
            this.playerNo = playerNo;
        }

        public double getAvgPayout() {
            return avgPayout;
        }

        protected void setAvgPayout(double avgPayout) {
            this.avgPayout = avgPayout;
        }

        public int getDepth() {
            return depth;
        }

        protected void setDepth(int depth) {
            this.depth = depth;
        }

        protected Card[] getActionPassedCards() {
            return actionPassedCards;
        }

        public void setActionPassedCards(Card[] actionPassedCards) {
            this.actionPassedCards = actionPassedCards;
        }        

        /** 
         * Method that checks whether a node has no children set.
         * @return True, if the node has no children, i.e. its children attribute equals null. False, otherwise.
         */
        public boolean isLeaf() {
            return getChildren() == null;
        }

    }

    /**
     * This inner class extends the Tree class with (attributes and) methods that are specifically 
     * required for the MCTS implementations here.
     */
    public class MCTSTree extends Tree{
        
        public MCTSTree(MCTSNode root){
            super(root);
        }

        /** 
         * Method that selects for the node, for which it is called, its child node with the most visits of all children.
         * @return MCTSNode The child node with the most visits of all children.
         */
        public MCTSNode selectBestNode() {           
            return (MCTSNode) Collections.max(root.getChildren(),
                                Comparator.comparing(Node::getVisits));
        }

    }
    
}
