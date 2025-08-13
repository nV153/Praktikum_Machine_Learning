package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import game.HandCards;
import game.CardCoding;

public class HandCardsTest {

    HandCards cards = new HandCards();

    @Before
    public void init(){
        // Alle Karten den Handkarten hinzufügen
        for (int i = 1; i <= cards.getIsInHand().length; i++){
            cards.addCard(i);
        }
    }
    
    @Test
    public void hasCards(){
       
        cards.removeCard(CardCoding.DIAMONDS_3.getId());   
        
        assertTrue(cards.containsCard(CardCoding.DIAMONDS_2.getId()));
        assertTrue(cards.containsCard(CardCoding.CLUBS_ACE.getId()));
        assertFalse(cards.containsCard(CardCoding.DIAMONDS_3.getId()));
    }

    @Test
    public void testHasHigherValue(){
    
        // Assert: verschiedene Kartenwerte miteinander vergleichen, Farbe ist egal 
        assertTrue(cards.hasHigherValue(CardCoding.DIAMONDS_ACE.getId(), CardCoding.DIAMONDS_KING.getId())); 
        assertEquals(cards.getHigherId(CardCoding.DIAMONDS_ACE.getId(), CardCoding.DIAMONDS_KING.getId()), CardCoding.DIAMONDS_ACE.getId());

        assertTrue(cards.hasHigherValue(CardCoding.DIAMONDS_ACE.getId(), CardCoding.HEARTS_2.getId())); 
        assertEquals(cards.getHigherId(CardCoding.DIAMONDS_ACE.getId(), CardCoding.HEARTS_2.getId()), CardCoding.DIAMONDS_ACE.getId());
        
        assertTrue(cards.hasHigherValue(CardCoding.DIAMONDS_ACE.getId(), CardCoding.SPADES_QUEEN.getId())); 
        assertEquals(cards.getHigherId(CardCoding.DIAMONDS_ACE.getId(), CardCoding.SPADES_QUEEN.getId()), CardCoding.DIAMONDS_ACE.getId());

        assertFalse(cards.hasHigherValue(CardCoding.DIAMONDS_ACE.getId(), CardCoding.HEARTS_ACE.getId())); 
        assertEquals(cards.getHigherId(CardCoding.DIAMONDS_ACE.getId(), CardCoding.HEARTS_ACE.getId()), 0); // gleicher Wert --> Rückgabe 0
        
        assertFalse(cards.hasHigherValue(CardCoding.DIAMONDS_KING.getId(), CardCoding.HEARTS_ACE.getId())); 
        assertEquals(cards.getHigherId(CardCoding.DIAMONDS_KING.getId(), CardCoding.HEARTS_ACE.getId()), CardCoding.HEARTS_ACE.getId());   
    }

    @Test
    public void testCardsInRange(){
    
        HandCards newCards = new HandCards();

        newCards.addCard(CardCoding.DIAMONDS_KING.getId());
        newCards.addCard(CardCoding.DIAMONDS_ACE.getId());
        newCards.addCard(CardCoding.HEARTS_2.getId());
        newCards.addCard(CardCoding.HEARTS_ACE.getId());
        newCards.addCard(CardCoding.SPADES_QUEEN.getId());
        
        // Assert: Anzahl der Karten im angegebenen Bereich zählen
        assertEquals(newCards.countCardsInRange(1, 52), 5);
        assertEquals(newCards.countCardsInRange(1, CardCoding.DIAMONDS_KING.getId()), 1);
        assertEquals(newCards.countCardsInRange(1, CardCoding.HEARTS_8.getId()), 3);  
        assertEquals(newCards.countCardsInRange(1, CardCoding.SPADES_5.getId()), 4);
        assertEquals(newCards.countCardsInRange(1, CardCoding.DIAMONDS_QUEEN.getId()), 0); 
    }

}
