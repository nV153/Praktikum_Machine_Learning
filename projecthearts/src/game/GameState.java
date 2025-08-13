package game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import game.AI.AiTypes;
import rules.Rules;

import java.util.Collections;

/**
 * Represents a game and its state.
 * An instance of this class represents a single game, consisting of several rounds and tricks.
 */
public class GameState implements Cloneable{
    
    private Player[] players; 
    private GameRoundTricks gameRoundTricks; 
    private Trick[] roundTricks; 
    private Deck deck; 
    private int tricksPlayed; 
    private GameMode gameMode;
    private Difficulty difficulty;
    private Memory memory;
    private boolean isGameWithPassing;
    private boolean isRoundWithPassing;
    private Difficulty difficulty4thAi;
    private Memory memory4thAi;
    private int numberOfSimulationGames;    
    private int roundNo;
    private boolean[] isSelectedCardInGUI; 
    private int[] cardNumbersInGUI; // cards in hand displayed in GUI; -1 = empty slot
    private int numberOfSelectedCards; 
    private boolean cardsHaveBeenPassed; 
    private boolean isHeartBroken; 
    private boolean isHumanPlayersTurnToMoveCard; 
    private boolean isSimulationModeAbortButtonPressed;
    private boolean isSimulationModeRunning;
    private boolean isMoonShot;
    private boolean[] cardsPlayedInThisRound;
    private List<Integer> listOfCardIdsPlayedInThisRound;
    private int playerToMove;
    private AiTypes ai1Type;
    private AiTypes ai2Type;


    /**
    * Constructs a new game state with default values.
    */  
    public GameState(){        
    }

    /**
    * Initializes the game state for a new game (start button).
    * All attributes are set/reset, and the controller adjusts them accordingly.
    */
    public void init(){
        this.players = new Player[4];        
        this.setAIPlayers();
        this.gameRoundTricks = new GameRoundTricks();
        this.roundTricks = new Trick[13];
        this.cardsPlayedInThisRound = new boolean[52];
        this.listOfCardIdsPlayedInThisRound = new ArrayList<Integer>();
        this.deck = null;
        this.tricksPlayed = 0;
        this.gameMode = null;
        this.difficulty = null;
        this.memory = null;
        this.isGameWithPassing = false;
        this.isRoundWithPassing = false;
        this.difficulty4thAi = null;
        this.memory4thAi = null;
        if (!isSimulationModeRunning) this.numberOfSimulationGames = 0;
        else this.numberOfSimulationGames--;
        this.roundNo = 0;
        this.isSelectedCardInGUI = null;
        this.cardNumbersInGUI = null;
        this.numberOfSelectedCards = 0;
        this.cardsHaveBeenPassed = false;
        this.isHeartBroken = false;
        this.isHumanPlayersTurnToMoveCard = false;
        this.isSimulationModeAbortButtonPressed = false; 
        this.isMoonShot = false;
    }

    /**
    * Sets up the three computer players for the game. 
    */
    public void setAIPlayers(){

        Player player1 = new Player("Frodo", false, 0);
        Player player2 = new Player("Sam", false, 1);
        Player player3 = new Player("Gandalf", false, 2);
           
        players[0] = player1;
        players[1] = player2;
        players[2] = player3;
    }

    /**
    * Creates a human player with the specified name.
    *
    * @param humanPlayerName name of the human player.
    */
    public void setHumanPlayer(String humanPlayerName){
        
        Player humanPlayer = new Player(humanPlayerName, true, 3);
        players[3] = humanPlayer;
    }

    /**
    * Sets the fourth player as an AI player, used in simulation mode.
    */
    public void setSimulationAI(){
        Player aiPlayer = new Player("Aragorn", false, 3);
        players[3] = aiPlayer;
    }

    /**
    * Gets the name of the human player if one exists.
    *
    * @return The name of the human player or a message indicating that there is no human player in the game.
    */
    public String getHumanPlayerName(){
        
        Player player = this.players[3];
        if (player.isHuman()) return player.getName();

        else return "There is no human player in this game.";
    }

    /**
    * Creates the deck.
    */
    public void createDeck(boolean isStandardDeck){
        deck = new Deck(isStandardDeck);
    }

    /**
    * Shuffles the deck.
    */
    public void shuffleDeck(){
        deck.shuffle();
    }
    
    /**
    * Deals cards to players, with each player receiving 13 (for a deck size of 52) or 8 (for a deck size of 32) cards.
    */
    public void handOutCards(){

        int numPlayers = players.length;

        for (int i = 0; i < deck.getSize(); i++){
            Player player = players[i % numPlayers];
            player.addHandCard(deck.getCards()[i]);
        }
    }

