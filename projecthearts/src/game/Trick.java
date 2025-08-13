package game;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a trick in a round of the game, containing 0-4 cards
 */
public class Trick implements Cloneable{
    
    private int starter;  // in range [0, 4]
    private int[] cardsPlayed; // card of player i is stored in cardsPlayed[i]  
    private int numPlayedCards;

    /**
     * Initializes a new trick with the specified starting player.
     * 
     * @param starter The player who starts the trick. Should be in the range {0, 1, 2, 3}.
     * @throws IllegalArgumentException If the player number is invalid.
     */
    public Trick (int starter) throws IllegalArgumentException{
        
        if (starter < 0 || starter > 3){
            throw new IllegalArgumentException("Invalid playerNo.");
        }

        this.starter = starter;
        cardsPlayed = new int[4];
        numPlayedCards = 0;
    }

    /**
     * Adds a card to the trick played by a specific player.
     * 
     * @param card The card to be added to the trick.
     * @param playerNo The player who played the card. Should be in the range {0, 1, 2, 3}.
     * @throws IllegalArgumentException If the card is null or the player number is invalid.
     */ 
    public void addCard(int cardId, int playerNo) throws IllegalArgumentException{
        
        if (cardId < 1 || cardId > 52){
            throw new IllegalArgumentException("No valid cardId selected.");
        }
        if (playerNo < 0 || playerNo > 3){
            throw new IllegalArgumentException("PlayerNo is not invalid.");
        }
        
        cardsPlayed[playerNo] = cardId;
        numPlayedCards++;
    }

    /**
     * Gets the identifier of the starting card in the trick.
     * 
     * @return The identifier of the starting card if a card has been played; otherwise, -1.
     */
    public int getStartingCard(){
        if (numPlayedCards > 0) return cardsPlayed[starter];
        else return -1;
    }

    /**
     * Gets the cardId that currently wins the trick.
     * 
     * @return The cardId that currently wins the trick, or null if no cards have been played.
     */
    public int getHighCard(){

        if (numPlayedCards == 0){
            return -1;
        
        } else return cardsPlayed[getWinner()];       
    }

    /**
     * Gets the player number who currently wins the trick.
     * 
     * @return The player number who currently wins the trick, or -1 if no cards have been played yet.
     */
    public int getWinner() {

        if (numPlayedCards == 0){
            return -1;
        }

        int winner = starter;
        int winningCard = cardsPlayed[starter];
        
        for (int i = 1; i < numPlayedCards; i++){

            int currentIndex = (starter + i) % cardsPlayed.length;
            int otherCard = cardsPlayed[currentIndex];

            if (CardCoding.getSuitById(otherCard) == CardCoding.getSuitById(winningCard)){
                if (otherCard > winningCard){
                   winningCard = otherCard;
                   winner = currentIndex; 
                } 

            }
        }
        return winner;
    }

    /**
     * Counts the points of the trick based on the cards played so far.
     * 
     * @return The total points of the trick.
     */
    public int countPoints(){

        int count = 0;
        
        for (int i = 0; i < numPlayedCards; i++){
            
            int index = (starter + i) % cardsPlayed.length;
            int cardId = cardsPlayed[index];
            if (CardCoding.getSuitById(cardId) == Suits.HEARTS) count++;
            if (cardId == CardCoding.SPADES_QUEEN.getId()) count += 13;
        }

        return count;
    }

    /**
     * Gets the card played by a specific player in the trick.
     * 
     * @param playerNo The player whose card is to be retrieved.
     * @return The cardId played by the specified player.
     */
    public int getCardPlayedByPlayer(int playerNo){

        return cardsPlayed[playerNo];
    }

    /**
     * Gets the number of cards played in the trick.
     * 
     * @return The number of cards played in the trick.
     */
    public int getNumPlayedCards(){
        
        return numPlayedCards;
    }

    /**
     * Gets the starting player of the trick.
     * 
     * @return The player number who started the trick.
     */
    public int getStarter(){
        
        return starter;
    }

    /**
     * Gets a copy of the cards played in the trick.
     * 
     * @return An array containing copies of the cards played in the trick.
     */
    public int[] getCardsPlayedCopy(){
        
        return Arrays.copyOf(cardsPlayed, cardsPlayed.length);
    }

    /**
     * Gets the cards played in the trick.
     * 
     * @return An array containing the cards played in the trick.
     */
    public int[] getCardsPlayed(){

        return cardsPlayed;
    }

    /**
    * Creates and returns a copy of this object. The method overrides the 
    * clone method of the Object class. It attempts to create a shallow copy 
    * of the current Trick object. If cloning is not supported or if an error 
    * occurs during the cloning process, a new Trick object is created with 
    * the same starter as the original Trick object.
    * 
    * @return a clone of the current Trick object.
    */
    @Override
    public Object clone() {
        Trick trickCopy = null;
        try {
            trickCopy = (Trick) super.clone();
            trickCopy.cardsPlayed = Arrays.copyOf(cardsPlayed, cardsPlayed.length);
            
        } catch (CloneNotSupportedException e) {
            System.out.println("clone not supported for class: " + this.getClass().getName());
            trickCopy = new Trick(this.starter); 
        }
        return trickCopy;
    }

    /**
     * Gets the cards played in the trick as a list.
     * 
     * @return A list containing the cards played in the trick.
     */
    public List<Integer> getListOfCardsIdsPlayed(){
        List<Integer> cardIds = new ArrayList<Integer>();
        for (int i = 0; i < numPlayedCards; i++){
            cardIds.add(cardsPlayed[(starter + i) % 4]);
        }
        return cardIds;
    }

}

