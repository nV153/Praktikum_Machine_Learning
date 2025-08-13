package game.AI.AiRuleBased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import game.*;
import game.AI.AiInterface;
import game.AI.Utils.CardEvaluation;
import game.AI.Utils.GameplayAnalysis;
import game.AI.Utils.SuitCount;

public class AiRuleBased implements AiInterface{
    private static final int MAX_PASS_CARDS = 3;
    private static final int DEFAULT_SCORE_IN_HAND = 100;
    private static final int DEFAULT_SCORE_NOT_IN_HAND = -10000;

    private int trickWhenHeartBroken;
    private boolean passedForStM;
    private boolean heartbroken;                     
    private Difficulty difficulty;
    private Memory memory;
    private Player aiPlayer;
    private HandCards starterHandCards;
    
    public boolean isPassedForStM() {return passedForStM;}
    public boolean isHeartbroken() {return heartbroken;}
    public Memory getMemory() {return memory;}
    public Difficulty getDifficulty() {return difficulty;}
    
    private boolean isExpertDifficulty() {return difficulty == Difficulty.EXPERT;}
    private boolean isHardDifficulty() {return difficulty == Difficulty.HARD;}
    private boolean isMediumDifficulty() {return difficulty == Difficulty.MEDIUM;}

    public AiRuleBased(){
        trickWhenHeartBroken = -1;
    }

    /**
     * {@inheritDoc}
     * @see AiRuleBased#bestPass(GameState, int, int)
     */
    @Override
    public int [] bestPass(GameState gameState, int playerNo, int receiverNo) {
        int numPassedCards = 0;
        int[] bestCardsToPass = new int[MAX_PASS_CARDS];
        aiPlayer = gameState.getPlayers()[playerNo];
        starterHandCards = aiPlayer.getHandCards();
        difficulty = gameState.getDifficultyOfPlayer(playerNo);
        memory = gameState.getMemoryOfPlayer(playerNo);

        if (difficulty == Difficulty.EASY) {
            return rndPass();
        }

        if (shouldTryShootTheMoon()) {
            return passForSlam(bestCardsToPass, numPassedCards);
        }

        numPassedCards = passSpades(bestCardsToPass, numPassedCards);
        numPassedCards = passHearts(bestCardsToPass, numPassedCards);
        numPassedCards = passOther(bestCardsToPass, numPassedCards);

        if (numPassedCards < MAX_PASS_CARDS) {
            fillRemainingCards(bestCardsToPass, numPassedCards);
        }

        return bestCardsToPass;
    }

    /**
     * {@inheritDoc}
     * @see AiRuleBased#bestMove(GameState, int)
     */
    @Override
    public int bestMove(GameState gameState, int playerNo) {
        aiPlayer = gameState.getPlayers()[playerNo];
        GameplayAnalysis gameAnalysis = new GameplayAnalysis(gameState, playerNo);   
        HandCards currentHandCards = aiPlayer.getHandCardsPlayable();
        ArrayList<Card> playableCards = CardEvaluation.getPlaybleCards(currentHandCards);
        Trick currentTrick = gameState.getNewestTrick();
        Trick[] allTricks = gameState.getRoundTricks();         
        int cardIdWinningTrick = currentTrick.getHighCard();                     
        difficulty = gameState.getDifficultyOfPlayer(playerNo);
        memory = gameState.getMemoryOfPlayer(playerNo);
        
        if (playableCards.size() == 1){
            return playableCards.get(0).getId();
        }
        
        if (difficulty == Difficulty.EASY) {
            return rndMove();
        }  
        
        int aiPlayerPos = getPlayerPosition(currentTrick.getStarter(), playerNo);          
        int tricksToConsider = memory.getTricksToConsider(gameState.getTricksPlayed(), gameState.getDeckSize());
        trickWhenHeartBroken = getTrickWhenHeartBroken(gameState);
        heartbroken = memory.updateHeartBrokenStatus(gameState.getIsHeartBroken(), trickWhenHeartBroken, tricksToConsider);
        int[] cardScore = setCardScore(currentHandCards);
        SuitCount suitCount = CardEvaluation.countSuitsWithCards(currentHandCards);  
        boolean[] cardsPlayed = CardEvaluation.calculatePlayedCards(allTricks, tricksToConsider, gameState.getTricksPlayed());
        boolean[] cardsAvailable = CardEvaluation.calculateAvailableCards(cardsPlayed, currentHandCards);
        boolean[][] suitVoidsByPlayer = gameAnalysis.checkForPlayerVoids(allTricks, gameState.getTricksPlayed(),tricksToConsider);
        Player playerGoingForStM = gameAnalysis.findStMPlayer(difficulty);
                            

        if (gameAnalysis.shouldPlayForStM(difficulty, passedForStM)) {
            return playforStM(cardScore, playableCards, gameState, currentTrick, suitCount); 
        }          

        if (playerGoingForStM != null) {
            cardScore = stopPlayerFromStM(cardScore, playerGoingForStM, currentTrick, playableCards); 
        }

        //Calculate which Card should be played 
        if(gameState.isFirstTrick()){
            cardScore = ruleWinFirstTrick(cardScore, playableCards, gameState);
        }else{          
            if(aiPlayerPos == 0){
                cardScore = ruleDontLeadWithOnlyHeart(cardScore, aiPlayerPos, playableCards, cardsAvailable); 
                cardScore = ruleIfFirstPlayLow(cardScore, aiPlayerPos, playableCards, gameState, currentTrick);
            } else {
                cardScore = ruleHighCardNoWin(cardScore, aiPlayerPos, playableCards, gameState, cardIdWinningTrick ,suitVoidsByPlayer);
                cardScore = ruleHighCardEarlyGame(cardScore, aiPlayerPos, playableCards, gameState, currentTrick, suitVoidsByPlayer);
                cardScore = ruleGiveOtherPoints(cardScore, aiPlayerPos, playableCards, cardIdWinningTrick, currentHandCards);     
                cardScore = ruleHighCardZeroPoints(cardScore, aiPlayerPos, playableCards, gameState, currentTrick);                                        
                cardScore = rulePlaySafeHighValueCards(cardScore, playableCards, allTricks, tricksToConsider, gameState.getTricksPlayed());         //TODO        
            }        
            cardScore = ruleAvoidQoS(cardScore, tricksToConsider, cardsAvailable, currentHandCards, currentTrick);            
            cardScore = ruleAvoidOtherVoids(cardScore, aiPlayerPos, playableCards, gameState, currentTrick, suitVoidsByPlayer);                                    
        }        
        cardScore = createVoids(cardScore , suitCount);

        if (isNoRuleApplied(cardScore, playableCards)) {
            return rndMove();
        }

        return chooseCardForDifficulty(cardScore, playableCards);
    }

