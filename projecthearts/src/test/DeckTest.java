package test;

import static org.junit.Assert.*;
import org.junit.Test;
import game.Deck;
import game.Card;


public class DeckTest {

    @Test
    public void testStandardDeck() {
        
        Deck deck = new Deck(true);
        
        // Assert: alle Karten wurden korrekt erstellt
        assertEquals(deck.getSize(), 52);
        for (int i = 0; i < deck.getSize(); i++){
            Card card = new Card(i+1);
            assertTrue(deck.containsCard(card));
        }
    }

    @Test
    public void test32Deck() {
        
        Deck deck = new Deck(false);
        
        // Assert: alle Karten wurden korrekt erstellt
        assertEquals(deck.getSize(), 32);
        for (int i = 6; i <= deck.getSize(); i++){
            if (i % 13 == 1) i+= 5;
            Card card = new Card(i);
            assertTrue(deck.containsCard(card));
        }
    }

}
