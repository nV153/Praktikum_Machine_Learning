package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import weka.core.SerializationHelper;

/**
 * The Logger class is responsible for logging game-related data in the
 * simulation mode. The following data are getting logged:
 * passed cards, hand cards after passing, played tricks, round points, and game
 * points. The logged data can be exported to a CSV file.
 */

public class Logger {

    private ArrayList<String> numberOfTricksPlayed = new ArrayList<String>();
    private ArrayList<String> handCardsP0 = new ArrayList<String>();
    private ArrayList<String> handCardsP1 = new ArrayList<String>();
    private ArrayList<String> handCardsP2 = new ArrayList<String>();
    private ArrayList<String> handCardsP3 = new ArrayList<String>();
    private ArrayList<String> availableCards = new ArrayList<String>();
    private ArrayList<String> handCardsPlayableP0 = new ArrayList<String>();
    private ArrayList<String> handCardsPlayableP1 = new ArrayList<String>();
    private ArrayList<String> handCardsPlayableP2 = new ArrayList<String>();
    private ArrayList<String> handCardsPlayableP3 = new ArrayList<String>();
    private ArrayList<String> directionToPass = new ArrayList<String>();
    private ArrayList<String> cardsPassed = new ArrayList<String>();
    private ArrayList<String> numberOfStarter = new ArrayList<String>();
    private ArrayList<String> tricksPlayed = new ArrayList<String>();
    private ArrayList<String> roundPoints = new ArrayList<String>();
    private ArrayList<String> gamePoints = new ArrayList<String>();
    private ArrayList<String> roundNumbers = new ArrayList<String>();
    private ArrayList<String> gameNumbers = new ArrayList<String>();
    private ArrayList<String> payoff = new ArrayList<String>();
    private ArrayList<double[]> payoffAtTerminalState = new ArrayList<double[]>();
    private ArrayList<String> handCardsBeforePassing = new ArrayList<String>();
    private ArrayList<boolean[]> cardsPlayedInThisRound = new ArrayList<boolean[]>();

    // stores at index x, how often player with playerNo x has won a game in simulation mode 
    private int[] numberOfWins;
    private int[] numberOf2ndPlaces;
    private int[] numberOf3rdPlaces;
    private int[] numberOf4thPlaces;
    private int[] numberOfShootTheMoons;
    private long[] totalGamePts;
    private double[] avgGamePts;
    private long[] totalGamePtsPayoff;
    private double[] avgGamePtsPayoff;

    private int numberOfGames;
    private int gameNumber;
    private int roundNumber;
    private int trickNumber;
    private int deckSize;
    private String mode;
    private int viewPlayerNo;

    /**
     * Initializes a new Logger with the specified number of games.
     *
     * @param numberOfGames The number of games to be logged.
     */
    public Logger(int numberOfGames, int deckSize, String mode, int viewPlayerNo) {
        this.numberOfGames = numberOfGames;
        this.numberOfWins = new int[4];
        numberOf2ndPlaces = new int[4];
        numberOf3rdPlaces = new int[4];
        numberOf4thPlaces = new int[4];
        numberOfShootTheMoons = new int[4];
        gameNumber = 0;
        this.deckSize = deckSize;
        this.mode = mode;
        this.viewPlayerNo = viewPlayerNo;
        totalGamePts = new long[4];
        avgGamePts = new double[4];
        totalGamePtsPayoff = new long[4];
        avgGamePtsPayoff = new double[4];
    }

    /**
     * Exports the logged data to a CSV file with the specified file name and
     * serializes the payoff values.
     *
     * @param fileName The name of the CSV file to export the data to.
     */
    public void exportData(String fileNamePass, String fileNamePlay) {

        if (mode.equals("readable")) {
            exportReadablePassData(fileNamePass);
            exportReadablePlayData(fileNamePlay);

        } else {
            exportExpandedPassData(fileNamePass);
            exportExpandedPlayData(fileNamePlay);
        }

        //serializePayoff(payoffAtTerminalState);  //TODO prüfen ob aktuell noch gebraucht
    }
    