    /**
    * Resets the hand cards of all players, removing all cards from their hands.
    */ 
    public void resetHandCardsOfPlayers(){
        for (Player player : players){
            player.resetHandCards();
        }
    }
   
    /**
    * Gets the starting player at the beginning of a round.
    *
    * @return The player who starts the round.
    * @throws IllegalStateException If no player has the required starting card on hand.
    */
    public Player getStarterOfRound() throws IllegalStateException{

        if (deck.getSize() == 52){
           for (Player player : players){
            if (player.hasClubsTwo()) return player;
            }
        }
        if (deck.getSize() == 32){
            for (Player player : players){
                if (player.hasClubsSeven()) return player;
                }
        }
        
        throw new IllegalStateException("Deck was created incorrectly. No player has the required starting card on hand.");
    }

    /**
    * Initializes a new trick for the current round.
    *
    * @param startingPlayer The player number who starts the trick.
    */
    public void initTrick(int startingPlayer){
        roundTricks[tricksPlayed-1] = new Trick(startingPlayer);
    }

    /**
    * Adds a card to the current trick.
    *
    * @param cardId The identifier of the card to be added.
    * @param playerNo The player number adding the card to the trick.
    */
    private void addCardToNewestTrick(int cardId, int playerNo){
        roundTricks[tricksPlayed - 1].addCard(cardId, playerNo);       
    }

    /**
    * Creates a new array of round tricks with a size of 13.
    *
    * @return The created round tricks array.
    */
    public Trick[] createAndSetNewRoundTricks(){
        this.roundTricks = new Trick[13];
        return roundTricks;
    }

    
    /**
    * Adds an array of round tricks to the list of round trick arrays.
    *
    */
    public void addRoundTricksToGameRoundTricks(Trick[] roundTricks){
        this.gameRoundTricks.addRoundTricksToGameRoundTricks(roundTricks);
    }
 
    /**
    * Checks if the current trick is the first trick.
    *
    * @return {@code true} if it is the first trick; otherwise, {@code false}.
    */
    public boolean isFirstTrick(){
        return tricksPlayed == 1;
    }

    /**
     * Removes a card from the hand cards of a player and adds is to the current trick.
     * 
     * @param cardId The identifier of the card to be removed.
     * @param playerNo The player number whose hand cards are being modified.
     */
    public void playCard(int cardId, int playerNo){
        players[playerNo].removeHandCard(cardId);
        addCardToNewestTrick(cardId, playerNo);
        cardsPlayedInThisRound[cardId - 1] = true;
        listOfCardIdsPlayedInThisRound.add(cardId);

    }

    /**
    * Determines the winner of the current trick.
    *
    * @return The player who won the trick.
    */
    public Player determineTrickWinner(){

        Trick trick = getNewestTrick();
        return players[trick.getWinner()];
    }

    /**
    * Updates the round points for the trick winner when a trick is completed.
    *
    * @param trickWinner The player who won the trick.
    */
    public void updateRoundPoints(Player trickWinner){

        Trick trick = getNewestTrick();
        int points = trick.countPoints();
        trickWinner.addTrickPoints(points);
    }

    /**
    * Determines whether at least one player has 100 points or more.
    *
    * @return {@code true} if at least one player has 100 points or more; otherwise, {@code false}.
    */
    public boolean atLeastOnePlayerHas100PointsOrMore(){
        boolean result = false;
        Player[] players = this.getPlayers();
        for (Player player : players){
            if (player.getGamePts()>= 100){
                result = true;
            }
        }
        return result;
    }

    /**
    * Checks if the current round is over.
    *
    * @return {@code true} if the round is over; otherwise, {@code false}.
    */
    public boolean isRoundOver(){

        int requiredTricks = (getDeckSize() == 52) ? 13 : 8;

        if (tricksPlayed == requiredTricks && getNewestTrick().getNumPlayedCards() == 4) {
            return true;
        }
        
        return false;
    }

