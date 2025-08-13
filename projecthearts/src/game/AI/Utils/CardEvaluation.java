package game.AI.Utils;

import java.util.ArrayList;
import java.util.List;

import game.Card;
import game.CardCoding;
import game.HandCards;
import game.Suits;
import game.Trick;

/**
 * The {@code CardEvaluation} class provides static helper methods for
 * evaluating cards in the card game Hearts.
 */
public class CardEvaluation {

    /**
     * Checks if the cards passed by a player indicate an attempt to "Shoot the Moon".
     *
     * This method examines the list of passed cards and looks for indicators that
     * the player might be attempting to "Shoot the Moon". Specifically, it checks if
     * high cards, heart cards, and the Queen of Spades are absent, as passing such cards
     * could jeopardize the attempt.
     *
     * @param passedCards the list of cards that have been passed by the player
     * @return {@code true} if the passed cards indicate an attempt to "Shoot the Moon";
     *         {@code false} otherwise.
     */
    public static boolean checkPassedCardsForStM(List<Integer> passedCards) {
        int highCardCount = 0;
        int heartCount = 0;
        boolean queenOfSpadesPassed = false;

        for (int cardId : passedCards) {
            
            if (isHighCard(cardId)) {
                highCardCount++;
            }

            if (CardCoding.getSuitById(cardId) == Suits.HEARTS) {
                heartCount++;
            }

            if (cardId == CardCoding.SPADES_QUEEN.getId()) {
                queenOfSpadesPassed = true;
            }
        }

        boolean isSuspicious = (highCardCount == 0) && (heartCount == 0) && !queenOfSpadesPassed;
    
        return isSuspicious;
    }

    /**
     * Determines the highest scoring card from an array of card scores.
     *
     * This method iterates through the provided array, which contains the scores for each card,
     * and identifies the card with the highest score. The card scores typically represent
     * the strategic value of each card in the context of the game, with a higher score indicating
     * a more advantageous card.
     *
     * @param cardScore an array of integers representing the scores for each card,
     *                  where the array index corresponds to the card ID minus one (array index 0 corresponds to card 1).
     * @return the card ID of the highest-rated card in the array. In case of multiple cards with
     *         the same highest score, the first encountered card is returned.
     */
    public static int getBestCard(int[] cardScore) {
        int idBestCard = 1;

        for (int i = 1; i < 53; i++) {
            if (cardScore[i - 1] > cardScore[idBestCard - 1]) {
                idBestCard = i;
            }
        }
        return idBestCard;
    }

    /**
     * Creates a list of all playable cards from a player's hand cards.
     *
     * This method goes through all possible cards in the standard 52-card deck
     * and adds each card that is in the player's hand to the list of playable cards.
     *
     * @param currentHandCards The current hand cards of the player.
     * @return A {@code ArrayList<Card>} containing all playable cards of the player.
     */
    public static ArrayList<Card> getPlaybleCards(HandCards currentHandCards) {
        ArrayList<Card> playableCards = new ArrayList<>();

        for (int i = 1; i < 53; i++) {
            if (currentHandCards.containsCard(i)) {
                playableCards.add(new Card(i));
            }
        }
        return playableCards;
    }

    /**
     * Creates an array of boolean values indicating which cards from the deck have already been played.
     *
     * This method goes through the tricks played so far and marks each played card in the array `cardsPlayed`.
     * Each element in the boolean array `cardsPlayed` is set to `true` if the corresponding card
     * has already been played. This information can be used to make decisions in the game based on the
     * cards that have already been played.
     *
     * @param allTricks Array of `Trick` objects representing all tricks played so far.
     * @param deckSize  The size of the deck, defining the length of the returned boolean array.
     * @param tricksToConsider From which trick onwards the played cards should be considered.
     * @param tricksPlayed How many tricks have been played so far, defining the upper bound of the loop.
     * @return A boolean array indicating whether each card position in the deck has been played.
     */
    public static boolean[] calculatePlayedCards(Trick[] allTricks, int tricksToConsider, int tricksPlayed) {
        boolean[] cardsPlayed = new boolean[52];

        for (int i = tricksToConsider; i < tricksPlayed - 1; i++) {
            int[] cardsFromTrick = allTricks[i].getCardsPlayed();
            for (int cardId : cardsFromTrick) {
                cardsPlayed[cardId - 1] = true;
            }
        }
        return cardsPlayed;
    }

    /**
     * Determines an array of boolean values indicating which cards from the deck are still available.
     *
     * This method compares the given array of played cards (`cardsPlayed`) and the cards
     * on hand (`handCards`) to determine which cards have not yet been played and are also not on hand.
     * Each element in the boolean array `cardsAvailable` is set to `true` if the corresponding card
     * is still available.
     *
     * @param cardsPlayed A boolean array indicating whether each card in the deck has been played already.
     * @param handCards The hand cards of the player, used to check which cards are still available.
     * @return A boolean array indicating which cards have not been played and are not in hand.
     */
    public static boolean[] calculateAvailableCards(boolean[] cardsPlayed, HandCards handCards) {
        int deckSize = 52;
        boolean[] cardsAvailable = new boolean[deckSize];

        for (int i = 0; i < deckSize; i++) {
            if (!cardsPlayed[i] && !handCards.containsCard(i + 1)) {
                cardsAvailable[i] = true;
            } else {
                cardsAvailable[i] = false;
            }
        }
        return cardsAvailable;
    }