    //Determines the trick number when hearts were first broken in the game.
    private int getTrickWhenHeartBroken(GameState gameState) {
        
        if(gameState.isFirstTrick()){
            trickWhenHeartBroken = -1;
        }

        if (gameState.getIsHeartBroken() && trickWhenHeartBroken == -1) {
            trickWhenHeartBroken = gameState.getTricksPlayed();
        }
        return trickWhenHeartBroken;
    }

    // Chooses a card based on difficulty. Sorts playable cards by score and selects randomly within a percentage range.
    private int chooseCardForDifficulty(int[] cardScore, ArrayList<Card> playableCards) {
        Collections.sort(playableCards, (card1, card2) -> Integer.compare(cardScore[card2.getId() - 1], cardScore[card1.getId() - 1]));

        int index = 0;

        if (difficulty == Difficulty.HARD) {
            index = getRandomIndexWithinRange(playableCards, 0.20, 0.50);
        } else if (difficulty == Difficulty.MEDIUM) {
            index = getRandomIndexWithinRange(playableCards, 0.30, 0.80);
        }

        return playableCards.get(index).getId();
    }

    // Checks if no scoring rule has been applied to any of the playable cards.
    private boolean isNoRuleApplied(int[] cardScore, ArrayList<Card> playableCards) {
        int noRuleApplied = 0;

        for (Card card : playableCards) {
            if (cardScore[card.getId() - 1] == DEFAULT_SCORE_IN_HAND) {
                noRuleApplied++;
            }
        }
    
        return noRuleApplied == playableCards.size();
    }

    // Generates a random index within a specified range of percentages of the PlayableCards size.
    private int getRandomIndexWithinRange(ArrayList<Card> playbleCards, double minPercentage, double maxPercentage) {
        int minIndex = Math.max((int) Math.ceil(minPercentage * playbleCards.size()), 1);
        int maxIndex = Math.max((int) Math.ceil(maxPercentage * playbleCards.size()), 1) - 1;
        maxIndex = Math.max(minIndex, maxIndex); 
    
        Random random = new Random();
        return minIndex + random.nextInt(maxIndex - minIndex + 1);
    }

    // Modifies card scores to strategically counter a player attempting "Shoot the Moon", focusing on winning tricks when hearts are in play.
    private int[] stopPlayerFromStM(int[] cardScore, Player stMPlayer, Trick currentTrick, ArrayList<Card> playableCards) {
        int cardPlayedByStMPlayer = currentTrick.getCardPlayedByPlayer(stMPlayer.getPlayerNo());

        if (cardPlayedByStMPlayer != 0 && CardCoding.getSuitById(cardPlayedByStMPlayer) == Suits.HEARTS && aiPlayer.getGamePts() < 99) {
            Card highestHeartCard = CardEvaluation.findHighestCardInSuit(playableCards, Suits.HEARTS);

            if (highestHeartCard != null && highestHeartCard.getId() > cardPlayedByStMPlayer) {
                cardScore[highestHeartCard.getId() - 1] += 1000;
            }
        }
        return cardScore;
    }
    

    private int playforStM(int[] cardScore , ArrayList<Card> playableCards , GameState gameState , Trick currentTrick , SuitCount suitCount) {
        int min = -1;
        int max = -1;
        
        if(gameState.isFirstTrick()){
            for(Card a : playableCards){
                if(min == -1){
                    min = a.getId();
                }
                if(new Card(min).isHigherAs(a)){
                    min  = a.getId();
                }
            }
            return min;
        }

        if((heartbroken || (currentTrick.countPoints() != 0 ) )){         
            for(Card a : playableCards){                                              
                if(max == -1){
                    max = a.getId();
                }
                if(a.isHigherAs(new Card(max))){
                    max = a.getId();
                }else if(!a.isHigherAs(new Card(max)) & !new Card(max).isHigherAs(a) & (new Card(max).getSuit() == Suits.HEARTS)){   //If multiple max Cards dont play Heart
                    max = a.getId();
                }
            }
            return max;
        } else{                                         
            for(Card a : playableCards){               
                if(min == -1){
                    min = a.getId();
                }
                if(new Card(min).isHigherAs(a) & !a.isHeart()){
                    min  = a.getId();
                }else if(!new Card(min).isHigherAs(a)  & !new Card(min).isHigherAs(a) ){
                    cardScore[a.getId()-1] = cardScore[a.getId()-1]+1;
                }
            }
            cardScore[new Card(min).getId()-1] = cardScore[new Card(min).getId()-1] + 1;
            if((new Card(min).getId() % 13 > 5) & (new Card(min).getId() % 13 != 0 )){        
                int[] tempp = new int[4];
                tempp[0] = suitCount.cntKaro;
                tempp[1] = suitCount.cntHearts;
                tempp[2] = suitCount.cntPic;
                tempp[3] = suitCount.cntKreuz;
                Suits[] tempS = new Suits[4];
                tempS[0] = Suits.DIAMONDS;
                tempS[1] = Suits.HEARTS;
                tempS[2] = Suits.SPADES;
                tempS[3] = Suits.CLUBS;
    
                for(int i = 0; i <4 ; i++){
                    if(i != 1){                        
                        if( tempp[i] < 4){
                            cardScore = plus(cardScore, tempS[i]);          
                            cardScore = plus(cardScore, tempS[i]);
                                if( tempp[i] < 3){
                                cardScore = plus(cardScore, tempS[i]);
                                if( tempp[i] < 2){
                                    cardScore = plus(cardScore , tempS[i]);
                                    }
                                }
                            }  
                    }
                }
            }
        }
        return CardEvaluation.getBestCard(cardScore);  
    }