    /**
    * Updates the game points at the end of a round.
    * Applies Old Moon. If New Moon is required, simply comment/uncomment the corresponding parts.
    */
    public void updateGamePoints(){
        isMoonShot = false;
        Player playerWhoShotTheMoon = null;
        /*
        //New Moon:
        int pointsToSubstract = 0;
        // Check for Shoot the Moon
        for (Player player : players){
            if (this.getDeckSize() == 52){
                if (player.getRoundPts() == 26){
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    pointsToSubstract = 26;
                    break;
                }
            } else{ // Decksize 32
                if (player.getRoundPts() == 21){ 
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    pointsToSubstract = 21;
                    break;
                }
            }
        }
        // Subtract points if Shoot the Moon, otherwise, add round points to all players
        if (isMoonShot){
            playerWhoShotTheMoon.subtractFromGamePoints(pointsToSubstract);
        } else{ 
            for (Player player : players){
                player.addRoundPoints();
            }
        }
        */
        //Old Moon
        int pointsToAdd = 0;
        // Check for Shoot the Moon
        for (Player player : players){
            if (this.getDeckSize() == 52){
                if (player.getRoundPts() == 26){
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    pointsToAdd = 26;
                    break;
                }
            } else{ // Decksize 32
                if (player.getRoundPts() == 21){ 
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    pointsToAdd = 21;
                    break;
                }
            }
        }
        // If Shoot the Moon: Add points to all Players except the player who shot the moon
        // , otherwise, add round points to all players.
        if (isMoonShot){
            for (Player player : players){
                if (player.getPlayerNo() != playerWhoShotTheMoon.getPlayerNo()){
                    player.addToGamePoints(pointsToAdd);
                }             
            }            
        } else{ 
            for (Player player : players){
                player.addRoundPoints();
            }
        }
    }

    /**
    * Determines the winner or winners of the game.
    *
    * @return An ArrayList containing the winner or winners of the game.
    */
    public ArrayList<Player> determineListOfGameWinnerOrWinners(){
        ArrayList<Player> listOfGameWinnerOrWinners = new ArrayList<Player>();
        int currentLowestGamePoints = 125; 
        // determine lowest points
        for (Player player : this.getPlayers()){
            if (player.getGamePts() <= currentLowestGamePoints){
                currentLowestGamePoints = player.getGamePts();
            }
        }
        // determine player with lowest points
        for (Player player : this.getPlayers()){
            if (player.getGamePts() == currentLowestGamePoints){
                listOfGameWinnerOrWinners.add(player);
            }
        }
        return listOfGameWinnerOrWinners;
    }

    /**
    * Updates the payoff for each player.
    *
    */
    public void updatePayoff(){
        ArrayList<Player> playerRanking = new ArrayList<>();
        double[] payoff = new double[4];

        for (Player player : players){         
            playerRanking.add(player);   
        }

        int numberOfWinners = 0;
        int ptsFirst;
        int ptsScnd;
        int ptsThird;
        int ptsFourth;
        
        if (isMoonShot) {
            // player who shot the moon has 26 (or 21) points
            Collections.sort(playerRanking, (p1, p2) -> Integer.compare(p2.getRoundPts(), p1.getRoundPts()));
            numberOfWinners = 1;
            ptsFirst = 0;
            int shootTheMoonPoints = (getDeckSize() == 52) ? 26 : 21;
            ptsScnd = shootTheMoonPoints;
            ptsThird = shootTheMoonPoints;
            ptsFourth = shootTheMoonPoints; 
        } 

        else {
            Collections.sort(playerRanking, (p1, p2) -> Integer.compare(p1.getRoundPts(), p2.getRoundPts()));
            numberOfWinners++;
            // max 3 winners
            for (int i = 0; i < 2; i++){
                if (playerRanking.get(i).getRoundPts() == playerRanking.get(i+1).getRoundPts()) numberOfWinners++;
                else break;
            }

            ptsFirst = playerRanking.get(0).getRoundPts();
            ptsScnd = playerRanking.get(1).getRoundPts();
            ptsThird = playerRanking.get(2).getRoundPts();
            ptsFourth = playerRanking.get(3).getRoundPts();
        }

        // more than one winner -> distribute points equally
        if (numberOfWinners > 1) { 
            double payoffWinners = (ptsScnd + ptsThird + ptsFourth - 3*ptsFirst) / numberOfWinners;
            payoff[playerRanking.get(0).getPlayerNo()] = payoffWinners;
            payoff[playerRanking.get(1).getPlayerNo()] = payoffWinners;
            if (numberOfWinners == 3){
                payoff[playerRanking.get(2).getPlayerNo()] = payoffWinners;
            }
            else payoff[playerRanking.get(2).getPlayerNo()] = ptsFirst - ptsThird;
        }
        
        // case one winner
        else {
            payoff[playerRanking.get(0).getPlayerNo()] = ptsScnd + ptsThird + ptsFourth - 3*ptsFirst;
            payoff[playerRanking.get(1).getPlayerNo()] = ptsFirst - ptsScnd;
            payoff[playerRanking.get(2).getPlayerNo()] = ptsFirst - ptsThird;
        }
            
        payoff[playerRanking.get(3).getPlayerNo()] = ptsFirst - ptsFourth;

        for (int i = 0; i < 4; i++){
            players[i].updatePayoffValue(payoff[i]);
        }
    }

