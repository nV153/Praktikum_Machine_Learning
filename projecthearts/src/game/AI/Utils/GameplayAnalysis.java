package game.AI.Utils;

import java.util.ArrayList;
import java.util.List;

import game.Card;
import game.CardCoding;
import game.Difficulty;
import game.GameState;
import game.Player;
import game.Trick;

/**
 * The GameplayAnalysis class is responsible for analyzing gameplay behavior.
 * It uses the AI Player and the AI strategy to make decisions
 * and anticipate possible behavior of opponents.
 */
public class GameplayAnalysis {
    private final GameState gameState;
    private final int playerNo;


    /**
     * Constructor for the GameplayAnalysis class.
     * @param memory
     * @param difficulty
     * @param passedForStM
     * 
     * @param player The player whose gameplay behavior is to be optimized. Typically,
     *               this is the artificial intelligence itself, making game decisions.
     */
    public GameplayAnalysis(GameState gameState, int playerNo) {
        this.gameState = gameState;
        this.playerNo = playerNo;
    }

    /**
     * Determines if a player is playing "Shoot the Moon" and returns that player.
     * 
     * @param gameState     The current state of the game, containing all relevant information.
     * @param trickwinnable Indicates if the current trick is winnable.
     * @return              Returns the player who might be playing "Shoot the Moon", otherwise null.
     */
    public Player findStMPlayer(Difficulty difficulty) {
        if (difficulty != Difficulty.EXPERT) {
            return null;
        }

        Player[] allPlayers = gameState.getPlayers();
        List<Player> playersWithPoints = new ArrayList<>();

        for (Player player : allPlayers) {
            if (player.getRoundPts() > 0) {
                playersWithPoints.add(player);
            }
        }

        if (playersWithPoints.size() >= 2) {
            return null; // If two players have points, "Shoot the Moon" is no longer possible.
        }

        // If only one player has points, check if they are playing for "Shoot the Moon".
        if (playersWithPoints.size() == 1) {
            Player playerWithPoints = playersWithPoints.get(0);
            if (playerWithPoints.getPlayerNo() != playerNo && isPlayerGoingForStM(playerWithPoints)) {
                return playerWithPoints; 
            }
        }

        return null;
    }

    // Checks if a player is likely attempting to "Shoot the Moon".
    private boolean isPlayerGoingForStM(Player playerWithPoints) {
        final int thresholdWithoutSpadesQueen = 7;
        final int thresholdWithSpadesQueen = 20;

        // If the player has passed us cards and they are already suspicious, it's likely they are playing for StM
        if (playerWithPoints.getReceiverNo() == playerNo && CardEvaluation.checkPassedCardsForStM(playerWithPoints.getCardsToPass())){
            return true;
        }
        // Also suspect if the player has accumulated more than a certain number of points,     
        if (playerWithPoints.getRoundPts() > thresholdWithoutSpadesQueen || (playedSpadedOfQueen(playerWithPoints) && playerWithPoints.getRoundPts() > thresholdWithSpadesQueen)) {
            return true; 
        }
    
        return false;
    }

    // Determines if the Queen of Spades has been played by a specific player in the current round.
    private boolean playedSpadedOfQueen(Player playerWithPoints) {
        ArrayList<Integer> cardsPlayed = gameState.getCardsPlayedByPlayerInThisRound(playerWithPoints.getPlayerNo());
        
        for (int cardId : cardsPlayed) {
            if (cardId == CardCoding.SPADES_QUEEN.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the AI should pursue the "Shoot the Moon" strategy based on the current game state and other relevant gameplay conditions.
     *
     * @param gameState     The current state of the game, containing all relevant information.
     * @param playerNo      The player ID of the AI making this decision.
     * @param trickwinnable Indicates if the current trick is winnable.
     * @return              Returns whether the AI should play "Shoot the Moon".
     */
    public boolean shouldPlayForStM(Difficulty difficulty, boolean isPassedForStM) {
        Player[] allPlayers = gameState.getPlayers();
        int roundNo = gameState.getRoundNo();
        roundNo  = roundNo % 4;
        boolean isNoPassRound = (roundNo == 0) ? true : false;
        boolean playforStM = false;
        
        // Check if it's a no Pass Round (every 4th round) or if the game setting is on noPass
        if (!gameState.getIsGameWithPassing() || isNoPassRound) {
            return playforStM = false;
        }
        // Check the difficulty, or if a pass was made for StM
        if (difficulty == Difficulty.EXPERT && isPassedForStM) {
            playforStM = true;
        }
        // Check if any player (other than AI itself) already has points
        for (Player player : allPlayers) {
            if (player.getRoundPts() > 0 && player.getPlayerNo() != playerNo) {
                playforStM = false;
                break;
            }
        }
        return playforStM;
    }

    /**
     * Checks which players may have no more cards in certain suits, based on the tricks played so far.
     *
     * @param allTricks    An array of all tricks played so far.
     * @param tricksPlayed The number of tricks played so far.
     * @param memory       The memory setting to determine how many past tricks to consider.
     * @return A two-dimensional boolean array indicating for each player and each suit whether a void (true) is suspected or not (false).
     */
    public boolean[][] checkForPlayerVoids(Trick[] allTricks, int tricksPlayed, int tricksToConsider) {
        // Initializing a two-dimensional array to store information on missing suits for players.
        boolean[][] suitVoidsByPlayer = new boolean[4][4]; // 4 players x 4 suits   

        for (int i = tricksToConsider; i < tricksPlayed - 1; i++) {
            Trick trick = allTricks[i];
            int[] cardsPlayed = trick.getCardsPlayed();
            int startingSuitIndex = CardCoding.getSuitById(cardsPlayed[0]).getIndex();

            for(int playerIndex = 0; playerIndex < cardsPlayed.length; playerIndex++) {
                // Check if the suit of the current card differs from the starting suit.
                if(CardCoding.getSuitById(cardsPlayed[playerIndex]).getIndex() != startingSuitIndex) {
                    // If yes, mark this player as void in the starting suit.
                    suitVoidsByPlayer[playerIndex][startingSuitIndex] = true;
                }
            }
        }
        // Return the two-dimensional array showing which players are missing which suits.
        return suitVoidsByPlayer;
    }

}