    // Initializes and sets card scores for hand cards. Cards in hand are scored positively, others negatively.
    private int[] setCardScore(HandCards currentHandCards) {
        int[] cardScore = new int[52];
        for (int i = 1; i < 53; i++) {
            if (currentHandCards.containsCard(i)) {
                cardScore[i - 1] = DEFAULT_SCORE_IN_HAND;
            } else {
                cardScore[i - 1] = DEFAULT_SCORE_NOT_IN_HAND;
            }
        }
        return cardScore;
    }

    // Calculates the relative position of the current player with respect to the starter player, adjusting for circular seating.
    private int getPlayerPosition(int starterPlayerNo, int currentPlayerNo) {
        int playerPosition = currentPlayerNo - starterPlayerNo;

        if (playerPosition < 0) {
            playerPosition += 4;
        }
        
        return playerPosition;
    }
    
    /**
     * Selects a random playable card from the AI player's hand.
     * This method counts the number of playable cards and selects one at random.
     * It uses a filter to ensure that only cards in hand are considered.
     *
     * @return The Card id of the randomly selected playable card.
     */
    public int rndMove(){
        int rndCardToPlay = 0;
        int playableCardIndex = 0;
        int cardCnt = aiPlayer.countPlayableCards();
        boolean[] isCardPlayable = aiPlayer.getHandCardsPlayable().getIsInHand();   
        Random random = new Random();                         
        int idxRnd = random.nextInt(cardCnt);
        idxRnd++;
        
        while (playableCardIndex < idxRnd) {
            if (isCardPlayable[rndCardToPlay] == true) {
                playableCardIndex++;
            }
            rndCardToPlay++;         
        }
        return rndCardToPlay;
    }
    
    /**
     * Selects three random cards to pass from the AI player's hand.
     * This method generates three distinct random numbers corresponding to the card positions
     * and ensures that the same card is not chosen more than once.
     * It uses a filter to ensure that only cards in hand are considered.
     *
     * @return An array of three integers representing the positions (1-indexed) of the randomly selected cards to pass.
     */
    public int[] rndPass(){
        int[] rndpass = new int[3];
        Random random = new Random(); 
        int cardCnt = aiPlayer.countCards();                   
        int idxRnd1 = random.nextInt(cardCnt);
        int idxRnd2 = random.nextInt(cardCnt);
        int idxRnd3 = random.nextInt(cardCnt);
        boolean[] isCardPlayable = aiPlayer.getHandCardsPlayable().getIsInHand();       

        
        while(idxRnd1 == 0){
            idxRnd1 = random.nextInt(cardCnt);
        }

        while(idxRnd1 == idxRnd2 || idxRnd2 == 0)  { 
            idxRnd2 = random.nextInt(cardCnt);
        }

        while(idxRnd1 == idxRnd3 || idxRnd2 == idxRnd3 || idxRnd3 == 0){
            idxRnd3 = random.nextInt(cardCnt);
        }
        
        int rndCardToPlay1 = 0; 
        int playbaleCardIdx = 0;
        while(playbaleCardIdx < idxRnd1){
            if(isCardPlayable[rndCardToPlay1] == true ){
                playbaleCardIdx++;
            } 
            rndCardToPlay1++;         
        }

        int rndCardToPlay2 = 0; 
        playbaleCardIdx = 0;
        while(playbaleCardIdx < idxRnd2){
            if(isCardPlayable[rndCardToPlay2] == true){
                playbaleCardIdx++;
            } 
            rndCardToPlay2++;         
        }

        int rndCardToPlay3 = 0; 
        playbaleCardIdx = 0;
        while(playbaleCardIdx < idxRnd3){
            if(isCardPlayable[rndCardToPlay3] == true){
                playbaleCardIdx++;
            } 
            rndCardToPlay3++;         
        }

        rndpass[0] = rndCardToPlay1;
        rndpass[1] = rndCardToPlay2;
        rndpass[2] = rndCardToPlay3;
        
        return rndpass;       
        
    }