    /**
    * Determines the winner or winners of the round.
    *
    * @return An ArrayList containing the winner or winners of the round.
    */
    public ArrayList<Player> determineListOfRoundWinnerOrWinners(){
        ArrayList<Player> listOfRoundWinnerOrWinners = new ArrayList<Player>();
        int currentLowestRoundPoints = 26; 
        // determine lowest points
        for (Player player : this.getPlayers()){
            if (player.getRoundPts() <= currentLowestRoundPoints){
                currentLowestRoundPoints = player.getRoundPts();
            }
        }
        // determine player with lowest points
        for (Player player : this.getPlayers()){
            if (player.getRoundPts() == currentLowestRoundPoints){
                listOfRoundWinnerOrWinners.add(player);
            }
        }
        
        isMoonShot = false;
        Player playerWhoShotTheMoon = null;
        // Check for Shoot the Moon
        for (Player player : players){
            if (this.getDeckSize() == 52){
                if (player.getRoundPts() == 26){
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    break;
                }
            } else{ // decksize 32
                if (player.getRoundPts() == 21){
                    isMoonShot = true;
                    playerWhoShotTheMoon = player;
                    break;
                }
            }
        }
        
        if (isMoonShot){
            listOfRoundWinnerOrWinners.clear();
            listOfRoundWinnerOrWinners.add(playerWhoShotTheMoon);
        }        

        return listOfRoundWinnerOrWinners;
    }

    /*
     * Resets the array with cards played in this round.
     */
    public void resetCardsPlayedInThisRound(){
        this.cardsPlayedInThisRound = new boolean[52];
    }

    /**
    * Resets the round points for every player to 0.
    */
    public void resetRoundPoints(){
        for (Player player : players){
            player.resetRoundPoints();
        }
    }

    /**
    * Resets the cards passed by every player.
    */
    public void resetCardsPassed(){
        for (Player player : players){
            player.resetCardsToPass();
        }
    }

    /**
    * Gets all cards played by a player in the current round.
    *
    * @param playerNo The player number
    * @return The list of cards played by the specified player in the current round.
    */
    public ArrayList<Integer> getCardsPlayedByPlayerInThisRound(int playerNo){
        ArrayList<Integer> cardsPlayed = new ArrayList<Integer>();
            for (int  i = 0; i < getTricksPlayed()-1;i++){             
                    cardsPlayed.add(roundTricks[i].getCardPlayedByPlayer(playerNo));
            }
        return cardsPlayed;
    }

    /**
    * Gets the number of cards of suit heart played in the current round.
    *
    * @return The number of cards of suit heart played in this round.
    */
    public int getNumberOfHeartsPlayedInThisRound(){
        int num = 0;
        for (int i = CardCoding.HEARTS_2.getId() -1; i < CardCoding.HEARTS_ACE.getId(); i++){
            if (cardsPlayedInThisRound[i]) num++;
        }
        return num;
    }

    /**
    * Determines the list of playable cards for the specified player.
    *
    * @param playerNo The number representing the player.
    * @return A list of playable cards for the specified player.
    */
    public List<Card> determineListOfPlayableCards(int playerNo){
        List<Card> result = new LinkedList<Card>();
        for (int cardNumber = 1; cardNumber <= this.getPlayers()[playerNo].getHandCardsPlayable().getIsInHand().length; cardNumber++){
            if (this.getPlayers()[playerNo].getHandCardsPlayable().getIsInHand()[cardNumber-1] == true){
                result.add(new Card(cardNumber)); 
                                                    
            }
        }
        return result;
    }

    /**
    * Determines the list of playable card IDs for the specified player.
    *
    * @param playerNo The number representing the player.
    * @return A list of playable card IDs for the specified player.
    */
    public List<Integer> determineListOfPlayableCardIds(int playerNo){
        List<Integer> result = new ArrayList<>();
        boolean[] isInHand = this.getPlayers()[playerNo].getHandCardsPlayable().getIsInHand();  
        
        for (int i = 0; i < isInHand.length; i++) {
            if (isInHand[i]) {
            result.add(i + 1);
            }
        }
        return result;
    }

