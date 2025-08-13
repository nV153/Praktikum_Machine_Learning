package test;

import static org.junit.Assert.*;
import org.junit.Test;
import game.Player;
import game.Card;
import game.Trick;
import game.CardCoding;



public class PlayerTest {
    
    Card clubs2 = new Card(CardCoding.CLUBS_2.getId());
    Card clubs7 = new Card(CardCoding.CLUBS_7.getId());
    Card diamonds3 = new Card(CardCoding.DIAMONDS_3.getId());
    Trick trick = new Trick(3);
    Player human = new Player("tester", true, 3);
    Player ai = new Player("ai", false, 0);
    int[] cardsToPass = {CardCoding.CLUBS_2.getId(), CardCoding.CLUBS_7.getId(), CardCoding.DIAMONDS_3.getId()};

    
    @Test
    public void hasHandCards(){   
        
        human.addHandCard(clubs2);
        human.addHandCard(clubs7);

        assertEquals(human.countCards(), 2);
        assertTrue(human.hasClubsSeven());
        assertTrue(human.hasClubsTwo());
        assertFalse(human.hasHandCard(diamonds3));  
    }

    @Test
    public void canPlayCards(){
        
        human.addHandCard(clubs2);
        
        human.playCard(clubs2.getId(), trick);
        
        assertFalse(human.hasClubsTwo());
        assertEquals(human.countCards(), 0);
    }

    @Test
    public void canPassCards(){
        
        human.addHandCard(clubs2);
        human.addHandCard(clubs7);
        human.addHandCard(diamonds3);
        human.savePassedCards(cardsToPass);
        
        human.passCards(ai);
        
        assertFalse(human.hasHandCard(clubs2));
        assertFalse(human.hasHandCard(clubs7));
        assertFalse(human.hasHandCard(diamonds3));
        assertTrue(ai.hasHandCard(clubs2));
        assertTrue(ai.hasHandCard(clubs7));
        assertTrue(ai.hasHandCard(diamonds3));
    }
}