    //Erhört im point Array alle Werte eines bestimmtes Suits um eins
    public int[] plus(int[] pts , Suits s){
        switch(s){
            case DIAMONDS:
            for(int i = 0; i < pts.length/4; i++){
                    pts[i] = pts[i]+1;
                }
                case HEARTS:
                for(int i = pts.length/4; i < 2* pts.length/4; i++){
                    pts[i] = pts[i]+1;
                }
            case SPADES:
            for(int i = 2*pts.length/4; i < 3*pts.length/4; i++){
                    pts[i] = pts[i]+1;
                }
                case CLUBS:
                for(int i = 3*pts.length/4; i < pts.length; i++){
                    pts[i] = pts[i]+1;
                }
        }
        return pts;

    }

    // Determines if the AI should attempt 'Shoot the Moon' based on its current hand and game points.
    private boolean shouldTryShootTheMoon() {
        int totalHeartsCount = starterHandCards.countCardsInRange(CardCoding.HEARTS_2.getId(), CardCoding.HEARTS_ACE.getId());
        int highHeartsCount = starterHandCards.countCardsInRange(CardCoding.HEARTS_10.getId(), CardCoding.HEARTS_ACE.getId());
        boolean hasQueenOfSpades = starterHandCards.containsCard(CardCoding.SPADES_QUEEN.getId());

        if (aiPlayer.getGamePts() > 85) {
            return passedForStM = false;
        }
    
        if (hasQueenOfSpades && highHeartsCount > 5) {
            return passedForStM = true;
        } else if (totalHeartsCount > 13 && highHeartsCount > 2) {
            return passedForStM = true;
        }

        return passedForStM = false;
    }

    // Passes cards strategically for the 'Shoot the Moon' attempt, prioritizing low heart cards and other low cards.
    private int[] passForSlam(int[] bestCardsToPass, int index) {
        // 1. Pass low heart cards
        index = passHighestCards(bestCardsToPass, index, CardCoding.HEARTS_2.getId(), CardCoding.HEARTS_4.getId(), 3);
    
        // 2. Pass other low cards
        while (index < MAX_PASS_CARDS) {
            int lowestClub = CardEvaluation.findLowestCardInRange(starterHandCards, CardCoding.CLUBS_2.getId(), CardCoding.CLUBS_ACE.getId(), bestCardsToPass);
            int lowestDiamond = CardEvaluation.findLowestCardInRange(starterHandCards, CardCoding.DIAMONDS_2.getId(), CardCoding.DIAMONDS_ACE.getId(), bestCardsToPass);
            int lowestSpade = CardEvaluation.findLowestCardInRange(starterHandCards, CardCoding.SPADES_2.getId(), CardCoding.SPADES_ACE.getId(), bestCardsToPass);
            int lowestCard = CardEvaluation.getLowestValueCard(starterHandCards, CardEvaluation.getLowestValueCard(starterHandCards, lowestClub, lowestDiamond), lowestSpade);

            if (lowestCard == -1) {
                break; 
            }
            bestCardsToPass[index++] = lowestCard;
        }
        return bestCardsToPass;
    }
    
    // Passes spade cards, focusing on the Queen, Ace, and King of Spades, based on the game's difficulty.
    private int passSpades(int[] bestCardsToPass, int index) {
        int numLowerSpades = starterHandCards.countCardsInRange(CardCoding.SPADES_2.getId(), CardCoding.SPADES_JACK.getId());
        boolean hasSpadeQueen = starterHandCards.containsCard(CardCoding.SPADES_QUEEN.getId());
        boolean hasSpadeAce = starterHandCards.containsCard(CardCoding.SPADES_ACE.getId());
        boolean hasSpadeKing = starterHandCards.containsCard(CardCoding.SPADES_KING.getId());
    
        // Pass the Queen of Spades in expert difficulty
        if (isExpertDifficulty() && hasSpadeQueen && numLowerSpades < 4 && index < MAX_PASS_CARDS) {
            bestCardsToPass[index++] = CardCoding.SPADES_QUEEN.getId();
        }

        // Pass the Ace or King of Spades in expert or hard difficulty
        if ((isExpertDifficulty() || isHardDifficulty()) && numLowerSpades < 3) {
            if (hasSpadeAce) {
                bestCardsToPass[index++] = CardCoding.SPADES_ACE.getId();
            } else if (hasSpadeKing) {
                bestCardsToPass[index++] = CardCoding.SPADES_KING.getId();
            }
            return index;
        }

        // Pass the Ace and King of Spades
        if (hasSpadeAce && index < MAX_PASS_CARDS) {
            bestCardsToPass[index++] = CardCoding.SPADES_ACE.getId();
        }

        if (hasSpadeKing && index < MAX_PASS_CARDS) {
            bestCardsToPass[index++] = CardCoding.SPADES_KING.getId();
        }

        return index;
    }
    
    // Passes heart cards based on the game's difficulty and the number of high and low heart cards in hand.
    private int passHearts(int[] bestCardsToPass, int index) {
        int numLowHearts = starterHandCards.countCardsInRange(CardCoding.HEARTS_2.getId(), CardCoding.HEARTS_5.getId());
        int numHighHearts = starterHandCards.countCardsInRange(CardCoding.HEARTS_10.getId(), CardCoding.HEARTS_ACE.getId());
    
        if (isExpertDifficulty() || isHardDifficulty()) {
            if (shouldNotPassHighHeart(numHighHearts, numLowHearts)) {
                return index;
            } else if (shouldPassSecondHighestHeart(numHighHearts, numLowHearts)) {
                return passSecondHighestHeart(bestCardsToPass, index);
            } else {
                return passHighestCards(bestCardsToPass, index, CardCoding.HEARTS_ACE.getId(), CardCoding.HEARTS_JACK.getId(), numHighHearts);
            }
        }
    
        if (isMediumDifficulty()) {
            return passHighestCards(bestCardsToPass, index, CardCoding.HEARTS_JACK.getId(), CardCoding.HEARTS_JACK.getId(), numHighHearts);
        }
    
        return index;
    }
    