    /**
     * Determines and returns the player who is to make the next move.
     *
     * @return The number of the player who is to make the next move.
     *         If the newest trick has no played cards, the starter of the trick is to move.
     *         If the newest trick has all 4 cards played, the winner of the trick is to move.
     *         In all other cases, the player to move is determined by adding the number of played cards to the starter's number
     *         and taking the remainder when divided by 4.
     */
    public int getPlayerToMove(){
        
        Trick newestTrick = getNewestTrick();

        if (newestTrick.getNumPlayedCards() == 0) {
            playerToMove = newestTrick.getStarter();
        }
        else if (newestTrick.getNumPlayedCards() == 4) {
            playerToMove = newestTrick.getWinner();
        }
        else {
            playerToMove = (newestTrick.getStarter() + newestTrick.getNumPlayedCards()) % 4;
        }
        return playerToMove;
    }

    /**
     * Calculates and returns the normalized payoff for a specified player.
     *
     * @param playerNr The number of the player for whom the normalized payoff is to be calculated.
     *                 Player numbers start from 0.
     * @return The normalized payoff for the specified player. The payoff is normalized by taking the cubic root of the payoff.
     */
    public double getResult(int playerNr) {
        double normalizedPayoff = Math.cbrt(players[playerNr].getPayoff());
        return normalizedPayoff;
    }

    public double getPayoff(int playerNo){
        return players[playerNo].getPayoff();
    }

    /**
     * Performs a determinization step for a specified observer player.
     * This method simulates the uncertainty in a card game by shuffling the unknown cards and redistributing them among the players.
     *
     * @param observer The number of the player who is observing the game state.
     *                 This player's hand cards are not changed.
     *                 For all other players, their hand cards are reset and then set to a random subset of the unknown cards.
     */
    public void determinization(int observer) {
        List<Integer> unknownCards = new ArrayList<Integer>();
    
        for (Card i : deck.getCards()){
            if (!players[observer].hasHandCard(i)) {
                unknownCards.add(i.getId());
            }
        }

        unknownCards.removeAll(getCardsPlayedWithMemory(observer));
        Collections.shuffle(unknownCards);

        for (Player player : players){
            if (player.getPlayerNo() != observer) {
                int numCards = player.countCards();
                List<Integer> dealtCards = unknownCards.subList(0, numCards);
                player.resetHandCards();
                player.setHandCards(convertToHandCards(dealtCards));
                unknownCards = unknownCards.subList(numCards, unknownCards.size());
            }
        }
    }

    /** 
     * Gets cards played in this round, considering the set level of memory. 
     * 
     */
    public List<Integer> getCardsPlayedWithMemory(int observer) {
        List<Integer> playedCards = new ArrayList<Integer>();
        Memory memory = getMemoryOfPlayer(observer);
        int lastTrickToRemember = memory.getTricksToConsider(getTricksPlayed(), getDeckSize());

        for (int i = lastTrickToRemember - 1; i < tricksPlayed; i++) {
            int[] cardsPlayed = getRoundTricks()[i].getCardsPlayed();
            for (int cardId : cardsPlayed) {
                if (cardId != 0) {
                    playedCards.add(cardId);
                }
            }
        }
        return playedCards;
    }

    /**
     *  Converts a list of card ids in a handCard-object. 
     */
    private HandCards convertToHandCards(List<Integer> cardIds) {
        HandCards handCards = new HandCards();
        for (int cardId : cardIds) {
            handCards.addCard(cardId);
        }
        return handCards;
    }
    
    /**
     * Performs a move in the game by playing a card for the player who is to move.
     *
     * @param cardNr The number of the card to be played.
     *               If the newest trick has all 4 cards played, the trick is won, hearts may be broken, round points are updated, and the payoff is updated.
     *               If the round is not over, a new trick is initialized and the playable cards for the starter of the next trick are set.
     *               If the newest trick has at least one card played, the playable card for the next player is set.
     */
    public void doMove(int cardNr) {
        playerToMove = getPlayerToMove();
        playCard(cardNr, playerToMove);

        if (getNewestTrick().getNumPlayedCards() == 4) {
            Player currentTrickWinner = determineTrickWinner();
            playerToMove = currentTrickWinner.getPlayerNo();
            updateHeartsBroken();
            updateRoundPoints(currentTrickWinner);
            updatePayoff();
            
            if (!isRoundOver()) {
                tricksPlayed++;
                initTrick(currentTrickWinner.getPlayerNo());
                setPlayableCardsForStarterInTrick2orLater(currentTrickWinner);
            }
        } else if (getNewestTrick().getNumPlayedCards() > 0) {
            setPlayableCardForNextPlayer(determineTrickWinner());
        } 
    }

