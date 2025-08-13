package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import game.Player;
import game.GameState;

public class GameStateTest {
    
    GameState gs = new GameState();
    
    @Before
    public void setUp(){
        gs.init();
        gs.setHumanPlayer("tester");
    }

    @Test
    public void initPlayersRight(){
        
        // Assert: Menschlicher Spieler wird an Position 3 gespeichert
        assertTrue(gs.getPlayers()[3].isHuman());
        assertFalse(gs.getPlayers()[0].isHuman());
    }

    @Test
    public void handOutThirteenCards(){
        
        gs.createDeck(true);
        gs.handOutCards();
        
        // Assert: bei einem Standarddeck erhält jeder Spieler 13 Karten
        for (Player player : gs.getPlayers()){
            assertEquals(player.countCards(), 13);
        }
    }

    @Test
    public void handOutEightCards(){
        
        gs.createDeck(false);
        gs.handOutCards();
        
        // Assert: bei einem Deck mit 32 Karten erhält jeder Spieler 8 Karten
        for (Player player : gs.getPlayers()){
            assertEquals(player.countCards(), 8);
        }
    }
     
    @Test
    public void getRightStartingPlayer52(){
        
        gs.createDeck(true);
        gs.handOutCards();
        Player starter = gs.getStarterOfRound();
        
        // Assert: bei einem Standarddeck fängt Spieler mit Kreuz 2 an
        assertTrue(starter.hasClubsTwo());  
    }

    @Test
    public void getRightStartingPlayer32(){
        
        gs.createDeck(false);
        gs.handOutCards();
        Player starter = gs.getStarterOfRound();
        
        // Assert: bei 32 Karten fängt der Spieler mit Kreuz 7 an
        assertTrue(starter.hasClubsSeven());  
    }


//TODO Tests fürs Punkte zählen / Stiche auswerten einfügen





}