    // Passes cards from other suits (clubs and diamonds).
    private int passOther(int[] bestCardsToPass, int index) {

        if (isExpertDifficulty() || isHardDifficulty()) { 
            index = passCompleteSuit(bestCardsToPass, index, CardCoding.CLUBS_2.getId(), CardCoding.CLUBS_ACE.getId());
            index = passCompleteSuit(bestCardsToPass, index, CardCoding.DIAMONDS_2.getId(), CardCoding.DIAMONDS_ACE.getId());
        }
        
        // Rule for all levels: Simply pass all remaining high cards
        while (index < MAX_PASS_CARDS) {
            int highestClub = CardEvaluation.findHighestCardInRange(starterHandCards, CardCoding.CLUBS_2.getId(), CardCoding.CLUBS_ACE.getId(), bestCardsToPass);
            int highestDiamond = CardEvaluation.findHighestCardInRange(starterHandCards, CardCoding.DIAMONDS_2.getId(), CardCoding.DIAMONDS_ACE.getId(), bestCardsToPass);
            
            int highestCard = CardEvaluation.getHighestValueCard(starterHandCards, highestClub, highestDiamond);
            
            if (highestCard == -1) {
                break;  // No more cards to add
            }
            
            bestCardsToPass[index++] = highestCard;
        }
        return index;
    }
    
    // Determines if high heart cards should not be passed based on their count in hand.
    private boolean shouldNotPassHighHeart(int numHighHearts, int numLowHearts) {
        return numHighHearts > 0 && numHighHearts <= 1 && numLowHearts >= 2;}
    
    // Determines if the second highest heart card should be passed based on the count of high and low heart cards.
    private boolean shouldPassSecondHighestHeart(int numHighHearts, int numLowHearts) {
        return numHighHearts > 1 && numLowHearts > 2;}
    
    // Passes a complete suit of cards if they fit within the allowed number of cards to pass.
    private int passCompleteSuit(int[] bestCardsToPass, int index, int start, int end) {
        int numCards = starterHandCards.countCardsInRange(start, end);

        if (numCards != 0 && numCards <= (MAX_PASS_CARDS - index)) {
            for (int i = start; i <= end; i++) {
                if (starterHandCards.containsCard(i)) {
                    bestCardsToPass[index++] = i;
                }
            }
        }
        return index;
    }
    
    // Passes the highest cards within a specified range, limited by the number of cards to pass.
    private int passHighestCards(int[] bestCardsToPass, int index, int start, int end, int numCards) {
        for (int i = start; i >= end; i--) {
            if (numCards > 0 && index < MAX_PASS_CARDS && starterHandCards.containsCard(i)) {
                bestCardsToPass[index++] = i;
                numCards--;
            }
        }
        return index;
    }
    
    // Identifies and passes the second highest heart card in the player's hand.
    private int passSecondHighestHeart(int[] bestCardsToPass, int index) {
        int highestHeartValue = 0;
        int secondHighestHeartValue = 0;
    
        for (int cardValue = CardCoding.HEARTS_JACK.getId(); cardValue <= CardCoding.HEARTS_ACE.getId(); cardValue++) {
            if (starterHandCards.containsCard(cardValue)) {
    
                if (cardValue > highestHeartValue) {
                    secondHighestHeartValue = highestHeartValue;
                    highestHeartValue = cardValue;
                } else if (cardValue > secondHighestHeartValue) {
                    secondHighestHeartValue = cardValue;
                }
            }
        }
        
        if (secondHighestHeartValue != 0 && index < 3) {
            bestCardsToPass[index++] = secondHighestHeartValue;
        }
        return index;
    }

    // Fills the remaining cards to pass with the highest cards from specified suits.
    private void fillRemainingCards(int[] bestCardsToPass, int index) {
        index = fillWithHighestCardsFromSuit(bestCardsToPass, index, CardCoding.HEARTS_2.getId(), CardCoding.HEARTS_ACE.getId());
        index = fillWithHighestCardsFromSuit(bestCardsToPass, index, CardCoding.SPADES_2.getId(), CardCoding.SPADES_ACE.getId());
        return;
    }
    
    // Fills the best cards to pass with the highest cards available from a specific suit.
    private int fillWithHighestCardsFromSuit(int[] bestCardsToPass, int index, int suitStartId, int suitEndId) {
        while (index < MAX_PASS_CARDS) {
            int highestCard = CardEvaluation.findHighestCardInRange(starterHandCards, suitStartId, suitEndId, bestCardsToPass);
    
            if (highestCard == -1) {
                break; 
            }
    
            bestCardsToPass[index++] = highestCard;
        }
        return index;
    }


