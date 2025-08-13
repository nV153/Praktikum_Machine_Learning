package game.AI.AiCFR;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import game.Card;
import game.CardCoding;
import game.Difficulty;
import game.Logger;
import game.Suits;

public class CFRUtil {

    /* Indices in the double[] values-Array which is used for clustering */
    // values in mode play
    public static final int NUM_TRICKS_IDX = 0;
    public static final int ACTION_START_IDX = 1; 
    public static final int STARTER_IDX = 14; 
    public static final int TRICK_START_IDX = 15; 
    public static final int QUEEN_IDX = 19;
    public static final int HEARTS_IDX = 20;
    // values in mode pass
    public static int hCBeforePassingStartIdx = 1;

    public static final double REGRET_LIMIT = 1e20;  

    /**
    * Selects a card based on a given probability strategy.
    *
    * @param strategy An array of probabilities influencing card selection.
    * @return The ID of the selected card according to the strategy. Returns -1 if no card is selected.
    */
     public static int getCardToMoveFromStrategy(double[] strategy){
        int cardIndex = -1;
        double sumOfProbabilities = Arrays.stream(strategy).sum();
        Random rand = new Random();

        if (sumOfProbabilities == 0) return cardIndex;

        else {
            while (cardIndex == -1) {
                double p = rand.nextDouble() * sumOfProbabilities;
            
                double cumulativeProbability = 0.0;
                int i;
                for (i = 0; i < strategy.length; i++) {
                    cumulativeProbability += strategy[i];
                    if (p <= cumulativeProbability) {
                        return i;  
                    }
                }
            }    
        }
        return cardIndex;  
    }
    
    /**
    * Selects three cards to pass based on a given probability strategy.
    *
    * @param strategy An array of probabilities influencing card selection.
    * @return An array containing the IDs of the three selected cards according to the strategy.
    */
    public static int[] getCardsToPassFromStrategy(double[] strategy){
        Random rand = new Random();
        double sumOfProbabilities = Arrays.stream(strategy).sum();

        if (sumOfProbabilities == 0.0) {
            int[] result = new int[3];
            for (int i = 0; i < 3; i++) {
                result[i] = rand.nextInt(strategy.length);
            }
            return result;
        }

        HashSet<Integer> indices = new HashSet<>();
        int counter = 0;
        
        // go through loop max 10 times //TODO ggf. runtersetzen
        while (indices.size() < 3 && counter < 10) {
            double p = rand.nextDouble() * sumOfProbabilities;
            
            double cumulativeProbability = 0.0;
            int i;
            for (i = 0; i < strategy.length; i++) {
                cumulativeProbability += strategy[i];
                if (p <= cumulativeProbability && !indices.contains(i)) {
                    indices.add(i);
                    break;
                }
            }
            if (i == strategy.length) counter++;
        }

        if (indices.size() < 3){
            while (indices.size() < 3){
                indices.add(rand.nextInt(strategy.length)); //fill with random cards if not enough cards can get selected with strategy
            }  
        }

        int[] result = new int[3];
        int j = 0;
        for (Integer index : indices) {
            result[j++] = index;
        }
        return result;
    }

