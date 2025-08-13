package game.AI.AiMCTS.AiMCTSBasic;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import game.Card;
import game.GameState;
import game.AI.Node;

/**
 * This class provides and extends the basic class AiMCTSBasic with basic functionalities 
 * for MCTS-based AI, more precisely with the expansion phases for bestMove and bestPass.
 */
public abstract class AiMCTSCheatingPI extends AiMCTSBasic{

    public AiMCTSCheatingPI(){
        super();
    }
    

    
    /**
     * {@inheritDoc}
     * According to https://mcts.ai/about/index.html :
     * If L is not a terminal node (i.e. it does not end the game) then create one or more child nodes and select one C.
     */    
    @Override
    protected MCTSNode expansionPhase(MCTSNode selectedNode) {
        MCTSNode resultNode = selectedNode; //First the already selected node.
        //selectedNode is possibly expanded in the following:
        //children are of type List<Node> and so far null.
        if ((selectedNode.getDepth() < this.expansionDepth) &&             
            //Instead of expanding until the next round, only expand until the end of the round at the most.
            (
            ((selectedNode.getGameState().getDeckSize() == 52 && selectedNode.getGameState().getTricksPlayed() < 13)
            || (selectedNode.getGameState().getDeckSize() == 32 && selectedNode.getGameState().getTricksPlayed() < 8) 
            )
            || 
            ( (selectedNode.getGameState().getDeckSize() == 52 && selectedNode.getGameState().getTricksPlayed() == 13 && selectedNode.getGameState().getNewestTrick().getNumPlayedCards() < 4)
            || (selectedNode.getGameState().getDeckSize() == 32 && selectedNode.getGameState().getTricksPlayed() == 8 && selectedNode.getGameState().getNewestTrick().getNumPlayedCards() < 4)
            )
            )
            ){
            
            //First, determine cards/actions of the player/opponent that can be played.
            //If there are playable cards/actions of the current player/opponent, then add/expand a child node for each card/action.
            if (selectedNode.getActionsPlayableCards().size()>0){
                selectedNode.setChildren(new LinkedList<Node>());
                for (int index=0; index<(selectedNode.getActionsPlayableCards()).size(); index++) {
                    MCTSNode newChild = new MCTSNode(null, selectedNode.getDepth()+1);                    
                    newChild.setActionPlayedCard(new Card(selectedNode.getActionsPlayableCards().get(index).getId()));
                    newChild.setParent(selectedNode);
                    
                    //1. Create deep copy of the game state of the parent:
                    GameState gameStateCopyOfParent = null;
                    try {            
                        if (newChild.getParent() == null){
                            throw new NullPointerException("expandedNode.getParent() == null");
                        } else{
                            gameStateCopyOfParent = newChild.getParent().getGameState().deepCopy();
                        }
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                    //2. Use transition simulation to determine the next game state and set this as the game state of the expandedNode.
                    List<Integer> nextPlayerNoList = new LinkedList<Integer>();
                    GameState nextGameStateOfNextPlayerOrNode = determineNextGameStateOfNextPlayerOrNode(gameStateCopyOfParent, newChild.getActionPlayedCard(), ((MCTSNode) newChild.getParent()).getPlayerNo(),nextPlayerNoList);
                    newChild.setGameState(nextGameStateOfNextPlayerOrNode);
                    //3. Determine and set the player number (playerNo) for the expandedNode.
                    newChild.setPlayerNo(nextPlayerNoList.get(0).intValue());
                    //4. For the expandedNode, determine and set the cards that can be played in its current gameState:
                    if (newChild.getPlayerNo() != -1){ //If there is still a next player! Because of access to array.
                        newChild.setActionsPlayableCards(newChild.getGameState().determineListOfPlayableCards(newChild.getPlayerNo()));     
                    }               
                    
                    selectedNode.getChildren().add(newChild);        
                }
                //Equally distributed choice between one of the added child nodes
                Random rng = new Random();
                int index = rng.nextInt(selectedNode.getChildren().size());
                MCTSNode expandedNodeSelected = (MCTSNode) selectedNode.getChildren().get(index);
                resultNode = expandedNodeSelected;
            }                        
        }  
        
        return resultNode; //Either the leaf selectedNode is returned if it could not be expanded, or one of the new child nodes.
    }

    
    /**
     * {@inheritDoc}
     * According to https://mcts.ai/about/index.html :
     * If L is not a terminal node (i.e. it does not end the game) then create one or more child nodes and select one C.
     */
    @Override
    protected MCTSNode expansionPhaseForBestPass(MCTSNode selectedNode) {
        MCTSNode resultNode = selectedNode; //First the already selected node.
        //selectedNode is possibly expanded in the following:
        //children are of type List<Node> and so far null.
        if (selectedNode.getDepth() < this.expansionDepth){
            //Generate a list with all possible combinations/subsets of 3 playable cards (to be passed) of the 13 or 8 playable/passable cards:
            List<int[]> listOf3CardNumberSubsets = new LinkedList<int[]>();            
            int[] arrOfPlayableCardNumbers;
            if (selectedNode.getGameState().getDeckSize() == 52){
                arrOfPlayableCardNumbers = new int[13];
            } else { //Decksize 32
                arrOfPlayableCardNumbers = new int[8];
            }
            int index2 = 0;
            try {
                if (selectedNode.getActionsPlayableCards().size() == 13 || selectedNode.getActionsPlayableCards().size() == 8){
                    for (Card card : selectedNode.getActionsPlayableCards()){
                        arrOfPlayableCardNumbers[index2] = card.getId();
                        index2++;
                    }
                } else{
                    throw new Exception("Fehler bei der Anzahl spielbarer Karten fürs Weitergeben.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
            listOf3CardNumberSubsets = AiMCTSBasicHelper.Permutation.getAllPossibleSubsetsOfASizeOfArrayWithDistinctElements(arrOfPlayableCardNumbers, 3);
            
            //Select from the list of all possible subsets listOf3CardNumberSubsets as many as specified in maxNumberOfPossiblePasses and save in new list:
            List<int[]> listOf3CardNumberSubsetsAsChildren = new LinkedList<int[]>();    

            //Performance/runtime improvement:
            // Maximum number of possible passes is at deck size 52: binomial coefficient (13, 3), 13 over 3 = 286.
            // Maximum number of possible transfers for deck size 32: binomial coefficient (8, 3), 8 over 3 = 56.
            // In this case, all elements of the list can be taken.
            // This case always occurs if an AI has not restricted the maxNumberOfPossiblePasses to a fraction of the maximum.
            if ((selectedNode.getGameState().getDeckSize() == 52 && maxNumberOfPossiblePasses == 286)
                || 
                (selectedNode.getGameState().getDeckSize() == 32 && maxNumberOfPossiblePasses == 56)
               ){
                listOf3CardNumberSubsetsAsChildren = listOf3CardNumberSubsets;      
            } else {         
                Random rngSubsets = new Random();
                try {
                    if (maxNumberOfPossiblePasses <= listOf3CardNumberSubsets.size()){
                        for (int i = 1; i <= maxNumberOfPossiblePasses; i++){
                            int index3 = rngSubsets.nextInt(listOf3CardNumberSubsets.size());
                            listOf3CardNumberSubsetsAsChildren.add(listOf3CardNumberSubsets.get(index3));
                            listOf3CardNumberSubsets.remove(index3);
                        }
                    } else{
                        throw new Exception("Fehler: maxNumberOfPossiblePasses zu groß für Anzahl möglicher Weitergaben.");
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
            }


            if (listOf3CardNumberSubsetsAsChildren.size() > 0){
                selectedNode.setChildren(new LinkedList<Node>());
                for (int index=0; index < listOf3CardNumberSubsetsAsChildren.size(); index++) {
                    MCTSNode newChild = new MCTSNode(null, selectedNode.getDepth()+1);
                    
                    Card[] passedCards = new Card[3];
                    passedCards[0] = new Card(listOf3CardNumberSubsetsAsChildren.get(index)[0]);
                    passedCards[1] = new Card(listOf3CardNumberSubsetsAsChildren.get(index)[1]);
                    passedCards[2] = new Card(listOf3CardNumberSubsetsAsChildren.get(index)[2]);
                    newChild.setActionPassedCards(passedCards);
                    
                    newChild.setParent(selectedNode);                    
                    
                    //Create a deep copy of the parent game state, as the original must not be changed in the subsequent simulation phase.
                    newChild.setGameState(newChild.getParent().getGameState().deepCopy());
                    
                    //Determine and set the playerNo for the newChild. As this is only determined after passing on 
                    // and sequentialization already takes place in the subsequent simulationPhaseForBestPass, -1 is set here.
                    newChild.setPlayerNo(-1);
                    //The same applies to the ActionsPlayableCards. So null here.
                    newChild.setActionsPlayableCards(null);
                             
                    selectedNode.getChildren().add(newChild);        
                }
                
            }         

            //Choose one of the child nodes:
            if (selectedNode.getChildren().size() > 0){
                //Equally distributed choice between one of the added child nodes
                Random rng = new Random();
                int index = rng.nextInt(selectedNode.getChildren().size());
                MCTSNode expandedNodeSelected = (MCTSNode) selectedNode.getChildren().get(index);
                resultNode = expandedNodeSelected;
            }
        }
        return resultNode;
    }


}
