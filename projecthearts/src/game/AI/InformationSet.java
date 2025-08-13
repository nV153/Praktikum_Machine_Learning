package game.AI;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.GameState;

public class InformationSet {
    private Map<String, List<List<Integer>>> otherPlayersHandCards;
    
    private int playerNo;
    private GameState gameState;
    
    public int getPlayerNo() {return playerNo;}
    public GameState getGameState() {return gameState;}
    public Map<String, List<List<Integer>>> getOtherPlayersHandCards() {return otherPlayersHandCards;}

    public InformationSet(int playerNo, GameState gameState) {
        this.gameState = gameState;
        this.playerNo = playerNo;
    }

    private Map<Integer, List<Integer>> calculateOtherPlayersHandcards(GameState gameState){
        
        // alle Karten ermitteln, die Teil der Handkarten der anderen Spieler sein können
        List<Integer> cardIdsLeft = new ArrayList<Integer>();
        for (int i = 1; i <= 52; i++) {
            if (!gameState.hasCardBeenPlayedInThisRound(i) && !gameState.getPlayers()[playerNo].hasHandCard(i)){
                cardIdsLeft.add(i);
            }
        }

        Map<Integer, List<Integer>> otherPlayersHands = new HashMap<>();

        for (int otherPlayer = 0; otherPlayer < gameState.getPlayers().length; otherPlayer++){
            if (otherPlayer != playerNo) {
                List<Integer> possibleHandCards = generateHandcardsCombinations(cardIdsLeft, gameState.getPlayers()[otherPlayer].countCards());
                otherPlayersHands.put(otherPlayer, possibleHandCards);
            }
        }
        return otherPlayersHands;
    
    }

    private List<Integer> generateHandcardsCombinations(List<Integer> possibleCards, int numberOfHandcards){
        
        List<Integer> allCombinations = new ArrayList<>();
        for (int i = 0; i < possibleCards.size(); i++) {
            //TODO Permutationen erstellen
            
        } 
        return allCombinations;       
        
    }

}
