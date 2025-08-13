package game;

/**
 * Difficulty levels for the game.
 *
 * This enum defines various difficulty levels that can be used in the game.
 * Each level is associated with an integer value from 0 to 3.
 *
 * - {@link Difficulty#EASY} corresponds to difficulty level 0.
 * - {@link Difficulty#MEDIUM} corresponds to difficulty level 1.
 * - {@link Difficulty#HARD} corresponds to difficulty level 2.
 * - {@link Difficulty#EXPERT} corresponds to difficulty level 3.
 */
public enum Difficulty {
    EASY, MEDIUM, HARD, EXPERT;

    /**
     * A method that determines the MCTS-AI's number of iterations depending on the difficulty level.
     * Best suited for bestMove.
     *
     * @param g The game state that contains information about the user's selected difficulty level for an AI.
     * @param playerNo The number of the player of interest for whom the number of iterations will be determined.
     * @return int The number of iterations.
     */
    public static int determineNumberOfIterationsVariant1(GameState g, int playerNo){
        Difficulty difficulty = g.getDifficultyOfPlayer(playerNo);
        int minNumberOfIterations = 30;
        if (g.getDeckSize()==52){ 
            minNumberOfIterations = 30;
            if (difficulty == EASY){
                return minNumberOfIterations; 
            } else if (difficulty == MEDIUM){
                return 20*minNumberOfIterations; 
            } else if (difficulty == HARD){
                return 40*minNumberOfIterations; 
            } else{ //Difficulty.Expert
                return 60*minNumberOfIterations; 
            }
        } else { //Decksize 32
            minNumberOfIterations = 30;
            if (difficulty == EASY){
                return minNumberOfIterations; 
            } else if (difficulty == MEDIUM){
                return 20*minNumberOfIterations; 
            } else if (difficulty == HARD){
                return 40*minNumberOfIterations; 
            } else{ //Difficulty.Expert
                return 60*minNumberOfIterations; 
            }
        }
    }

    /**
     * A method that determines the MCTS-AI's number of iterations depending on the difficulty level.
     * Best suited for bestPass.
     *
     * @param g The game state that contains information about the user's selected difficulty level for an AI.
     * @param playerNo The number of the player of interest for whom the number of iterations will be determined.
     * @return int The number of iterations.
     */
    public static int determineNumberOfIterationsVariant2(GameState g, int playerNo){
        Difficulty difficulty = g.getDifficultyOfPlayer(playerNo);
        int numberOfIterations = 56;
        if (g.getDeckSize()==52){ 
            //Maximum for the binomial coefficient (13,3), 13 over 3 = 286. Select a corresponding gradation below.
            int binomialCoefficient = 286;
            if (difficulty == EASY){
                numberOfIterations = binomialCoefficient; 
            } else if (difficulty == MEDIUM){
                numberOfIterations = 20*binomialCoefficient; 
            } else if (difficulty == HARD){
                numberOfIterations = 40*binomialCoefficient; 
            } else{ //Difficulty.Expert
                numberOfIterations = 60*binomialCoefficient; 
            }            
        } else{ //Decksize 32
            //Maximum for the binomial coefficient (8.3), 8 over 3 = 56. Select a corresponding gradation below.
            int binomialCoefficient = 56;
            if (difficulty == EASY){
                numberOfIterations = binomialCoefficient; 
            } else if (difficulty == MEDIUM){
                numberOfIterations = 20*binomialCoefficient; 
            } else if (difficulty == HARD){
                numberOfIterations = 40*binomialCoefficient; 
            } else{ //Difficulty.Expert
                numberOfIterations = 60*binomialCoefficient; 
            }                   
        }                
        return numberOfIterations;
    }
}
