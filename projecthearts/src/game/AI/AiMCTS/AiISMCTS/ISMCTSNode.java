package game.AI.AiMCTS.AiISMCTS;

import java.util.ArrayList;
import java.util.List;
import game.Card;
import game.GameState;

/**
 * Represents a node in the ISMCTS (Information Set Monte Carlo Tree Search) tree.
 * Each node has the Information, like: the player number, the card played to reach this state, the parent node, 
 * the child nodes, the win score, the number of visits, the availability, and the passed cards.
 */
public class ISMCTSNode {
    
    private int playerNr;
    private Card playedCard;
    private ISMCTSNode parent;
    private List<ISMCTSNode> children;
    private double winScore;
    private int visits;
    private int availability;
    private int[] passedCards;
    
    // Getters
    public int[] getPassedCards() {return passedCards;}
    public int getAvailability() {return availability;}
    public int getPlayerNr() {return playerNr;}
    public ISMCTSNode getParent() {return parent;}
    public List<ISMCTSNode> getChildren() {return children;}
    public double getWinScore() {return winScore;}
    public int getVisits() {return visits;}
    public Card getPlayedCard() {return playedCard;}
    
    // Setters
    public void setPassedCards(int[] passedCards) {this.passedCards = passedCards;}
    public void setAvailability(int availability) {this.availability = availability;}
    public void setPlayedCard(Card playedCard) {this.playedCard = playedCard;}
    public void setWinScore(int winScore) {this.winScore = winScore;}
    public void setVisits(int visits) {this.visits = visits;}
    public void setParent(ISMCTSNode parent) {this.parent = parent;}
    public void setChildren(List<ISMCTSNode> children) {this.children = children;}
    
    /**
     * Constructs a new ISMCTSNode object.
     * 
     * @param playerNr The player number.
     * @param playedCard The card played.
     * @param parent The parent node.
     */
    public ISMCTSNode(int playerNr, Card playedCard, ISMCTSNode parent) {
        this.playerNr = playerNr;
        this.playedCard = playedCard;
        this.parent = parent;
        this.winScore = 0.0;
        this.visits = 0;
        this.availability = 1;
        this.children = new ArrayList<>();
    }
    
    /**
     * Adds a child node to this node with the specified played card and player number.
     * 
     * @param playedCard The card that was played.
     * @param playerNr The player number to be associated with the child node.
     * @return The newly added child node.
     */
    public ISMCTSNode addChild(Card playedCard, int playerNr) {
        ISMCTSNode child = new ISMCTSNode(playerNr, (Card) playedCard.clone(), this);
        children.add(child);
        return child;
    }

    /**
     * Adds a child node to this node for a pass with the specified passed cards and player number.
     * 
     * @param passedCards The cards that were passed.
     * @param playerNr The player number to be associated with the child node.
     * @return The newly added child node.
     */
    public ISMCTSNode addChildForPass(int[] passedCards, int playerNr) {
        ISMCTSNode child = new ISMCTSNode(playerNr, null, this);
        child.setPassedCards(passedCards);
        children.add(child);
        return child;
    }
    
    /**
     * Updates this node based on the specified game state.
     * 
     * @param state The game state to update this node with.
     */
    public void update(GameState state) {
        incrementVisitCount();
        addWinScore(state);
    }

    public void incrementAvailability() {availability++;}
    private void incrementVisitCount() {visits++;}
    
    /**
     * Updates the siblings of this node by incrementing their availability.
     */
    public void updateSiblingsAvailability() {
        if (parent != null) {
            for (ISMCTSNode sibling : parent.getChildren()) {
                sibling.incrementAvailability();
            }
        }
    }
    
    // Adds the result of the game to the win score.
    private void addWinScore(GameState state) {
        double result = state.getResult(playerNr);
        winScore = winScore + result;
    }

    /**
     * Gets a List of untried moves based on the specified possible cards to move.
     * 
     * @param possibleCardsToMove The possible cards to move.
     * @return A list of untried moves.
     */
    public List<Card> getUntriedMoves(List<Card> possibleCardsToMove) {
        List<Card> untriedMoves = new ArrayList<>();
        
        for (Card card : possibleCardsToMove) {
            if (!containsChild(card)) {
                untriedMoves.add((Card) card.clone());
            }
        }
        return untriedMoves;
    }

    // Checks if this node already has a child with the specified card.
    private boolean containsChild(Card cardId) {
        for (ISMCTSNode child : children) {
            if (child.getPlayedCard().equals(cardId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if this node is fully expanded based on the specified possible cards to move.
     * It checks if all possible cards to move are already children of this node.
     * 
     * @param possibleCardsToMove The possible cards to move.
     * @return true if this node is fully expanded, false otherwise.
     */
    public boolean isFullyExpanded(List<Card> possibleCardsToMove) {
        return getUntriedMoves(possibleCardsToMove).isEmpty();
    }

    /**
     * Finds or creates a child node of this node with the specified card and player number.
     * Used in the MOISMCTS. 
     * 
     * @param card The card to find or create a child node with.
     * @param playerNr The player number to find or create a child node with.
     * @return The found or created child node.
     */
    public ISMCTSNode findOrCreateChild(Card card, int playerNr) {
        ISMCTSNode childNode = null;
        
        for(ISMCTSNode child : children){
            if (child.getPlayedCard().equals(card)) {
                childNode = child;
            }
        }

        if (childNode == null) {
            childNode = addChild(card, playerNr);
        }
        return childNode;
    }
    
    /**
     * Converts this node and its children to a string representation.
     * Presents the tree in a readable format.
     * 
     * @param indent The indentation of the node. For the root node, this should be 0.
     * @return A string representation of this node and its children.
     */
    public String treeToString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentString(indent));
        sb.append(this.toString());
        
        for (ISMCTSNode child : children) {
            sb.append(child.treeToString(indent + 1));
        }
        
        return sb.toString();
    }
    
    // Creates a string with the specified indentation.
    private String indentString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < indent; i++) {
            sb.append("| ");
        }
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString() {
        if(playedCard == null){
            return "Node: [Card = null, PL " + playerNr + " , Score = " + winScore + " , visits = " + visits + "]";}
        else{
            return "Node: [Card = " + playedCard.getId() + ", PL: " + playerNr + " , Score = " + winScore + " , visits = " + visits + "]";
        }
    }
}