    /**
    * Calculates a modified strategy based on the difficulty level.
    * The method adjusts the probabilities of playing cards according to the specified difficulty.
    *
    * @param avStrat The original strategy represented as an array of doubles.
    * @param diff    The difficulty level, which can be Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD or Difficulty.EXPERT.
    * @return        A modified strategy array with adjusted probabilities based on the specified difficulty level.
    */
    public static double[] calcStrategyWithDifficulty(double[] avStrat, Difficulty diff){
        
        // do nothing in difficulty expert
        if (diff == Difficulty.EXPERT) return avStrat;

        int numCardsToConsider = 0;
        int numToLower = 0;
        for (double value : avStrat){
            if (value > 0.0) numCardsToConsider++;
        }

        if (numCardsToConsider == 1) return avStrat;
        else {
            // with difficulty medium only consider the lower half of the cards according to strategy
            if (diff == Difficulty.MEDIUM) numToLower = numCardsToConsider / 2;
            // with difficulty hard consider 75% of the strategy
            if (diff == Difficulty.HARD) numToLower = numCardsToConsider / 4;
        }

        if (numToLower > 0){
            double[] newStrat = Arrays.copyOf(avStrat, avStrat.length);

            int[] indicesToLower = new int[numToLower];

            // save strategy value with index
            Map<Integer, Double> indexValueMap = new HashMap<>();
            for (int i = 0; i < avStrat.length; i++) {
                if (avStrat[i] > 0.0) {
                    indexValueMap.put(i, avStrat[i]);
                }
            }

            // sort 
            List<Map.Entry<Integer, Double>> sortedEntries = new ArrayList<>(indexValueMap.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // find the indices to lower
            for (int i = 0; i < numToLower; i++) {
                indicesToLower[i] = sortedEntries.get(i).getKey();
            }

            // Lower chance of playing the best cards
            for (int index : indicesToLower) {
                newStrat[index] *= 0.1; 
            } 
            return newStrat; 
        }  

        return avStrat;
    }


    /**
    * Calculates the current strategy based on the given regrets using regret matching algorithm.
    *
    * @param regret An array representing the regrets for each action.
    * @return An array containing the current strategy based on the regrets.
    */
    public static double[] calcCurrentStrategy(double[] regret){
        double regretSum = 0.0;
        double[] strategy = new double[regret.length];
        
        for (int i = 0; i < regret.length; i++){ 
            strategy[i] = (regret[i] > 0) ? regret[i] : 0.0;     
            regretSum += strategy[i];   
        } 
        
        for (int i = 0; i < strategy.length; i++) {
            if (regretSum > 0){
                strategy[i] /= regretSum;
            }
            else {
                strategy[i] = 1.0 / regret.length;       
            }
        } 
        return strategy;      
    } 

    /**
    * Calculates the current strategy based on the given regrets using regret matching algorithm with regularization.
    *
    * @param regret An array representing the regrets for each action.
    * @param numActions The number of legal actions at the corresponding information set.
    * @return An array containing the current strategy based on the regrets with regularization.
    */
    public static double[] calcCurrentStrategyWithRegularization(double[] regret, int numActions) {
        double epsilon = 0.001;
        double controlFactor = 0.5; //controls the strenght of the regularization
        double regretSum = 0.0;
        double[] strategy = new double[regret.length];

        for (int i = 0; i < numActions; i++) {
            strategy[i] = Math.max(regret[i], 0.0) + epsilon;
            regretSum += strategy[i];
        }

        for (int i = 0; i < numActions; i++) {
            if (regretSum > 0) {    
            strategy[i] /= (regretSum + controlFactor * epsilon);  
            }
            else {
            strategy[i] = 1.0 / numActions;
            }
        }
        return strategy;
    }


    /**
    * Calculates a sample strategy based on the given strategy and exploration. 
    *
    * @param strategy The original strategy to be adjusted.
    * @param numActions The number of legal actions.
    * @return A sample strategy that balances exploration and exploitation.
    */
    public static double[] calcSampleStrategy(double[] strategy, int numActions){
        double exploration = 0.6;
        double[] sampleStrategy = new double[numActions];
        for (int i=0; i<numActions; i++){
            sampleStrategy[i] = exploration * (1.0 / numActions) + (1.0 - exploration) * strategy[i];  
        }
        return sampleStrategy;        
    }

    /**
    * Calculates the average regret based on the cumulative regret stored in a HashMap. This method iterates through the entries of the HashMap,
    * and divides each element of the first dimension (where the cummulative regret is stored) by the number of iterations
    * performed. 
    * @param infoSetHashMap The HashMap, which's regret values shall get averaged.
    * @param numIterations The number of iterations performed.
    */    
    public static void calcAverageRegret(HashMap<String, double[][]> infoSetHashMap, int numIterations){
        for (Map.Entry<String, double[][]> entry : infoSetHashMap.entrySet()){
            double[][] values = entry.getValue();
            for (int i = 0; i < values[0].length; i++){
                values[0][i] /= numIterations;
            }
        }
    }

    /**
    * Normalizes the average strategy stored in a HashMap. This method iterates through the entries of the HashMap,
    * and normalizes each element of the second dimension (where the average strategy is stored). Currently,
    * normalizing the average strategy is done after updating it, so this method is not used.
    * @param infoSetHashMap The HashMap, which's strategy values shall get normalized.
    */    
    public static void normalizeAverageStrat(HashMap<String, double[][]> infoSetHashMap){
        for (Map.Entry<String, double[][]> entry : infoSetHashMap.entrySet()){
            double sum = 0;
            double[][] values = entry.getValue();
            for (int i=0; i < values[1].length; i++){
                sum += values[1][i];
            }
            for (int i = 0; i < values[1].length; i++){
                values[1][i] /= sum;
            }
        }
    }
    

    /**
    * Initializes a 2D array of values for each action, including regret and strategy.
    *
    * @param numActions The number of actions for which values need to be initialized.
    * @return A 2D array of doubles where the first row represents regrets and the second row represents strategies.
    */
    public static double[][] initInfoSetValues(int numActions){
        double[][] values = new double[2][numActions];

        for (int i = 0; i<numActions; i++){
            values[0][i] = 0.0; //regret
            values[1][i] = 1.0 / numActions;
        }

        return values;
    }

    /**
    * Initializes a 2D array of values for each action, including regret and strategy, where a uniform strategy is applied.
    *
    * @param numActions The number of actions for which values need to be initialized.
    * @return A 2D array of doubles where the first row represents regrets and the second row represents strategies.
    */
    public static double[][] initClusterInfoSetValues(int numActions){
        double[][] values = new double[2][13];

        for (int i = 0; i<13; i++){
            values[0][i] = 0.0; //regret
            if(i < numActions) values[1][i] = 1.0 / numActions;  //strategy
            else values[1][i] = 0.0; 
        }
        return values;
    }

    /**
    * Serializes the information sets represented by a HashMap of String keys and double[][] values
    * into a JSON file with the specified file name.
    *
    * @param infoSets A HashMap containing information sets where each key is a String and each value
    *                 is a 2D array of doubles.
    * @param fileName The name of the JSON file to which the information sets will be serialized.
    */
    public static void serializeInfoSets(HashMap<String, double[][]> infoSets, String fileName) {
        
        try (JsonWriter writer = new JsonWriter(new FileWriter(fileName))) {
            writeHashMapToJson(infoSets, writer);
            System.out.println("InfoSets wurden erfolgreich serialisiert.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Writes a HashMap containing String keys and double[][] values to a JSON writer.
    *
    * @param map    A HashMap containing information sets where each key is a String and each value
    *               is a 2D array of doubles.
    * @param writer A JsonWriter object used to write JSON data.
    */
    private static void writeHashMapToJson(HashMap<String, double[][]> map, JsonWriter writer){
        try{
            writer.beginObject();
                for (String key : map.keySet()) {
                writer.name(key);
                writeDoubleArrayToJson(map.get(key), writer);
            }
            writer.endObject();
        }
        catch(Exception e){
            System.out.println("Fehler bei der Serialisierung");
        }
    }

    /**
    * Writes a 2D double array to a JSON writer.
    */
    private static void writeDoubleArrayToJson(double[][] array, JsonWriter writer){
        try{
            writer.beginArray();
        for (double[] innerArray : array) {
            writer.beginArray();
            for (double value : innerArray) {
                writer.value(value);
            }
            writer.endArray();
        }
        writer.endArray();
        }
        catch (IOException e){
            System.out.println("Fehler beim Schreiben eines double[][] arrays");
        }  
    }

    /**
    * Deserializes information sets from a JSON file and returns them as a HashMap.
    *
    * @param fileName The name of the JSON file containing the information sets.
    * @return A HashMap containing information sets where each key is a String and each value
    *         is a 2D array of doubles.
    */
    public static HashMap<String, double[][]> deserializeInfoSets(String fileName) {
        HashMap<String, double[][]> infoSets = new HashMap<>();
        try (JsonReader reader = new JsonReader(new FileReader(fileName))) {
            infoSets = readJsonToHashMap(reader);
            System.out.println("InfoSets deserialisiert");
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Fehler beim Deserialisieren der InfoSets.");
        }
        return infoSets;
    }

    /**
    * Reads JSON data from a JsonReader and constructs a HashMap of information sets.
    */
    private static HashMap<String, double[][]> readJsonToHashMap(JsonReader reader) throws IOException {
        HashMap<String, double[][]> map = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            double[][] array = readJsonToDoubleArray(reader);
            map.put(key, array);
        }
        reader.endObject();
        return map;
    }

    /**
    * Reads JSON data from a JsonReader and constructs a 2D double array.
    */
    private static double[][] readJsonToDoubleArray(JsonReader reader) throws IOException {
      
        reader.beginArray();
        List<double[]> rowsList = new ArrayList<>();
    
        while (reader.peek() != JsonToken.END_ARRAY) {
            reader.beginArray();
            List<Double> rowValues = new ArrayList<>();
        
            while (reader.peek() != JsonToken.END_ARRAY) {
                double value = reader.nextDouble();
                rowValues.add(value);
            }
            reader.endArray();
            double[] rowArray = new double[rowValues.size()];
            for (int i = 0; i < rowValues.size(); i++) {
                rowArray[i] = rowValues.get(i);
            }
            rowsList.add(rowArray);
        }

        reader.endArray();
        double[][] array = new double[rowsList.size()][];
        for (int i = 0; i < rowsList.size(); i++) {
            array[i] = rowsList.get(i);
        }
        return array;
    }

    /**
    * Extracts various values from a Logger object based on the provided action
    * and stores them in a double[] array.
    *
    * @param logger The Logger from which the values are to be retrieved.
    * @param action The action for which the values are to be retrieved ("play" or "pass").
    * @param counter The counter for the current trick.
    * @param passLineCounter The counter for the current round.
    * @param playerNo The number of the player for which the values shall get extracted.
    * @return A double[] array containing the extracted values.
    * @throws IllegalArgumentException if the provided action is neither "play" nor "pass".
    */
    public static double[] getValuesFromLogger(Logger logger, String action, int counter, int passLineCounter, int playerNo){

        int numAttributes = (action.equals("play")) ? 21 : 14;

        double[] values = new double[numAttributes];

        if (action.equals("play")){

            //current trick number 
            values[NUM_TRICKS_IDX] = Double.parseDouble(logger.getNumberOfTricksPlayed().get(counter)); 
            
            //handcards playable
            String[] handCardsPlayable = getHandCardsPlayableFromLogger(logger, playerNo, counter);
            for (int i = 0; i < handCardsPlayable.length; i++){
                values[i+ACTION_START_IDX] = Double.parseDouble(handCardsPlayable[i]);
            }

            //starter of the trick
            values[STARTER_IDX] = Double.parseDouble(logger.getNumberOfStarter().get(counter));

            //played cards of the current trick
            String[] trickCards = getTrickFromLogger(logger, counter);
            for (int i = 0; i < trickCards.length; i++){
                values[TRICK_START_IDX + i] = Double.parseDouble(trickCards[i]);
            }

            values[QUEEN_IDX] = getQueenOfSpadesPlayedYet(logger, counter) ? 1 : 0;
            values[HEARTS_IDX] = getNumPlayedHeartsYet(logger, counter);
        }

        else if (action.equals("pass")){

            //direction to pass
            values[0] = Double.parseDouble(logger.getDirectionToPass().get(passLineCounter));

            //handcards before passing
            List<Integer> handCards = getHandCardsBeforePassing(logger, playerNo, passLineCounter);
            for (int i = 0; i < handCards.size(); i++){
                values[hCBeforePassingStartIdx + i] = handCards.get(i);
            }
        }
        else throw new IllegalArgumentException("Wrong mode in getValuesFromLogger()");

        return values;
    }

    public static boolean getQueenOfSpadesPlayedYet(Logger logger, int counter){
        return logger.getCardsPlayedInThisRound().get(counter)[CardCoding.SPADES_QUEEN.getId() - 1];
    }

    public static int getNumPlayedHeartsYet(Logger logger, int counter){
        boolean[] cardsPlayed = logger.getCardsPlayedInThisRound().get(counter);
        
        int num = 0;
        for (int i = CardCoding.HEARTS_2.getId() - 1; i < CardCoding.HEARTS_ACE.getId(); i++){
            if (cardsPlayed[i]) num++;
        }
        return num;
    }
    
    /**
    * Retrieves the hand cards of a specified player from the Logger object for a given round.
    *
    * @param logger    The Logger object from which the hand cards are to be retrieved.
    * @param playerNo  The number of the player (0, 1, 2, or 3) whose hand cards are to be retrieved.
    * @param counter   The counter representing the trick for which the hand cards are to be retrieved.
    * @return A String array containing the hand cards of the specified player for the given round.
    */
    public static String[] getHandCardsFromLogger(Logger logger, int playerNo, int counter){
        String handCards = "";
        switch (playerNo){
            case 0:
                handCards = logger.getHandCardsP0().get(counter);
                break;
            case 1:
                handCards = logger.getHandCardsP1().get(counter);
                break;
            case 2:
                handCards = logger.getHandCardsP2().get(counter);
                break;
            case 3:
                handCards = logger.getHandCardsP3().get(counter);
                break;
            default:
                break;
        }

        String cleanedHC = handCards.replace("[", "").replace("]", "");
        String[] hc = cleanedHC.split("\\.");
        return hc;
    }

    /**
    * Retrieves the playable hand cards of a specified player from the Logger object for a given round.
    *
    * @param logger    The Logger object from which the hand cards are to be retrieved.
    * @param playerNo  The number of the player (0, 1, 2, or 3) whose hand cards are to be retrieved.
    * @param counter   The counter representing the trick for which the hand cards are to be retrieved.
    * @return A String array containing the playable hand cards of the specified player for the given round.
    */
    public static String[] getHandCardsPlayableFromLogger(Logger logger, int playerNo, int counter){
        String handCardsPlayable="";
        switch (playerNo){
            case 0:
                handCardsPlayable = logger.getHandCardsPlayableP0().get(counter);
                break;
            case 1:
                handCardsPlayable = logger.getHandCardsPlayableP1().get(counter);
                break;
            case 2:
                handCardsPlayable = logger.getHandCardsPlayableP2().get(counter);
                break;

            case 3:
                handCardsPlayable = logger.getHandCardsPlayableP3().get(counter);
                break;
            default:
                break;
        }
        String cleanedHCP = handCardsPlayable.replace("[", "").replace("]", "");
        String[] hcP = cleanedHCP.split("\\.");
        return hcP;
    }

    /**
    * Retrieves the cards passed by a specific player at a given round from the provided logger.
    *
    * @param logger    The logger containing the game data.
    * @param playerNo  The number of the player whose passed cards are to be retrieved.
    * @param counter   The counter indicating the round for which passed cards are requested.
    * @return          An array of strings representing the cards passed by the player.
    */
    public static String[] getPassedCardsFromLogger(Logger logger, int playerNo, int counter){
        
        String cardsPassed = logger.getCardsPassed().get(counter);
        String[] pass = cardsPassed.split("\\,");
        
        if (pass.length == 3) return pass;
        else {
            String[] passOfPlayer = new String[3];
            int startingIdx = playerNo * 3; 
            for (int i=0; i<3; i++){
                passOfPlayer[i] = pass[startingIdx+i];
            }
            return passOfPlayer;
        }
    }

    /**
    * Retrieves the trick cards played in a specific trick from the Logger object.
    *
    * @param logger  The Logger object from which the trick cards are to be retrieved.
    * @param counter The counter representing the number of the trick to be retrieved.
    * @return A String array containing the trick cards played in the specified trick.
    */
    public static String[] getTrickFromLogger(Logger logger, int counter){
        String trick = logger.getTricksPlayed().get(counter);
        String trickCleaned = trick.replace("[", "").replace("]", "");
        String[] trickCards = trickCleaned.split("\\.");
        return trickCards;
    }

    /**
    * Retrieves the hand cards of a specified player before passing in a particular round from the Logger object.
    *
    * @param logger    The Logger object from which the hand cards before passing are to be retrieved.
    * @param playerNo  The number of the player (0, 1, 2, or 3) whose hand cards before passing are to be retrieved.
    * @param counter   The counter representing the round for which the hand cards before passing are to be retrieved.
    * @return A List of Integer containing the hand cards of the specified player before passing in the given round.
    */
    public static List<Integer> getHandCardsBeforePassing(Logger logger, int playerNo, int counter){
        List<Integer> handCardsBeforePassing = new ArrayList<Integer>();
        int index = counter*4 + playerNo;
        String handCards = logger.getHandCardsBeforePassing().get(index);

        String cleanedHC = handCards.replace("[", "").replace("]", "");
        String[] hc = cleanedHC.split("\\.");
        for (String card : hc){
            handCardsBeforePassing.add(Integer.parseInt(card));
        }

        return handCardsBeforePassing;
    }

    
    /**
    * Normalizes the given array of probabilities so that the sum of probabilities equals 1.
    * If any probability value is negative, it is set to 0.
    * If the sum of probabilities is not zero, each probability is divided by the sum to achieve normalization.
    * Additionally, each probability value is rounded to three decimal places for readability.
    *
    * @param probabilities The array of probabilities to be normalized.
    * @return The array of probabilities after normalization.
    */
    public static double[] normalize(double[] probabilities) {
        
        try{
           double sum = 0.0;
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] < 0) {
                probabilities[i] = 0;
                }
                sum += probabilities[i];
            }
        
            if (sum!= 0){
                for (int i = 0; i < probabilities.length; i++) {
                probabilities[i] /= sum; 

                //Hier Runde ich nochmal ab, dies dient eig. nur der Lesbarkeit  TODO: später entfernen
                BigDecimal bd = new BigDecimal(Double.toString(probabilities[i]));
                bd = bd.setScale(3, RoundingMode.HALF_DOWN);
                probabilities[i] = bd.doubleValue(); 
                }
            } 
        } catch (NumberFormatException e){
            probabilities = new double[probabilities.length];
        }
        
        return probabilities;
    }

