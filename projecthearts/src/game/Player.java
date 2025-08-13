package game;

import java.util.ArrayList;
import java.util.List;

import rules.Rules;

/**
 * Represents a player in the game.
 */
public class Player implements Comparable<Player>, Cloneable{
    private String name;
    private boolean isHuman;
    protected HandCards handCards;
    protected HandCards handCardsPlayable;
    private int roundPts, gamePts;
    private int playerNo;
    private int receiverNo; 
    private List<Integer> cardsToPass;
    private List<Integer> cardsReceivedFromPassing; 
    private double payoff;

    /**
     * Initializes a new player with the specified name, isHuman and player number.
     *
     * @param name     The name of the player.
     * @param isHuman  True if the player is human, false otherwise.
     * @param playerNo The player number.
     */
    public Player(String name, boolean isHuman, int playerNo){
        this.name = name;
        this.isHuman = isHuman;
        handCards = new HandCards();
        handCardsPlayable = new HandCards();
        this.playerNo = playerNo;
        roundPts = 0;
        gamePts = 0;
        receiverNo = -1; // -1, falls Modus No Pass oder so lange kein Receiver ermittelt wurde
        cardsToPass = new ArrayList<>();
        cardsReceivedFromPassing = new ArrayList<>();
    }

    /**
     * Saves the passed cards.
     *
     * @param cards An array of card IDs to be saved.
     */
    public void savePassedCards(int[] cards){
        for (int cardId : cards){
            cardsToPass.add(cardId);
        }
    }

    /**
     * Adds a card to the cards to pass.
     *
     * @param cardId The Id of the card to be added.
     */
    //für MCCFR benötigt
    public void addCardToPass(int cardId){
        cardsToPass.add(cardId);
    }

    /**
     * Passes cards to another player.
     *
     * @param receivingPlayer The player receiving the cards.
     * @throws IllegalArgumentException If the number of cards to pass is not 3.
     */
    public void passCards (Player receivingPlayer) throws IllegalArgumentException{
        if (cardsToPass.size() != 3){
            throw new IllegalArgumentException("Three cards must be selected. Only " + cardsToPass.size() + " cards selected yet.");
        }
        for (int i = 0; i < cardsToPass.size(); i++){
            int cardId = cardsToPass.get(i);
            removeHandCard(cardId);
            receivingPlayer.addHandCard(cardId);
        }
    }

    /**
     * Resets the cards to pass. To be called at the end of a round.
     */
    public void resetCardsToPass(){
        cardsToPass.clear();
    }
      
    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to be added.
     * @throws IllegalArgumentException if the card is already in hand or the ID is invalid.
     */
    public void addHandCard(Card card) throws IllegalArgumentException{
        if (handCards.containsCard(card.getId())) {
            throw new IllegalArgumentException("Player has card " + card.getId() + " already in hand.");
        }
        handCards.addCard(card.getId());
    }

    /**
     * Adds a card to the player's hand using its ID.
     *
     * @param cardId The ID of the card to be added.
     * @throws IllegalArgumentException if the card is already in hand or the ID is invalid.
     */ 
    public void addHandCard(int cardId) throws IllegalArgumentException{
        if (handCards.containsCard(cardId)) {
            throw new IllegalArgumentException("Player has card " + cardId + " already in hand.");
        }
        handCards.addCard(cardId);
    }

    /**
    * Removes a card from the player's hand.
    *
    * @param cardId The ID of the card to be removed.
    * @throws IllegalArgumentException if the card is not in the hand or the ID is invalid.
    */
    private void removeHandCard(Card card){
        if (!handCards.containsCard(card.getId())) {
            throw new IllegalArgumentException("Player does not have card " + card.getId() + " in hand.");
        }
        handCards.removeCard(card.getId());
    }

    /**
    * Removes a card from the player's hand using its ID.
    *
    * @param cardId The ID of the card to be removed.
    * @throws IllegalArgumentException if the card is not in the hand or the ID is invalid.
    */
    public void removeHandCard(int cardId) throws IllegalArgumentException{
        if (!handCards.containsCard(cardId)) {
            throw new IllegalArgumentException("Player: "+ this.playerNo + " does not have card " + cardId + " in hand.");
        }
        handCards.removeCard(cardId);
        handCardsPlayable.removeCard(cardId);                                           
    }

    /**
    * Checks if the player has the specified card in their hand.
    *
    * @param card The card to check.
    * @return {@code true} if the player has the card, {@code false} otherwise.
    */
    public boolean hasHandCard(Card card){
        return handCards.containsCard(card.getId());
    }

