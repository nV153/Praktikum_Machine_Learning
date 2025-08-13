package game.AI.AiMCTS.AiISMCTSANN;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import game.Card;
import game.GameState;
import game.AI.AiMCTS.AiISMCTS.ISMCTSNode;
import game.AI.AiMCTS.AiISMCTS.ISMCTSTree;
import game.AI.AiMCTS.AiISMCTS.SO_ISMCTS;


/**
 * The ISMCTSANN class extends SO_ISMCTS to incorporate ANN predictions into the ISMCTS algorithm for the game of Hearts. 
 * This class aims to improve decision-making for computer players through LSTM model predictions.
 */
public class ISMCTSANN extends SO_ISMCTS {

    /**
     * Initializes the ISMCTSANN class and prints an initialization message.
     */
    public ISMCTSANN() {
        super();
        System.out.println("ISMCTSANN initialized");
    }

    /**
     * Executes the ISMCTS algorithm, performing a deep copy and determinization of the game state, followed by selection, 
     * expansion, simulation, and backpropagation phases, enhanced with ANN predictions.
     * @param tree The current ISMCTS tree.
     * @param copiedState A deep copy of the current game state.
     * @return An array of integers representing the best card pass decision.
     */
    @Override
    protected int[] runISMCTSPass(ISMCTSTree tree, GameState copiedState) {
        for (int i = 0; i < iterations; i++) {
            //Copy, Determinization & get possible moves
            currentState = copiedState.deepCopy();
            currentState.determinization(observer);
            ISMCTSNode selectedNode = tree.getRoot();

            // ISMCTS 4 phases: Selection or Expansion, Simulation, Backpropagation
            if(!possiblePassCombinations.isEmpty()) {
                selectedNode = expansionPhaseForPass(selectedNode);
            }else {
                selectedNode = selectionPhaseForPass(selectedNode);
            }
            super.simulationPhase();
            backpropagationPhase(selectedNode);
        }
        return tree.selectBestNode().getPassedCards();
    }

    /**
     * Conducts the simulation phase, invoking the LSTM model to predict the best move.
     */
    @Override
    protected void simulationPhase() {
        int [] featureArray = new int[65];
        Arrays.fill(featureArray, 0);
        while (!currentState.isRoundOver()) {
        iterations = 1;
        System.out.println("Starting simulationPhase");
        int playerNo = currentState.getPlayerToMove();
        System.out.println("Player to move: " + playerNo);
        System.out.println("TricksPlayed: " + currentState.getTricksPlayed());
        featureArray = gameStateToFeatureArray(currentState, playerNo);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < featureArray.length; i++) {
            sb.append(featureArray[i]);
            if (i < featureArray.length - 1) {
                sb.append(",");
            }
        }

        String gameStateAsString = sb.toString();
        System.out.println("GameState as String for LSTM: " + gameStateAsString);
        String scriptPath = getScriptPathForPlayer(playerNo);
        System.out.println("Using script path: " + scriptPath);

        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            Process p = pb.start();
            System.out.println("Process started for LSTM prediction");

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            out.write(gameStateAsString);
            out.flush();
            out.close();
            System.out.println("GameState sent to LSTM model");

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String predictedCardIdAsString = in.readLine();
            System.out.println("Predicted card ID as String: " + predictedCardIdAsString);

            BufferedReader errIn = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorLine;
            while ((errorLine = errIn.readLine()) != null) {
            System.out.println("Python Error Output: " + errorLine);
            }
        errIn.close();
            
        if (predictedCardIdAsString != null) {
            int predictedCardId = Integer.parseInt(predictedCardIdAsString);
            System.out.println("predictedCardId: " + predictedCardId);
        
            if (predictedCardId != 0) {
                Card predictedCard = new Card(predictedCardId);
                System.out.println("Predicted card: " + predictedCard);
        
                if (isCardPlayable(playerNo, predictedCard)) {
                    System.out.println("Predicted card is playable, playing card: " + predictedCardId);
                    currentState.doMove(predictedCardId);
                } else {
                    System.out.println("Predicted card is not playable, playing fallback card");
                    playFallbackCard();
                }
            } else {
                System.out.println("Predicted card ID is 0, which is invalid. Playing fallback card.");
                playFallbackCard();
            }
        } else {
            System.out.println("No predicted card ID received. Playing fallback card.");
            playFallbackCard();
        }
        
            in.close();
        } catch (IOException e) {
            System.out.println("IOException occurred during LSTM prediction process");
            e.printStackTrace();
        }
    }
    
    }

    /**
     * Retrieves the script path for the LSTM model based on the player number.
     * @param playerNo The player number.
     * @return A string representing the path to the LSTM model script.
     */
    private String getScriptPathForPlayer(int playerNo) {
        switch (playerNo) {
            case 0: return "LSTM_player_zero.py";
            case 1: return "LSTM_player_one.py";
            case 2: return "LSTM_player_two.py";
            case 3: return "LSTM_player_three.py";
            default: throw new IllegalArgumentException("Ungültige Spieler-Nummer: " + playerNo);
        }
    }

    /**
     * Plays a fallback card if no valid prediction can be made or if the predicted card is not playable.
     */
    private void playFallbackCard() {
        List<Card> playableCards = currentState.determineListOfPlayableCards(currentState.getPlayerToMove());
        Card randomPlayableCard = playableCards.get(new Random().nextInt(playableCards.size()));
        currentState.doMove(randomPlayableCard.getId());
    }

    /**
     * Retrieves a list of card IDs that have been played until the current point in the game.
     * @return A list of integers representing played card IDs.
     */
    private List<Integer> cardsplayed() {
        List<Integer> cardsPlayedUntilNow = new ArrayList<>();
    for (int i = 0; i < currentState.getTricksPlayed() - 1; i++) {
        int[] cardsFromTrick = currentState.getRoundTricks()[i].getCardsPlayed();
        for (int cardId : cardsFromTrick) {
    
            cardsPlayedUntilNow.add(cardId);
        }
    }
    
    return cardsPlayedUntilNow;
}
    /**
     * Converts the current game state into a feature array suitable for the LSTM model.
     * @param currentState The current state of the game.
     * @param playerNo The player number.
     * @return An array of integers representing the game state features.
     */
    public int[] gameStateToFeatureArray(GameState currentState, int playerNo) {
        List<Integer> allFeatures = new ArrayList<>();
        
        List<Integer> playedCardIdsList =(cardsplayed());
        allFeatures.addAll(playedCardIdsList);
        System.out.println("playedCardIdsList: " + playedCardIdsList.size());

        List<Integer> playableCardIds = currentState.determineListOfPlayableCardIds(playerNo);
        allFeatures.addAll(playableCardIds);
        System.out.println("playableCardIds: " + playableCardIds.size());

        while (allFeatures.size() < 65) {
            allFeatures.add(-100);
        }
        
       
        return allFeatures.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Determines if a given card is playable based on the current game state and player number.
     * @param playerNo The player number.
     * @param card The card to check.
     * @return True if the card is playable, false otherwise.
     */
    private boolean isCardPlayable(int playerNo, Card card) {
        List<Card> playableCards = currentState.determineListOfPlayableCards(currentState.getPlayerToMove());
        return playableCards.contains(card);
    }
}


