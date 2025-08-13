package game.AI.AiCFR.MCCFR;
import game.GameState;
import game.AI.AiInterface;
import game.Player;
import rules.Rules;
import game.AI.AiCFR.CFRUtil;
import game.Difficulty;

import java.util.HashMap;
import java.util.List;

/**
 * MCCFR algorithm. The strategy for "pass" is determined by outcome sampling, the strategy for "play" is determined by external sampling.
 */

public class AiMCCFR implements AiInterface{

    // Hash Maps containing regret and strategy of the information sets
    private HashMap<String, double[][]> infoSetsValues; 
    private HashMap<String, double[][]> infoSetsValuesPass;
    private final static int REGRET_IDX = 0;
    private final static int STRAT_IDX = 1;
    private String fileNamePlay = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\MCCFRInfoSetsPlay.json";
    private String fileNamePass = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\MCCFRInfoSetsPass.json";

    /**
     * Constructs an AiMCCFR object and loads the hash maps with the information sets for pass and play.
     */
    public AiMCCFR(){

        infoSetsValuesPass = CFRUtil.deserializeInfoSets(fileNamePass);
        if (infoSetsValuesPass == null){
            System.out.println("Keine InfoSets zum Modus Pass vorhanden.");
            infoSetsValuesPass = new HashMap<String, double[][]>();
        }

        infoSetsValues = CFRUtil.deserializeInfoSets(fileNamePlay);
        if (infoSetsValues == null){
            System.out.println("Keine InfoSets zum Modus Play vorhanden.");
            infoSetsValues = new HashMap<String, double[][]>();
        }
    }


    /**
    * Determines the best pass for the given player number on the current game state.
    *
    * @param gameState The current game state.
    * @param playerNo The player number for which the best pass shall be determined.
    * @param receiverNo The number representing the receiver of the passed cards.
    * @return The best pass determined by the average strategy of the corresponding information set.
    */
    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {
        
        List<Integer> actions = gameState.determineListOfPlayableCardIds(playerNo);
        Difficulty diff = gameState.getDifficultyOfPlayer(playerNo);

        // generate key of the current information set
        String key = actions.toString();
        double[][] infoSetValues;
        
        // retrieve average strategy of the information set
        infoSetValues = infoSetsValuesPass.get(key);
        if (infoSetValues == null){
            infoSetValues = CFRUtil.initInfoSetValues(actions.size());
            infoSetsValuesPass.put(key, infoSetValues);
        }
        double[] avStrat = infoSetValues[STRAT_IDX];

        // with difficulty medium or hard lower the chance for a specified amount of good cards
        double[] strategyDiff;
        if (diff == Difficulty.MEDIUM || diff == Difficulty.HARD){
            strategyDiff = CFRUtil.calcStrategyWithDifficulty(avStrat, diff);
        }
        // with difficulty easy get uniform strategy
        else if (diff == Difficulty.EASY){
            double[] regret = new double[13];
            strategyDiff = CFRUtil.calcCurrentStrategy(regret);
        }
        else strategyDiff = avStrat;

        // determine cards to pass
        int[] cardIdsToPass = CFRUtil.getCardsToPassFromStrategy(strategyDiff);
        int[] cardsToPass = new int[3];
        for (int i=0; i<3; i++){
            cardsToPass[i] = actions.get(cardIdsToPass[i]); 
        }
 
        return cardsToPass;
    }

