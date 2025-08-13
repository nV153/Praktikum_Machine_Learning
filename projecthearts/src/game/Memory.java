 package game;

/**
 * The AI's memory capacity for tracking tricks in the game.
 *
 * This enum defines various memory capacities that the AI can have for remembering tricks during the game.
 * Each memory capacity corresponds to one of the four defined difficulty levels.
 *
 * - {@link Memory#LOW} corresponds to a low memory capacity, where only the last trick is remembered (Level 0).
 * - {@link Memory#NORMAL} corresponds to a medium memory capacity - (Level 1).
 * - {@link Memory#HIGH} corresponds to a high memory capacity - (Level 2).
 * - {@link Memory#SUPER} corresponds to a maximum memory capacity, where all tricks in the game are remembered (Level 3).
 *
 * The exact number of tricks remembered depends on both the chosen memory capacity and the deck size.
 */
public enum Memory {
    LOW {
        @Override
        public boolean updateHeartBrokenStatus(boolean heartBroken, int trickWhenHeartBroken, int tricksToConsider) {
            return heartBroken && trickWhenHeartBroken >= tricksToConsider;
        }
        @Override
        public int getTricksToConsider(int tricksPlayed, int deckSize) {
            return calculateTricksToConsider(tricksPlayed, deckSize == FULL_DECK_SIZE ? 1 : 1);
        }
    },
    NORMAL {
        @Override
        public boolean updateHeartBrokenStatus(boolean heartBroken, int trickWhenHeartBroken, int tricksToConsider) {
            return heartBroken && trickWhenHeartBroken >= tricksToConsider;
        }
        @Override
        public int getTricksToConsider(int tricksPlayed, int deckSize) {
            return calculateTricksToConsider(tricksPlayed, deckSize == FULL_DECK_SIZE ? 3 : 2);
        }
    },
    HIGH {
        @Override
        public boolean updateHeartBrokenStatus(boolean heartBroken, int trickWhenHeartBroken, int tricksToConsider) {
            return heartBroken && trickWhenHeartBroken >= tricksToConsider;
        }
        @Override
        public int getTricksToConsider(int tricksPlayed, int deckSize) {
            return calculateTricksToConsider(tricksPlayed, deckSize == FULL_DECK_SIZE ? 7: 4);
        }
    },
    SUPER {
        @Override
        public boolean updateHeartBrokenStatus(boolean heartBroken, int trickWhenHeartBroken, int tricksToConsider) {
            return heartBroken; // SUPER always remembers
        }
        @Override
        public int getTricksToConsider(int tricksPlayed, int deckSize) {
            return calculateTricksToConsider(tricksPlayed, deckSize == FULL_DECK_SIZE ? 14:10);
        }
    };

    /**
     * Gibt die Anzahl der Stiche zurück, die die KI auf Basis der gewählten Speicherkapazität 
     * und der Deck-Größe behalten sollte.
     *
     * @param tricksPlayed Die Gesamtzahl der bisher gespielten Stiche.
     * @param deckSize Die Größe des verwendeten Decks, entweder {@link #FULL_DECK_SIZE} oder eine 32.
     * @return Die Anzahl der Stiche, die die KI basierend auf der Speicherkapazität und der Deck-Größe behalten sollte.
     */
    public abstract int getTricksToConsider(int tricksPlayed, int deckSize);

    protected int calculateTricksToConsider(int tricksPlayed, int tricksToRemember) {
        int startTrickIndex = tricksPlayed - tricksToRemember;

        return Math.max(startTrickIndex, 1);
    }

    public abstract boolean updateHeartBrokenStatus(boolean heartBroken, int trickWhenHeartBroken, int tricksToConsider);

    private static final int FULL_DECK_SIZE = 52;
    
    /**
     * Method that determines the MCTS-AI's expansion depth depending on the memory.
     *
     * @param g The game state that contains information about user's selected memory capacity for an AI.
     * @param playerNo The number of the player of interest for whom the expansion depth will be determined.
     * @return int The expansion depth.
     */
    public static int determineExpansionDepthVariant1(GameState g, int playerNo){
        Memory memory = g.getMemoryOfPlayer(playerNo);
        int expansionDepth = 3;
        if (memory == Memory.LOW){
            expansionDepth = 3;
        } else if (memory == Memory.NORMAL){
            expansionDepth = 6;
        } else if (memory == Memory.HIGH){
            expansionDepth = 10;
        } else { //SUPER
            expansionDepth = 13;
        }           
        return expansionDepth;
    }

    /**
     * Method that determines the MCTS-AI's maximum number of possible passes depending on the memory.
     *
     * @param g The game state that contains information about user's selected memory capacity for an AI.
     * @param playerNo The number of the player of interest for whom the maximum number of possible passes will be determined.
     * @return int The maximum number of possible passes.
     */
    public static int determineMaxNumberOfPossiblePassesVariant1(GameState g, int playerNo){
        Memory memory = g.getMemoryOfPlayer(playerNo);
        int maxNumberOfPossiblePasses = 1;
        if (g.getDeckSize()==52){ 
            //Maximum for the binomial coefficient (13,3), 13 over 3 = 286. Select a corresponding gradation below.
            int binomialCoefficient = 286;
            if (memory == Memory.LOW){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.25);
            } else if (memory == Memory.NORMAL){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.5);
            } else if (memory == Memory.HIGH){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.75);
            } else { //SUPER
                maxNumberOfPossiblePasses = binomialCoefficient;
            }           
        } else{ //Decksize 32
            //Maximum for the binomial coefficient (8.3), 8 over 3 = 56. Select a corresponding gradation below.
            int binomialCoefficient = 56;
            if (memory == Memory.LOW){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.25);
            } else if (memory == Memory.NORMAL){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.5);
            } else if (memory == Memory.HIGH){
                maxNumberOfPossiblePasses = (int) (binomialCoefficient * 0.75);
            } else { //SUPER
                maxNumberOfPossiblePasses = binomialCoefficient;
            }           
        }        
        return maxNumberOfPossiblePasses;
    }    

}