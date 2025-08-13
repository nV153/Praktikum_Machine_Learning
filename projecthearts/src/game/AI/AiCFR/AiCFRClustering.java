package game.AI.AiCFR;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import weka.clusterers.SimpleKMeans;
import game.Difficulty;
import game.GameController;
import game.GameState;
import game.Memory;
import game.AI.AiInterface;
import game.AI.AiTypes;
import game.Logger;
import game.AI.AiCFR.MCCFR.CFRNode;
import rules.Rules;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class AiCFRClustering implements AiInterface{

    private Logger logger;
    private int numTricks;
    private Instances datasetPlay;
    private Instances datasetPass;
    private SimpleKMeans kmeansPlay;
    private SimpleKMeans kmeansPass;
    private final String fileNamePass = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPass.json";
    private final String fileNamePlay = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPlay.json";
    
    private HashMap<String, double[][]> infoSetHashMapPlay;
    private HashMap<String, double[][]> infoSetHashMapPass;

    // model for easy and low difficulty
    private HashMap<String, double[][]> infoSetHashMapPlayLow;
    private SimpleKMeans kmeansPlayLow;
    private final String fileNamePlayLowDiff = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPlayLow.json";

    /**
    * Constructor for initializing the CFR clustering algorithm.
    * Loads information for pass and play modes and initializes cluster models and datasets for clustering.
    * If no stored InfoSets are available, empty HashMaps are initialized.
    */
    public AiCFRClustering(){
        
        // Loading cluster models and creating attribute lists for pass and play modes
        kmeansPass = loadClusterModel("pass");
        ArrayList<Attribute> attributesPass = KMeansClusterer.createATTPass();  
        datasetPass = new Instances("dataset", attributesPass, 0);
        
        kmeansPlay = loadClusterModel("play");
        ArrayList<Attribute> attributesPlay = KMeansClusterer.createATTPlay();
        datasetPlay = new Instances("dataset", attributesPlay, 0);

        kmeansPlayLow = loadClusterModel("playLow");
        datasetPlay = new Instances("dataset", attributesPlay, 0);
        
        // load InfoSets for the pass mode
        infoSetHashMapPass = CFRUtil.deserializeInfoSets(fileNamePass);
        if (infoSetHashMapPass == null){
            System.out.println("Keine InfoSets zum Modus Pass vorhanden.");
            infoSetHashMapPass = new HashMap<String, double[][]>();
        }

        // load InfoSets for the play mode
        infoSetHashMapPlay = CFRUtil.deserializeInfoSets(fileNamePlay);
        if (infoSetHashMapPlay == null){
            System.out.println("Keine InfoSets zum Modus Play vorhanden.");
            infoSetHashMapPlay = new HashMap<String, double[][]>();
        } 

        // load InfoSets for the low difficulty play mode
        infoSetHashMapPlayLow = CFRUtil.deserializeInfoSets(fileNamePlayLowDiff);
        if (infoSetHashMapPlayLow == null){
            System.out.println("Keine InfoSets zum Modus Play mit Schwierigkeitsgrad Easy/Low vorhanden.");
            infoSetHashMapPlayLow = new HashMap<String, double[][]>();
        } 

    }
    
    /**
    * Determines the best cards to pass for a given player based on the current game state and strategy.
    *
    * @param gameState   The current state of the game.
    * @param playerNo    The player number for whom the best cards to pass are determined.
    * @param receiverNo  The receiver's player number for passing direction determination.
    * @return            An array of integers representing the best cards to pass.
    */

    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {
        List<Integer> listOfHandCards = gameState.determineListOfPlayableCardIds(playerNo);
        int[] idxToPass = new int[3];

        try {   
            double[] values = new double[14]; 
            
            //direction to pass
            values[0] = Rules.getPassReceiverNo(0, gameState.getRoundNo());

            //handCards
            for (int i = 0; i < listOfHandCards.size(); i++) {
                values[CFRUtil.hCBeforePassingStartIdx + i] = listOfHandCards.get(i);
            }

            Instance newInstance = new DenseInstance(1.0, values); 
            newInstance.setDataset(datasetPass);
            int clusterNumber = kmeansPass.clusterInstance(newInstance);

            // strategy for the determined cluster
            double[] strat = infoSetHashMapPass.get(Integer.toString(clusterNumber))[1];

            idxToPass = CFRUtil.getCardsToPassFromStrategy(strat);
            int[] cardsToPass = new int[3];

            for (int i = 0; i < 3; i++) {
                cardsToPass[i] = listOfHandCards.get(idxToPass[i]);
            } 

            List<Integer> a = new ArrayList<>();
            for (int i= 0; i< cardsToPass.length;i++) {
                a.add(cardsToPass[i]);
            }
            List<Integer> temp = CFRUtil.similarCards(listOfHandCards, a);

            int[] aa = new int[3];
            aa[0] = temp.get(0);
            aa[1] = temp.get(1);
            aa[2] = temp.get(2);
                     
            return aa;
        }

        catch (Exception e){
            System.out.println("Exception in bestPass.");
            e.printStackTrace();
        }

        return idxToPass; 
    }

    /**
    * Determines the best move for a player based on the current game state and strategy.
    *
    * @param state      The current state of the game.
    * @param playerNo   The player number for whom the best move is determined.
    * @return           The ID of the best move determined by the strategy.
    */
    @Override
    public int bestMove(GameState state, int playerNo) {
        try{
            List<Integer> playableCards = state.determineListOfPlayableCardIds(playerNo);

            if (playableCards.size() == 1){
                return playableCards.get(0);
            }

            double[] values = new double[21];
            
            // tricks played
            values[CFRUtil.NUM_TRICKS_IDX] = state.getTricksPlayed();
        
            // handcards playable
            for (int i = 0; i < playableCards.size(); i++) {
                values[CFRUtil.ACTION_START_IDX + i] = playableCards.get(i);
            }
            for (int i = 0; i < 13; i++) {
                if (values[CFRUtil.ACTION_START_IDX + i] == 0) {
                    values[CFRUtil.ACTION_START_IDX + i] = -100.0;
                }
            }

            // starter of the trick
            values[CFRUtil.STARTER_IDX] = state.getStarterOfNewestTrick();

            // played cards of the current trick
            List<Integer> trickCards = state.getNewestTrick().getListOfCardsIdsPlayed();
            for (int i = 0; i < 4; i++){
                if (i >= trickCards.size()) values[CFRUtil.TRICK_START_IDX + i] = -100.0;
                else values[CFRUtil.TRICK_START_IDX + i] = trickCards.get(i);
            }

            // apply the given memory level
            List<Integer> cardsToConsider = state.getCardsPlayedWithMemory(playerNo);
            // Queen of Spades played yet
            values[CFRUtil.QUEEN_IDX] = CFRUtil.getQueenOfSpadesPlayedWithMemory(cardsToConsider);

            // number of heart cards played yet
            values[CFRUtil.HEARTS_IDX] = CFRUtil.getNumHeartsPlayedWithMemory(cardsToConsider);

            Instance newInstance = new DenseInstance(1.0, values);
            newInstance.setDataset(datasetPlay);
            
            // strategy of the determined cluster
            int clusterNumber;
            double[] strat;
            Difficulty diff = state.getDifficultyOfPlayer(playerNo);
            
            // in lower difficulty levels, a modell with lower cluster centroids is selected
            if (diff.equals(Difficulty.EASY) || diff.equals(Difficulty.MEDIUM)){
                clusterNumber = kmeansPlayLow.clusterInstance(newInstance);
                strat = infoSetHashMapPlayLow.get(Integer.toString(clusterNumber))[1];
            }
            // in higher difficulty levels, a modell with the regular number of cluster centroids is selected
            else{
                clusterNumber = kmeansPlay.clusterInstance(newInstance);
                strat = infoSetHashMapPlay.get(Integer.toString(clusterNumber))[1];
            }

            double[] stratWithDiff = new double[strat.length];
            if (diff.equals(Difficulty.EASY) || diff.equals(Difficulty.HARD)){
                // change the selected strategy -> with Difficulty.HARD, the upper 25% of strategy values get multiplied by 0.1
                stratWithDiff = CFRUtil.calcStrategyWithDifficulty(Arrays.copyOf(strat, strat.length), Difficulty.HARD);
            }
            else stratWithDiff = strat;
            
            int cardpos = CFRUtil.getCardToMoveFromStrategy(stratWithDiff);     
            int cardNo = -1;


            Instance clusterCenter = kmeansPlay.getClusterCentroids().instance(clusterNumber);

            int Ctp = (int) clusterCenter.value(cardpos+1);

            if(playableCards.contains(Ctp)){
                return Ctp;
            }else{
                cardNo = CFRUtil.similiarCard(playableCards,Ctp);
            }

            return cardNo;
            
        }catch (Exception e){
            System.out.println("Exception in bestmove!");
            e.printStackTrace();
        }

        return 0;
    } 

    public void trainCFR(int numberOfGames , int numberOfIterations){
        
        try {
            int iterationsPerformed = 0;
            while (iterationsPerformed < numberOfIterations){
                
                simulateGames(-1, numberOfGames, Difficulty.EXPERT, Memory.HIGH, Difficulty.EXPERT, Memory.HIGH);
                iterateCFROS();
                System.out.println("Iteration Nr. " + iterationsPerformed + " abgeschlossen.");
                iterationsPerformed++;
            }

            CFRUtil.calcAverageRegret(infoSetHashMapPass, iterationsPerformed);
            CFRUtil.calcAverageRegret(infoSetHashMapPlay, iterationsPerformed);
            CFRUtil.calcAverageRegret(infoSetHashMapPlayLow, iterationsPerformed);

            CFRUtil.serializeInfoSets(infoSetHashMapPass, fileNamePass);
            CFRUtil.serializeInfoSets(infoSetHashMapPlay, fileNamePlay);
            CFRUtil.serializeInfoSets(infoSetHashMapPlayLow, fileNamePlayLowDiff);
            
        } catch (Exception e) {
            System.out.println("Fehler bei trainCFR");
            e.printStackTrace();
        } 
    }

    /**
    * Iterates through the CFR outcome sampling process to update strategy regrets and average strategies
    * based on the simulation data collected in the logger. In one iteration, the entire generated 
    * data set of the previously simulated games is run through, whereby the game tree of one round 
    * is run through for each player in turn.  
    */
    private void iterateCFROS(){

        int size = logger.getTricksPlayed().size();
        int roundCounter = 0;
        int lineCounter = 0;
        int passLineCounter = 0;
        numTricks = logger.getDeckSize() / 4;
        ArrayList<double[]> payoffValues = logger.getPayoffAtTerminalState();
        
        // go through every line of the new data
        while (lineCounter < size) {
            
            // next player after one round is finished
            for (int playerNo = 0; playerNo < 4; playerNo++){

                double payoff = payoffValues.get(roundCounter)[playerNo];
                
                List<Integer> actions = CFRUtil.getHandCardsBeforePassing(logger, playerNo, passLineCounter);
                boolean isNodeForPassing = false;
                if (getReceiverNo(passLineCounter, playerNo) != -1) isNodeForPassing = true;
                
                //round with pass -> traverse pass and play tree
                if (isNodeForPassing){
                    CFRNode rootPass = new CFRNode(isNodeForPassing, playerNo, -1, null, actions);
                    traverseGameTreePass(rootPass, playerNo, passLineCounter, 1.0, 1.0, 1.0, payoff);

                    List<Integer> actionsPlay = getListOfPlayableCards(playerNo, lineCounter);
                    CFRNode rootPlay = new CFRNode(false, playerNo, -1, null, actionsPlay);
                    traverseGameTreePlay(rootPlay, playerNo, lineCounter,  1.0, 1.0, 1.0, payoff, Difficulty.EXPERT);
                    CFRNode rootPlayLow = new CFRNode(isNodeForPassing, playerNo, -1, null, actions);
                    traverseGameTreePlay(rootPlayLow, playerNo, lineCounter,  1.0, 1.0, 1.0, payoff, Difficulty.EASY);

                }
                
                //round without pass -> only traverse play tree
                else {
                    CFRNode root = new CFRNode(isNodeForPassing, playerNo, -1, null, actions);
                    traverseGameTreePlay(root, playerNo, lineCounter,  1.0, 1.0, 1.0, payoff, Difficulty.EXPERT);
                    CFRNode rootLow = new CFRNode(isNodeForPassing, playerNo, -1, null, actions);
                    traverseGameTreePlay(rootLow, playerNo, lineCounter,  1.0, 1.0, 1.0, payoff, Difficulty.EASY);
                }
                
                passLineCounter++;
                lineCounter += numTricks;
                if (lineCounter >= size) break;
            }
        }
    }
 
    /**
    * Traverses the game tree recursively to compute the counterfactual values and update strategy regrets
    * for the player during the playing phase of the game using outcome sampling.
    *
    * @param node          The current CFRNode representing the game state.
    * @param playerNo      The player number for whom counterfactual values and strategy updates are computed.
    * @param counter       The counter indicating the current trick in the playing phase.
    * @param prob          The probability of reaching the current game state.
    * @param probOpp       The probability of the opponent reaching the current game state.
    * @param sampleReach   The probability of reaching the current game state in the sampled trajectory.
    * @param payoff        The payoff value at the current game state.
    * @param diff          Determines wether the model for high or for low difficulty is trained.
    * @return              The counterfactual value computed for the current game state.
    */
    private double traverseGameTreePlay(CFRNode node, int playerNo, int counter, double prob, double probOpp, double sampleReach, double payoff, Difficulty diff){
            
        if ((counter + 1) % numTricks == 0) {
            return CFRUtil.getRootValue(payoff);   
        } else {
    
            int currentPlayer = node.getPlayerNo();
            List<Integer> actions = node.getActions(); 
            int numActions = actions.size();
            
            // Assign values from the current line to a cluster center and retrieve strategy and regrets
            double[] values = CFRUtil.getValuesFromLogger(logger, "play", counter, -1, currentPlayer);
            Instance newInstance = new DenseInstance(1.0, values);     
            newInstance.setDataset(datasetPlay);
            int clusterNumber = -1;
            try {
                if (diff.equals(Difficulty.EXPERT)) clusterNumber = kmeansPlay.clusterInstance(newInstance);   
                else clusterNumber = kmeansPlayLow.clusterInstance(newInstance);          
            }
            catch (Exception e){
                System.out.println("Konnte keinem Cluster zugeordnet werden");
            }

            // get the information set
            double[][] infoSetValues;
            if (clusterNumber == -1) infoSetValues = CFRUtil.initInfoSetValues(numActions);
            else {
                if (diff.equals(Difficulty.EXPERT)) infoSetValues = infoSetHashMapPlay.get(Integer.toString(clusterNumber));
                else infoSetValues = infoSetHashMapPlayLow.get(Integer.toString(clusterNumber));
            }

            // counterfactual regret
            double value = 0;
            double childValue;
            double[] childValues = new double[numActions];
            
            // regret matching
            double[] sampleStrategy;
            double[] strategy = CFRUtil.calcCurrentStrategyWithRegularization(infoSetValues[0], numActions);
            
            // determine played card
            int actionIndex = getPlayedCardIndex(values, currentPlayer);

            // if player = traverser, the strategy is calculated proportionally depending on exploration
            if (node.getPlayerNo() == playerNo){
                sampleStrategy = CFRUtil.calcSampleStrategy(strategy, numActions);
            }
            else sampleStrategy = strategy;

            CFRNode child;                
                
            if (isTrickCompleted(counter, currentPlayer)) { 
                counter ++; 
                int starter = getStarter(counter);
                child = getChild(starter, counter, 0, node, actions.get(actionIndex));
            }
            else {  
                int nextPlayer = (currentPlayer + 1) % 4;
                child = getChild(nextPlayer, counter, node.getNumCardsPlayedInThisTrick() + 1, node, actions.get(actionIndex)); 
            }
            
            double newProb;
            double newProbOpp;
            if (playerNo == currentPlayer){
                newProb = prob*(strategy[actionIndex]);
                newProbOpp = probOpp;   
            }    
            else {
                newProb = prob;  
                newProbOpp = probOpp*strategy[actionIndex];    
            }
            double newSampleReach = sampleReach*sampleStrategy[actionIndex];
            childValue = traverseGameTreePlay(child, playerNo, counter, newProb, newProbOpp, newSampleReach, payoff, diff); 
            
            //baseline corrected child values according to Lanctot / Schmidt et. al 2019
            double baseline = 0.0;
            for (int i = 0; i < numActions; i++){
                if (i == actionIndex) childValues[i] = baseline + (childValue - baseline) / sampleStrategy[actionIndex];
                else childValues[i] = baseline;
                value += strategy[i] * childValues[i];
            }
             
            //update average strategie and regret 
            if (node.getPlayerNo() == playerNo){
                double factor = probOpp/sampleReach;
                double cv = value * factor;
                
                for (int i = 0; i < numActions; i++){
                    double cvi = childValues[i] * factor;   
                    infoSetValues[0][i] += cvi - cv; 
                    infoSetValues[1][i] += strategy[i] * prob/sampleReach;
                }
                infoSetValues[0] = CFRUtil.onlyPositiveRegret(infoSetValues[0]);
                infoSetValues[0] = CFRUtil.limitRegret(infoSetValues[0], CFRUtil.REGRET_LIMIT);
                infoSetValues[1] = CFRUtil.normalize(infoSetValues[1]);   
            }    
            return value;
        }    
    }

    /**
    * Traverses the game tree recursively to compute the counterfactual values and update strategy regrets
    * for the player during the passing phase of the game using outcome sampling.
    *
    * @param node              The current CFRNode representing the game state.
    * @param playerNo          The player number for whom counterfactual values and strategy updates are computed.
    * @param passLineCounter   The counter indicating the current line in csv / index of the logger.
    * @param prob              The probability of reaching the current game state.
    * @param probOpp           The probability of the opponent reaching the current game state.
    * @param sampleReach       The probability of reaching the current game state in the sampled trajectory.
    * @param payoff            The payoff value at the current game state.
    * @return                  The counterfactual value computed for the current game state.
    */
    private double traverseGameTreePass(CFRNode node, int playerNo, int passLineCounter, double prob, double probOpp, double sampleReach, double payoff){
            
        if (node.getNumCardsPassed() == 11) {
            return CFRUtil.getRootValue(payoff);   
        } else {
    
            int currentPlayer = node.getPlayerNo();
            List<Integer> actions = node.getActions(); 
            int numActions = actions.size();

            // Assign values from the current line to a cluster center and retrieve strategy and regrets
            double[] values = CFRUtil.getValuesFromLogger(logger, "pass", -1, passLineCounter, currentPlayer);
            Instance newInstance = new DenseInstance(1.0, values);     
            newInstance.setDataset(datasetPass);
            int clusterNumber = -1;
            try {
                clusterNumber = kmeansPass.clusterInstance(newInstance); 
            }
            catch (Exception e){
                System.out.println("Konnte keinem Cluster zugeordnet werden");
            }
            double[][] infoSetValues;
            if (clusterNumber == -1) infoSetValues = CFRUtil.initInfoSetValues(numActions);
            else infoSetValues = infoSetHashMapPass.get(Integer.toString(clusterNumber));
            
            // counterfactual value
            double value = 0;
            double childValue;
            double[] childValues = new double[numActions];
            
            // regret matching bestimmen
            double[] sampleStrategy;
            double[] strategy = CFRUtil.calcCurrentStrategyWithRegularization(infoSetValues[0], numActions);
            
            // determine passed card
            int actionIndex = getPassedCardIndex(node, passLineCounter);

            // if player = traverser, the strategy is calculated proportionally depending on exploration
            if (node.getPlayerNo() == playerNo){
                sampleStrategy = CFRUtil.calcSampleStrategy(strategy, numActions);
            }
            else sampleStrategy = strategy;

            int nextPlayer = (currentPlayer + 1) % 4;
            CFRNode child = getChildForPassing(nextPlayer, passLineCounter, node, actions.get(actionIndex));;                
                
            if (playerNo == currentPlayer){
                childValue = traverseGameTreePass(child, playerNo, passLineCounter, prob*strategy[actionIndex], probOpp, sampleReach*sampleStrategy[actionIndex], payoff);     
            }    
            else {
                childValue = traverseGameTreePass(child, playerNo, passLineCounter, prob, probOpp*strategy[actionIndex], sampleReach*sampleStrategy[actionIndex], payoff); 
            }

            // baseline corrected child values according to Lanctot / Schmidt et. al 2019
            double baseline = 0.0;
            for (int i = 0; i < numActions; i++){
                if (i == actionIndex) childValues[i] = baseline + (childValue - baseline) / sampleStrategy[actionIndex];
                else childValues[i] = baseline;
                value += strategy[i] * childValues[i];
            }
             
            // update regret and average strategy
            if (node.getPlayerNo() == playerNo){
                double cv = value*probOpp / sampleReach;  // estimated counterfactual value based on child values 
                for (int i = 0; i < numActions; i++){
                    double cvi = childValues[i] * probOpp / sampleReach;  // estimated counterfactual value of always choosing i
                    infoSetValues[0][i] += cvi - cv; 
                    infoSetValues[1][i] += (prob * strategy[i] / sampleReach);  
                }
                infoSetValues[0] = CFRUtil.onlyPositiveRegret(infoSetValues[0]);
                infoSetValues[0] = CFRUtil.limitRegret(infoSetValues[0], CFRUtil.REGRET_LIMIT);
                infoSetValues[1] = CFRUtil.normalize(infoSetValues[1]);  
            }    
            return value;
        }    
    }

    /**
    * Returns a child node representing the state after passing cards.
    */
    private CFRNode getChildForPassing(int playerNo, int counter, CFRNode state, int actionId){
        List<Integer> actions = CFRUtil.getHandCardsBeforePassing(logger, playerNo, counter);
        CFRNode child = new CFRNode(true, playerNo, actionId, state, actions);
        child.setNumCardsPassed(state.getNumCardsPassed() + 1);
        return child;
    }

    /**
    * Returns a child node representing the state after playing a card.
    */
    private CFRNode getChild(int playerNo, int counter, int numCardsPlayed, CFRNode state, int actionId){
        List<Integer> actions = getListOfPlayableCards(playerNo, counter);
        CFRNode child = new CFRNode(false, playerNo, actionId, state, actions);
        child.setNumCardsPlayedInThisTrick(numCardsPlayed);

        return child;
    }
 
    /**
    * Loads the cluster model based on the specified action. If no model exists for the specified action, 
    * a new model is generated based on the simulated number of games.
    */
    private SimpleKMeans loadClusterModel(String action){
        File file;
        if (action.equals("pass")){
            file = new File("projecthearts" + File.separator + "src" + File.separator + "game" + File.separator + "AI" + File.separator + "AiCFR" + File.separator + "Data" + File.separator + "passCluster.model");  
        }
        else if (action.equals("play")){
            file = new File("projecthearts" + File.separator + "src" + File.separator + "game" + File.separator + "AI" + File.separator + "AiCFR" + File.separator + "Data" + File.separator +"playCluster.model"); 
        }
        else if (action.equals("playLow")){
            file = new File("projecthearts" + File.separator + "src" + File.separator + "game" + File.separator + "AI" + File.separator + "AiCFR" + File.separator + "Data" + File.separator +"playLowCluster.model"); 
        }
        else throw new IllegalArgumentException("Kein gültiger Modus gewählt"); 
    
        SimpleKMeans kmeans;     
        if(!file.exists() ){
            simulateGames(-1, 1000, Difficulty.EASY, Memory.LOW, Difficulty.EASY, Memory.LOW);

            KMeansClusterer.initClusterModel(action, logger);   
            kmeans = KMeansClusterer.loadClusterModel(action); 
        }else{
            System.out.println("Model für " + action + " gefunden, lade.");
            kmeans = KMeansClusterer.loadClusterModel(action);    
        }
        return kmeans;
    }

    /**
    * Retrieves the list of playable cards for a given player at the specified entry of the logger.
    */
    private List<Integer> getListOfPlayableCards(int playerNo, int counter){
        List<Integer> actions = new ArrayList<>();
        String handCardsPlayable = "";

        switch (playerNo){
            case 0: handCardsPlayable = logger.getHandCardsPlayableP0().get(counter);
                    break;
            case 1: handCardsPlayable = logger.getHandCardsPlayableP1().get(counter);
                    break;
            case 2: handCardsPlayable = logger.getHandCardsPlayableP2().get(counter);
                    break;
            case 3: handCardsPlayable = logger.getHandCardsPlayableP3().get(counter);
                    break;
            default:
                    break;
        }
   
        String cleanedHCP = handCardsPlayable.replace("[", "").replace("]", "");
        String[] hcP = cleanedHCP.split("\\.");

        for (String action : hcP){
            if (!action.equals("-100")) actions.add(Integer.parseInt(action));
        }
        return actions;
    }

    /**
    * Retrieves the starter of the trick at the specified entry of the logger.
    */
    private int getStarter(int counter){
        return Integer.parseInt(logger.getStartingPlayer().get(counter));
    }

    /**
    * Checks if the current trick is completed by checking if the current player is the last player of the trick.
    */
    private boolean isTrickCompleted(int counter, int currentPlayer){
        if (currentPlayer == ((getStarter(counter) + 3) % 4)) return true;
        else return false;
    }
    
    /**
    * Retrieves the index of the played card by getting the value of the played card in the current trick 
    * and then comparing it with the playable hand cards at the beginning of the round.
    */
    private int getPlayedCardIndex(double[] values, int playerNo){
        int playedCardId = -1;
        int index = (playerNo - (int)values[CFRUtil.STARTER_IDX] + 4) % 4; //calculate relative position of played card in trick
        int playedCardValue = (int) values[CFRUtil.TRICK_START_IDX + index];
              
        // search handcards for the played card value
        for (int i = 0; i < 13; i++){
            if ((int) values[CFRUtil.ACTION_START_IDX + i] == playedCardValue) {
                playedCardId = i;
                break;
            }
        }
        return playedCardId;           
    }

    /**
    * Retrieves the index of the card passed by the player at a given round within the player's hand.
    */
    private int getPassedCardIndex(CFRNode node, int counter){
        int numCardsPassedOfPlayer = node.getNumCardsPassed() / 4;
        String[] passedCards = CFRUtil.getPassedCardsFromLogger(logger, node.getPlayerNo(), counter);
        int passedCard = Integer.parseInt(passedCards[numCardsPassedOfPlayer]);

        int index = -1;
        List<Integer> handcards = node.getActions();
        for (int i=0; i<handcards.size(); i++){
            if (handcards.get(i) == passedCard) return i; 
        }
        return index;
    }

    /* Returns the player number of the pass receiver, returns -1 in case of no passing (round No 4) */
    private int getReceiverNo(int counter, int playerNo){
        int dirPassing = Integer.parseInt(logger.getDirectionToPass().get(counter)); //0 = no pass, 1 = left, 2 = opposite, 3 = right
        if (dirPassing == 0) return -1;
        int receiver = (playerNo + dirPassing) % 4;
        return receiver;
    }
    
    /**
    * Simulates multiple games with specified configurations and generates new simulation data.
    *
    * @param playerNo      The player number for whom the game is simulated.
    * @param numberOfGames The number of games to be simulated.
    * @param diffFirstAi   The difficulty of the first AI.
    * @param memFirstAi    The memory of the first AI.
    * @param diffScndAi    The difficulty of the second AI.
    * @param memScndAi     The memory of the second AI.
    */
    public void simulateGames(int playerNo, int numberOfGames, Difficulty diffFirstAi, Memory memFirstAi, Difficulty diffScndAi, Memory memScndAi){
        GameState gameState = new GameState();
        gameState.init();
        gameState.createDeck(true);
        gameState.setDifficulty(diffFirstAi);
        gameState.setMemory(memFirstAi);
        gameState.setDifficulty4thAi(diffScndAi);
        gameState.setMemory4thAi(memScndAi);
        gameState.setIsGameWithPassing(true);

        GameController gameController = new GameController(gameState, AiTypes.RULE_BASED, AiTypes.RULE_BASED);
        gameController.startSimulationModeGame(numberOfGames, "data", playerNo);
        logger = gameController.getLogger();
    }

    /**
    * Retrieves the logger instance associated with this class.
    * 
    * @return The logger instance associated with this class.
    */
    public Logger getLog(){
        return logger;
    }
}