    /**
    * Determines the best move for the AI based on the current game state.
    *
    * @param gameState The current game state.
    * @param playerNo The player number for which the best move shall be determined.
    * @return The best move determined by the average strategy of the corresponding information set.
    */
    @Override
    public int bestMove(GameState gameState, int playerNo){
        
        Difficulty diff = gameState.getDifficultyOfPlayer(playerNo);
        List<Integer> actions = gameState.determineListOfPlayableCardIds(playerNo);

        // generate key of the current information set
        String key = generatePlayKey(actions, gameState);
        double[][] infoSetValues;
        
        // retrieve average strategy of the information set
        infoSetValues = infoSetsValues.get(key);
        if (infoSetValues == null){
            infoSetValues = CFRUtil.initInfoSetValues(actions.size());
            infoSetsValues.put(key, infoSetValues);
        }

        double[] avStrat = infoSetValues[STRAT_IDX];

        // with difficulty medium or hard lower the chance for a specified amount of good cards
        double[] strategyDiff;
        if (diff == Difficulty.MEDIUM || diff == Difficulty.HARD){
            strategyDiff = CFRUtil.calcStrategyWithDifficulty(avStrat, diff);
        }
        // with difficulty easy get uniform strategy
        else if (diff == Difficulty.EASY){
            double[] regret = new double[13];
            strategyDiff = CFRUtil.calcCurrentStrategy(regret);
        }
        else strategyDiff = avStrat;
        
        // determine card to move
        int cardIndex = CFRUtil.getCardToMoveFromStrategy(strategyDiff);
        int cardToMove;
        
        try {
            cardToMove = actions.get(cardIndex);
        } catch (Exception e){
            int max = CFRUtil.findMaxProbabilityIndex(strategyDiff); 
            try {
                cardToMove = actions.get(max);                 
            } catch (Exception eee) {                    
                cardToMove = CFRUtil.closeCard(actions ,max);    // otherwise play closest Card to the best Card               
            }
        }

        return cardToMove;
    }

    

    /**
     * Trains MCCFR with a specified number of iterations for the mode play. External sampling is used.
     *
     * @param numberOfIterations The number of iterations for training MCCFR.
     */
    public void trainMCCFR(int numberOfIterations){

        try {
            int iterationsPerformed = 0;
            while (iterationsPerformed < numberOfIterations){   
                iterateMCCFR();      
                iterationsPerformed++;
            }
            CFRUtil.calcAverageRegret(infoSetsValues, numberOfIterations);
            CFRUtil.serializeInfoSets(infoSetsValues, fileNamePlay);
            
        } catch (Exception e) {
            System.out.println("Exception in trainMCCFR");
            e.printStackTrace();
        } 
    }

    /**
    * Iterates over the MCCFR algorithm.
    * This method runs one iteration of the MCCFR algorithm for each player in the game.
    */
    public void iterateMCCFR(){
        
        GameState gameState = initNewGame(false);

        for (Player player : gameState.getPlayers()){
        
            // create a new copy of the current game state
            GameState newGameState = gameState.deepCopy();

            initNewRound(newGameState);

            // get root node for the current game state and traverse the game tree
            int starter = initNewTrickAndGetStarter(newGameState);
            List<Integer> actions = newGameState.determineListOfPlayableCardIds(starter);
            CFRNode root = new CFRNode(false, starter, -1, null, actions);   
            traverseTree(player.getPlayerNo(), root, 1.0, 1.0, newGameState);
    
        }   
    }   
    