    /**
    * Checks if the player has the specified card in their hand.
    *
    * @param int The id of the card to check.
    * @return {@code true} if the player has the card, {@code false} otherwise.
    */
    public boolean hasHandCard(int cardId){
        return handCards.containsCard(cardId);
    }

    /**
    * Checks if the player has the "Clubs 2" card in his hand.
    *
    * @return {@code true} if the player has the "Clubs 2" card, {@code false} otherwise.
    */
    public boolean hasClubsTwo(){
        return handCards.containsCard(40);
    }

    /**
    * Checks if the player has the "Clubs 7" card in their hand.
    * Applicable when the deck size is 32.
    *
    * @return {@code true} if the player has the "Clubs 7" card, {@code false} otherwise.
    */
    public boolean hasClubsSeven(){
        return handCards.containsCard(45);
    }
    
    /**
    * Counts the number of cards currently in the player's hand.
    *
    * @return The count of cards in the hand.
    */
    public int countCards(){          
        int count = 0;

        for(int i = 0; i < handCards.getIsInHand().length; i++){
            if(handCards.getIsInHand()[i] == true){
                count++;
            }
        }

        return count;
    }

    /**
    * Counts the number of playable cards currently in the player's hand.
    *
    * @return The count of playable cards in the hand.
    */
    public int countPlayableCards(){
        int count = 0;

        for(int i = 0; i < handCardsPlayable.getIsInHand().length; i++){
            if(handCardsPlayable.getIsInHand()[i] == true){
                count++;
            }
        }

        return count;
    }
    
    /**
     * Plays a card and adds it to the current trick.
     *
     * @param int  The id of the card to be played.
     * @param trick The trick to which the card is added.
     * @throws IllegalArgumentException if the card is not in the hand or the ID is invalid.
     * @throws IndexOutOfBoundsException if already 4 cards have been played
     */
    public void playCard(int cardId, Trick trick) throws IndexOutOfBoundsException, IllegalArgumentException{
        if (trick.getNumPlayedCards() == 4){
            throw new IndexOutOfBoundsException("Trick already has 4 cards.");
        }

        trick.addCard(cardId, playerNo);
        removeHandCard(cardId);
    }

    /**
     * Adds the points of a trick to the round points. Called when winning a trick.
     *
     * @param points The points to be added.
     */    public void addTrickPoints(int points){
        roundPts += points;
    }

    /**
     * Adds the points of a round to the game points. Called for all players at the end of a round.
     */    public void addRoundPoints(){
        gamePts += roundPts;
    }

    /**
     * Adds a specified value to the game points. Used for Shoot the Moon. (Old Moon)
     *
     * @param addThisInteger The value to subtract from game points.
     */
    public void addToGamePoints(int addThisInteger){
        gamePts += addThisInteger;
    }

    /**
     * Updates the payoff value.
     *
     * @param value The payoff is set to this value.
     */
    public void updatePayoffValue(double value){
        this.payoff = value;
    }

    /**
     * Subtracts a specified value from the game points. Used for Shoot the Moon.
     *
     * @param subtractThisInteger The value to subtract from game points.
     */
    public void subtractFromGamePoints(int subtractThisInteger){
        gamePts -= subtractThisInteger;
    }

    /**
     * Resets the round points to 0.
     */
    public void resetRoundPoints(){
        this.roundPts = 0;
    }
 
    /**
    * Compares players based on their game points.
    * 
    * @param otherPlayer The player to compare.
    * @return A negative integer, zero, or a positive integer as this player's game points
    *         are less than, equal to, or greater than the specified player's game points.
    */
    @Override
    public int compareTo(Player otherPlayer){
        return Integer.compare(otherPlayer.getGamePts(), this.gamePts);
    }

    /**
    * Resets the hand cards of the player, including both regular and playable hand cards.
    * Called at the end of a round to prepare for a new round.
    */
    public void resetHandCards(){
        handCards.reset();
        handCardsPlayable.reset();
    }

    /**
     * Sets the playable cards for this player in trick 0 or 1 according to the rules.
     * @param gameState The current game state.
     */
    public void setHandCardsPlayableFromRulesAndIsInHandInTrick0or1(GameState gameState){
        this.handCardsPlayable.setIsInHand(Rules.getPlayableCardsInTrick0or1(gameState, this));
    }

    /**
     * Sets the playable cards for this starting player in trick 2 or later according to the rules.
     * @param gameState The current game state.
     */
    public void setHandCardsPlayableFromRulesAndIsInHandForStarterInTrick2orLater(GameState gameState){
        this.handCardsPlayable.setIsInHand(Rules.getPlayableCardsOfStarterInTrick2orLater(gameState, this));
    }

