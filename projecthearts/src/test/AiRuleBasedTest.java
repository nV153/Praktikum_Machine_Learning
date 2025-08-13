package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import game.*;
import game.AI.AiRuleBased.AiRuleBased;

public class AiRuleBasedTest {
    AiRuleBased ai;
    Player player;
    GameState gameState;
    int playerNo;
    HandCards handcards;

    @Before
    public void init(){
        playerNo = 1;
        gameState = new GameState();
        gameState.init();
        gameState.setAIPlayers();
        gameState.setGameMode(GameMode.HUMAN);

        handcards = gameState.getPlayers()[playerNo].getHandCards();

        ai = new AiRuleBased();
    }

    @Test
    public void testBestPass_passSpades_expert() {
        // Arrange
        gameState.setDifficulty(Difficulty.EXPERT);
        handcards.addCard(39); // Annahme: Pik Ass
        handcards.addCard(38); // Annahme: Pik König
        handcards.addCard(27); // Annahme: Pik Zwei

        int[] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 39; // Pik Ass
        expectedCardsToPass[1] = 0; // nichts
        expectedCardsToPass[2] = 0; // nichts

        // Act
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        // Assert
        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }

    
    @Test
    public void testBestPass_passOther_passOnlyHighest() {
        gameState.setDifficulty(Difficulty.MEDIUM);
        // Arrange
        handcards.addCard(13); // Annahme: Ass Kreuz
        handcards.addCard(2); // Annahme: drei Kreuz
        handcards.addCard(51); // Annahme: König Diamant
        
        int [] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 13;
        expectedCardsToPass[1] = 51;
        expectedCardsToPass[2] = 2;
        
        // Assert
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }
    
    @Test
    public void testBestPass_passHearts_medium() {
        // Arrange
        gameState.setDifficulty(Difficulty.MEDIUM);
        handcards.addCard(26); // Annahme: Ass Herz
        handcards.addCard(25); // Annahme: Ass Herz
        handcards.addCard(14); // Annahme: zwei Herz

        int[] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 26; // Ass Herz
        expectedCardsToPass[1] = 25; // König Herz
        expectedCardsToPass[2] = 0; // nichts

        // Act
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        // Assert
        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }
    
    @Test
    public void testBestPass_passOther_expert() {
        // Arrange
        gameState.setDifficulty(Difficulty.EXPERT);
        handcards.addCard(45); // Annahme: Sieben Kreuz
        handcards.addCard(5); // Annahme: Sieben Diamant
        handcards.addCard(25); // Annahme: Sieben Herz

        int[] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 25; // Sieben Diamant
        expectedCardsToPass[1] = 5; // Sieben Kreuz
        expectedCardsToPass[2] = 45; // nichts

        // Act
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        // Assert
        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }

    @Test
    public void testBestPass_passOther_medium() {
        // Arrange
        gameState.setDifficulty(Difficulty.MEDIUM);
        handcards.addCard(3); // Annahme: Drei Kreuz
        handcards.addCard(41); // Annahme: Drei Diamant
        handcards.addCard(32); // Annahme: Drei Herz

        int[] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 3; // Drei Diamant
        expectedCardsToPass[1] = 41; // Drei Kreuz
        expectedCardsToPass[2] = 0; // Drei Herz

        // Act
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        // Assert
        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }

    @Test
    public void testBestPass_lowCards_hard() {
        // Arrange
        gameState.setDifficulty(Difficulty.HARD);
        handcards.addCard(2);  // Zwei Diamant
        handcards.addCard(14); // Zwei Herz
        handcards.addCard(27); // Zwei Pik
        handcards.addCard(40); // Zwei Kreuz
        handcards.addCard(41); // Drei Kreuz

        int[] expectedCardsToPass = new int[3];
        expectedCardsToPass[0] = 2;  // Zwei Diamant
        expectedCardsToPass[1] = 40; // Zwei Kreuz
        expectedCardsToPass[2] = 41; // drei Kreuz

        // Act
        int[] actualCardsPassed = ai.bestPass(gameState, playerNo, 0);

        // Assert
        assertArrayEquals("Fehler, gespasste Karten stimmen nicht überein", expectedCardsToPass, actualCardsPassed);
    }


    // ************************************************************************************************
    // Hier beginnt der Abschnitt für Test: bestMove()
    // ************************************************************************************************

}