    /**
    * Traverses the tree for play nodes using external sampling.
    *
    * @param playerNo         The number of the player.
    * @param stateNode        The current state node in the game tree.
    * @param prob             The probability of reaching the current state.
    * @param probOpp          The probability of the opponents reaching the current state.
    * @param currentGameState The current state of the game.
    * @return The root value of the utility in the terminal state.
    */
    private double traverseTree(int playerNo, CFRNode stateNode, double prob, double probOpp, GameState currentGameState){

        // return utility in terminal state
        if (currentGameState.isRoundOver()) return CFRUtil.getRootValue(currentGameState.getPayoff(playerNo));     
        else {
            
            int currentPlayer = stateNode.getPlayerNo();
            List<Integer> actions = stateNode.getActions(); 

            // key representing the history using domain specific knowledge
            String key = stateNode.getHistory() + "-" + currentGameState.getNewestTrick().getListOfCardsIdsPlayed().toString();
            double[][] infoSetValues = infoSetsValues.get(key);  
            
            // create new node if none exists yet
            if (infoSetValues == null){
                infoSetValues = CFRUtil.initInfoSetValues(actions.size());
                infoSetsValues.put(key, infoSetValues);
            }

            double value = 0; 
            double[] childValues = new double[actions.size()];
            double[] strategy;
            
            if (playerNo == currentPlayer){

                // calculate strategy using regret matching
                strategy = CFRUtil.calcCurrentStrategy(infoSetValues[REGRET_IDX]); 

                // if player = traverser walk the tree for every legal action
                for (int i=0; i < actions.size(); i++){
                
                    // create new gamestate copy and do move
                    GameState nextGameState = currentGameState.deepCopy();
                    nextGameState.doMove(actions.get(i));

                    CFRNode child = getChild(nextGameState, stateNode, actions.get(i));
                    
                    childValues[i] = traverseTree(playerNo, child, prob*strategy[i], probOpp, nextGameState);
        
                    value = value + strategy[i] * childValues[i];   //update counterfactual value               
                } 

                // update regret and average strategy
                for (int i = 0; i < actions.size(); i++){
                    infoSetValues[REGRET_IDX][i] += probOpp*(childValues[i] - value); 
                    infoSetValues[STRAT_IDX][i] += prob * strategy[i];    
                }
                infoSetValues[REGRET_IDX] = CFRUtil.onlyPositiveRegret(infoSetValues[0]);
                infoSetValues[REGRET_IDX] = CFRUtil.limitRegret(infoSetValues[0], CFRUtil.REGRET_LIMIT);
                infoSetValues[STRAT_IDX] = CFRUtil.normalize(infoSetValues[1]);

            }
            
            // Sample enemy move from average strategy
            else {
                GameState nextGameState = currentGameState.deepCopy();
                strategy = CFRUtil.calcCurrentStrategy(infoSetValues[0]);
                int randomIndex = CFRUtil.getCardToMoveFromStrategy(strategy);
                int sampledCardId = actions.get(randomIndex);
                nextGameState.doMove(sampledCardId);
                CFRNode child = getChild(nextGameState, stateNode, sampledCardId);

                childValues[randomIndex] = traverseTree(playerNo, child, prob, probOpp*strategy[randomIndex], nextGameState);
                value = value + strategy[randomIndex] * childValues[randomIndex];
            }

            return value;
        } 
    }

    /**
     * Trains MCCFR with a specified number of iterations for the mode pass. Outcome sampling is used.
     *
     * @param numberOfIterations The number of iterations for training MCCFR.
     */
    public void trainMCCFRPass(int numberOfIterations){

        try {

            int iterationsPerformed = 0;
            while (iterationsPerformed < numberOfIterations){
                
                iterateMCCFRPass();
                iterationsPerformed++;
            }

            CFRUtil.calcAverageRegret(infoSetsValues, numberOfIterations);
            CFRUtil.serializeInfoSets(infoSetsValuesPass, fileNamePass);
            
        } catch (Exception e) {
            System.out.println("Exception in trainMCCFRPass");
            e.printStackTrace();
        } 
    }

    /**
    * One MCCFR iteration for passing actions using outcome sampling.
    * This method performs one iteration of MCCFR for passing actions for all players in the game.
    */
    private void iterateMCCFRPass(){
        
        GameState gameState = initNewGame(true);

        for (Player player : gameState.getPlayers()){
        
            GameState newGameState = gameState.deepCopy();
            int passStarter = 0;
            
            initNewRound(newGameState);
    
            List<Integer> actions = newGameState.determineListOfPlayableCardIds(passStarter);
            CFRNode root = new CFRNode(true, passStarter, -1, null, actions);
              
            try{
                traverseTreeOSForPassing(player.getPlayerNo(), root, 1.0, 1.0, 1.0, newGameState);
            } catch (IllegalStateException e){
                e.printStackTrace();
            }

            passStarter = (passStarter + 1) % 4;
        } 
    }  
    
