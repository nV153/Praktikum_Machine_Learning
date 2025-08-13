package game.AI.AiCFR.MCCFR;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the Counterfactual Regret Minimization (CFR) algorithm.
 */
public class CFRNode {
    private int playerNo; 
    private CFRNode parent; 
    private boolean terminal; 
    private int playedCardId;
    private ArrayList<CFRNode> children;
    private List<Integer> actions;
    private int queenOfSpadesPlayedYet; 
    private int numPlayedHearts;
    private boolean isNodeForPassing;
    private int numCardsPlayedInThisTrick; 
    private int numCardsPassed; 
    
    private double[] payoff;

    /**
     * Constructs a CFRNode object.
     *
     * @param isNodeForPassing Indicates if the node is for passing.
     * @param playerNo The number representing the player.
     * @param playedCardId The ID of the played card.
     * @param parent The parent node of the current node.
     * @param actions The list of available actions.
     */
    public CFRNode(boolean isNodeForPassing, int playerNo, int playedCardId, CFRNode parent, List<Integer> actions) {
        
        this.playerNo = playerNo;
        this.parent = parent;
        this.playedCardId = playedCardId;
        this.terminal = false;
        this.children = new ArrayList<>();
        this.actions = actions;
        this.isNodeForPassing = isNodeForPassing; 

        if (parent == null){
            queenOfSpadesPlayedYet = 0;
            numPlayedHearts = 0;
        }

        else {
            if (playedCardId==37 && !isNodeForPassing) queenOfSpadesPlayedYet = 1;
            else queenOfSpadesPlayedYet = parent.getQueenOfSpadesPlayedYet();
        
            //increase counter if heart was played
            numPlayedHearts = parent.getNumPlayedHearts();
            if (playedCardId >= 14 && playedCardId <= 26 && !isNodeForPassing){
                numPlayedHearts++;
            }
        }

    }

    /**
     * Retrieves the history of the node. The history contains the number of cards of suit hearts played yet
     * and an indicator, if the queen of spades has been played yet.
     *
     * @return The history of the node.
     */ 
    public String getHistory(){ 
        if (isNodeForPassing) return actions.toString();
        else {
            String history = String.valueOf(numPlayedHearts) + "-" + String.valueOf(queenOfSpadesPlayedYet) + "-" + actions.toString();
            return history;
        }    
    }

    public ArrayList<CFRNode> getChildren() {
        return children;
    }

    public int getNumPlayedHearts(){
        return numPlayedHearts;
    }

    public int getQueenOfSpadesPlayedYet(){
        return queenOfSpadesPlayedYet;
    }

    public CFRNode addChild(CFRNode child) {
        children.add(child);
        return child;
    }

    public void setTerminal(){
        terminal = true;
    }
    
    public boolean isTerminal() {
        return terminal;
    }

    public void setPayoff(double[] payoff){
        this.payoff = payoff;
    }

    public double getPayoff(int id){
        return payoff[id];
    }

    public int getPlayerNo(){
        return playerNo;
    }

    public List<Integer> getActions(){
        return actions;
    }

    public boolean isNodeForPassing(){
        return isNodeForPassing;
    }

    public int getNumCardsPlayedInThisTrick(){
        return numCardsPlayedInThisTrick;
    }

    public void setNumCardsPlayedInThisTrick(int num){
        this.numCardsPlayedInThisTrick = num;
    }

    public void setNumCardsPassed(int num){
        this.numCardsPassed = num;
    }

    public int getNumCardsPassed(){
        return this.numCardsPassed;
    }

}
