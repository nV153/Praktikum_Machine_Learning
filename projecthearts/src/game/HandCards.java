package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents the hand cards of a player.
 */
public class HandCards implements Cloneable{
    private boolean[] isInHand; //size 52

    /**
     * Constructs a new HandCards object
     */
    public HandCards(){
        isInHand = new boolean[52];
    }

    /**
     * Adds a card to the hand cards.
     *
     * @param cardId The identifier of the card to be added.
     * @throws IllegalArgumentException if the card is already in the hand or the id is invalid.
     */
    public void addCard (int cardId) throws IllegalArgumentException{
        if (!isValidId(cardId)){
            throw new IllegalArgumentException("Invalid ID: " + cardId);
        }
        
        if (isInHand[cardId - 1]){
            throw new IllegalArgumentException("Card with ID " + cardId + "is already in hand.");
        } 
        isInHand[cardId - 1] = true;
    }

    /**
     * Removes a card from the hand cards.
     *
     * @param cardId The identifier of the card to be removed.
     * @throws IllegalArgumentException if the card is not in the hand or the id is invalid.
     */
    public void removeCard(int cardId) throws IllegalArgumentException{
        if (!isValidId(cardId)){
            throw new IllegalArgumentException("Invalid ID: " + cardId);
        }
        
        if (!isInHand[cardId - 1]){                       
            throw new IllegalArgumentException("Card with ID " + cardId + "is not in hand.");
        }
        
        isInHand[cardId - 1] = false;
    }

    /**
     * Checks if the handcards contain a specific card.
     *
     * @param cardId The identifier of the card to check.
     * @return {@code true} if the card is in hand, {@code false} otherwise.
     * @throws IllegalArgumentException if the cardId is invalid.
     */
    public boolean containsCard(int cardId) throws IllegalArgumentException{   
        
        if (!isValidId(cardId)){
            throw new IllegalArgumentException("Invalid ID: " + cardId);
        }

        return isInHand[cardId - 1];
    }

    /**
     * Checks if card 1 has a higher numerical value than card 2, regardless of suit.
     *
     * @param id1 The identifier of the first card.
     * @param id2 The identifier of the second card.
     * @return {@code true} if card 1 has a higher value, {@code false} otherwise.
     * @throws IllegalArgumentException if the player doesn't have at least one of the selected cards or at least one id is invalid.
     */
    public boolean hasHigherValue(int id1, int id2) throws IllegalArgumentException{

        if (!isValidId(id1) || !isValidId(id2)){
            throw new IllegalArgumentException("One or more invalid IDs: " + id1 + ", " + id2);
        }

        if (!containsCard(id1) || !containsCard(id2)){
            throw new IllegalArgumentException("At least one of the selected cards is not in hand.");
        }
        
        // 13 cards per suit, id start with 1
        int value1 = (id1 - 1) % 13;
        int value2 = (id2 - 1) % 13;

        return value1 > value2;   
    } 

    /**
     * Returns the identifier of the card with the higher numerical value, regardless of suit.
     *
     * @param id1 The identifier of the first card.
     * @param id2 The identifier of the second card.
     * @return The identifier of the card with the higher value, or 0 if values are equal.
     * @throws IllegalArgumentException if the player doesn't have at least one of the selected cards or the id is invalid.
     */
    public int getHigherId(int id1, int id2) throws IllegalArgumentException{

        if (hasHigherValue(id1, id2)) return id1;
        else return id2;
    }

    /**
     * Counts the number of cards in a specified range.
     *
     * @param startValue The starting value of the range.
     * @param endValue   The ending value of the range.
     * @return The number of cards in the specified range.
     */
    public int countCardsInRange(int startValue, int endValue) {
        int count = 0;
    
        for (int i = startValue; i <= endValue; i++) {
            if (containsCard(i)) {
                count++;
            }
        }
        return count;
    }

    /* Returns if the id is in the range of isInHand*/
    private boolean isValidId(int id) {
        int maxId = isInHand.length;
        return id >= 1 && id <= maxId;
    }

    /**
     * Resets the hand cards, setting all values to false.
     */
    public void reset(){
        Arrays.fill(isInHand, false);
    } 

    /**
     * Gets a copy of the isInHand array.
     *
     * @return A copy of the isInHand array.
     */
    public boolean[] getIsInHandCopy(){
        
        if (isInHand == null) return new boolean[0];
        return Arrays.copyOf(isInHand, isInHand.length);
    }

    /**
     * Sets the isInHand array to a provided copy.
     *
     * @param isInHand The copy of the isInHand array to set.
     */
    public void setIsInHandToCopy(boolean[] isInHand){
        
        this.isInHand = Arrays.copyOf(isInHand, isInHand.length);
    }

    /**
     * Gets the isInHand array.
     *
     * @return The isInHand array.
     */
    public boolean[] getIsInHand() {
        return isInHand;
    }

    /**
     * Sets the isInHand array to a provided array.
     *
     * @param isInHand The array of the isInHand array to set.
     */
    public void setIsInHand(boolean[] isInHand) {
        this.isInHand = isInHand;
    }

    /**
    * Clones the HandCards object to create a deep copy.
    *
    * @return A clone of the HandCards object.
    */
    @Override  
    public Object clone() {
        HandCards handCardsCopy = new HandCards();
        handCardsCopy.isInHand = Arrays.copyOf(this.isInHand, this.isInHand.length);
        return handCardsCopy;    
    }

    /**
    * Counts the number of cards in hand.
    *
    * @return The number of cards in hand.
    */
    public int countCards() {
        int count = 0;
        for (int i = 0; i < isInHand.length; i++) {
            if (isInHand[i]) {
                count++;
            }
        }
        return count;
    }

    /**
    * Provides a string representation of the HandCards object.
    *
    * @return A string representing the HandCards object.
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < isInHand.length; i++) {
            if (isInHand[i]) {
                sb.append(i + 1);
                sb.append(", ");
            }
        }

        return "HandCards [ isInHand= " + sb + "]";
    }

    /**
    * Generates possible combinations of cards to pass.
    * 
    * @return A list of possible pass combinations.
    */
    public List<int[]> generatePossiblePassCombinations() {
        List<int[]> possiblePassCombinations = new ArrayList<>();

        for (int i = 0; i < isInHand.length; i++) {
            if (isInHand[i]) {
                for (int j = i + 1; j < isInHand.length; j++) {
                    if (isInHand[j]) {
                        for (int k = j + 1; k < isInHand.length; k++) {
                            if (isInHand[k]) {
                                int[] passCombination = new int[]{i+1 , j+1, k+1};
                                possiblePassCombinations.add(passCombination);
                            }
                        }
                    }
                }
            }
        }
        return possiblePassCombinations;
    }

    /**
    * Generates a random combination of cards to pass.
    *
    * @return An array representing a random pass combination.
    */
    public int[] generateRandomPass() {
        int[] passCombination = new int[3];
        Random random = new Random();
        Set<Integer> selectedCards = new HashSet<Integer>();
        
        int counter = 0;
        while (counter < 3) {
            int randomCard = random.nextInt(51);
            if (isInHand[randomCard] && !selectedCards.contains(randomCard)) {
                passCombination[counter] = randomCard + 1; // +1 for Card-Id
                selectedCards.add(randomCard);
                counter++;
            }
        }
        return passCombination;
    }
}
