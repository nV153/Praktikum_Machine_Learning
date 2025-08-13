package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import game.Logger;
import game.Player;
import game.Trick;
import game.Card;
import game.CardCoding;

public class LoggerTest {

    Player human = new Player("tester", true, 3);
    Player ai1 = new Player("ai", false, 0);
    Player ai2 = new Player("ai2", false, 1);
    Player ai3 = new Player("ai3", false, 2);
    Player[] players;

    Trick trick = new Trick(0);
    private Card diamonds2 = new Card(CardCoding.DIAMONDS_2.getId());
    private Card diamonds9 = new Card (CardCoding.DIAMONDS_9.getId());
    private Card hearts10 = new Card (CardCoding.HEARTS_10.getId());
    private Card queenOfSpades = new Card (CardCoding.SPADES_QUEEN.getId());
    

    Logger logger = new Logger(10, 52, "data", -1);
    

    @Before
    public void init(){
        // init players, set handcards
        players = new Player[]{ai1, ai2, ai3, human};
        ai1.addHandCard(diamonds9);
        ai1.addHandCard(diamonds2);
        ai2.addHandCard(hearts10);
        ai3.addHandCard(queenOfSpades);
        human.getHandCards().addCard(CardCoding.SPADES_9.getId());

    }

    @Test
    public void logPassed(){
        int[] cards1 = {CardCoding.DIAMONDS_2.getId(), CardCoding.DIAMONDS_9.getId(), CardCoding.HEARTS_10.getId()};
        int[] cards2 = {CardCoding.DIAMONDS_3.getId(), CardCoding.DIAMONDS_10.getId(), CardCoding.HEARTS_9.getId()};
        int[] cards3 = {CardCoding.DIAMONDS_2.getId(), CardCoding.DIAMONDS_9.getId(), CardCoding.HEARTS_10.getId()};
        int[] cards4 = {CardCoding.DIAMONDS_5.getId(), CardCoding.DIAMONDS_6.getId(), CardCoding.HEARTS_7.getId()};

        int[][] allCards = {cards1, cards2, cards3, cards4};
        logger.logCardsPassed(allCards);

        assertEquals(logger.getCardsPassed().get(0), "[[1.8.22].[2.9.21].[1.8.22].[4.5.19]]");
    }

    @Test
    public void logHandCards(){

        logger.logHandCards(players);

        assertEquals(logger.getHandCardsP0AfterPassing().get(0), "[1.8]");
    }

    @Test
    public void logTricks(){
        trick.addCard(diamonds2.getId(), 0);
        trick.addCard(diamonds9.getId(), 1);
        trick.addCard(hearts10.getId(), 2);
        trick.addCard(queenOfSpades.getId(), 3);

        logger.logTrickPlayed(trick, 0);

        assertEquals(logger.getTricksPlayed().get(0), "1.8.22.37");
        assertEquals(logger.getStartingPlayer().get(0), "0");
    }

    @Test
    public void logRoundPts(){
        ai1.addTrickPoints(3);
        human.addTrickPoints(10);

        logger.logRoundPoints(players);

        assertEquals(logger.getRoundPoints().get(0), "3.0.0.10");
    }

    @Test
    public void logGamePts(){
        ai1.addTrickPoints(4);
        human.addTrickPoints(6);
        ai1.addRoundPoints();
        human.addRoundPoints();

        logger.logGamePoints(players);

        assertEquals(logger.getGamePoints().get(0), "4.0.0.6");
    }
}