    /**
    * Traverses the game tree using external sampling. Regret and average strategy is only updated
    * for pass nodes.
    *
    * @param playerNo       The player number of the traverser.
    * @param stateNode      The current state node in the game tree.
    * @param prob           The probability of reaching the current state.
    * @param probOpp        The probability of the opponents reaching the current state.
    * @param sampleReach    The sample reach probability.
    * @param currentGameState The current state of the game.
    * @return The root value of the utility value.
    */
    private double traverseTreeOSForPassing(int playerNo, CFRNode stateNode, double prob, double probOpp, double sampleReach, GameState currentGameState){

        if (currentGameState.isRoundOver()) return CFRUtil.getRootValue(currentGameState.getPayoff(playerNo));   //if the gameState is terminal, return utility  
        else {
            
            int currentPlayer = stateNode.getPlayerNo();
            List<Integer> actions = stateNode.getActions(); 
            int numActions = actions.size();

            // get key and information set
            String key;
            double[][] infoSetValues;
            if (stateNode.isNodeForPassing()){
                key = stateNode.getHistory();
                infoSetValues = infoSetsValuesPass.get(key);
            }
            else {
                key = stateNode.getHistory() + "-" + currentGameState.getNewestTrick().getListOfCardsIdsPlayed().toString();
                infoSetValues = infoSetsValues.get(key);  
            }
            
            // create a new node if no information set existing yet
            if (infoSetValues == null){
                infoSetValues = CFRUtil.initInfoSetValues(numActions);
                if (stateNode.isNodeForPassing()){
                    infoSetsValuesPass.put(key, infoSetValues);
                }
                else infoSetsValues.put(key, infoSetValues);
            }

            double value = 0; 
            double childValue;
            double[] childValues = new double[numActions];
            double[] strategy;
            double[] sampleStrategy;
            
            // calculate the current strategy with regret matching and exploration factor according to 
            // Lanctot 2009 Monte Carlo Sampling for Regret Minimization in Extensive Games
            strategy = CFRUtil.calcCurrentStrategy(infoSetValues[REGRET_IDX]);  
            if (stateNode.getPlayerNo() == playerNo){
                sampleStrategy = CFRUtil.calcSampleStrategy(strategy, numActions);
            }
            else sampleStrategy = strategy;
         
            int randomIndex = CFRUtil.getCardToMoveFromStrategy(sampleStrategy); 
            CFRNode child;                
                 
            // Pass
            if (stateNode.isNodeForPassing()){ 
                
                // add the sampled card to the cards to pass
                int receiverNo = Rules.getPassReceiverNo(currentPlayer, currentGameState.getRoundNo());
                currentGameState.getPlayers()[currentPlayer].setReceiverNo(receiverNo);
                currentGameState.getPlayers()[currentPlayer].addCardToPass(actions.get(randomIndex));

                // check if passing is over
                int nextPlayer = (currentPlayer + 1) % 4;
                if (currentGameState.getPlayers()[nextPlayer].getCardsToPass().size() < 3){
                    child = getChildForPassing(nextPlayer, currentGameState, stateNode, actions.get(randomIndex));
                }
                // pass cards for all players
                else {
                    for (Player player : currentGameState.getPlayers()){
                        player.passCards(currentGameState.getPlayers()[player.getReceiverNo()]);   
                    }
                    // start playing
                    initNewTrickAndGetStarter(currentGameState);
                    child = getChild(currentGameState, stateNode, actions.get(randomIndex));
                }
            }
            
            // Play
            else { 
                currentGameState.doMove(actions.get(randomIndex));
                child = getChild(currentGameState, stateNode, actions.get(randomIndex));
            }
            if (playerNo == currentPlayer){

                childValue = traverseTreeOSForPassing(playerNo, child, prob*strategy[randomIndex], probOpp, sampleReach*sampleStrategy[randomIndex], currentGameState);     
            }    
            else {
                childValue = traverseTreeOSForPassing(playerNo, child, prob, probOpp*strategy[randomIndex], sampleReach*sampleStrategy[randomIndex], currentGameState); 
            }

            // baseline corrected child values, here baseline = 0
            // Schmid et al 2019: Variance Reduction in Monte Carlo Counterfactual Regret Minimization(VR-MCCFR) for Extensive Form Games using Baselines
            double baseline = 0.0;
            for (int i = 0; i < numActions; i++){
                if (i == randomIndex) childValues[i] = baseline + (childValue - baseline)  / sampleStrategy[randomIndex];
                else childValues[i] = baseline;
                value += strategy[i] * childValues[i];
            }
             
            // update strategy and regret for pass nodes
            if (stateNode.getPlayerNo() == playerNo && child.isNodeForPassing()){
                double factor = probOpp/sampleReach;
                double cv = value*factor;                   // estimated counterfactual value based on child values 
                for (int i = 0; i < numActions; i++){
                    double cvi = childValues[i] = childValues[i] * factor;
                    infoSetValues[REGRET_IDX][i] += cvi - cv;
                    infoSetValues[STRAT_IDX][i] += strategy[i] * (prob/sampleReach);   
                }

                infoSetValues[REGRET_IDX] = CFRUtil.onlyPositiveRegret(infoSetValues[0]);
                infoSetValues[REGRET_IDX] = CFRUtil.limitRegret(infoSetValues[0], CFRUtil.REGRET_LIMIT);
                infoSetValues[STRAT_IDX] = CFRUtil.normalize(infoSetValues[1]);      
            }    
            return value;
        } 
    }

