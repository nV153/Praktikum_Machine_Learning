package game;

/**
 * Enum representing the coding of playing cards.
 * Each enum constant corresponds to a specific card, with a unique identifier.
 */

public enum CardCoding {
    
    DIAMONDS_2(1),
    DIAMONDS_3(2),
    DIAMONDS_4(3),
    DIAMONDS_5(4),
    DIAMONDS_6(5),
    DIAMONDS_7(6),
    DIAMONDS_8(7),
    DIAMONDS_9(8),
    DIAMONDS_10(9),
    DIAMONDS_JACK(10),
    DIAMONDS_QUEEN(11),
    DIAMONDS_KING(12),
    DIAMONDS_ACE(13),
    
    HEARTS_2(14),
    HEARTS_3(15),
    HEARTS_4(16),
    HEARTS_5(17),
    HEARTS_6(18),
    HEARTS_7(19),
    HEARTS_8(20),
    HEARTS_9(21),
    HEARTS_10(22),
    HEARTS_JACK(23),
    HEARTS_QUEEN(24),
    HEARTS_KING(25),
    HEARTS_ACE(26),
    
    SPADES_2(27),
    SPADES_3(28),
    SPADES_4(29),
    SPADES_5(30),
    SPADES_6(31),
    SPADES_7(32),
    SPADES_8(33),
    SPADES_9(34),
    SPADES_10(35),
    SPADES_JACK(36),
    SPADES_QUEEN(37),
    SPADES_KING(38),
    SPADES_ACE(39),
    
    CLUBS_2(40),
    CLUBS_3(41),
    CLUBS_4(42),
    CLUBS_5(43),
    CLUBS_6(44),
    CLUBS_7(45),
    CLUBS_8(46),
    CLUBS_9(47),
    CLUBS_10(48),
    CLUBS_JACK(49),
    CLUBS_QUEEN(50),
    CLUBS_KING(51),
    CLUBS_ACE(52);

    private final int id;

    /**
     * Constructs a CardCoding enum constant with a specified identifier.
     *
     * @param id The identifier of the card. Value Range [1, 52].
     */
    private CardCoding(int id){
        this.id = id;
    }

    /**
    * Gets the id of the card. Value Range [1, 52]
    *
    * @return The identifier of the card.
    */
    public int getId(){
        return id;
    }

    /**
     * Gets the suit of the card based on its ID.
     *
     * @param id The Id of the card.
     * @return The suit of the card as enum.
     */
    public static Suits getSuitById(int id) {
        if (id >= 1 && id <= 13) {
            return Suits.DIAMONDS;
        } else if (id >= 14 && id <= 26) {
            return Suits.HEARTS;
        } else if (id >= 27 && id <= 39) {
            return Suits.SPADES;
        } else if (id >= 40 && id <= 52) {
            return Suits.CLUBS;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
