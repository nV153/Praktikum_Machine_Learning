package rules;
import java.util.Arrays;
import game.CardCoding;
import game.GameState;
import game.Player;

/**
* This class is used to contain game rules and to apply them when its methods are called.
* <p>
* The sources are: 
* <p>
* https://www.pagat.com/de/reverse/hearts.html ,
* <p>
* https://de.wikipedia.org/wiki/Hearts and
* <p>
* the introductory event of the course.
*/
public final class Rules {
   
    /**
     * This method returns for a player the player to whom he must pass his cards if they are passed on.
     * <p>
     * It is generally called at the start of a round, when the players have to choose the cards they want to pass on 
     * and pass them on to a player.
     * <p>
     * In the first round, cards are passed to the left neighbor, in the second to the right, 
     * in the third to the opposite player and in the fourth not at all. This pattern continues as the number of rounds increases.
     * <p>
     * @param playerNo The number of the player who must pass on his cards.
     * @param roundNo The round number the game is currently in.
     * @return The number of the player to whom the cards must be passed.
     */
    public final static int getPassReceiverNo(int playerNo, int roundNo){
        int result = -1;
        switch (roundNo % 4) {
            case 1:
                result = (playerNo + 1) % 4;
                break;
            case 2:
                result = (playerNo + 3) % 4;
                break;
            case 3:
                result = (playerNo + 2) % 4;
                break;
            case 0:
                result = playerNo;
                break;            
        }
        return result;
    }

