package game;

/**
 * The Suits enum represents the four standard suits in a deck of cards: Diamonds, Hearts, Spades, and Clubs.
 * Each suit has an associated index ranging from 0 to 3.
 */
public enum Suits {
    DIAMONDS(0), 
    HEARTS(1), 
    SPADES(2), 
    CLUBS(3);

    private final int index;


    /**
     * Constructs a Suits enum constant with a specified suit index.
     *
     * @param index The index of the suit. Value Range [0, 3].
     */
    private Suits(int index){
        this.index = index;
    }

    /**
    * Gets the index of the suit. Value Range [0, 3]
    *
    * @return The index of the suit.
    */
    public int getIndex(){
        return index;
    }
}