    //Rule 1: Play the highest Card which doesnt win the Trick
    public int[] ruleHighCardNoWin(int[] cardScore ,int playpos , ArrayList<Card> playableCards,GameState gameState , int cardIdWinningTrick , boolean[][] suitVoidsByPlayer  ){  
        Card cardWinningTrick = new Card(cardIdWinningTrick);
        boolean iHaveVoid = false;    
        for(int i = 0; i < 4;i++){
            if(suitVoidsByPlayer[aiPlayer.getPlayerNo()][i] == true ){
                iHaveVoid = true;
            } 
        }

        if(!iHaveVoid){
            ArrayList<Card> GivePointsA = new ArrayList<>();  
            if(playpos != 0 && !gameState.isFirstTrick()){            
                for(Card a : playableCards){                       
                    if(cardWinningTrick.isHigherAs(a)){ 
                            GivePointsA.add(a);
                        }
                }

                Collections.sort(GivePointsA , Collections.reverseOrder(new Comparator<Card>() {         //sort Cards descending
                    @Override
                    public int compare(Card c1, Card c2) {               
                        if(c1.isHigherAs(c2)){
                            return 1;
                        }else if (c2.isHigherAs(c1)){
                            return -1;
                        }else{
                            return 0;
                        }
                        }           
                }));    

                int pnt1 = 20;
                for(Card a : GivePointsA){
                    cardScore[a.getId()-1] = cardScore[a.getId()-1] + pnt1;
                    pnt1--;
                }
                GivePointsA.clear();
            }
        }

        return cardScore;
    }

    
    //Rule 2: Play high Cards if early in Game
    private int[] ruleHighCardEarlyGame(int[] cardScore,int playpos, ArrayList<Card> playableCards , GameState gameState ,Trick currentTrick ,boolean[][] suitVoidsByPlayer){
        if(playpos == 0 && playpos ==3){
            return cardScore;
        }else{
            ArrayList<Card> GivePointsA = new ArrayList<>();
            boolean noOtherVoids = true;
            int stemp = 0;
            Suits s = playableCards.get(0).getSuit();         
            switch(s){
                case DIAMONDS:stemp = 0; break;
                case HEARTS:stemp = 1; break;
                case SPADES: stemp = 2; break;
                case CLUBS: stemp = 3; break;
            }
            if(playpos == 1){                           
                int posTemp1 = (aiPlayer.getPlayerNo()+1) % 4;
                for(int i = 0; i< 4;i++){                      
                    if(i != aiPlayer.getPlayerNo() && (suitVoidsByPlayer[i][stemp] == true) && (i != posTemp1)){
                        noOtherVoids = false;
                    }
                }
            }else if(playpos == 2){
                int posTemp1 = (aiPlayer.getPlayerNo()+1) % 4;
                int posTemp2 = (aiPlayer.getPlayerNo()+2) % 4;

                for(int i = 0; i< 4;i++){                      
                    if(i != aiPlayer.getPlayerNo() && (suitVoidsByPlayer[i][stemp] == true) && (i != posTemp1) && (i != posTemp2)){
                        noOtherVoids = false;
                    }
                }
            }else{
                return cardScore;
            }

            if(currentTrick.countPoints()== 0 && noOtherVoids && (gameState.getTricksPlayed()<4)){
                for(Card a :playableCards){
                    if(((a.getId()-1)%13) > 9 && !a.isQueenOfSpades()){
                        GivePointsA.add(a);
                    }             
                }

                Collections.sort(GivePointsA , Collections.reverseOrder(new Comparator<Card>() {        
                    @Override
                    public int compare(Card c1, Card c2) {  
                        if(c1.isHigherAs(c2)){
                            return 1;
                        }else if (c2.isHigherAs(c1)){
                            return -1;
                        }else{
                            return 0;
                        }
                    }           
                }));  

                int pnt2 = 50;                      
                for(Card a : GivePointsA){
                    //System.out.println("Regel 2  " + a.getCardName());
                    cardScore[a.getId()-1] = cardScore[a.getId()-1] + pnt2;
                    pnt2 = pnt2-2;
                }
                GivePointsA.clear();
            }
            return cardScore;
        }
    }


    //Rule 3: Try to win first Trick or atleast discard a high Card
    private int[] ruleWinFirstTrick(int[] cardScore , ArrayList<Card> playableCards, GameState gameState ){
        ArrayList<Card> GivePointsA = new ArrayList<>();  
        if(gameState.isFirstTrick()){            //analog Regel 1 bis auf Anfang
            for(Card a :playableCards){
                if(!a.isQueenOfSpades()){
                    GivePointsA.add(a);
                }
            }

            Collections.sort(GivePointsA , Collections.reverseOrder(new Comparator<Card>() {        
                @Override
                public int compare(Card c1, Card c2) {  
                    if(c1.isHigherAs(c2)){
                        return 1;
                    }else if (c2.isHigherAs(c1)){
                        return -1;
                    }else{
                        return 0;
                    }
                }           
            }));  

            int pnt3 = 25;                      
            for(Card a : GivePointsA){
                //System.out.println("Regel 3  " + a.getCardName());
                cardScore[a.getId()-1] = cardScore[a.getId()-1] + pnt3;
                pnt3 = pnt3-2;
            }
        }
        GivePointsA.clear();

        return cardScore;
    }

    //Rule 4: Try to create Voids
    private int[] createVoids(int[] cardScore , SuitCount suitCount ){       
        int[] tempp = new int[4];
        tempp[0] = suitCount.cntKaro;
        tempp[1] = suitCount.cntHearts;
        tempp[2] = suitCount.cntPic;
        tempp[3] = suitCount.cntKreuz;
        
        Suits[] tempS = new Suits[4];
        tempS[0] = Suits.DIAMONDS;
        tempS[1] = Suits.HEARTS;
        tempS[2] = Suits.SPADES;
        tempS[3] = Suits.CLUBS;
        
        
        for(int i = 0; i <4 ; i++){
            if( tempp[i] < 4){
            cardScore = plus(cardScore, tempS[i]);
             if( tempp[i] < 3){
                cardScore = plus(cardScore, tempS[i]);
                if( tempp[i] < 2){
                    cardScore = plus(cardScore , tempS[i]);
                    }
                }
            }  
        }

        return cardScore;
    }