    /**
    * Filters out negative regret values and sets them to zero (CFR+).
    *
    * @param regret The array of regret values to filter.
    * @return The array of regret values with negative values set to zero.
    */
    public static double[] onlyPositiveRegret(double[] regret){
        for (int i = 0; i < regret.length; i++) {
            if (regret[i] < 0) {
                regret[i] = 0.0;
            }
        }
        return regret;
    }

    /**
    * Limits regret values.
    *
    * @param regret The array of regret values to limit.
    * @param limit The upper limit for regret values.
    * @return The array of regret values with values limited to the specified limit.
    */
    public static double[] limitRegret(double[] regret, double limit){
        for (int i = 0; i < regret.length; i++){
            if (regret[i] > limit) {
                regret[i] = limit;
            }
        }
    return regret;
    }


    /**
    * Counts the number of hearts played with memory.
    *
    * @param cardsToConsider The list of card IDs to consider.
    * @return The number of hearts played from the provided list of cards.
    */
    public static int getNumHeartsPlayedWithMemory(List<Integer> cardsToConsider){
        int numHearts = 0;
        for (int cardId : cardsToConsider){
            if (CardCoding.getSuitById(cardId) == Suits.HEARTS) numHearts++; 
        }
        return numHearts;
    }

    /**
    * Checks if the Queen of Spades is played with memory.
    *
    * @param cardsToConsider The list of card IDs to consider.
    * @return 1 if the Queen of Spades is played, otherwise 0.
    */
    public static int getQueenOfSpadesPlayedWithMemory(List<Integer> cardsToConsider){
        int queenPlayed = 0;
        for (int cardId : cardsToConsider){
            if (cardId == CardCoding.SPADES_QUEEN.getId()){
                queenPlayed = 1;
                break;
            } 
        } 
        return queenPlayed; 
    }