    /**
     * This method determines a player's playable cards at the start of the round for passing and for the 1st trick.
     * <p>
     * At the start of the round and immediately before passing on, i.e. before the 1st trick, any of the cards 
     * in the hand may also be passed on.
     * <p>
     * For the 1st trick, however, the following applies: The player with clubs 2 (for deck size 52) or clubs 7 (for deck size 32) 
     * in hand is the starter and must play this card. The following applies to all other players: 
     * Since the starter begins the trick with clubs, the other players may initially only play clubs,
     * if they have them in their hand. If there are no clubs in the hand, then only diamonds and spades may be played,
     * except the queen of spades, provided they are in the hand. However, if these cards are not in the hand either,
     * then the hearts cards and the queen of spades may be played, provided they are in the hand.
     * <p>
     * Important is the "if in hand", as the computers are not restricted in their selection of cards via the GUI,
     * but only see the boolean array through bestPass or bestMove. Another advantage of this is that there is no need to 
     * differentiate between deck sizes 52 and 32, as only the cards in the hand are taken into account.
     * <p>
     * @param gameState The current state of the Hearts game.
     * @param thePlayerToDetermineHisPlayableCards The player for whom his playable cards are to be determined.
     * @return An array of 52 boolean values, of which true means that the corresponding card may be played and false that it may not.
     */
    public final static boolean[] getPlayableCardsInTrick0or1(GameState gameState, Player thePlayerToDetermineHisPlayableCards){
        boolean[] playableCards = new boolean[52];
        Arrays.fill(playableCards, Boolean.FALSE);        
        if (gameState.getTricksPlayed() == 0){
            playableCards = Arrays.copyOf(thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand(), 
                                            thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand().length);
        } else if (gameState.getTricksPlayed() == 1){
            if (gameState.getStarterOfRound().getPlayerNo() == thePlayerToDetermineHisPlayableCards.getPlayerNo()){
                if (gameState.getDeckSize() == 52){
                    int cardNumberIdClubs2 = CardCoding.CLUBS_2.getId();
                    playableCards[cardNumberIdClubs2-1] = true;
                } else if (gameState.getDeckSize() == 32){    
                    int cardNumberIdClubs7 = CardCoding.CLUBS_7.getId();
                    playableCards[cardNumberIdClubs7-1] = true;
                } else {                    
                }
            } else{
                boolean atLeastOneCardIsPlayable = false;
                for (int cardNumber = CardCoding.CLUBS_2.getId(); cardNumber <= CardCoding.CLUBS_ACE.getId(); cardNumber++){
                    if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                        playableCards[cardNumber-1] = true;
                        atLeastOneCardIsPlayable = true;
                    }
                }
                if (atLeastOneCardIsPlayable == false){
                    for (int cardNumber = CardCoding.DIAMONDS_2.getId(); cardNumber <= CardCoding.DIAMONDS_ACE.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                            atLeastOneCardIsPlayable = true;
                        }
                    }
                    for (int cardNumber = CardCoding.SPADES_2.getId(); cardNumber <= CardCoding.SPADES_ACE.getId(); cardNumber++){
                        if (cardNumber != CardCoding.SPADES_QUEEN.getId()){
                            if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                                playableCards[cardNumber-1] = true;
                                atLeastOneCardIsPlayable = true;
                            }
                        }
                    }
                }
                if (atLeastOneCardIsPlayable == false){
                    for (int cardNumber = CardCoding.HEARTS_2.getId(); cardNumber <= CardCoding.HEARTS_ACE.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                        }
                    }
                    for (int cardNumber = CardCoding.SPADES_QUEEN.getId(); cardNumber <= CardCoding.SPADES_QUEEN.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                        }
                    }
                }              
            }
        }
        return playableCards;
    }

    /**
     * This method determines the starter's playable cards from the 2nd trick.
     * <p>
     * The starter may begin the trick with any card in his hand except hearts. 
     * The starter may only play hearts if hearts have already been played in a previous trick in this round, i.e,
     * Hearts have already been broken or if he has no other choice, i.e. he only has hearts cards in his hand, 
     * in which case the whole hand can be played.
     * <p>
     * @param gameState The current state of the Hearts game.
     * @param thePlayerToDetermineHisPlayableCards The player for whom his playable cards are to be determined.
     * @return An array of 52 boolean values, of which true means that the corresponding card may be played and false that it may not.
     */
    public final static boolean[] getPlayableCardsOfStarterInTrick2orLater(GameState gameState, Player thePlayerToDetermineHisPlayableCards){
        boolean[] playableCards = new boolean[52];
        Arrays.fill(playableCards, Boolean.FALSE);
        if (gameState.getIsHeartBroken() == true){
            playableCards = Arrays.copyOf(thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand(), 
                                            thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand().length);
            
        } else {
            boolean atLeastOnePlayableCardIsNotHeart = false;
            for (int cardNumber = CardCoding.DIAMONDS_2.getId(); cardNumber <= CardCoding.CLUBS_ACE.getId(); cardNumber++){
                if (cardNumber <= CardCoding.DIAMONDS_ACE.getId() || cardNumber >= CardCoding.SPADES_2.getId()){
                    if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                        playableCards[cardNumber-1] = true;
                        atLeastOnePlayableCardIsNotHeart = true;
                    }
                }
            }
            if (atLeastOnePlayableCardIsNotHeart == false){
                playableCards = Arrays.copyOf(thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand(), 
                                            thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand().length);                
            }
        }           
        return playableCards;
    }

    /**
     * This method determines the playable cards of a player from the 2nd trick if he is not the starter of the trick.
     * <p>
     * First, a player must respond with the same suit as the card played by the starter.
     * If there is no card of the same suit in the hand, i.e. this suit cannot be played, then the entire hand is playable,
     * i.e. there is no alternative.
     * <p>
     * @param gameState The current state of the Hearts game.
     * @param thePlayerToDetermineHisPlayableCards The player for whom his playable cards are to be determined.
     * @param firstCardNumberPlayed The number of the card played by the starter of the trick.
     * @return An array of 52 boolean values, of which true means that the corresponding card may be played and false that it may not.
     */
    public final static boolean[] getPlayableCardsOfNonStarterInTrick2orLater(GameState gameState, 
                                                                                Player thePlayerToDetermineHisPlayableCards,
                                                                                int firstCardNumberPlayed){
        boolean[] playableCards = new boolean[52];
        Arrays.fill(playableCards, Boolean.FALSE);
        int suitCardNumberRangeOfFirstPlayedCardStart;
        int suitCardNumberRangeOfFirstPlayedCardEnd;
        boolean[] isInHand = thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand();
        if (firstCardNumberPlayed >= CardCoding.DIAMONDS_2.getId() && firstCardNumberPlayed <= CardCoding.DIAMONDS_ACE.getId()){
            suitCardNumberRangeOfFirstPlayedCardStart = CardCoding.DIAMONDS_2.getId();
            suitCardNumberRangeOfFirstPlayedCardEnd = CardCoding.DIAMONDS_ACE.getId();
        } else if (firstCardNumberPlayed >= CardCoding.HEARTS_2.getId() && firstCardNumberPlayed <= CardCoding.HEARTS_ACE.getId()){
            suitCardNumberRangeOfFirstPlayedCardStart = CardCoding.HEARTS_2.getId();
            suitCardNumberRangeOfFirstPlayedCardEnd = CardCoding.HEARTS_ACE.getId();
        } else if (firstCardNumberPlayed >= CardCoding.SPADES_2.getId() && firstCardNumberPlayed <= CardCoding.SPADES_ACE.getId()){
            suitCardNumberRangeOfFirstPlayedCardStart = CardCoding.SPADES_2.getId();
            suitCardNumberRangeOfFirstPlayedCardEnd = CardCoding.SPADES_ACE.getId();
        } else if (firstCardNumberPlayed >= CardCoding.CLUBS_2.getId() && firstCardNumberPlayed <= CardCoding.CLUBS_ACE.getId()){
            suitCardNumberRangeOfFirstPlayedCardStart = CardCoding.CLUBS_2.getId();
            suitCardNumberRangeOfFirstPlayedCardEnd = CardCoding.CLUBS_ACE.getId();
        } else{
            suitCardNumberRangeOfFirstPlayedCardStart = -1;
            suitCardNumberRangeOfFirstPlayedCardEnd = -1;
        }
        boolean atLeastOnePlayableCardIsOfSameSuit = false;
        for (int cardNumber = suitCardNumberRangeOfFirstPlayedCardStart; cardNumber <= suitCardNumberRangeOfFirstPlayedCardEnd; cardNumber++){            
            if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                playableCards[cardNumber-1] = true;
                atLeastOnePlayableCardIsOfSameSuit = true;
            }            
        }
        if (atLeastOnePlayableCardIsOfSameSuit == false){
            playableCards = Arrays.copyOf(isInHand, isInHand.length);
        }        
        return playableCards;
    }       


    /**
     * This method determines a player's playable cards at the start of the round for passing and for the 1st trick.
     * This method is identical to getPlayableCardsInTrick0or1(GameState gameState, Player thePlayerToDetermineHisPlayableCards),
     * but with the following small difference: Due to the current implementation of the AI determinization, 
     * the call gameState.getStarterOfRound() no longer works. Therefore, the call is replaced by a parameter to be passed.
     * <p>
     * At the start of the round and immediately before passing on, i.e. before the 1st trick, any of the cards 
     * in the hand may also be passed on.
     * <p>
     * For the 1st trick, however, the following applies: The player with clubs 2 (for deck size 52) or clubs 7 (for deck size 32) 
     * in hand is the starter and must play this card. The following applies to all other players: 
     * Since the starter begins the trick with clubs, the other players may initially only play clubs,
     * if they have them in their hand. If there are no clubs in the hand, then only diamonds and spades may be played,
     * except the queen of spades, provided they are in the hand. However, if these cards are not in the hand either,
     * then the hearts cards and the queen of spades may be played, provided they are in the hand.
     * <p>
     * Important is the "if in hand", as the computers are not restricted in their selection of cards via the GUI,
     * but only see the boolean array through bestPass or bestMove. Another advantage of this is that there is no need to 
     * differentiate between deck sizes 52 and 32, as only the cards in the hand are taken into account.
     * <p>
     * @param gameState The current state of the Hearts game.
     * @param thePlayerToDetermineHisPlayableCards The player for whom his playable cards are to be determined.
     * @param starterOfRound The starter of the round.
     * @return An array of 52 boolean values, of which true means that the corresponding card may be played and false that it may not.
     */
    public final static boolean[] getPlayableCardsInTrick0or1ForAiDeterminization(GameState gameState, Player thePlayerToDetermineHisPlayableCards, Player starterOfRound){
        boolean[] playableCards = new boolean[52];
        Arrays.fill(playableCards, Boolean.FALSE);        
        if (gameState.getTricksPlayed() == 0){
            playableCards = Arrays.copyOf(thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand(), 
                                            thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand().length);
        } else if (gameState.getTricksPlayed() == 1){
            if (starterOfRound.getPlayerNo() == thePlayerToDetermineHisPlayableCards.getPlayerNo()){
                if (gameState.getDeckSize() == 52){
                    int cardNumberIdClubs2 = CardCoding.CLUBS_2.getId();
                    playableCards[cardNumberIdClubs2-1] = true;
                } else if (gameState.getDeckSize() == 32){    
                    int cardNumberIdClubs7 = CardCoding.CLUBS_7.getId();
                    playableCards[cardNumberIdClubs7-1] = true;
                } else {                    
                }
            } else{
                boolean atLeastOneCardIsPlayable = false;
                for (int cardNumber = CardCoding.CLUBS_2.getId(); cardNumber <= CardCoding.CLUBS_ACE.getId(); cardNumber++){ 
                    if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){ 
                        playableCards[cardNumber-1] = true;
                        atLeastOneCardIsPlayable = true;
                    }
                }
                if (atLeastOneCardIsPlayable == false){
                    for (int cardNumber = CardCoding.DIAMONDS_2.getId(); cardNumber <= CardCoding.DIAMONDS_ACE.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                            atLeastOneCardIsPlayable = true;
                        }
                    }
                    for (int cardNumber = CardCoding.SPADES_2.getId(); cardNumber <= CardCoding.SPADES_ACE.getId(); cardNumber++){
                        if (cardNumber != CardCoding.SPADES_QUEEN.getId()){
                            if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                                playableCards[cardNumber-1] = true;
                                atLeastOneCardIsPlayable = true;
                            }
                        }
                    }
                }
                if (atLeastOneCardIsPlayable == false){
                    for (int cardNumber = CardCoding.HEARTS_2.getId(); cardNumber <= CardCoding.HEARTS_ACE.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                        }
                    }
                    for (int cardNumber = CardCoding.SPADES_QUEEN.getId(); cardNumber <= CardCoding.SPADES_QUEEN.getId(); cardNumber++){
                        if (thePlayerToDetermineHisPlayableCards.getHandCards().getIsInHand()[cardNumber-1] == true){
                            playableCards[cardNumber-1] = true;
                        }
                    }
                }              
            }
        }
        return playableCards;
    }

}
