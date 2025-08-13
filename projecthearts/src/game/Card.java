package game;

/**
 * Represents a playing card.
 * Each card has a identifier, suit, and image path.
 */

public class Card implements Cloneable{
    
    private int id; // Card id, 0 < x < 53 
    private String imagePath;
    private Suits suit; 

    private String[] suits = { "diamonds", "hearts", "spades", "clubs" };
    private String[] ranks = { "02", "03", "04", "05", "06", "07", "08", "09", "10", "jack", "queen", "king", "ace" };

    /**
    * Constructs a new Card instance with the specified identifier.
    *
    * @param id The identifier of the card.
    * @throws IllegalArgumentException If the provided identifier is outside the valid range [1, 52].
    */
    public Card (int id) throws IllegalArgumentException{
        
        setId(id);
        imagePath = calculateImagePath();
        suit = calculateSuit();
    }

    /* Sets the id of the card if the input parameter is within the valid value range. */
    private void setId(int id) throws IllegalArgumentException{
        if (id > 0 && id < 53) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("Invalid Card Id:" + id);
        } 
    }

    
    /* Calculates the color of a card based on its id. */
    private Suits calculateSuit() throws IllegalArgumentException{

        int suitIndex = (id - 1) / 13;
        
        switch (suitIndex){

            case 0: 
            return Suits.DIAMONDS;
            case 1: 
            return Suits.HEARTS;
            case 2: 
            return Suits.SPADES;
            case 3: 
            return Suits.CLUBS;
            default: 
            throw new IllegalArgumentException("Invalid Card Id:" + this.id);
        }    
    }

    /* Calculates the path of the corresponding image file. */
    private String calculateImagePath(){
        
        int suitIndex = (id - 1) / 13;
        int rankIndex = ((id - 1) % 13) + 1;

        return "../GUI/assets/" + suits[suitIndex] + String.format("%02d", rankIndex) + ".png";  
    }

    /**
    * Checks if the card belongs to the Hearts suit.
    * A card is considered a Hearts suit card if its id is within the range [14, 26].
    *
    * @return {@code true} if the card is of Hearts suit, {@code false} otherwise.
    */
    public boolean isHeart(){
        return id >= 14 && id <= 26;
    }

    /**
    * Checks if the card is the Queen of Spades.
    * A card is considered the Queen of Spades if its id is 37.
    *
    * @return {@code true} if the card is the Queen of Spades, {@code false} otherwise.
    */
    public boolean isQueenOfSpades(){
        return id == 37;
    }

    /**
    * Checks if the current card has the same suit as another card.
    *
    * @param otherCard The other card to compare the suit against.
    * @return {@code true} if both cards have the same suit, {@code false} otherwise.
    */
    public boolean sameSuit(Card otherCard){
        return (this.suit == otherCard.getSuit());
    }

    
    /**
    * Checks whether the current card beats the other card in a trick.
    * A card beats another if it is either the first card played or a higher card of the same suit.
    *
    * @param firstCard The other card to compare against.
    * @return {@code true} if the current card beats the other card, {@code false} otherwise.
    */
    public boolean isHigherAs(Card firstCard){
        
        if(sameSuit(firstCard)){
            if(this.id > firstCard.getId()) return true;
        }
        return false;
    }

    /**
    * Returns the higher of two cards, considering the suit.
    *
    * @param firstCard The card that determines the winning suit.
    * @return The higher card between the current card and the specified card.
    */
    public Card getHigherCard(Card firstCard){
        if (isHigherAs(firstCard)) return this;
        return firstCard;
    }

    /**
    * Returns the name of the card based on its encoding.
    * Useful, for example, for testing whether a deck has been successfully created.
    *
    * @return The name of the card, including its suit and rank.
    */
    public String getCardName() {

        int suitIndex = (id - 1) / 13;
        int rankIndex = (id - 1) % 13;

        return suits[suitIndex] + " " + ranks[rankIndex];
    }

    /**
    * Gets the file path to the image associated with the card.
    *
    * @return The file path to the image of the card.
    */
    public String getImagePath(){
        return imagePath;
    }

    /**
    * Gets the suit of the card.
    *
    * @return The suit of the card.
    */
    public Suits getSuit(){
        return suit;
    }

    /**
    * Gets the id of the card. Value Range [1, 52]
    *
    * @return The identifier of the card, which is representing a combination of the card's suit and rank.
    *
    */
    public int getId(){
        return id;
    }

    /**
     Gets the file path to the image of the backside of a card.
    *
    * @return The file path to the image of the backside of a card.
    */
    public static String getImagePathOfBacksideCard(){
        return "../GUI/assets/backside.png";  
    }

    /**
    * Gets the index of the card's suit.
    *
    * @return The index of the card's suit (0 for Diamonds, 1 for Hearts, 2 for Spades, 3 for Clubs).
    * @throws IllegalArgumentException If the card has an unknown suit.
    */
    public int getSuitIndex() throws IllegalArgumentException{
        switch(suit) {
            case DIAMONDS: return 0;
            case HEARTS:   return 1;
            case SPADES:   return 2;
            case CLUBS:    return 3;
            default:       throw new IllegalArgumentException("Unbekannte Farbe: " + suit);
        }
    }

    @Override
    public Object clone() {
        Card cardCopy = null;
        try {
            cardCopy = (Card) super.clone();
            // Für alle Attribute von Card gilt: Sie können nicht geändert werden (sind also immutable, keine (public) setter verfügbar), d. h., es reichen shallow copys aus.
        } catch (CloneNotSupportedException e) {
            //return new Address(this.street, this.getCity(), this.getCountry()); //Aus Beispiel //TODO: Am Ende entfernen
            System.out.println("clone not supported for class: " + this.getClass().getName());
            cardCopy = new Card(this.id); //TODO: hier evtl. noch einen Fehler werfen oder so. catch sollte aber nie pasieren, da Card Cloneable ist/implementiert.
        }
        return cardCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return id == card.id;
    }
}
