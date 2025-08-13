package game.AI.AiMCTS.AiMCTSBasic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import game.Card;
import game.GameController;
import game.GameState;
import game.Player;
import rules.Rules;

/**
 * This class provides and extends the class AiMCTSCheatingPI with basic functionalities 
 * for MCTS-based AI, more precisely with the simulation phases for bestMove and bestPass until the round's end.
 */
public abstract class AiMCTSCheatingPIRoundSim extends AiMCTSCheatingPI{    

    public AiMCTSCheatingPIRoundSim(){
        super();
    }
    
    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestMove until the round's end.
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove was called.
     * @return List<Double> The round's result that is determined at the end of the simulation of the remaining round.
     */
    @Override
    protected List<Double> simulationPhase(MCTSNode expandedNode, int rootPlayerNo){
        //For the expandedNode, the rest of the round is simulated below.           

        List<Double> resultList = null;
        
        //A GameState variable from which the results are later determined and saved in a List<Double> as a return.
        GameState gameStateResultOfSimulation = null;

        //0. First check whether the round has already ended in the game state of the expandedNode:
        if (expandedNode.getGameState().isRoundOver()){ //Round is over
            gameStateResultOfSimulation = expandedNode.getGameState();
        } else{ //The round is not yet over and will therefore be simulated until the end.
            //Execute the actual simulation phase from the gameState of the expandedNode to the end of the round:
            //1. Make a deep copy of the game state of the expandedNode, as this will be changed in the simulation:
            GameState gameStateForSimulation = expandedNode.getGameState().deepCopy();
            //   New GameController instance to have access to GameController methods:
            GameController gameControllerHelper = new GameController(gameStateForSimulation);
            
            //(For better readability) the player number:  
            int nextPlayerNo = expandedNode.getPlayerNo();

            //3. If the current trick is not yet finished, simulate it until the end of the trick.
            // A variable to hold a player/the player's number in here.
            int playerHelper = nextPlayerNo;

            // if without else
            if (gameStateForSimulation.getNewestTrick().getNumPlayedCards() < 4){ //Trick not yet finished.
                // Randomly/evenly select one of the playable cards of the next player/opponent.
                Random rngCard = new Random();
                int index = rngCard.nextInt(expandedNode.getActionsPlayableCards().size()); //Zufällige int-zahl zwischen 0 und getActionsPlayableCards.size() (exclusive)
                Card cardToPlay = expandedNode.getActionsPlayableCards().get(index);
                int cardNumberIdToMove = cardToPlay.getId();
                //Play this card now, i.e. remove this card from the player's hand and add it to the trick.
                gameStateForSimulation.playCard(cardNumberIdToMove, nextPlayerNo);
                //Check whether the trick is finished and needs to be completed and evaluated.
                if (gameStateForSimulation.getNewestTrick().getNumPlayedCards() < 4){
                    //Trick is not yet finished. The remaining players must each play their card.          
                    //Check whether the player who has just played the card was the starter of the trick.
                    if (nextPlayerNo == gameStateForSimulation.getNewestTrick().getStarter()){
                        //The player who has just placed the card was the starter of the trick. The playable cards of the other players must then be set first.
                        // But only from the 2nd trick, i.e. if the current trick is at least the 2nd trick or later.
                        // Because in the 1st trick, it was already set toegther with the player's/starter's playable cards before the turn of the player/starter.
                        if (gameStateForSimulation.getTricksPlayed() >= 2){
                            for (int playerNumber = (nextPlayerNo+1)%4; playerNumber != nextPlayerNo; playerNumber = (playerNumber+1)%4){
                                gameStateForSimulation.setPlayableCardsForNonStarterInTrick2orLater(gameStateForSimulation.getPlayers()[playerNumber], gameStateForSimulation.getNewestTrick().getStartingCard());
                            }
                        }
                    } else{ //He was not the starter.
                        if (gameStateForSimulation.getTricksPlayed() >= 2){
                            for (int playerNumber = (nextPlayerNo+1)%4; playerNumber != gameStateForSimulation.getNewestTrick().getStarter(); playerNumber = (playerNumber+1)%4){
                                gameStateForSimulation.setPlayableCardsForNonStarterInTrick2orLater(gameStateForSimulation.getPlayers()[playerNumber], gameStateForSimulation.getNewestTrick().getStartingCard());
                            }
                        }
                        ; //The playable cards are now set in any case.
                    }
                    //Play the cards of the remaining players.
                    for (int playerNumber = (nextPlayerNo+1)%4; playerNumber != gameStateForSimulation.getNewestTrick().getStarter(); playerNumber = (playerNumber+1)%4){
                        try {                            
                            List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(playerNumber);
                            rngCard = new Random();
                            int index2 = rngCard.nextInt(listOfPlayableCards.size());
                            Card cardToPlay2 = listOfPlayableCards.get(index2);
                            int cardNumberIdToMove2 = cardToPlay2.getId();
                            //Play this card now.                       
                            gameStateForSimulation.playCard(cardNumberIdToMove2, playerNumber);                            
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.exit(0);
                        }
                    }                                    
                }
                //The cards of all players have been played in the current trick.
                //Evaluate this trick now.
                // - If hearts has not yet been broken/played in the current round, but has been played in this trick, then set isHeartBroken true.
                gameControllerHelper.updateHeartsBroken();
                //   - Then complete the trick: Determine the winner, count the points of the trick, credit them to the winner's round account and return the winner.
                Player currentTrickWinner = gameControllerHelper.completeTrick(); 
                //    - Save the player number (PlayerNo) of the trick winner so that he can be found as the starter for the next trick.
                int currentTrickWinnerPlayerNo = currentTrickWinner.getPlayerNo();
                //Save the trick winner as the starter of a potential new trick in playerHelper (available outside of if).
                playerHelper = currentTrickWinnerPlayerNo;
                //Trick evaluation completed.     
            }
            //No more open tricks, i.e. NumPlayedCards must equal 4 here.

            //Check whether there are no more open tricks to verify the correct procedure here!
            if (gameStateForSimulation.getNewestTrick().getNumPlayedCards() < 4){
                System.out.println("Still open trick. That shouldn't be the case! Program is terminated!");
                System.exit(0);
            }
                                           

            //4. No open tricks up to this point, so the trick is over. Now, if the round is not yet over, simulate the remaining tricks of this round until the end of the round.
            // if without else
            //Check whether the round is not yet over.
            if ((gameStateForSimulation.getDeckSize() == 52 && gameStateForSimulation.getTricksPlayed() < 13)
                || (gameStateForSimulation.getDeckSize() == 32 && gameStateForSimulation.getTricksPlayed() < 8)){                
                //Simulate remaining tricks! Starter can be found in playerHelper.                
                //Initialize the new trick.
                gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
                gameStateForSimulation.initTrick(playerHelper);
                // Simulate the remaining tricks.
                simulateRemainingTricksUntilRoundOver(gameStateForSimulation, gameControllerHelper);

                gameControllerHelper.completeRound();
                        
            }
            //No more open round, i.e. 13 or 8 tricks have been played, or in short: gameStateForSimulation.isRoundOver() == true !

            //Check if no more open round to verify correct procedure here!
            if (!gameStateForSimulation.isRoundOver()){
                System.out.println("Still open round. That shouldn't be the case! Program is terminated!");
                System.exit(0);
            }
            
            
            //Now this round is also finished.
            // So now save the game state of the end of the round ("gameStateForSimulation") in gameStateResultOfSimulation.
            gameStateResultOfSimulation = gameStateForSimulation;

        }
        
        //Evaluate the end of the round in the game state "gameStateResultOfSimulation" and determine the results and store them in resultList:        
        resultList = evaluateRoundEnd(gameStateResultOfSimulation, rootPlayerNo);

        //Return the results in a list.
        return resultList;

    }

