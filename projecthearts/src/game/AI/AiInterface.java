package game.AI;

import game.GameState;

/**
 * Interface defining the behavior and strategies of an AI player.
 */
public interface AiInterface {

    /**
     * Determines the "best" cards that the AI should pass to another player.
     *
     * This method analyzes the current state of the game, the AI (playerNo) wishing to pass the cards,
     * and the player (receiverNo) who is to receive the cards. It calculates the "best" cards based on a rule-based approach
     * and returns them as an array of 3 integers.
     *
     * The returned array contains the cards that the player should pass to the receiver.
     * Priority order - (According to 'The Complete Win At Hearts' and other sources)
     *
     * @param gameState The current state of the Hearts game.
     * @param playerNo The player number of the AI wishing to pass the cards.
     * @param receiverNo The player number of the player who is to receive the cards.
     * @return An array of 3 integers containing the "best" cards (1-52) for the exchange.
     */
    int[] bestPass(GameState gameState, int playerNo, int receiverNo);

    /**
     * Determines the best move for the AI based on the current game state, difficulty level, and memory.
     *
     * This method takes into account the difficulty level and how many tricks the AI is allowed to remember (Memory),
     * to determine the best move in the current situation of the Hearts game.
     *
     * At higher difficulty levels and with more remembered tricks, the AI is expected to make more sophisticated and
     * strategic moves.
     * Priority order - (According to 'The Complete Win At Hearts' and other sources)
     *
     * @param g The current state of the Hearts game.
     * @param playerNo The player number of the AI.
     * @return The card number (1-52) of the best card to be played.
     */
    int bestMove(GameState g, int playerNo);
}
