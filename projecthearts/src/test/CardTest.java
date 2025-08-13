package test;

import static org.junit.Assert.*;
import org.junit.Test;
import game.Card;
import game.CardCoding;


public class CardTest {

    //Vergleichskarte Herz König
    private Card otherCard = new Card(CardCoding.HEARTS_KING.getId());

    @Test
    public void testDiamondsTen() {
        
        Card card = new Card(CardCoding.DIAMONDS_10.getId());
        
        assertFalse(card.isHeart()); 
        assertFalse(card.isQueenOfSpades()); 
        assertFalse(card.sameSuit(otherCard)); 
        assertFalse(card.isHigherAs(otherCard)); // gewinnt nicht, wenn Herz König zuerst gelegt wurde
        assertEquals(card.getHigherCard(otherCard), otherCard); // Herz König gewinnt, wenn zuerst gelegt
        assertEquals(card.getImagePath(), "../GUI/assets/diamonds09.png");
    }

    @Test
    public void testHeartsAce() {
        
        Card card = new Card(CardCoding.HEARTS_ACE.getId());
        
        assertTrue(card.isHeart());
        assertFalse(card.isQueenOfSpades());
        assertTrue(card.sameSuit(otherCard));
        assertTrue(card.isHigherAs(otherCard)); // gewinnt, wenn Herz König zuerst gelegt wurde
        assertEquals(card.getHigherCard(otherCard), card); // ist die höhere Karte, wenn Herz König zuerst gelegt wurde
        assertEquals(card.getImagePath(), "../GUI/assets/hearts13.png");
    }

    @Test
    public void testQueenOfSpades() {
        
        Card card = new Card(CardCoding.SPADES_QUEEN.getId());
        
        assertFalse(card.isHeart());
        assertTrue(card.isQueenOfSpades());
        assertFalse(card.sameSuit(otherCard));
        assertFalse(card.isHigherAs(otherCard)); // gewinnt nicht, wenn Herz König zuerst gelegt wurde
        assertEquals(card.getHigherCard(otherCard), otherCard);
        assertEquals(card.getImagePath(), "../GUI/assets/spades11.png");
    }
}