    /**
     * Method that simulates remaining tricks of a round until the round is over.
     * @param gameStateForSimulation The current GameState instance for which the simulation is continued.
     * @param gameControllerHelper The instance of GameController to have access to the class' methods.
     */
    public void simulateRemainingTricksUntilRoundOver(GameState gameStateForSimulation, GameController gameControllerHelper){
        while (!gameStateForSimulation.isRoundOver()){    
            //Begin with the starter.
            int numberOfStarter = -1;                                                       
            if (gameStateForSimulation.isFirstTrick()) { 
                numberOfStarter = gameStateForSimulation.getStarterOfNewestTrick();
                gameStateForSimulation.setPlayableCardsForEachPlayerInTrick0or1();                
            }                    
            else {
                numberOfStarter = gameStateForSimulation.getStarterOfNewestTrick(); 
                gameStateForSimulation.setPlayableCardsForStarterInTrick2orLater(gameStateForSimulation.getPlayers()[numberOfStarter]);                
            }                    

            //Determine randomly/equally distributed the card to be played by the starter.                   
            List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(numberOfStarter);
            Random rngCard = new Random(); 
            int index = rngCard.nextInt(listOfPlayableCards.size()); 
            Card cardToPlay = listOfPlayableCards.get(index);
            int firstCardId = cardToPlay.getId();
            //Play this card now.
            if (firstCardId < 1 || firstCardId > 52){
                throw new IllegalArgumentException("Equally distributed selection incorrect for starting player. Card: " + firstCardId);
            }                            
            gameStateForSimulation.playCard(firstCardId, numberOfStarter);                 
            
            // Let the remaining three players play.
            for (int playerCount = 1; playerCount < 4; playerCount++){                        
                int currentPlayerNumber = (numberOfStarter + playerCount) % 4;                        
                // Set playable cards                                              
                if (!gameStateForSimulation.isFirstTrick()) { 
                    gameStateForSimulation.setPlayableCardsForNonStarterInTrick2orLater(gameStateForSimulation.getPlayers()[currentPlayerNumber], gameStateForSimulation.getFirstCardOfNewestTrick());
                }
                else {                    
                }                        
                //Determine randomly/equally distributed the card to be played by the player.   
                List<Card> listOfPlayableCards2 = gameStateForSimulation.determineListOfPlayableCards(currentPlayerNumber);
                Random rngCard2 = new Random(); 
                int index2 = rngCard2.nextInt(listOfPlayableCards2.size());
                Card cardToPlay2 = listOfPlayableCards2.get(index2);
                int cardIdToMove = cardToPlay2.getId();
                //Play this card now.
                if (cardIdToMove < 1 || cardIdToMove > 52){     
                    throw new IllegalArgumentException("Equally distributed selection incorrect for currentPlayer. Card: " + cardIdToMove);
                }                            
                gameStateForSimulation.playCard(cardIdToMove, currentPlayerNumber);                
            }
                
            Player trickWinner = gameControllerHelper.completeTrick();
            int numberOfTrickWinner = trickWinner.getPlayerNo();
                        
            gameControllerHelper.updateHeartsBroken();                    
                        
            if (!gameStateForSimulation.isRoundOver()){
                gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
                gameStateForSimulation.initTrick(numberOfTrickWinner);
            }                

        }
    }