    /**
     * Checks if a card is considered a high card.
     * High cards are typically those with a higher chance of winning a trick.
     * 
     * @param int The card id to check.
     * @return {@code true} if the card is a high card, {@code false} otherwise.
     */
    public static boolean isHighCard(int cardId) {
        int cardValue = (cardId - 1) % 13;
        
        if (cardValue > 9){
            return true;
        }
        
        return false;
    }

    /**
     * Finds the highest card of a specific suit from a list of cards.
     * 
     * @param cards The list of cards to search through.
     * @param suit  The suit to search for the highest card.
     * @return The highest card of the specified suit, or null if no such card is found.
     */
    public static Card findHighestCardInSuit(ArrayList<Card> cards, Suits suit) {
        Card highestCard = null;
    
        for (Card card : cards) {
            if (card.getSuit() == suit) {
                if (highestCard == null || card.getId() > highestCard.getId()) {
                    highestCard = card;
                }
            }
        }
    
        return highestCard;
    }

    /**
     * Determines the card with the higher value from two given cards.
     * If one of the cards has an invalid value (-1), the other card is returned.
     * If both cards have an invalid value, -1 is returned.
     *
     * @param handCards The hand cards used to determine the value.
     * @param card1 The ID of the first card.
     * @param card2 The ID of the second card.
     * @return The ID of the card with the higher value, or -1 if both cards are invalid.
     */
    public static int getHighestValueCard(HandCards handCards, int card1, int card2) {
        if (card1 == -1 && card2 == -1) return -1;
        if (card1 == -1) return card2;
        if (card2 == -1) return card1;
        return handCards.hasHigherValue(card1, card2) ? card1 : card2;
    }

    /**
     * Determines the card with the lower value from two given cards.
     * If either of the cards has an invalid value (-1), the other card is returned.
     *
     * @param handCards The hand cards used to determine the value.
     * @param card1 The ID of the first card.
     * @param card2 The ID of the second card.
     * @return The ID of the card with the lower value.
     */
    public static int getLowestValueCard(HandCards handCards, int card1, int card2) {
        if (card1 == -1) return card2;
        if (card2 == -1) return card1;
        return handCards.hasHigherValue(card1, card2) ? card2 : card1;
    }
    
    /**
     * Finds the highest card within a specified range in a hand that has not been passed yet.
     * 
     * @param handCards    The hand cards to search through.
     * @param start        The starting value of the range (inclusive).
     * @param end          The ending value of the range (inclusive).
     * @param passedCards  An array containing cards that have already been passed.
     * @return The ID of the highest card in the range, or -1 if no card is found.
     */
    public static int findHighestCardInRange(HandCards handCards, int start, int end, int[] passedCards) {
        for (int i = end; i >= start; i--) {
            if (handCards.containsCard(i) && !alreadyPassed(passedCards, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the lowest card within a specified range in a hand that has not been passed yet.
     * 
     * @param handCards    The hand cards to search through.
     * @param start        The starting value of the range (inclusive).
     * @param end          The ending value of the range (inclusive).
     * @param passedCards  An array containing cards that have already been passed.
     * @return The ID of the lowest card in the range, or -1 if no card is found.
     */
    public static int findLowestCardInRange(HandCards handCards, int start, int end, int[] passedCards) {
        for (int i = start; i <= end; i++) {
            if (handCards.containsCard(i) && !alreadyPassed(passedCards, i)) {
                return i;
            }
        }
        return -1; 
    }

    /**
     * Checks if a specific card has already been selected for passing.
     * 
     * @param bestCardsToPass An array with cards that have been selected for passing.
     * @param card            The card to check.
     * @return {@code true} if the card has already been selected for passing, {@code false} otherwise.
     */
    public static boolean alreadyPassed(int[] bestCardsToPass, int card) {
        for (int value : bestCardsToPass) {
            if (value == card) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts the number of cards belonging to each suit in the current hand.
     * This method assumes that the deck is evenly divided into the four suits.
     * 
     * @param currentHandCards The current hand cards of the player.
     * @return A {@code SuitCount} object containing the count of cards for each suit.
     */
    public static SuitCount countSuitsWithCards(HandCards currentHandCards) {
        int deckSize = 52;
        int cntKaro = currentHandCards.countCardsInRange(1, deckSize / 4);
        int cntHearts = currentHandCards.countCardsInRange(deckSize / 4 + 2, 2 * deckSize / 4);
        int cntPic = currentHandCards.countCardsInRange(2*deckSize / 4 + 1, 3 * deckSize / 4);
        int cntKreuz = currentHandCards.countCardsInRange(3*deckSize / 4 + 1, deckSize);
        int cntHandCards = currentHandCards.countCardsInRange(1, 52);
    
        return new SuitCount(cntKaro, cntHearts, cntPic, cntKreuz, cntHandCards);
    }
    
}