    /**
    * Exports readable pass data to a file specified by the given fileNamePass.
    * This method constructs a CSV file containing pass data such as round number, game number,
    * direction to pass, passed cards, and hand cards before passing.
    * The file is formatted for readability.
    * The viewPlayerNo decides, how much information is added to the csv file. In case -1, all information
    * available is exported. In case viewPlayerNo = playerNo, only the information visible to the player
    * with the given playerNo is exported.
    *
    **/
    private void exportReadablePassData(String fileNamePass) {
        try (FileWriter passWriter = new FileWriter(fileNamePass)) {

            // build header
            StringBuilder header = new StringBuilder("roundNo,gameNo,directionToPass,");

            if (viewPlayerNo == -1) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 3; j++) {
                        header.append("pass").append(j).append("P").append(i).append(",");
                    }
                }
                for (int i = 0; i < 4; i++) {
                    header.append("handCardsP").append(i).append(",");
                }
            }
            else {
                for (int j = 0; j < 3; j++) {
                    header.append("pass").append(j).append("P").append(viewPlayerNo).append(",");
                }
                header.append("handCardsP").append(viewPlayerNo);
            }

            passWriter.append(header);
            passWriter.append("\n");

            int numberOfTricks = (deckSize == 52) ? 13 : 8;

            for (int i = 0; i < cardsPassed.size(); i++) {
                passWriter.append(roundNumbers.get(i * numberOfTricks));
                passWriter.append(",");

                passWriter.append(gameNumbers.get(i * numberOfTricks));
                passWriter.append(",");

                passWriter.append(directionToPass.get(i));
                passWriter.append(",");

                passWriter.append(cardsPassed.get(i));
                passWriter.append(",");
                int baseIndex = i*4;

                if (viewPlayerNo == -1){
                    
                    passWriter.append(handCardsBeforePassing.get(baseIndex));
                        passWriter.append(",");
                        passWriter.append(handCardsBeforePassing.get(baseIndex + 1));
                        passWriter.append(",");
                        passWriter.append(handCardsBeforePassing.get(baseIndex + 2));
                        passWriter.append(",");
                        passWriter.append(handCardsBeforePassing.get(baseIndex + 3));
                } 
                else passWriter.append(handCardsBeforePassing.get(baseIndex + viewPlayerNo));

                if (i != cardsPassed.size() - 1)
                    passWriter.append("\n");
            }

            //System.out.println("Pass data stored successfully.");
        }
        catch (IOException e) {
            System.out.println("Passed data could not be stored: " + e.getMessage());
        }
    }

    /**
    * Exports expanded pass data to a file specified by the given fileNamePass.
    * This method constructs a CSV file containing pass data such as round number, game number,
    * direction to pass, passed cards, and hand cards before passing.
    * The file is formatted for data analysis. This means, every entry has a separate column and empty 
    * entries (e.g. missing hand cards) are filled with -100.
    * The viewPlayerNo decides, how much information is added to the csv file. In case -1, all information
    * available is exported. In case viewPlayerNo = playerNo, only the information visible to the player
    * with the given playerNo is exported.
    *
    **/
    private void exportExpandedPassData(String fileNamePass) {
        try (FileWriter passWriter = new FileWriter(fileNamePass)) {

            // build header
            StringBuilder header = new StringBuilder("roundNo,gameNo,directionToPass,");

            if (viewPlayerNo == -1) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 3; j++) {
                        header.append("pass").append(j).append("P").append(i).append(",");
                    }
                }
                
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 13; j++) {
                        header.append("hC").append(j).append("P").append(i).append(",");
                    }                   
                }
                header.delete(header.length() - 1, header.length());
            }

            else {
                
                for (int j = 0; j < 3; j++) {
                    header.append("pass").append(j).append("P").append(viewPlayerNo).append(",");
                }
                for (int i = 0; i < 13; i++) {
                    header.append("hC").append(i).append("P").append(viewPlayerNo).append(",");
                }
                header.delete(header.length() - 1, header.length());
            }

            passWriter.append(header);
            passWriter.append("\n");

            int numberOfTricks = (deckSize == 52) ? 13 : 8;

            for (int i = 0; i < cardsPassed.size(); i++) {
                passWriter.append(roundNumbers.get(i * numberOfTricks));
                passWriter.append(",");

                passWriter.append(gameNumbers.get(i * numberOfTricks));
                passWriter.append(",");

                passWriter.append(directionToPass.get(i));
                passWriter.append(",");

                passWriter.append(cardsPassed.get(i));
                passWriter.append(",");
                
                int baseIndex = i*4;
                if (viewPlayerNo == -1){
                    
                        passWriter.append(getStringCommaSeparated(handCardsBeforePassing.get(baseIndex)));  
                        passWriter.append(",");
                        passWriter.append(getStringCommaSeparated(handCardsBeforePassing.get(baseIndex + 1)));
                        passWriter.append(",");
                        passWriter.append(getStringCommaSeparated(handCardsBeforePassing.get(baseIndex + 2)));
                        passWriter.append(",");
                        passWriter.append(getStringCommaSeparated(handCardsBeforePassing.get(baseIndex + 3)));
                } 
                else passWriter.append(getStringCommaSeparated(handCardsBeforePassing.get(baseIndex + viewPlayerNo)));

                if (i != cardsPassed.size() - 1)
                    passWriter.append("\n");
            }
            //System.out.println("Pass data stored successfully.");
        }
        catch (IOException e) {
            System.out.println("Passed data could not be stored: " + e.getMessage());
        }
    }

    /**
    * Exports readable play data to a file specified by the given fileNamePlay.
    * This method constructs a CSV file containing play data such as round number, game number,
    * number of tricks played, hand cards, hand cards playable, starting player number,
    * tricks played, round points, game points, and payoff.
    * The file is formatted for readability.
    * The viewPlayerNo decides, how much information is added to the csv file. In case -1, all information
    * available is exported. In case viewPlayerNo = playerNo, only the information visible to the player
    * with the given playerNo is exported. 
    *
    */
    private void exportReadablePlayData(String fileNamePlay) {
        try (FileWriter writer = new FileWriter(fileNamePlay)) {

            StringBuilder header = new StringBuilder("roundNo,gameNo,numberOfTricksPlayed,");
            if (viewPlayerNo == -1) {
                for (int i = 0; i < 4; i++) {
                    header.append("handCardsP").append(i).append(",");
                    header.append("handCardsPlayableP").append(i).append(",");
                }
            } else {
                header.append("handCardsP").append(viewPlayerNo).append(",");
                header.append("handCardsPlayableP").append(viewPlayerNo).append(",");
            }
            header.append("startingPlayerNo,tricksplayed,roundPoints,gamePoints,payoff");

            writer.append(header);
            writer.append("\n");

            for (int i = 0; i < tricksPlayed.size(); i++) {

                writer.append(roundNumbers.get(i));
                writer.append(",");

                writer.append(gameNumbers.get(i));
                writer.append(",");

                writer.append(numberOfTricksPlayed.get(i));
                writer.append(",");

                // logg handcards depending on view
                switch (viewPlayerNo) {
                    case -1:
                        writer.append(handCardsP0.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP0.get(i));
                        writer.append(",");
                        writer.append(handCardsP1.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP1.get(i));
                        writer.append(",");
                        writer.append(handCardsP2.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP2.get(i));
                        writer.append(",");
                        writer.append(handCardsP3.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP3.get(i));
                        writer.append(",");
                        break;

                    case 0:
                        writer.append(handCardsP0.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP0.get(i));
                        writer.append(",");
                        break;

                    case 1:
                        writer.append(handCardsP1.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP1.get(i));
                        writer.append(",");
                        break;

                    case 2:
                        writer.append(handCardsP2.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP2.get(i));
                        writer.append(",");
                        break;

                    case 3:
                        writer.append(handCardsP3.get(i));
                        writer.append(",");
                        writer.append(handCardsPlayableP3.get(i));
                        writer.append(",");
                        break;

                    default:
                        break;
                }

                writer.append(numberOfStarter.get(i));
                writer.append(",");

                writer.append(tricksPlayed.get(i));
                writer.append(",");

                writer.append(roundPoints.get(i));
                writer.append(",");

                writer.append(gamePoints.get(i));
                writer.append(",");

                writer.append(payoff.get(i));
                if (i != tricksPlayed.size() - 1)
                    writer.append("\n");

            }
            //System.out.println("Play data stored successfully.");
        } catch (IOException e) {
            System.out.println("Gespielte Karten konnten nicht gespeichert werden: " + e.getMessage());
        }
    }

    /**
    * Exports play data to a file specified by the given fileNamePlay.
    * This method constructs a CSV file containing play data such as round number, game number,
    * number of tricks played, hand cards, hand cards playable, starting player number,
    * tricks played, round points, game points, and payoff.
    * The file is formatted for data analysis. This means, every entry has a separate column and empty 
    * entries (e.g. missing hand cards) are filled with -100.
    * The viewPlayerNo decides, how much information is added to the csv file. In case -1, all information
    * available is exported. In case viewPlayerNo = playerNo, only the information visible to the player
    * with the given playerNo is exported. 
    *
    */
    private void exportExpandedPlayData(String fileNamePlay) {
        try (FileWriter writer = new FileWriter(fileNamePlay)) {

            StringBuilder header = new StringBuilder("roundNo,gameNo,n0TricksPlayed,");
            //build header
            if (viewPlayerNo == -1) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 13; j++) {
                        header.append("hC").append(j).append("P").append(i).append(",");
                    }
                    for (int k = 0; k < 13; k++) {
                        header.append("hCP").append(k).append("P").append(i).append(",");
                    }
                }
            } else {
                for (int j = 0; j < 13; j++) {
                    header.append("hC").append(j).append("P").append(viewPlayerNo).append(",");
                }
                for (int k = 0; k < 13; k++) {
                    header.append("hCP").append(k).append("P").append(viewPlayerNo).append(",");
                }
                for (int i = 0; i < 39; i++) {
                    header.append("C").append(i).append(",");
                }
            }
            header.append(
                    "startPNo,trickCard0,trickCard1,trickCard2,trickCard3,roundPtsP0,roundPtsP1,roundPtsP2,roundPtsP3,gamePtsP0,gamePtsP1,gamePtsP2,gamePtsP3");

            writer.append(header);
            writer.append("\n");

            for (int i = 0; i < tricksPlayed.size(); i++) {

                writer.append(roundNumbers.get(i));
                writer.append(",");

                writer.append(gameNumbers.get(i));
                writer.append(",");

                writer.append(numberOfTricksPlayed.get(i));
                writer.append(",");

                switch (viewPlayerNo) {
                    case -1:
                        writer.append(getStringCommaSeparated(handCardsP0.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP0.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsP1.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP1.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsP2.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP2.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsP3.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP3.get(i)));
                        writer.append(",");
                        break;

                    case 0:
                        writer.append(getStringCommaSeparated(handCardsP0.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP0.get(i)));
                        writer.append(",");
                        break;

                    case 1:
                        writer.append(getStringCommaSeparated(handCardsP1.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP1.get(i)));
                        writer.append(",");
                        break;

                    case 2:
                        writer.append(getStringCommaSeparated(handCardsP2.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP2.get(i)));
                        writer.append(",");
                        break;

                    case 3:
                        writer.append(getStringCommaSeparated(handCardsP3.get(i)));
                        writer.append(",");
                        writer.append(getStringCommaSeparated(handCardsPlayableP3.get(i)));
                        writer.append(",");
                        break;

                    default:
                        break;

                }

                if (viewPlayerNo != -1) {
                    writer.append(getStringCommaSeparated(availableCards.get(i)));
                    writer.append(",");
                }

                writer.append(numberOfStarter.get(i));
                writer.append(",");

                writer.append(getStringCommaSeparated(tricksPlayed.get(i)));
                writer.append(",");

                writer.append(getStringCommaSeparated(roundPoints.get(i)));
                writer.append(",");

                writer.append(getStringCommaSeparated(gamePoints.get(i)));
                
                if (i != tricksPlayed.size() - 1)
                    writer.append("\n");
            }
            //System.out.println("Play data stored successfully.");
        } catch (IOException e) {
            System.out.println("Gespielte Karten konnten nicht gespeichert werden: " + e.getMessage());
        }
    }

    /**
    * Converts a string containing values separated by dots into a comma-separated string and removes square brackets.
    *
    */
    private String getStringCommaSeparated(String input) {

        String cleanedString = input.replace("[", "").replace("]", "");
        String result = cleanedString.replace(".", ",");

        return result;
    }

    /**
     * Logs the number of tricks played. 0 = passing
     *
     * @param number number of tricks played, is incremented when the first card of
     *               the trick is played
     */
    public void logNumberOfTricksPlayed(int number) {
        this.numberOfTricksPlayed.add(Integer.toString(number));
        this.trickNumber = number;

    }

    /**
    * Logs available cards based on the boolean array indicating their availability.
    * This method constructs a string representation of available cards and appends it to the list of available cards.
    * In "data" mode, missing cards are logged as -100 to maintain consistent data length.
    *
    * @param availableCards An array of boolean values indicating the availability of cards.
    */
    public void logAvailableCards(boolean[] availableCards) {
        StringBuilder cards = new StringBuilder();
        if (cards.length() == 0)
            cards.append("[");
        int numberOfCardsAvailable = 0;

        for (int i = 0; i < 52; i++) {

            if (availableCards[i]) {
                cards.append(i + 1);
                cards.append(".");
                numberOfCardsAvailable++;
            }
        }

        // in mode "data", missing cards are logged as -100
        if (mode.equals("data")) {
            for (int i = numberOfCardsAvailable; i < 39; i++) {
                cards.append("-100");
                cards.append(".");
            }
        }

        if (cards != null)
            cards.delete(cards.length() - 1, cards.length());
        cards.append("]");

        this.availableCards.add(cards.toString());
    }

    public void logCardsPlayedInThisRound(boolean[] cardsPlayed){
        this.cardsPlayedInThisRound.add(Arrays.copyOf(cardsPlayed, cardsPlayed.length));
    }

    /**
     * Logs the passed cards for each player in a round.
     *
     * @param passOfPlayer the cards passed by each player.
     */
    public void logCardsPassed(int[][] passOfPlayers) {

        StringBuilder newPassOfAllPlayers = new StringBuilder();

        if (viewPlayerNo != -1) {
            StringBuilder newPassOfOnePlayer = new StringBuilder();

            for (int id : passOfPlayers[viewPlayerNo]) {

                if (newPassOfOnePlayer.length() != 0)
                    newPassOfOnePlayer.append(",");
                newPassOfOnePlayer.append(id);
            }

            newPassOfAllPlayers.append(newPassOfOnePlayer);
        }

        else {
            for (int[] passOfPlayer : passOfPlayers) {

                if (newPassOfAllPlayers.length() != 0)
                    newPassOfAllPlayers.append(",");

                StringBuilder newPassOfOnePlayer = new StringBuilder();
                for (int id : passOfPlayer) {

                    if (newPassOfOnePlayer.length() != 0)
                        newPassOfOnePlayer.append(",");
                    newPassOfOnePlayer.append(id);
                }
                newPassOfAllPlayers.append(newPassOfOnePlayer);
            }
        }
        this.cardsPassed.add(newPassOfAllPlayers.toString());
    }

    /**
     * Logs the payoff value at the end of each trick.
     * 
     * @param players The players of the game, whos payoff values shall be logged
     */
    public void logPayoff(Player[] players) {

        if (trickNumber != 13)
            this.payoff.add("0.0.0.0");

        else {
            StringBuilder payoff = new StringBuilder();

            double[] allPlayersPayoff = new double[4];
            for (int i = 0; i < 4; i++) {
                double value = players[i].getPayoff();
                allPlayersPayoff[i] = value;
                payoff.append(value);
                payoff.append(".");
            }

            if (payoff != null)
                payoff.delete(payoff.length() - 1, payoff.length());
            this.payoff.add(payoff.toString());

            payoffAtTerminalState.add(allPlayersPayoff);
        }

    }

    /* Serializes the payoff values of each round in a separate file */
    private static void serializePayoff(ArrayList<double[]> payoff) {
        try {
            SerializationHelper.write("payoff.ser", payoff);

        } catch (Exception e) {
            System.out.println("Fehler beim Serialisieren der Payoff-Werte");
            e.printStackTrace();
        }

    }

    /**
     * Logs the direction to pass. 0 = no pass, 1 = left, 2 = opposite, 3 = right
     *
     * @param receiverNo the number of the player, that player number 0 will pass to
     */
    public void logPassReceiverNo(int receiverNo) {
        this.directionToPass.add(Integer.toString(receiverNo));
    }

    /**
     * Logs the hand cards for all player at the beginning of a trick (before
     * passing or playing a card) if viewPlayerNo = -1.
     * Logs the hand cards for one player if viewPlayerNo != -1.
     *
     * @param players the players in the game.
     */
    public void logHandCards(Player[] players) {

        for (Player player : players) {
            if (player.getPlayerNo() == viewPlayerNo || viewPlayerNo == -1) {
                logHandCardsOfOnePlayer(player);
            }
        }
    }

    /**
     * Logs the hand cards for one player.
     *
     */
    private void logHandCardsOfOnePlayer(Player player) {

        StringBuilder handCardsString = new StringBuilder();
        handCardsString.append("[");
        HandCards handcards = player.getHandCards();
        int numberOfHandCards = 0;
        for (int id = 1; id <= 52; id++) {

            if (handCardsString.length() == 0)
                handCardsString.append("[");

            if (handcards.containsCard(id)) {
                handCardsString.append(id);
                handCardsString.append(".");
                numberOfHandCards++;
            }
        }

        // in mode "data", missing handcards are logged as -100
        if (mode.equals("data")) {
            for (int i = numberOfHandCards; i < 13; i++) {
                handCardsString.append("-100");
                handCardsString.append(".");
            }
        }

        if (handCardsString != null)
            handCardsString.delete(handCardsString.length() - 1, handCardsString.length());
        handCardsString.append("]");

        if (player.getPlayerNo() == 0) {
            this.handCardsP0.add(handCardsString.toString());
        } else if (player.getPlayerNo() == 1) {
            this.handCardsP1.add(handCardsString.toString());
        } else if (player.getPlayerNo() == 2) {
            this.handCardsP2.add(handCardsString.toString());
        } else
            this.handCardsP3.add(handCardsString.toString());
    }

    /**
     * Logs the playable hand cards for one player.
     *
     * @param player the player whos playable handcards shall be logged
     */
    public void logHandCardsPlayable(Player player) {

        StringBuilder allHandCardsPlayable = new StringBuilder();

        if (allHandCardsPlayable.length() == 0)
            allHandCardsPlayable.append("[");

        HandCards handcards = player.getHandCardsPlayable();
        int numberOfHandCardsPlayable = 0;
        for (int id = 1; id <= 52; id++) {

            if (handcards.containsCard(id)) {
                allHandCardsPlayable.append(id);
                allHandCardsPlayable.append(".");
                numberOfHandCardsPlayable++;
            }
        }
        // in mode "data", missing handcards are logged as -100
        if (mode.equals("data")) {
            for (int i = numberOfHandCardsPlayable; i < 13; i++) {
                allHandCardsPlayable.append("-100");
                allHandCardsPlayable.append(".");
            }
        }

        if (allHandCardsPlayable.length() > 1)
            allHandCardsPlayable.delete(allHandCardsPlayable.length() - 1, allHandCardsPlayable.length());
        allHandCardsPlayable.append("]");

        if (player.getPlayerNo() == 0) {
            this.handCardsPlayableP0.add(allHandCardsPlayable.toString());
        } else if (player.getPlayerNo() == 1) {
            this.handCardsPlayableP1.add(allHandCardsPlayable.toString());
        } else if (player.getPlayerNo() == 2) {
            this.handCardsPlayableP2.add(allHandCardsPlayable.toString());
        } else
            this.handCardsPlayableP3.add(allHandCardsPlayable.toString());
    }

    /*
     * Updates the counters when logging a new game.
     */
    public void initNewGame() {
        roundNumber = 1;
        gameNumber++;
    }

    /**
     * Logs the played trick in a round and the player who started the trick.
     *
     * @param trick   the played trick.
     * @param starter the starting player.
     */
    public void logTrickPlayed(Trick trick, int starter) {

        int[] cardsPlayed = trick.getCardsPlayed();
        StringBuilder cardsOfTrick = new StringBuilder();
        int cardsPlayedInThisTrick;

        if (viewPlayerNo != -1) {
            cardsPlayedInThisTrick = (viewPlayerNo - starter + 4) % 4 + 1;
        } else
            cardsPlayedInThisTrick = 4;

        for (int i = 0; i < cardsPlayedInThisTrick; i++) {
            cardsOfTrick.append(cardsPlayed[(starter + i) % 4]);
            cardsOfTrick.append(".");
        }

        for (int i = cardsPlayedInThisTrick; i < 4; i++) {
            cardsOfTrick.append(-100);
            cardsOfTrick.append(".");

        }

        if (cardsOfTrick != null)
            cardsOfTrick.delete(cardsOfTrick.length() - 1, cardsOfTrick.length());
        this.tricksPlayed.add(cardsOfTrick.toString());
        this.numberOfStarter.add(Integer.toString(starter));
    }

    /**
     * Logs the current round points for all players in a round.
     *
     * @param players An array of Player objects representing the players in the
     *                game.
     */
    public void logRoundPoints(Player[] players) {

        StringBuilder currentRoundPoints = new StringBuilder();

        for (Player player : players) {
            int points = player.getRoundPts();
            currentRoundPoints.append(points);
            currentRoundPoints.append(".");
        }

        if (currentRoundPoints != null)
            currentRoundPoints.delete(currentRoundPoints.length() - 1, currentRoundPoints.length());
        this.roundPoints.add(currentRoundPoints.toString());
    }

    /**
     * Logs the current game points for all players after a round.
     *
     * @param players An array of Player objects representing the players in the
     *                game.
     */
    public void logGamePoints(Player[] players) {

        StringBuilder currentGamePoints = new StringBuilder();

        for (Player player : players) {
            int points = player.getGamePts();
            currentGamePoints.append(points);
            currentGamePoints.append(".");
        }

        if (currentGamePoints != null)
            currentGamePoints.delete(currentGamePoints.length() - 1, currentGamePoints.length());
        this.gamePoints.add(currentGamePoints.toString());

    }

    /*
     * Logs the current number of the game / round at the end of a trick (before
     * trick number gets incremented).
     */
    public void logRoundAndGameNumber() {
        int numberOfTricks = (deckSize == 52) ? 13 : 8;

        for (int i = 0; i < numberOfTricks; i++) {
            roundNumbers.add(Integer.toString(roundNumber));
            gameNumbers.add(Integer.toString(gameNumber));
        }
        if (trickNumber == numberOfTricks) {
            roundNumber++;
        }

    }

     /**
     * Adjust the number of entries in no pass mode.
     */
    public void handleNoPass() {

        if (viewPlayerNo == -1) cardsPassed.add("0,0,0,0,0,0,0,0,0,0,0,0");
        else cardsPassed.add("0,0,0");
        directionToPass.add("0");
    }

    /**
     * Logs game statistics, especially how often each player has secured each
     * position (1st, 2nd, 3rd, 4th) in the game.
     * This information is logged after completing a game.
     *
     * @param players The players of the game.
     * @throws IllegalArgumentException if the players array does not have exactly 4
     *                                  elements.
     */
    public void logGameStatistic(Player[] players) throws IllegalArgumentException {

        if (players.length != 4) {
            throw new IllegalArgumentException("The players array must contain 4 players.");
        }

        ArrayList<Player> playerRanking = new ArrayList<>();

        for (Player player : players) {

            playerRanking.add(player);
        }

        Collections.sort(playerRanking, (p1, p2) -> Integer.compare(p1.getGamePts(), p2.getGamePts()));

        int numberOfWinners = 1; 

        int currGamePtsPayoff = 0;

        Player first = playerRanking.get(0);
        numberOfWins[first.getPlayerNo()]++;
        //--
        totalGamePts[first.getPlayerNo()] = totalGamePts[first.getPlayerNo()] + first.getGamePts();
        avgGamePts[first.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePts[first.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) first.getGamePts());
        currGamePtsPayoff = playerRanking.get(1).getGamePts() - first.getGamePts()
            + playerRanking.get(2).getGamePts() - first.getGamePts()
            + playerRanking.get(3).getGamePts() - first.getGamePts();
        totalGamePtsPayoff[first.getPlayerNo()] = totalGamePtsPayoff[first.getPlayerNo()] + currGamePtsPayoff;
        avgGamePtsPayoff[first.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[first.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
        //--

        Player second = playerRanking.get(1);
        if (first.getGamePts() == second.getGamePts()){
            numberOfWins[second.getPlayerNo()]++; 
            numberOfWinners++;
            //--
            currGamePtsPayoff = playerRanking.get(0).getGamePts() - second.getGamePts()
                + playerRanking.get(2).getGamePts() - second.getGamePts()
                + playerRanking.get(3).getGamePts() - second.getGamePts();
            totalGamePtsPayoff[second.getPlayerNo()] = totalGamePtsPayoff[second.getPlayerNo()] + currGamePtsPayoff;
            avgGamePtsPayoff[second.getPlayerNo()] = 
                (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[second.getPlayerNo()]
                + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
            //--
        } 
        else {
            numberOf2ndPlaces[second.getPlayerNo()]++;
            //--
            currGamePtsPayoff = first.getGamePts() - second.getGamePts();
            totalGamePtsPayoff[second.getPlayerNo()] = totalGamePtsPayoff[second.getPlayerNo()] + currGamePtsPayoff;
            avgGamePtsPayoff[second.getPlayerNo()] = 
                (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[second.getPlayerNo()]
                + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
            //--
        }
        //--
        totalGamePts[second.getPlayerNo()] = totalGamePts[second.getPlayerNo()] + second.getGamePts();
        avgGamePts[second.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePts[second.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) second.getGamePts());
        //--

        Player third = playerRanking.get(2);
        if (second.getGamePts() == third.getGamePts()) {     
            if (numberOfWinners > 1){
                numberOfWins[second.getPlayerNo()]++;
                numberOfWinners++;   
                //--
                currGamePtsPayoff = playerRanking.get(0).getGamePts() - third.getGamePts()
                    + playerRanking.get(1).getGamePts() - third.getGamePts()
                    + playerRanking.get(3).getGamePts() - third.getGamePts();
                totalGamePtsPayoff[third.getPlayerNo()] = totalGamePtsPayoff[third.getPlayerNo()] + currGamePtsPayoff;
                avgGamePtsPayoff[third.getPlayerNo()] = 
                    (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[third.getPlayerNo()]
                    + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
                //--                
            }
            // case second and third player have equal points, but different from first place
            else {
                numberOf2ndPlaces[third.getPlayerNo()]++;    
                //--
                currGamePtsPayoff = first.getGamePts() - third.getGamePts();
                totalGamePtsPayoff[third.getPlayerNo()] = totalGamePtsPayoff[third.getPlayerNo()] + currGamePtsPayoff;
                avgGamePtsPayoff[third.getPlayerNo()] = 
                    (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[third.getPlayerNo()]
                    + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
                //--                
            }   
        } 
        else {
            numberOf3rdPlaces[third.getPlayerNo()]++;
            //--
            currGamePtsPayoff = first.getGamePts() - third.getGamePts();
            totalGamePtsPayoff[third.getPlayerNo()] = totalGamePtsPayoff[third.getPlayerNo()] + currGamePtsPayoff;
            avgGamePtsPayoff[third.getPlayerNo()] = 
                (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[third.getPlayerNo()]
                + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
            //--              
        }
        //--
        totalGamePts[third.getPlayerNo()] = totalGamePts[third.getPlayerNo()] + third.getGamePts();
        avgGamePts[third.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePts[third.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) third.getGamePts());
        //--

        Player fourth = playerRanking.get(3);
        if (third.getGamePts() == fourth.getGamePts()){
            if (second.getGamePts() == fourth.getGamePts()){
                numberOf2ndPlaces[fourth.getPlayerNo()]++;
            }
            else numberOf3rdPlaces[fourth.getPlayerNo()]++;
        }
        else numberOf4thPlaces[fourth.getPlayerNo()]++;        
        //--
        totalGamePts[fourth.getPlayerNo()] = totalGamePts[fourth.getPlayerNo()] + fourth.getGamePts();
        avgGamePts[fourth.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePts[fourth.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) fourth.getGamePts());
        //--
        currGamePtsPayoff = first.getGamePts() - fourth.getGamePts();
        totalGamePtsPayoff[fourth.getPlayerNo()] = totalGamePtsPayoff[fourth.getPlayerNo()] + currGamePtsPayoff;
        avgGamePtsPayoff[fourth.getPlayerNo()] = 
            (((double) gameNumber-1)/((double) gameNumber))*avgGamePtsPayoff[fourth.getPlayerNo()]
            + (((double) 1)/((double) gameNumber))* ((double) currGamePtsPayoff);
        //--  

        /* //TODO: Alternative zur fortlaufenden Durschnittsbildung. 
            //Vorteil: insgesamt schneller und kein Genauigkeitsverlust. Nachteil: Überlaufgefahr des Wertebereichs bei zu vielen Spielen bzw. Punkten.
            //TODO: Entscheidung noch ausstehend.
        if (gameNumber == numberOfGames) {
            for (int i = 0; i<avgGamePts.length; i++){
                avgGamePts[i] = ((double) totalGamePts[i]) / ((double) numberOfGames);
                //...avgGamePtsPayoff[i] = ...
            }
        }
        */

    }

    /**
    * Logs the hand cards of each player before passing.
    * This method constructs a string representation of hand cards for each player and adds it to the list of hand cards before passing.
    * In "data" mode, missing hand cards are logged as -100 to maintain a consistent data length.
     *
    * @param players The players whose hand cards are to be logged.
    */
    public void logHandCardsBeforePassing(Player[] players){
        for (Player player : players) {
            
                StringBuilder handCardsString = new StringBuilder();
                handCardsString.append("[");
                HandCards handcards = player.getHandCards();
                int numberOfHandCards = 0;
                for (int id = 1; id <= 52; id++) {

                    if (handCardsString.length() == 0)
                        handCardsString.append("[");

                    if (handcards.containsCard(id)) {
                        handCardsString.append(id);
                        handCardsString.append(".");
                        numberOfHandCards++;
                    }
                }

                // in mode "data", missing handcards are logged as -100
                if (mode.equals("data")) {
                    for (int i = numberOfHandCards; i < 13; i++) {
                        handCardsString.append("-100");
                        handCardsString.append(".");
                    }
                }

                if (handCardsString != null) handCardsString.delete(handCardsString.length() - 1, handCardsString.length());
                handCardsString.append("]");
                this.handCardsBeforePassing.add(handCardsString.toString());
            
        }
    }

    /**
    * This method increments the count of shooting the moon occurrences for the specified player.
    *
    * @param playerNo The number of the player who shot the moon.
    */
    public void logShotTheMoon(int playerNo) {
        numberOfShootTheMoons[playerNo]++;
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public int[] getNumberOfWins() {
        return numberOfWins;
    }

    public int[] getNumber2ndPlaces() {
        return numberOf2ndPlaces;
    }

    public int[] getNumberOf3rdPlaces() {
        return numberOf3rdPlaces;
    }

    public int[] getNumberOf4thPlaces() {
        return numberOf4thPlaces;
    }

    public int[] getNumberOfShootTheMoons() {
        return numberOfShootTheMoons;
    }

    public ArrayList<String> getCardsPassed() {
        return cardsPassed;
    }

    public ArrayList<String> getHandCardsP0AfterPassing() {
        return handCardsP0;
    }

    public ArrayList<String> getStartingPlayer() {
        return numberOfStarter;
    }

    public ArrayList<String> getTricksPlayed() {
        return tricksPlayed;
    }

    public ArrayList<String> getRoundPoints() {
        return roundPoints;
    }

    public ArrayList<String> getGamePoints() {
        return gamePoints;
    }

    public int getViewPlayerNo(){
        return viewPlayerNo;
    }

    public ArrayList<String> getHandCardsP0(){
        return handCardsP0;
    }

    public ArrayList<String> getHandCardsP1(){
        return handCardsP1;
    }

    public ArrayList<String> getHandCardsP2(){
        return handCardsP2;
    }

    public ArrayList<String> getHandCardsP3(){
        return handCardsP3;
    }

    public ArrayList<String> getHandCardsPlayableP0(){
        return handCardsPlayableP0;
    }

    public ArrayList<String> getHandCardsPlayableP1(){
        return handCardsPlayableP1;
    }

    public ArrayList<String> getHandCardsPlayableP2(){
        return handCardsPlayableP2;
    }

    public ArrayList<String> getHandCardsPlayableP3(){
        return handCardsPlayableP3;
    }

    public ArrayList<String> getNumberOfStarter(){
        return numberOfStarter;
    }

    public ArrayList<String> getDirectionToPass() {
        return directionToPass;
    }

    public int getDeckSize() {
        return deckSize;
    }

    public ArrayList<String> getHandCardsBeforePassing(){
        return handCardsBeforePassing;
    }

    public ArrayList<String> getNumberOfTricksPlayed(){
        return numberOfTricksPlayed;
    }

    public ArrayList<double[]> getPayoffAtTerminalState(){
        return payoffAtTerminalState;
    }

    public long[] getTotalGamePts() {
        return totalGamePts;
    }

    public double[] getAvgGamePts() {
        return avgGamePts;
    }

    public long[] getTotalGamePtsPayoff() {
        return totalGamePtsPayoff;
    }

    public double[] getAvgGamePtsPayoff() {
        return avgGamePtsPayoff;
    }

    public ArrayList<String> getAvailableCards(){
        return availableCards;
    }

    public ArrayList<boolean[]> getCardsPlayedInThisRound(){
        return cardsPlayedInThisRound;
    }


}
