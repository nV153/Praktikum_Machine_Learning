package test;

import static org.junit.Assert.*;
import org.junit.Test;
import game.Trick;
import game.Card;
import game.CardCoding;

public class TrickTest {

    private Card diamonds2 = new Card(CardCoding.DIAMONDS_2.getId());
    private Card diamonds9 = new Card (CardCoding.DIAMONDS_9.getId());
    private Card hearts10 = new Card (CardCoding.HEARTS_10.getId());
    private Card queenOfSpades = new Card (CardCoding.SPADES_QUEEN.getId());
    private Card clubs10 = new Card (CardCoding.CLUBS_10.getId());
    private Card queenOfHearts = new Card(CardCoding.HEARTS_QUEEN.getId());
     
    @Test
    public void numberOfPlayedCards(){
        
        Trick trick = new Trick(2);
        assertEquals(trick.getNumPlayedCards(), 0);
        
        trick.addCard(diamonds2.getId(), 2);
        assertEquals(trick.getNumPlayedCards(), 1);
        
        trick.addCard(hearts10.getId(), 3);
        assertEquals(trick.getNumPlayedCards(), 2);
        
        trick.addCard(queenOfSpades.getId(), 0);
        assertEquals(trick.getNumPlayedCards(), 3);
        
        trick.addCard(clubs10.getId(), 1);
        assertEquals(trick.getNumPlayedCards(), 4);
    }
    
    @Test
    public void startingPlayerWinsOneCardPlayed(){
    
        Trick trick = new Trick(2);
        
        trick.addCard(diamonds2.getId(), 2);    
    
        assertEquals(trick.getHighCard(), diamonds2);
        assertEquals(trick.getWinner(), 2);
    }

    @Test
    public void secondPlayerWinsTwoCardsPlayed(){
    
        Trick trick = new Trick(3);
    
        trick.addCard(diamonds2.getId(), 3);
        trick.addCard(diamonds9.getId(), 0);    
    
        assertEquals(trick.getHighCard(), diamonds9);
        assertEquals(trick.getWinner(), 0);
    }

    @Test
    public void thirdPlayerWinsThreeCardsPlayed(){
    
        Trick trick = new Trick(1);
        
        trick.addCard(diamonds2.getId(), 1);
        trick.addCard(hearts10.getId(), 2);
        trick.addCard(diamonds9.getId(), 3);    
        
        assertEquals(trick.getHighCard(), diamonds9);
        assertEquals(trick.getWinner(), 3);
    }

    @Test
    public void firstPlayerWinsFourCardsPlayed(){
    
        Trick trick = new Trick(0);
    
        trick.addCard(diamonds2.getId(), 0);
        trick.addCard(hearts10.getId(), 1);
        trick.addCard(queenOfSpades.getId(), 2);    
        trick.addCard(clubs10.getId(), 3);
    
        assertEquals(trick.getHighCard(), diamonds2);
        assertEquals(trick.getWinner(), 0);
    }

    @Test
    public void countingCards(){
    
        Trick trick = new Trick(0);
    
        trick.addCard(diamonds2.getId(), 0);
        assertEquals(trick.countPoints(), 0);
    
        trick.addCard(hearts10.getId(), 1);
        assertEquals(trick.countPoints(), 1);
    
        trick.addCard(queenOfSpades.getId(), 2); 
        assertEquals(trick.countPoints(), 14);   
    
        trick.addCard(queenOfHearts.getId(), 3);
        assertEquals(trick.countPoints(), 15);
    }


}