    /* Sets the playable cards for the next player. */
    private void setPlayableCardForNextPlayer(Player currentTrickWinner) {
        int playerToMove = getPlayerToMove();
        if (playerToMove != currentTrickWinner.getPlayerNo()) {
            if (isFirstTrick()) {
                players[playerToMove].setHandCardsPlayableFromRulesAndIsInHandInTrick0or1ForAiDeterminization(this, players[getStarterOfNewestTrick()]);
            }
            else {
                setPlayableCardsForNonStarterInTrick2orLater(players[playerToMove], getFirstCardOfNewestTrick());
            }
        } 
    }

    /** 
     * Checks if hearts got broken by the current trick and, if yes, sets the GameState attribute isHeartBroken to true.
     */
    private void updateHeartsBroken(){
        if (isHeartBroken == false){
                for (int cardPlayed : getNewestTrick().getCardsPlayed()){
                    if (cardPlayed >= 14 && cardPlayed <= 26){
                        isHeartBroken = true;
                    }
                }
            }
    }

    /**
     * Performs a pass action in the game for a specified player.
     *
     * @param playerNo        The number of the player who is performing the pass action.
     * @param passCombination An array of integers representing the cards that the player is passing.
     */
    public void doPass(int playerNo, int[] passCombination) {
        clearPassedCards();
        setPlayableCardsForEachPlayerInTrick0or1();
        savePassedCards(playerNo, passCombination);
        passCardsToReceivers();
        initializeNewTrick();
    }

    /* Clears the passed cards. */
    private void clearPassedCards() {
        Player player1 = players[0]; //AI-Player 1
        Player player2 = players[1]; //AI-Player 2
        Player player3 = players[2]; //AI-Player 3
        Player player4 = players[3]; //AI-Player 4 or Human-Player

        player1.getCardsToPassOriginalReference().clear();
        player2.getCardsToPassOriginalReference().clear();
        player3.getCardsToPassOriginalReference().clear();
        player4.getCardsToPassOriginalReference().clear();
        
        int roundNo = getRoundNo();
        int player1ReceiverNo = Rules.getPassReceiverNo(player1.getPlayerNo(), roundNo);
        player1.setReceiverNo(player1ReceiverNo);
        int player2ReceiverNo = Rules.getPassReceiverNo(player2.getPlayerNo(), roundNo);
        player2.setReceiverNo(player2ReceiverNo);
        int player3ReceiverNo = Rules.getPassReceiverNo(player3.getPlayerNo(), roundNo);
        player3.setReceiverNo(player3ReceiverNo);
        int player4ReceiverNo = Rules.getPassReceiverNo(player4.getPlayerNo(), roundNo);
        player4.setReceiverNo(player4ReceiverNo);
    }

    /* Saves the passed cards. */
    private void savePassedCards(int observer, int[] passCombination) {
        for (Player player : players) {
            if (player.getPlayerNo() != observer) {
                int[] randomPass = player.getHandCards().generateRandomPass();
                player.savePassedCards(randomPass);
            } else {
                player.savePassedCards(passCombination);
            }
        }
    }

    /* Passes the saved cards to the pass receiver. */
    private void passCardsToReceivers() {
        for(Player player : players) {
            player.passCards(players[player.getReceiverNo()]);
        }
        cardsHaveBeenPassed = true;
    }

    /* Initializes a new trick by determing the starter of the round and resetting tricksPlayed. */
    private void initializeNewTrick() {
        setTricksPlayed(tricksPlayed + 1);
        int numberOfStarter = getStarterOfRound().getPlayerNo();                              
        initTrick(numberOfStarter);
    }

    /**
     * Sets the playable cards for each player in trick 0 or 1 according to the rules.
     */
    public void setPlayableCardsForEachPlayerInTrick0or1(){
        for (int i = 0; i < players.length; i++){
            players[i].setHandCardsPlayableFromRulesAndIsInHandInTrick0or1(this);
        }
    }

    /**
     * Sets the playable cards for the starter in trick 2 or later according to the rules.
     * @param startingPlayer The starting player.
     */
    public void setPlayableCardsForStarterInTrick2orLater(Player startingPlayer){
        startingPlayer.setHandCardsPlayableFromRulesAndIsInHandForStarterInTrick2orLater(this);
    }

