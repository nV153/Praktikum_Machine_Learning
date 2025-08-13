package game;

import java.util.Random;
import java.util.Arrays;

/**
 * Represents a deck of playing cards.
 * The deck can be either a standard 52-card deck or a 32-card deck.
 */

public class Deck {

    private Card[] cards;    
    private int size; // 52 or 32
    private boolean isStandardDeck;
    
    /**
    * Constructs a new deck of cards with a specified size.
    *
    * @param isStandardDeck If {@code true}, creates a standard deck of 52 cards; if {@code false}, creates a deck of 32 cards.
    */
    public Deck(boolean isStandardDeck){

        this.isStandardDeck = isStandardDeck;
        if (isStandardDeck) size = 52;
        else size = 32;

        initDeck();
        shuffle();
    }

    /*
    * Initializes the deck by creating the cards.
    * The ID of a card remains the same regardless of the deck size.
    */
    private void initDeck(){

        this.cards = new Card[size];

        int cardValue = isStandardDeck ? 1 : 6;

        for (int i = 0; i < size; i++){
            
            boolean isFirstCardOfSuit = false;
            if (cardValue % 13 == 1) isFirstCardOfSuit = true;
            
            // Special case 32 cards --> after Ace comes 7
            if (!isStandardDeck && isFirstCardOfSuit) cardValue += 5;
            
            cards[i] = new Card(cardValue);
            cardValue++;
        }
    }

    /**
    * Shuffles the deck using the Fisher-Yates Shuffle algorithm.
     */
    public void shuffle() {
        
        Random random = new Random();
        
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            
            // swap two cards
            Card temp = cards[i];
            cards[i] = cards[j];
            cards[j] = temp;
        }
    }

    /**
    * Prints the cards in the deck for testing purposes.
    */
    public void printDeck(){
        
        System.out.println("The deck contains the following cards: ");
        for (int i = 0; i < size; i++) {
            Card card = cards[i];
            System.out.println("Card [" + card.getId() + "] = " + card.getCardName());
        }
    }

    /**
    * Checks if the deck contains a specific card, used for testing purposes.
    *
    * @param otherCard The card to check for in the deck.
    * @return {@code true} if the deck contains the specified card, {@code false} otherwise.
    */
    public boolean containsCard(Card otherCard){
        for (Card card : cards){
            if (card.getId() == otherCard.getId()) return true;
        }
        return false;
    }

    /**
    * Gets the size of the deck.
    *
    * @return The size of the deck.
    */
    public int getSize(){
        return size;
    }

    /**
    * Gets the cards in the deck.
    *
    * @return An array containing copies of the cards in the deck.
    */
    public Card[] getCards(){

        if (cards == null) return new Card[0];
        
        return Arrays.copyOf(cards, cards.length);
    }
}