    //Rule 5: Play High Card if Heart not broken and low possibilty that it will be broken
    private int[] rule5(int[] cardScore , int playpos, ArrayList<Card> playableCards ,Trick currentTrick ,  boolean[][] suitVoidsByPlayer ){
        ArrayList<Card> GivePointsA = new ArrayList<>();  
        if(!heartbroken && ( currentTrick.countPoints() > 0) ){             
            for(Card a : playableCards){
                if(((a.getId()-1) % 13) > 9){         
                    GivePointsA.add(a);
                }               
            }
            boolean tempVDia = false;
            boolean tempVSpa = false;
            boolean tempVClu = false;
            for(int  i = playpos+1; i < 4;i++){        
                if(suitVoidsByPlayer[i][0] = true){
                    tempVDia = true;
                }
                if(suitVoidsByPlayer[i][2] = true){
                    tempVSpa = true;
                }
                if(suitVoidsByPlayer[i][3] = true){
                    tempVClu = true;
                }

            }
             ArrayList<Card> GivePointsA2 = new ArrayList<>(); 
            for(Card a : GivePointsA){
                Suits s = a.getSuit();
                switch(s){
                    case DIAMONDS:
                    if(tempVDia){
                        GivePointsA2.add(a);
                    }
                    break;
                    case SPADES:
                    if(tempVSpa){
                        GivePointsA2.add(a);
                    }
                    break;
                    case CLUBS:
                    if(tempVClu){
                        GivePointsA2.add(a);
                    }
                    break;
                    case HEARTS:
                    break;
                }
            }
            for(Card a : GivePointsA2){   
                cardScore[a.getId()-1] = cardScore[a.getId()-1] +25;
            }
        }
        GivePointsA.clear();
        return cardScore;
    }

    //Rule 6: Play Heart if possible and if you dont win the Trick
    private int[] ruleGiveOtherPoints(int[] cardScore , int playpos ,  ArrayList<Card> playableCards , int cardIdWinningTrick ,HandCards currentHandCards){
        boolean heartbreakable = false;
        for(Card a : playableCards){
            if(a.isHeart() || a.isQueenOfSpades()){
                heartbreakable = true;
            }
        }

        if(heartbroken || heartbreakable){
            if(playpos != 0){
                int pnt6 = 70;
                for(int i = 14; i < 27; i++){             //Index so gewählt das nur Herz Karten betrachtet werden
                    if(currentHandCards.containsCard(i)){
                        if((CardCoding.getSuitById(cardIdWinningTrick) == Suits.HEARTS) && (cardIdWinningTrick > i)){
                            cardScore[i-1] = cardScore[i-1] + pnt6;
                        }else if(CardCoding.getSuitById(cardIdWinningTrick) != Suits.HEARTS){
                            cardScore[i-1] = cardScore[i-1] + pnt6;
                        }
                    }
                } 
                pnt6 = pnt6 +75;                        //Hier wird nun QoS (id:37) betrachtet Rest analog
                    if(currentHandCards.containsCard(CardCoding.SPADES_QUEEN.getId())){
                        if((CardCoding.getSuitById(cardIdWinningTrick) == Suits.SPADES) && (cardIdWinningTrick > CardCoding.SPADES_QUEEN.getId())){
                            cardScore[37-1] = cardScore[37-1] + pnt6;
                        }else if(CardCoding.getSuitById(cardIdWinningTrick) != Suits.SPADES ){
                            cardScore[37-1] = cardScore[37-1] + pnt6;
                        }
                    }

            }
        }
        return cardScore;    
    }

    //Rule 7: Play Low Card if first in Trick
    private int[] ruleIfFirstPlayLow(int[] cardScore , int playpos , ArrayList<Card> playableCards , GameState gameState ,Trick currentTrick){
        ArrayList<Card> GivePointsA = new ArrayList<>();  
        if(playpos == 0 && !gameState.isFirstTrick()){
            for(Card a : playableCards){
                if(!a.isQueenOfSpades()){
                     GivePointsA.add(a);
                }
            }

            if(currentTrick.countPoints() == 0 && gameState.getTricksPlayed() < 5){
                Collections.sort(GivePointsA , new Comparator<Card>() {       
                @Override
                public int compare(Card c1, Card c2) {  
                    if((c1.getId()-1)%13 > (c2.getId()-1)%13){
                        return 1;
                    }else if ((c2.getId()-1)%13> (c1.getId()-1)%13){
                        return -1;
                    }else{
                        return 0;
                    }
                }           
            }); 
            }else{
                Collections.sort(GivePointsA , new Comparator<Card>() {       
                @Override
                public int compare(Card c1, Card c2) {  
                    if((c1.getId()-1)%13 > (c2.getId()-1)%13){
                        return 1;
                    }else if ((c2.getId()-1)%13> (c1.getId()-1)%13){
                        return -1;
                    }else{
                        return 0;
                    }
                }           
            }); 
            }

            int pnt7 = 30;                        
            for(Card a: GivePointsA){
                cardScore[a.getId()-1] =   cardScore[a.getId()-1] + pnt7;
                pnt7 = pnt7-3;
            }
            GivePointsA.clear();
        }
        return cardScore;
    }