    /**
    * Finds the index with the highest probability in an array of probabilities.
    *
    * @param probabilities An array of probabilities.
    * @return The index of the element with the highest probability.
    * @throws IllegalArgumentException If the array of probabilities is empty.
    */
    public static int findMaxProbabilityIndex(double[] probabilities) {
        if (probabilities == null || probabilities.length == 0) {
            throw new IllegalArgumentException("Prob Array is empty ?");    
        }

        int maxIndex = 0;
        double maxProb = probabilities[0];

        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxIndex = i;
                maxProb = probabilities[i];
            }
        }

        return maxIndex;
    }

    /**
    * Finds the card with the value closest to the specified card value in an ArrayList of cards.
    *
    * @param arrayList  The ArrayList of cards.
    * @param CardtoPlay The value of the card to compare against.
    * @return The value of the card in the ArrayList that is closest to the specified card value.
    * @throws IllegalArgumentException If the ArrayList is null or empty.
    */
    public static int closeCard(ArrayList<Card> arrayList, int cardtoPlay) {
        if (arrayList == null || arrayList.isEmpty()) {
            throw new IllegalArgumentException("Die ArrayList sollte nicht leer sein.");
        }

        int min = Integer.MAX_VALUE;
        int ergebnis = 0;

        for (Card c : arrayList) {
            int abstand = Math.abs(c.getId() - cardtoPlay);

            if (abstand < min) {
                min = abstand;
                ergebnis = c.getId();
            }
        }

        return ergebnis;
    }

    /**
    * Finds the card with the value closest to the specified card value in an ArrayList of cardIds.
    *
    * @param cardIds  The ArrayList of cardIds.
    * @param cardtoPlay The value of the card to compare against.
    * @return The value of the card in the ArrayList that is closest to the specified card value.
    * @throws IllegalArgumentException If the ArrayList is null or empty.
    */
    public static int closeCard(List<Integer> cardIds, int cardtoPlay) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("Keine spielbaren Karten.");
        }

        int min = Integer.MAX_VALUE;
        int ergebnis = 0;

        for (int id : cardIds) {
            int abstand = Math.abs(id - cardtoPlay);

            if (abstand < min) {
                min = abstand;
                ergebnis = id;
            }
        }

        return ergebnis;
    }

    /**
    * Calculates the square root of the absolute value of the given payoff and retains its original sign.
    */
    public static double getRootValue(double payoff){
        double absPayoff = Math.abs(payoff);
        double root = Math.sqrt(absPayoff);
        if (payoff < 0) {
            return -root; 
        } else {
            return root; 
        }
    }


    /**
    * Finds the card in the given list of card IDs that is most similar to the card to be played.
    * The similarity is determined by the difference between card values and suit offsets.
    * If only one of the cards is a Heart card, the suit offset is set to 15, otherwise it is set to 5.
    *
    * @param cardIds    The list of card IDs to search through.
    * @param cardtoPlay The value of the card to be played.
    * @return The ID of the card in the list that is most similar to the card to be played.
    * @throws IllegalArgumentException if the list of card IDs is null or empty.
    */
    public static int similiarCard(List<Integer> cardIds, int cardtoPlay) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("No playable cards.");
        }
    
        int minDistance = Integer.MAX_VALUE;
        int result = 0;
    
        for (int id : cardIds) {
            int suitOffset = 0;
            
            // Wenn nur eine der Karten eine Herz-Karte ist, setze suitOffset auf 15
            if (((id - 1) / 13 == 1 && (cardtoPlay - 1) / 13 != 1) || ((id - 1) / 13 != 1 && (cardtoPlay - 1) / 13 == 1)) {
                suitOffset = 15;
            } else {
                suitOffset = 5;
            }

            int distance =0;
            
            if (((id - 1) / 13 == (cardtoPlay - 1) / 13)) {
                distance = Math.abs((id - 1) % 13 - (cardtoPlay - 1) % 13);
            } else{
                distance = Math.abs((id - 1) % 13 - (cardtoPlay - 1) % 13) + suitOffset;

            }   
    
            // Überprüfe, ob dieser Abstand kleiner als der bisherige Mindestabstand ist
            if (distance < minDistance) {
                minDistance = distance;
                result = id;
            }
        }    
    
        return result;
    }

      /**
     * Finds three different cards in the given list of card IDs that are most similar to the cards to be played.
     * The similarity is determined by the difference between card values and suit offsets.
     * If only one of the cards is a Heart card, the suit offset is set to 15, otherwise it is set to 5.
     *
     * @param cardIds    The list of card IDs to search through.
     * @param cardsToPlay The values of the cards to be played.
     * @return Three different IDs of the cards in the list that are most similar to the cards to be played.
     * @throws IllegalArgumentException if the list of card IDs is null or empty.
     */
    public static List<Integer> similarCards(List<Integer> cardIds, List<Integer> cardsToPlay) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("No playable cards.");
        }

        List<Integer> similarCardIds = new ArrayList<>();
        Set<Integer> foundCardIds = new HashSet<Integer>();

        for (int cardtoPlay : cardsToPlay) {
            int minDistance = Integer.MAX_VALUE;
            int result = 0;

            for (int id : cardIds) {
                int suitOffset = 0;

                // Wenn nur eine der Karten eine Herz-Karte ist, setze suitOffset auf 15
                if (((id - 1) / 13 == 1 && (cardtoPlay - 1) / 13 != 1) || ((id - 1) / 13 != 1 && (cardtoPlay - 1) / 13 == 1)) {
                    suitOffset = 15;
                } else {
                    suitOffset = 5;
                }

                int distance;
                if (((id - 1) / 13 == (cardtoPlay - 1) / 13)) {
                    distance = Math.abs((id - 1) % 13 - (cardtoPlay - 1) % 13);
                } else {
                    distance = Math.abs((id - 1) % 13 - (cardtoPlay - 1) % 13) + suitOffset;
                }

                // Überprüfe, ob dieser Abstand kleiner als der bisherige Mindestabstand ist
                if (distance < minDistance && !foundCardIds.contains(id)) {
                    minDistance = distance;
                    result = id;
                }
            }

            similarCardIds.add(result);
            foundCardIds.add(result);
        }

        return similarCardIds;
    }


}