    /**
     * Sets the playable cards for this non-starting player in trick 2 or later according to the rules.
     * @param gameState The current game state.
     * @param firstCardNumberPlayed The number of the card played by the starter of the trick.
     */
    public void setHandCardsPlayableFromRulesAndIsInHandForNonStarterInTrick2orLater(GameState gameState, int firstCardNumberPlayed){
        this.handCardsPlayable.setIsInHand(Rules.getPlayableCardsOfNonStarterInTrick2orLater(gameState, this, firstCardNumberPlayed));
    }

    /**
     * Sets the playable cards for this player in trick 0 or 1 according to the rules, only usable in case of
     * determinization of hand cards in MCTS-based AIs.
     * @param gameState The current game state.
     * @param starterOfRound The starter of the round.
     */
    public void setHandCardsPlayableFromRulesAndIsInHandInTrick0or1ForAiDeterminization(GameState gameState, Player starterOfRound){
        this.handCardsPlayable.setIsInHand(Rules.getPlayableCardsInTrick0or1ForAiDeterminization(gameState, this, starterOfRound));
    }


    public HandCards getHandCardsPlayable() {
        return handCardsPlayable;
    }

    public boolean[] getHandCardsPlayableIsInHand(){
        return handCardsPlayable.getIsInHand();
    }

    public boolean isHuman(){
        return isHuman;
    }

    public String getName(){
        return name;
    }

    public int getPlayerNo(){
        return playerNo;
    }

    public HandCards getHandCards(){
        return handCards;
    }

    public void setHandCards(HandCards handCards) {
        this.handCards = handCards;
    }

    public int getGamePts(){
        return gamePts;
    }

    public int getRoundPts(){
        return roundPts;
    }

    public List<Integer> getCardsToPass(){
        return new ArrayList<>(cardsToPass);
    }

    public List<Integer> getCardsToPassOriginalReference(){
        return cardsToPass;
    }

    public void setCardsToPass(List<Integer> cardsToPass){
        this.cardsToPass = cardsToPass;
    }

    public int getReceiverNo(){
        return receiverNo;
    }

    public void setReceiverNo(int receiverNo){
        this.receiverNo = receiverNo;
    }

    public List<Integer> getCardsReceivedFromPassing() {
        return cardsReceivedFromPassing;
    }

    public void setCardsReceivedFromPassing(List<Integer> cardsReceivedFromPassing) {
        this.cardsReceivedFromPassing = cardsReceivedFromPassing;
    }

    public int getPlayerPosition(int starterPlayerNo) {
        int playerPosition = this.getPlayerNo() - starterPlayerNo;
    
        if (playerPosition < 0) {
            playerPosition += 4;
        }
        
        return playerPosition;
    }

    public double getPayoff(){
        return this.payoff;
    }

    public void setRoundPoints(int points){
        this.roundPts = points;
    }

    /**
     * Method that clones this Player object and returns the clone.
     */
    @Override
    public Object clone() {
        Player playerCopy = null;
        try {
            playerCopy = (Player) super.clone();
            if (this.handCards != null){
                playerCopy.handCards = (HandCards) this.handCards.clone();
            }
            if (this.handCardsPlayable != null){ 
                playerCopy.handCardsPlayable = (HandCards) this.handCardsPlayable.clone();
            }
            if (this.cardsToPass != null){ 
                playerCopy.cardsToPass = new ArrayList<Integer>(this.cardsToPass.size());  
                for (int i = 0; i < this.cardsToPass.size(); i++){
                    if (this.cardsToPass.get(i) != null){
                        playerCopy.cardsToPass.add(i, cardsToPass.get(i));
                    }
                }
            }            
            if (this.cardsReceivedFromPassing != null){ 
                playerCopy.cardsReceivedFromPassing = new ArrayList<Integer>(this.cardsReceivedFromPassing.size()); 
                for (int i = 0; i < this.cardsReceivedFromPassing.size(); i++){
                    if (this.cardsReceivedFromPassing.get(i) != null){
                        playerCopy.cardsReceivedFromPassing.add(i, cardsReceivedFromPassing.get(i));
                    }
                }
            }
        } catch (CloneNotSupportedException e) {            
            System.out.println("clone not supported for class: " + this.getClass().getName());
            playerCopy = new Player(this.name,this.isHuman,this.playerNo); 
        }
        return playerCopy;
    }

}