    /**
     * Sets the playable cards for a non-starting player in trick 2 or later according to the rules.
     * @param startingPlayer The non-starting player for whom the playable cards will be set.
     * @param firstCardNumberPlayed The number of the first played card in the trick.
     */
    public void setPlayableCardsForNonStarterInTrick2orLater(Player nonStartingPlayer, int firstCardNumberPlayed){
        nonStartingPlayer.setHandCardsPlayableFromRulesAndIsInHandForNonStarterInTrick2orLater(this, firstCardNumberPlayed);
    } 
    
    /**
     * Gets the starter of the current trick.
     * 
     * @return the starter of the current trick or -1, if the round hasn't started yet
     **/ 
    public int getStarterOfNewestTrick(){
        if (tricksPlayed > 0) return roundTricks[tricksPlayed - 1].getStarter();
        else return -1;
    }

    public Trick getNewestTrick(){
        return roundTricks[tricksPlayed - 1];
    }

    public int getFirstCardOfNewestTrick(){
        return getNewestTrick().getStartingCard();
    }

    public Player[] getCopyOfPlayers(){ 
        return players.clone();
    }

    public Player[] getPlayers() {
        return players;
    }
    
    public Trick[] getRoundTricksClone(){
        return roundTricks.clone();
    }

    public Trick[] getRoundTricks() {
        return this.roundTricks;
    }
    
    public int getRoundNo() {
        return roundNo;
    }
    
    public void setRoundNo(int roundNo){
        this.roundNo = roundNo;
    }
    
    public GameMode getGameMode(){
        return gameMode;
    }  

    public void setGameMode(GameMode gameMode){
        this.gameMode = gameMode;
    }
    
    public Difficulty getDifficulty(){
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty){
        this.difficulty = difficulty;
    }

    public Memory getMemory(){
        return memory;
    }

    public void setMemory(Memory memory){
        this.memory = memory;
    }    

    public Difficulty getDifficulty4thAi() {
        return difficulty4thAi;
    }

    public void setDifficulty4thAi(Difficulty difficulty4thAi) {
        this.difficulty4thAi = difficulty4thAi;
    }

    public Memory getMemory4thAi() {
        return memory4thAi;
    }

    public void setMemory4thAi(Memory memory4thAi) {
        this.memory4thAi = memory4thAi;
    }

    public void setRoundWithPassing(boolean isRoundWithPassing) {
        this.isRoundWithPassing = isRoundWithPassing;
    }

    public boolean getIsRoundWithPassing(){
        return this.isRoundWithPassing;
    }

    
    public boolean getIsGameWithPassing(){
        return this.isGameWithPassing;
    }
    
    public void setIsGameWithPassing(boolean isGameWithPassing) {
        this.isGameWithPassing = isGameWithPassing;
    }

    public int getTricksPlayed(){
        return tricksPlayed;
    }

    public void setTricksPlayed(int tricksPlayed) {
        this.tricksPlayed = tricksPlayed;
    }

    public Deck getDeck(){
        return deck;
    }

    public int getDeckSize(){
        return this.deck.getSize();
    }

    public boolean[] getIsSelectedCardInGUI() {
        return isSelectedCardInGUI;
    }

    public void setIsSelectedCardInGUI(boolean[] isSelectedCardInGUI) {
        this.isSelectedCardInGUI = isSelectedCardInGUI;
    }

    public int getNumberOfSelectedCards() {
        return numberOfSelectedCards;
    }

    public void setNumberOfSelectedCards(int numberOfSelectedCards) {
        this.numberOfSelectedCards = numberOfSelectedCards;
    }

    public boolean getCardsHaveBeenPassed(){
        return this.cardsHaveBeenPassed;
    }

    public boolean[] getCardsPlayedInThisRound(){
        return this.cardsPlayedInThisRound;
    }

    public List<Integer> getListOfCardIdsPlayedInThisRound(){
        return this.listOfCardIdsPlayedInThisRound;
    }

    public boolean hasCardBeenPlayedInThisRound(int cardId){
        return this.cardsPlayedInThisRound[cardId - 1];
    }

    public void setCardsHaveBeenPassed(boolean cardsHaveBeenPassed) {
        this.cardsHaveBeenPassed = cardsHaveBeenPassed;
    }

    public int[] getCardNumbersInGUI() {
        return cardNumbersInGUI;
    }

    public void setCardNumbersInGUI(int[] cardNumbersInGUI) {
        this.cardNumbersInGUI = cardNumbersInGUI;
    }

    public boolean getIsHumanPlayersTurnToMoveCard() {
        return this.isHumanPlayersTurnToMoveCard;
    }