   /**
    * Initializes a new game state.
    */
    private GameState initNewGame(boolean isGameWithPassing){
        GameState gameState = new GameState();
        gameState.init();
        gameState.setSimulationAI();
        gameState.createDeck(true);   
        gameState.setIsGameWithPassing(isGameWithPassing);
        
        return gameState;
    }

    /**
    * Initializes a new round.
    */
    public static void initNewRound(GameState gameState){
        gameState.addRoundTricksToGameRoundTricks(gameState.createAndSetNewRoundTricks()); 
        gameState.setTricksPlayed(0); 
        gameState.resetCardsPlayedInThisRound();
        gameState.resetRoundPoints(); 
        gameState.setIsHeartBroken(false); 
        gameState.setCardsHaveBeenPassed(false); 
        gameState.resetCardsPassed(); 
        gameState.resetHandCardsOfPlayers(); 
        gameState.setRoundNo(gameState.getRoundNo()+1); 
        gameState.shuffleDeck(); 
        gameState.handOutCards();
        if (gameState.getIsGameWithPassing() == true){  
            gameState.setRoundWithPassing(true);
            gameState.setPlayableCardsForEachPlayerInTrick0or1();
        }
    }

    /**
    * Initializes a new trick.
    */
    private int initNewTrickAndGetStarter(GameState gameState){
        gameState.setTricksPlayed(gameState.getTricksPlayed() + 1);
        int numberOfStarter;
                    
        if (gameState.isFirstTrick()) {
                        
            numberOfStarter = gameState.getStarterOfRound().getPlayerNo();            
            gameState.setPlayableCardsForEachPlayerInTrick0or1();          
            gameState.initTrick(numberOfStarter);
        }
        
        else {
            numberOfStarter = gameState.getStarterOfNewestTrick(); 
            gameState.setPlayableCardsForStarterInTrick2orLater(gameState.getPlayers()[numberOfStarter]);
        }

        return numberOfStarter;

    }

    /**
    * Creates a child node and sets the legal actions according to the current gamestate.
    */
    private CFRNode getChild(GameState gameState, CFRNode state, int actionId){
        int playerNo = gameState.getPlayerToMove();
        List<Integer> actions = gameState.determineListOfPlayableCardIds(playerNo);
        CFRNode child = new CFRNode(false, playerNo, actionId, state, actions);

        return child;
    }

    /**
    * Creates a child node for passing and sets the legal actions according to the current gamestate.
    * Additionally, the method removes the cards to pass, which have already been selected, from the legal actions.
    */
    private CFRNode getChildForPassing(int playerNo, GameState gameState, CFRNode state, int actionId){
        List<Integer> actions = gameState.determineListOfPlayableCardIds(playerNo);
       
        List<Integer> cardsToPass = gameState.getPlayers()[playerNo].getCardsToPass();
        if (!cardsToPass.isEmpty()){
            actions.removeIf(cardsToPass::contains);    
        }
        CFRNode child = new CFRNode(true, playerNo, actionId, state, actions);
        return child;
    }

    /**
    * Generates the key for the information set, which represents the current state of the game.
    * Considers the memory level when computing the history of the current state.
    */
    private String generatePlayKey(List<Integer> actions, GameState gameState){
        
        List<Integer> cardsToConsider = gameState.getCardsPlayedWithMemory(gameState.getPlayerToMove());
        int numHeartsPlayed = CFRUtil.getNumHeartsPlayedWithMemory(cardsToConsider);
        int queenPlayed = CFRUtil.getQueenOfSpadesPlayedWithMemory(cardsToConsider);
        
        String key = String.valueOf(numHeartsPlayed) + ":" + String.valueOf(queenPlayed) + ":" + actions.toString() + ":" + gameState.getNewestTrick().getListOfCardsIdsPlayed().toString();
        return key;
    }

}