    //Rule 8:Play High Card if last in Trick an zero Points in Trick
    private int[] ruleHighCardZeroPoints(int[] cardScore , int playpos , ArrayList<Card> playableCards , GameState gameState ,Trick currentTrick){
        ArrayList<Card> GivePointsA = new ArrayList<>();  
        if(playpos == 3 && ( currentTrick.countPoints() == 0) ){
            for(Card a :playableCards){
                GivePointsA.add(a);
            }

            Collections.sort(GivePointsA , Collections.reverseOrder(new Comparator<Card>() {        
                @Override
                public int compare(Card c1, Card c2) {  
                    if(c1.isHigherAs(c2)){
                        return 1;
                    }else if (c2.isHigherAs(c1)){
                        return -1;
                    }else{
                        return 0;
                    }
                }           
            }));  

            int pnt8 = 60;                      
            for(Card a : GivePointsA){
                cardScore[a.getId()-1] = cardScore[a.getId()-1] + pnt8;
                pnt8 = pnt8-3;
            }
            GivePointsA.clear();
        }
        return cardScore;
    }

    //Rule 9: If First in Trick try to play a Suits in which no one got a Void
    private int[] ruleAvoidOtherVoids(int[] cardScore , int playpos , ArrayList<Card> playableCards , GameState gameState ,Trick currentTrick , boolean[][] suitVoidsByPlayer){    
        ArrayList<Card> GivePointsA = new ArrayList<>();     
        if(playpos == 0){                   
            for(Card a :playableCards){
                Suits s = a.getSuit();
                int stemp = 0;                  
                switch(s){
                    case DIAMONDS:stemp = 0; break;
                    case HEARTS:stemp = 1; break;
                    case SPADES: stemp = 2; break;
                    case CLUBS: stemp = 3; break;
                }
            
                boolean otherHasVoid = false;
                for(int i = 0; i< 4;i++){                      
                    if(suitVoidsByPlayer[i][stemp] == true  ){       //Test if other Players have a Void in Suit
                        otherHasVoid = true;
                    }      
                }
                if(otherHasVoid = false){
                    GivePointsA.add(a);
                }            
            }

            int pnt9 = 15;   
            for(Card a : GivePointsA){                             
                cardScore[a.getId()-1] = cardScore[a.getId()-1] + pnt9;
            }
            GivePointsA.clear();
        }
        return cardScore;
    }

    //Rule 10: If you got the only Hearts dont lead with them
    private int[] ruleDontLeadWithOnlyHeart(int[] cardScore,int playpos, ArrayList<Card> playableCards  , boolean[] cardsAvailable ){
        boolean noHearts = true;
        for(int i =14; i <27;i++){
            if(cardsAvailable[i-1] == true){
                noHearts = false;
            }
        }
        if(noHearts){
            if(playpos == 0){
                for(Card a : playableCards){
                    if(a.isHeart()){
                        cardScore[a.getId()-1] = cardScore[a.getId()-1] -100;
                    }
                }
            }else {           
                 for(Card a :  playableCards){
                    if(a.isHeart()){
                        cardScore[a.getId()-1] = cardScore[a.getId()-1] +100;
                    }
                } 
            }
        }

        return cardScore;
    } 

    //Rule 11: Do not play Spades King or Ace if QoS is still ingame
    private int[] ruleAvoidQoS(int[] cardScore,int playpos, boolean[] cardsAvailable , HandCards currentHandCards , Trick currentTrick ){
        if(currentHandCards.countCardsInRange(27,39) == currentHandCards.countCardsInRange(1, 52) ){
            if(cardsAvailable[37-1] && (currentTrick.countPoints() ==0)){
                if(playpos != 3){
                    if(currentHandCards.containsCard(38)){
                        cardScore[38-1] = cardScore[38-1] - 500;
                    }
                    if(currentHandCards.containsCard(39)){
                        cardScore[39-1] = cardScore[39-1] - 500;
                    }              
                }else{      
                    if(currentHandCards.containsCard(38)){
                        cardScore[38-1] = cardScore[38-1] +110;
                    }
                    if(currentHandCards.containsCard(39)){
                        cardScore[39-1] = cardScore[39-1] +110;
                    }  

                }
            }
        }
        return cardScore;
    }

    public int[] rulePlaySafeHighValueCards(int[] cardScore, ArrayList<Card> playableCards, Trick[] allTricks, int tricksToConsider, int tricksPlayed) {
        boolean[] cardsPlayed = CardEvaluation.calculatePlayedCards(allTricks, tricksToConsider, tricksPlayed);
        int safePlayPointIncrease = 40; 
    
        for (Card card : playableCards) {
            if (isSafeToPlay(card, cardsPlayed, playableCards)) {
                cardScore[card.getId() - 1] += safePlayPointIncrease;
            }
        }
    
        return cardScore;
    }
    
    private boolean isSafeToPlay(Card card, boolean[] cardsPlayed, ArrayList<Card> handCards) {
        int suit = card.getSuitIndex();
        int value = (card.getId() - 1) % 13;
        
        // Tests if all low Card of this Suit are already played
        boolean allLowerPlayed = true;
        for (int i = 0; i < value; i++) {
            int lowerCardId = suit * 13 + i;
            if (!cardsPlayed[lowerCardId]) {
                allLowerPlayed = false;
                break;
            }
        }
    
        // Tests if higher Cards of the Suit are not already played or not in the Hand
        boolean higherCardsInPlay = false;
        for (int i = value + 1; i < 13; i++) {
            int higherCardId = suit * 13 + i;
            if (!cardsPlayed[higherCardId] && !handCardsContainsCardWithId(handCards, higherCardId + 1)) {
                higherCardsInPlay = true;
                break;
            }
        }
    
        return allLowerPlayed && higherCardsInPlay;
    }

    private boolean handCardsContainsCardWithId(ArrayList<Card> handCards, int cardId) {
        for (Card card : handCards) {
            if (card.getId() == cardId) {
                return true;
            }
        }
        return false;
    }
}
