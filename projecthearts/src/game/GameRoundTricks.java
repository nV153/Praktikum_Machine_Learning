package game;

import java.util.ArrayList;

/**
 * Represents the tricks played in a single round of a card game.
 * Keeps track of a list of tricks played during the round.
 */

public class GameRoundTricks {
    private ArrayList<Trick[]> listOfRoundTricks;

    /**
     * Constructs a new GameRoundTricks instance with an empty list of tricks.
     */
    public GameRoundTricks(){
        listOfRoundTricks = new ArrayList<Trick[]>();
    }

    /**
     * Adds a set of tricks to the list of tricks played in the round.
     *
     * @param roundTricks The array of tricks to be added to the round.
     */
    public void addRoundTricksToGameRoundTricks(Trick[] roundTricks){
        this.listOfRoundTricks.add(roundTricks);
    }

    /**
     * Gets the list of tricks played in the round.
     *
     * @return The list of tricks played in the round.
     */
    public ArrayList<Trick[]> getListOfRoundTricks() {
        return listOfRoundTricks;
    }

    /**
     * Gets the trick at the specified position in the list.
     *
     * @param index The position of the trick in the list.
     * @return The array of tricks at the specified position.
     */
    public Trick[] getTrickAtIndex(int index) {
        return listOfRoundTricks.get(index);
    }
    
}