    public void setHumanPlayersTurnToMoveCard(boolean isHumanPlayersTurnToMoveCard) {
        this.isHumanPlayersTurnToMoveCard = isHumanPlayersTurnToMoveCard;
    }

    public boolean getIsHeartBroken(){
        return this.isHeartBroken;
    }

    public void setIsHeartBroken(boolean isHeartBroken) {
        this.isHeartBroken = isHeartBroken;
    } 

    public boolean getIsMoonShot(){
        return this.isMoonShot;
    }

    public int getNumberOfSimulationGames() {
        return numberOfSimulationGames;
    }

    public void setNumberOfSimulationGames(int numberOfSimulationGames) {
        this.numberOfSimulationGames = numberOfSimulationGames;
    }

    public boolean getIsSimulationModeRunning(){
        return this.isSimulationModeRunning;
    }

    public void setIsSimulationModeRunning(boolean isRunning){
        this.isSimulationModeRunning = isRunning;
    }

    public boolean getIsSimulationModeAbortButtonPressed() {
        return this.isSimulationModeAbortButtonPressed;
    }  

    public void setIsSimulationModeAbortButtonPressed(boolean isSimulationModeAbortButtonPressed) {
        this.isSimulationModeAbortButtonPressed = isSimulationModeAbortButtonPressed;
    } 


    /**
     * Copies this game state object. A deep copy of its attributes is made were necessary, i.e., where later changes in the
     * original game state object are prohibited, and a shallow copy of its attributes are made where such a deep copy is not necessary.
     * @return A copy of this game state.
     */
    public GameState deepCopy() {        
        return (GameState) this.clone();
    }

    
    /**
     * Method that clones this GameState object and returns the clone.
     * According to https://www.baeldung.com/java-deep-copy , the reader of this code should 
     * note that the super.clone() call returns a shallow copy of an object, 
     * but we set deep copies of mutable fields manually, so the result is correct.
     * See this class' method deepCopy for further explanantion. Mutable fields are:
     * players, roundTricks, cardsPlayedInThisRound.
     */
    @Override
    public Object clone() {
        GameState gameStateCopy = null;
        try {
            gameStateCopy = (GameState) super.clone();
            gameStateCopy.players = new Player[this.players.length];
            for (int i = 0; i < this.players.length; i++){
                if (this.players[i] != null){
                    gameStateCopy.players[i] = (Player) this.players[i].clone();
                }
            }
            gameStateCopy.roundTricks = new Trick[this.roundTricks.length];
            for (int i = 0; i < this.roundTricks.length; i++){
                if (this.roundTricks[i] != null){
                    gameStateCopy.roundTricks[i] = (Trick) this.roundTricks[i].clone();
                }
            }
            gameStateCopy.cardsPlayedInThisRound = new boolean[this.cardsPlayedInThisRound.length];
            for (int i = 0; i < this.cardsPlayedInThisRound.length; i++){
                gameStateCopy.cardsPlayedInThisRound[i] = this.cardsPlayedInThisRound[i];
            }          
        } catch (CloneNotSupportedException e) {
            System.out.println("clone not supported for class: " + this.getClass().getName());
            gameStateCopy = new GameState();
        }
        return gameStateCopy;
    }

    /**
     * Returns the difficulty level of the specified player.
     *
     * @param playerNo The number of the player for whom the difficulty level is to be returned.
     *                 Player numbers start from 0.
     * @return The difficulty level of the player. If the player number is 3, the difficulty level of the fourth AI is returned.
     *         For all other player numbers, the general difficulty level is returned.
     */
    public Difficulty getDifficultyOfPlayer(int playerNo){
        if (playerNo == 3) 
        return this.difficulty4thAi;
        else return this.difficulty;
    }

    /**
     * Returns the memory level of the specified player.
     *
     * @param playerNo The number of the player for whom the memory level is to be returned.
     *                 Player numbers start from 0.
     * @return The memory level of the player. If the player number is 3, the memory level of the fourth AI is returned.
     *         For all other player numbers, the general memory level is returned.
     */
    public Memory getMemoryOfPlayer(int playerNo){
        if (playerNo == 3) 
        return this.memory4thAi;
        else return this.memory;
    }

    public void setAi1Type(AiTypes ai1Type){
        this.ai1Type = ai1Type;
    }
    
    public AiTypes getAi1Type(){
        return this.ai1Type;
    }

    public void setAi2Type(AiTypes ai2Type){
        this.ai2Type = ai2Type;
    }

    public AiTypes getAi2Type(){
        return this.ai2Type;
    }

    
}