    /** 
     * Method that evaluates the end of the round in the game state "gameStateResultOfSimulation" and determines the results and returns them in a List<Double>.
     * @param gameStateResultOfSimulation The game state to evaluate the round's end from.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove or bestPass was called.
     * @return List<Double> The round's result that is determined at the end of the simulated round.
     */
    public List<Double> evaluateRoundEnd(GameState gameStateResultOfSimulation, int rootPlayerNo){
        List<Double> resultList = null;
        boolean hasRootPlayerFewestPointsOrShotTheMoon = false;
        int payOut = 0;
        int lossOrWin = -1; 
        double normalizedPayoff = -1; //normalisierter Payoff zur besseren Vergleichbarkeit der implementierten AIs
        int winnersRoundPoints = 26;
        if (gameStateResultOfSimulation.getDeckSize()==52){
            winnersRoundPoints = 26; //Highest round score that can be achieved with deck size 52.
        } else if (gameStateResultOfSimulation.getDeckSize()==32){
            winnersRoundPoints = 21; //Highest round score that can be achieved with deck size 32.
        }

        ArrayList<Player> listOfWinners = gameStateResultOfSimulation.determineListOfRoundWinnerOrWinners();
        winnersRoundPoints = listOfWinners.get(0).getRoundPts();
        for (Player winner : listOfWinners){
            if (winner.getPlayerNo() == rootPlayerNo){
                hasRootPlayerFewestPointsOrShotTheMoon = true;
                break;
            }
        }
        if (hasRootPlayerFewestPointsOrShotTheMoon == true){ //rootPlayer has won.
            lossOrWin = 1;
            if (gameStateResultOfSimulation.getDeckSize()==52){
                if (winnersRoundPoints == 26){ //The winner of the round has shot the moon, i.e. 26 points.
                    payOut = 3*winnersRoundPoints; //Old Moon: +26 points for the non-winner, +0 for the winner/moon shooter -> payout function: r2-r1 + r3-r1 + r4-r1, r1=0, all non-winners rj = 26, so payout= 26-0 + 26-0 + 26-0 = 3*winnersRoundPoints 
                } else{ //The winner of the round did not shoot the moon, but has the fewest points!
                    for (int playerNumber = 0; playerNumber <= 3; playerNumber++){
                        if (playerNumber != rootPlayerNo){
                            payOut = payOut 
                                    + gameStateResultOfSimulation.getPlayers()[playerNumber].getRoundPts() 
                                    - gameStateResultOfSimulation.getPlayers()[rootPlayerNo].getRoundPts();
                        } 
                    }
                }
            } else if (gameStateResultOfSimulation.getDeckSize()==32){
                if (winnersRoundPoints == 21){ //The round winner has shot the moon, so 21 points.
                    payOut = 3*winnersRoundPoints; 
                } else{ //The winner of the round did not shoot the moon, but has the fewest points!
                    for (int playerNumber = 0; playerNumber <= 3; playerNumber++){
                        if (playerNumber != rootPlayerNo){
                            payOut = payOut 
                                    + gameStateResultOfSimulation.getPlayers()[playerNumber].getRoundPts() 
                                    - gameStateResultOfSimulation.getPlayers()[rootPlayerNo].getRoundPts();
                        } 
                    }
                }                
            }             
        } else{ //hasRootPlayerFewestPointsOrShotTheMoon == false, rootPlayer has not the fewest points or not shot the moon.
            lossOrWin = 0;
            if (gameStateResultOfSimulation.getDeckSize()==52){
                if (winnersRoundPoints == 26){ //The winner of the round has shot the moon, i.e. 26 points.
                    payOut = 0 - winnersRoundPoints; //Old Moon: +26 points for the non-winner, +0 for the winner/moon shooter -> payout function: r1 - rj
                } else{ //The winner of the round did not shoot the moon, but has the fewest points!
                    payOut = winnersRoundPoints - gameStateResultOfSimulation.getPlayers()[rootPlayerNo].getRoundPts();
                }
            } else if (gameStateResultOfSimulation.getDeckSize()==32){
                if (winnersRoundPoints == 21){ //The round winner has shot the moon, so 21 points.
                    payOut = 0 - winnersRoundPoints; //Old Moon: +21 points for the non-winner, +0 for the winner/moon shooter -> payout function: r1 - rj
                } else{ //The winner of the round did not shoot the moon, but has the fewest points!
                    payOut = winnersRoundPoints - gameStateResultOfSimulation.getPlayers()[rootPlayerNo].getRoundPts();
                }
                
            }            
        }

        normalizedPayoff = gameStateResultOfSimulation.getResult(rootPlayerNo);

        resultList = new LinkedList<Double>();
        resultList.add(Double.valueOf(Integer.valueOf(payOut)));
        resultList.add(Double.valueOf(Integer.valueOf(lossOrWin)));
        resultList.add(Double.valueOf(normalizedPayoff));
        
        return resultList;
    }
    
    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestPass until the round's end.
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestPass was called.
     * @return List<Double> The round's result that is determined at the end of the simulation of the remaining round.
     */
    @Override
    protected List<Double> simulationPhaseForBestPass(MCTSNode expandedNode, int rootPlayerNo){
        
        List<Double> resultList = null;

        GameState gameStateForSimulation = expandedNode.getGameState().deepCopy();
        // New GameController instance to have access to GameController methods:
        GameController gameControllerHelper = new GameController(gameStateForSimulation);  
        
        //A GameState variable from which the results are later determined and saved in a List<Double> as a return.
        GameState gameStateResultOfSimulation = null;
           
        Player[] players = gameStateForSimulation.getPlayers();
        Player player1 = players[0]; 
        Player player2 = players[1]; 
        Player player3 = players[2]; 
        Player player4 = players[3]; 
        
        player1.getCardsToPassOriginalReference().clear();
        player2.getCardsToPassOriginalReference().clear();
        player3.getCardsToPassOriginalReference().clear();
        player4.getCardsToPassOriginalReference().clear();
        
        //Determine and save the receiver of each player's cards.
        int roundNo = gameStateForSimulation.getRoundNo();
        int player1ReceiverNo = Rules.getPassReceiverNo(player1.getPlayerNo(), roundNo);
        player1.setReceiverNo(player1ReceiverNo);
        int player2ReceiverNo = Rules.getPassReceiverNo(player2.getPlayerNo(), roundNo);
        player2.setReceiverNo(player2ReceiverNo);
        int player3ReceiverNo = Rules.getPassReceiverNo(player3.getPlayerNo(), roundNo);
        player3.setReceiverNo(player3ReceiverNo);
        int player4ReceiverNo = Rules.getPassReceiverNo(player4.getPlayerNo(), roundNo);
        player4.setReceiverNo(player4ReceiverNo);
                
        int playerNoOfCurrentPlayer = rootPlayerNo; //Root-Player        

        for (int playerNo = 0; playerNo <= 3; playerNo++){            
            //For each player, except the root player, make the equally distributed choice of 3 of his (playable) cards and save them:
            if (playerNo != playerNoOfCurrentPlayer){
                int[] tempPass = new int[3];
                List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(players[playerNo].getPlayerNo());                                    
                Random rngCard = new Random();
                int index1 = rngCard.nextInt(listOfPlayableCards.size());
                Card cardToPlay1 = listOfPlayableCards.get(index1);
                int cardNumberIdToMove1 = cardToPlay1.getId();
                tempPass[0] = cardNumberIdToMove1;
                int index2 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index2){
                    index2 = rngCard.nextInt(listOfPlayableCards.size());
                }
                Card cardToPlay2 = listOfPlayableCards.get(index2);
                int cardNumberIdToMove2 = cardToPlay2.getId();
                tempPass[1] = cardNumberIdToMove2;
                int index3 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index3 || index2==index3){
                    index3 = rngCard.nextInt(listOfPlayableCards.size());
                }
                Card cardToPlay3 = listOfPlayableCards.get(index3);
                int cardNumberIdToMove3 = cardToPlay3.getId();
                tempPass[2] = cardNumberIdToMove3;
                //Save:
                players[playerNo].savePassedCards(tempPass);                            
            }
        }

        //Save the cards of the current/root player:
        int[] passOfCurrentPlayer = new int[3];
        passOfCurrentPlayer[0] = expandedNode.getActionPassedCards()[0].getId();
        passOfCurrentPlayer[1] = expandedNode.getActionPassedCards()[1].getId();
        passOfCurrentPlayer[2] = expandedNode.getActionPassedCards()[2].getId();
        players[playerNoOfCurrentPlayer].savePassedCards(passOfCurrentPlayer);      
                
        //Pass the cards.
        for (Player player : players){
            player.passCards(players[player.getReceiverNo()]);
        }
                            
        gameStateForSimulation.setCardsHaveBeenPassed(true);                         
    
        gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
        int numberOfStarter = gameStateForSimulation.getStarterOfRound().getPlayerNo();                              
        gameStateForSimulation.initTrick(numberOfStarter);

        // Simulate remaining tricks.
        simulateRemainingTricksUntilRoundOver(gameStateForSimulation, gameControllerHelper);

        gameControllerHelper.completeRound();                                

        //Check if no more open round to verify correct procedure here!
        if (!gameStateForSimulation.isRoundOver()){
            System.out.println("Still open round. That shouldn't be the case! Program is terminated!");
            System.exit(0);
        }
        
        //Now this round is also finished.
        // So now save the gameState of the end of the round ("gameStateForSimulation") in gameStateResultOfSimulation.
        gameStateResultOfSimulation = gameStateForSimulation;

        //Evaluation
        resultList = evaluateRoundEnd(gameStateResultOfSimulation, rootPlayerNo);

        return resultList;
    }    
    
    
    
}
