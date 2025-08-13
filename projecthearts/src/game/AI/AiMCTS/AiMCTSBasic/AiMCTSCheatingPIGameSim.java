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
 * for MCTS-based AI, more precisely with the simulation phases for bestMove and bestPass until the game's end.
 */
public abstract class AiMCTSCheatingPIGameSim extends AiMCTSCheatingPI{

    public AiMCTSCheatingPIGameSim(){
        super();
    }
      
    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestMove until the game's end.
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove was called.
     * @return List<Double> The game's result that is determined at the end of the simulation of the remaining game.
     */
    @Override  
    protected List<Double> simulationPhase(MCTSNode expandedNode, int rootPlayerNo){        
        //For the expandedNode, the rest of the game is simulated below.        
        List<Double> resultList = null;

        //A GameState variable from which the results are later determined and saved in a List<Double> as a return.
        GameState gameStateResultOfSimulation = null;

        //0. First check whether the game is already over in the game state of the expandedNode:
        if (expandedNode.getGameState().atLeastOnePlayerHas100PointsOrMore()){ //Spiel zuende
            gameStateResultOfSimulation = expandedNode.getGameState();
        } else{ //The game is not yet finished and is therefore simulated to the end.
            //Execute the actual simulation phase from the game state of the expandedNode to the end of the game:
            //1. Make a deep copy of the gameState of the expandedNode, as this will be changed in the simulation:
            GameState gameStateForSimulation = expandedNode.getGameState().deepCopy();
            //  New instance of GameController to have access to GameController methods:
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
                int index = rngCard.nextInt(expandedNode.getActionsPlayableCards().size());
                Card cardToPlay = expandedNode.getActionsPlayableCards().get(index);
                int cardNumberIdToMove = cardToPlay.getId();
                //Play this card now, i.e. remove this card from the player's hand and add it to the trick.
                gameStateForSimulation.playCard(cardNumberIdToMove, nextPlayerNo);
                //Check whether the trick is over and needs to be completed and evaluated.
                if (gameStateForSimulation.getNewestTrick().getNumPlayedCards() < 4){
                    //The trick is not yet finished. The remaining players must each play their card.          
                    //Check whether the player who has just played the card was the starter of the trick.
                    if (nextPlayerNo == gameStateForSimulation.getNewestTrick().getStarter()){
                        //The player who has just played the card was the starter of the trick; the playable cards of the other players must then be played first.
                        // But only from the 2nd trick, i.e. if the current trick is at least the 2nd trick or later.
                        // Because in the 1st trick they have already been set together with the player's/starter's playable cards before the player/starter's turn.
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
                            int index2 = rngCard.nextInt(listOfPlayableCards.size()); //Zufällige int-zahl zwischen 0 und listOfPlayableCards.size() (exclusive)
                            Card cardToPlay2 = listOfPlayableCards.get(index2);
                            int cardNumberIdToMove2 = cardToPlay2.getId();
                            //Play this card now, i.e. remove this card from the player's hand and add it to the trick.
                            gameStateForSimulation.playCard(cardNumberIdToMove2, playerNumber);                            
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.exit(0);
                        }
                    }                                    
                }
                //The cards of all players have been played in the current trick.
                //Evaluate this trick now.
                // - If hearts have not yet been broken/played in the current round, but have been played in this trick, then set isHeartBroken true.
                gameControllerHelper.updateHeartsBroken();
                // - Then complete the trick: Determine the winner, count the points of the trick, credit them to the winner's round account and return the winner.
                Player currentTrickWinner = gameControllerHelper.completeTrick(); 
                // - Save the PlayerNo of the trick winner so that it can be found as the starter for the next trick.
                int currentTrickWinnerPlayerNo = currentTrickWinner.getPlayerNo(); //TODO: Kann am Ende wahrscheinlich raus, da nur für Test benötigt.
                //Save the trick winner as the starter of a potential new trick in playerHelper (available outside of if).
                playerHelper = currentTrickWinnerPlayerNo;
                //Trick evaluation completed.    
            }
            //No open trick anymore, i.e. NumPlayedCards must equal 4 here.

            //Check whether there are no more open tricks to verify the correct procedure here!
            if (gameStateForSimulation.getNewestTrick().getNumPlayedCards() < 4){ //Trick not yet finished, so still open
                System.out.println("Still open trick. That shouldn't really be the case! Program is terminated!");
                System.exit(0);
            }
                                           

            //4. No open tricks up to this point, so the trick is over. Now, if the round is not yet over, simulate the remaining tricks of this round until the end of the round.
            // if without else
            //Check whether the round is not yet over.
            if ((gameStateForSimulation.getDeckSize() == 52 && gameStateForSimulation.getTricksPlayed() < 13)
                || (gameStateForSimulation.getDeckSize() == 32 && gameStateForSimulation.getTricksPlayed() < 8)){
                //Alternativ: !nextGameState.isRoundOver(), prüft aber zusätzlich nochmal NumPlayedCards == 4
                //Simulate remaining tricks! //Starter can be found in playerHelper.                
                //Initialize the new trick. (last trick has already been completed, so no open trick possible here, see above)
                gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
                gameStateForSimulation.initTrick(playerHelper);
                // Play the remaining tricks.
                simulateRemainingTricksUntilRoundOver(gameStateForSimulation, gameControllerHelper);

                gameControllerHelper.completeRound();
                        
            }
            //No more open round, i.e. 13 or 8 tricks have been played, or in short: gameStateForSimulation.isRoundOver() == true !

            //Check if no more open round to verify correct procedure here!
            if (!gameStateForSimulation.isRoundOver()){ //Round not yet finished, so still open
                System.out.println("Still open round. That shouldn't be the case! Program is terminated!");
                System.exit(0);
            }
            
            //5. Up to this point there are no more tricks left in a round, i.e. the round is over/no longer open, i.e. no more open rounds.
            // Now if no player has at least 100 points, i.e. the game is not yet over, 
            // then simulate new rounds until at least one player has 100 points at the end of a round, i.e. the game is over.
            simulateRemainingRoundsUntilGameOver(gameStateForSimulation, gameControllerHelper);
            //The game is over, i.e. at least 1 player has 100 points or more, so the following applies: gameStateForSimulation.atLeastOnePlayerHas100PointsOrMore() == true

            //Check if game is over to verify correct procedure here!
            if (!gameStateForSimulation.atLeastOnePlayerHas100PointsOrMore()){ //Game not yet finished.
                System.out.println("Game not over yet. That shouldn't be the case! Program is terminated!");
                System.exit(0);
            }

            //6. Now this game is also finished.
            // So now save the game state of the end of the game ("gameStateForSimulation") in gameStateResultOfSimulation!
            gameStateResultOfSimulation = gameStateForSimulation;

        }        
        
        //Evaluate the end of the game in the game state "gameStateResultOfSimulation" and determine the results and store them in resultList:        
        resultList = evaluateGameEnd(gameStateResultOfSimulation, rootPlayerNo);
        
        //Return the results in a list.
        return resultList;

    }

    /**
     * Method that simulates remaining tricks of a round until the round is over.
     * @param gameStateForSimulation The current GameState instance for which the simulation is continued.
     * @param gameControllerHelper The instance of GameController to have access to the class' methods.
     */
    public void simulateRemainingTricksUntilRoundOver(GameState gameStateForSimulation, GameController gameControllerHelper){
        //Play the remaining tricks.
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

            //Determine the card to be played by the starter at random.             
            List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(numberOfStarter);
            Random rngCard = new Random();
            int index = rngCard.nextInt(listOfPlayableCards.size());
            Card cardToPlay = listOfPlayableCards.get(index);
            int firstCardId = cardToPlay.getId();
            //Play the card.
            if (firstCardId < 1 || firstCardId > 52){
                throw new IllegalArgumentException("Gleichverteilte Wahl fehlerhaft für starting player: " + firstCardId);
            }                            
            gameStateForSimulation.playCard(firstCardId, numberOfStarter);                 
            
            //Let the remaining 3 players play.
            for (int playerCount = 1; playerCount < 4; playerCount++){                        
                int currentPlayerNumber = (numberOfStarter + playerCount) % 4;                        
                // Set playable cards                                              
                if (!gameStateForSimulation.isFirstTrick()) {  
                    gameStateForSimulation.setPlayableCardsForNonStarterInTrick2orLater(gameStateForSimulation.getPlayers()[currentPlayerNumber], gameStateForSimulation.getFirstCardOfNewestTrick());
                }
                else {                     
                }                        
                //Randomly determine the card to be played by the player.    
                List<Card> listOfPlayableCards2 = gameStateForSimulation.determineListOfPlayableCards(currentPlayerNumber);
                Random rngCard2 = new Random();
                int index2 = rngCard2.nextInt(listOfPlayableCards2.size());
                Card cardToPlay2 = listOfPlayableCards2.get(index2);
                int cardIdToMove = cardToPlay2.getId();
                //Play the card.
                if (cardIdToMove < 1 || cardIdToMove > 52){     
                    throw new IllegalArgumentException("Equally distributed selection incorrect for currentPlayer: " + cardIdToMove);
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
     * Method that simulates remaining rounds of a game until the game is over.
     * @param gameStateForSimulation The current GameState instance for which the simulation is continued.
     * @param gameControllerHelper The instance of GameController to have access to the class' methods.
     */
    public void simulateRemainingRoundsUntilGameOver(GameState gameStateForSimulation, GameController gameControllerHelper){
        //Play rounds, until the game is over.
        while (!gameStateForSimulation.atLeastOnePlayerHas100PointsOrMore()) {
            //Simulate another round.

            //Initialize the new round.

            //General initialization independent of the game mode
            gameStateForSimulation.addRoundTricksToGameRoundTricks(gameStateForSimulation.createAndSetNewRoundTricks());
            gameStateForSimulation.setTricksPlayed(0);
            gameStateForSimulation.resetRoundPoints(); 
            gameStateForSimulation.setIsHeartBroken(false); 
            gameStateForSimulation.setCardsHaveBeenPassed(false); 
            gameStateForSimulation.resetCardsPassed(); 
            gameStateForSimulation.resetHandCardsOfPlayers(); 
            gameStateForSimulation.setRoundNo(gameStateForSimulation.getRoundNo()+1); 
            gameStateForSimulation.shuffleDeck(); 
            gameStateForSimulation.handOutCards();          

            //In a game with passing, passing is omitted in every 4th round.
            if (gameStateForSimulation.getIsGameWithPassing() == true){
                if (gameStateForSimulation.getRoundNo()%4 == 0){
                    gameStateForSimulation.setRoundWithPassing(false);
                } else{
                    gameStateForSimulation.setRoundWithPassing(true);
                }
            }
                                    
            if (gameStateForSimulation.getIsGameWithPassing() == true
                && gameStateForSimulation.getIsRoundWithPassing() == true){
                gameStateForSimulation.setPlayableCardsForEachPlayerInTrick0or1(); //Für jeden Spieler seine spielbaren Karten bestimmen.                                
            }

            //A possible passing follows.
            
            int player1ReceiverNoHelper = Rules.getPassReceiverNo(0, gameStateForSimulation.getRoundNo());
            
            if (gameStateForSimulation.getIsGameWithPassing() && (player1ReceiverNoHelper != 0)) {
                
                Player[] players = gameStateForSimulation.getPlayers(); 
                Player player1 = players[0]; 
                Player player2 = players[1]; 
                Player player3 = players[2];
                Player player4 = players[3];
                
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

                //For each player, make an equally distributed choice of 3 of their cards and save them
                Random rngCard = new Random();
                //Player 1
                int[] passOfPlayer1 = new int[3];
                List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(player1.getPlayerNo());                    
                int index1 = rngCard.nextInt(listOfPlayableCards.size()); 
                Card cardToPlay1 = listOfPlayableCards.get(index1);
                int cardNumberIdToMove1 = cardToPlay1.getId();
                passOfPlayer1[0] = cardNumberIdToMove1;
                int index2 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index2){
                    index2 = rngCard.nextInt(listOfPlayableCards.size());
                }
                Card cardToPlay2 = listOfPlayableCards.get(index2);
                int cardNumberIdToMove2 = cardToPlay2.getId();
                passOfPlayer1[1] = cardNumberIdToMove2;
                int index3 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index3 || index2==index3){
                    index3 = rngCard.nextInt(listOfPlayableCards.size());
                }
                Card cardToPlay3 = listOfPlayableCards.get(index3);
                int cardNumberIdToMove3 = cardToPlay3.getId();
                passOfPlayer1[2] = cardNumberIdToMove3;
                player1.savePassedCards(passOfPlayer1);
                //Player 2
                int[] passOfPlayer2 = new int[3];
                listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(player2.getPlayerNo());                    
                index1 = rngCard.nextInt(listOfPlayableCards.size());
                cardToPlay1 = listOfPlayableCards.get(index1);
                cardNumberIdToMove1 = cardToPlay1.getId();
                passOfPlayer2[0] = cardNumberIdToMove1;
                index2 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index2){
                    index2 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay2 = listOfPlayableCards.get(index2);
                cardNumberIdToMove2 = cardToPlay2.getId();
                passOfPlayer2[1] = cardNumberIdToMove2;
                index3 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index3 || index2==index3){
                    index3 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay3 = listOfPlayableCards.get(index3);
                cardNumberIdToMove3 = cardToPlay3.getId();
                passOfPlayer2[2] = cardNumberIdToMove3;
                player2.savePassedCards(passOfPlayer2);
                //Player 3
                int[] passOfPlayer3 = new int[3];
                listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(player3.getPlayerNo());                    
                index1 = rngCard.nextInt(listOfPlayableCards.size());
                cardToPlay1 = listOfPlayableCards.get(index1);
                cardNumberIdToMove1 = cardToPlay1.getId();
                passOfPlayer3[0] = cardNumberIdToMove1;
                index2 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index2){
                    index2 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay2 = listOfPlayableCards.get(index2);
                cardNumberIdToMove2 = cardToPlay2.getId();
                passOfPlayer3[1] = cardNumberIdToMove2;
                index3 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index3 || index2==index3){
                    index3 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay3 = listOfPlayableCards.get(index3);
                cardNumberIdToMove3 = cardToPlay3.getId();
                passOfPlayer3[2] = cardNumberIdToMove3;
                player3.savePassedCards(passOfPlayer3);
                //Player 4
                int[] passOfPlayer4 = new int[3];
                listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(player4.getPlayerNo());                    
                index1 = rngCard.nextInt(listOfPlayableCards.size());
                cardToPlay1 = listOfPlayableCards.get(index1);
                cardNumberIdToMove1 = cardToPlay1.getId();
                passOfPlayer4[0] = cardNumberIdToMove1;
                index2 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index2){
                    index2 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay2 = listOfPlayableCards.get(index2);
                cardNumberIdToMove2 = cardToPlay2.getId();
                passOfPlayer4[1] = cardNumberIdToMove2;
                index3 = rngCard.nextInt(listOfPlayableCards.size());
                while (index1==index3 || index2==index3){
                    index3 = rngCard.nextInt(listOfPlayableCards.size());
                }
                cardToPlay3 = listOfPlayableCards.get(index3);
                cardNumberIdToMove3 = cardToPlay3.getId();
                passOfPlayer4[2] = cardNumberIdToMove3;
                player4.savePassedCards(passOfPlayer4);                                                    
                
                // Pass cards in hand to other players for each player
                for (Player player : players){
                    player.passCards(players[player.getReceiverNo()]);
                }
                                    
                gameStateForSimulation.setCardsHaveBeenPassed(true);  
                            
            }
            
            gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
                            
            // Play all the tricks. 
            // Basically, the following is taken from the SimulationMode. 
            // But since there are often logger calls in the methods, which should not be made here, this is written down again in detail and shortened by logger.
            while (!gameStateForSimulation.isRoundOver()){
                                    
                int numberOfStarter;
                
                if (gameStateForSimulation.isFirstTrick()) {
                                
                    numberOfStarter = gameStateForSimulation.getStarterOfRound().getPlayerNo();            
                    gameStateForSimulation.setPlayableCardsForEachPlayerInTrick0or1();          
                    gameStateForSimulation.initTrick(numberOfStarter);
                }
                
                else {
                    numberOfStarter = gameStateForSimulation.getStarterOfNewestTrick(); 
                    gameStateForSimulation.setPlayableCardsForStarterInTrick2orLater(gameStateForSimulation.getPlayers()[numberOfStarter]);
                }                                        
                
                //Starter plays first.                            
                List<Card> listOfPlayableCards = gameStateForSimulation.determineListOfPlayableCards(numberOfStarter);
                Random rngCard = new Random(); 
                int index = rngCard.nextInt(listOfPlayableCards.size()); 
                Card cardToPlay = listOfPlayableCards.get(index);
                int firstCardId = cardToPlay.getId();
                if (firstCardId < 1 || firstCardId > 52){
                    throw new IllegalArgumentException("Equally distributed selection incorrect for starting player. Card: " + firstCardId);
                }                            
                gameStateForSimulation.playCard(firstCardId, numberOfStarter);                 
                
                // The other three players play.
                for (int playerCount = 1; playerCount < 4; playerCount++){                        
                    int currentPlayerNumber = (numberOfStarter + playerCount) % 4;                        
                    // Set playable cards                                              
                    if (!gameStateForSimulation.isFirstTrick()) {   
                        gameStateForSimulation.setPlayableCardsForNonStarterInTrick2orLater(gameStateForSimulation.getPlayers()[currentPlayerNumber], gameStateForSimulation.getFirstCardOfNewestTrick());
                    }                     
                    List<Card> listOfPlayableCards2 = gameStateForSimulation.determineListOfPlayableCards(currentPlayerNumber);
                    Random rngCard2 = new Random(); 
                    int index2 = rngCard2.nextInt(listOfPlayableCards2.size());
                    Card cardToPlay2 = listOfPlayableCards2.get(index2);
                    int cardIdToMove = cardToPlay2.getId();
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
            gameControllerHelper.completeRound();
        }


    }

    /** 
     * Method that evaluates the end of the game in the game state "gameStateResultOfSimulation" and determines the results and returns them in a List<Double>.
     * @param gameStateResultOfSimulation The game state to evaluate the game's end from.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestMove or bestPass was called.
     * @return List<Double> The game's result that is determined at the end of the simulated game.
     */
    public List<Double> evaluateGameEnd(GameState gameStateResultOfSimulation, int rootPlayerNo){
        List<Double> resultList = null;
        boolean isPlayerWinner = false;
        int payOut = 0;
        int lossOrWin = -1;
        double normalizedPayoff = -1; //normalisierter Payoff zur besseren Vergleichbarkeit der implementierten AIs
        int winnersGamePoints = 125; //Wikipedia: "The highest score that can be achieved is 125."
        ArrayList<Player> listOfWinners = gameStateResultOfSimulation.determineListOfGameWinnerOrWinners();
        winnersGamePoints = listOfWinners.get(0).getGamePts();
        int[] arrayOfPlayersPayoffs = new int[4];

        for (int playerNo = 0; playerNo <= 3; playerNo++){
        
            for (Player winner : listOfWinners){
                if (winner.getPlayerNo() == playerNo){
                    isPlayerWinner = true;
                    break;
                }
            }
            if (isPlayerWinner == true){ //Player has won.
                if (playerNo == rootPlayerNo){
                    lossOrWin = 1;
                }                
                for (int playerNumber = 0; playerNumber <= 3; playerNumber++){
                    if (playerNumber != playerNo){
                        arrayOfPlayersPayoffs[playerNo] = arrayOfPlayersPayoffs[playerNo] 
                                + gameStateResultOfSimulation.getPlayers()[playerNumber].getGamePts() 
                                - gameStateResultOfSimulation.getPlayers()[playerNo].getGamePts();
                    }
                }
            } else{ //isPlayerWinner == false, Player has not won.
                if (playerNo == rootPlayerNo){
                    lossOrWin = 0;
                } 
                arrayOfPlayersPayoffs[playerNo] = winnersGamePoints - gameStateResultOfSimulation.getPlayers()[playerNo].getGamePts();
            }
            if (playerNo == rootPlayerNo){
                payOut = arrayOfPlayersPayoffs[playerNo];
            }

        }
                
        double maxPayoff = Double.MIN_VALUE;
        for (int playersPayoff : arrayOfPlayersPayoffs) {
            if (playersPayoff > maxPayoff) {
                maxPayoff = playersPayoff;
            }
        }
        double minPayoff = Double.MAX_VALUE;
        for (int playersPayoff : arrayOfPlayersPayoffs) {
            if (playersPayoff < minPayoff) {
                minPayoff = playersPayoff;
            }
        }
        normalizedPayoff = (payOut - minPayoff) / (maxPayoff - minPayoff);
                
        resultList = new LinkedList<Double>();
        resultList.add(Double.valueOf(Integer.valueOf(payOut)));
        resultList.add(Double.valueOf(Integer.valueOf(lossOrWin)));
        resultList.add(Double.valueOf(normalizedPayoff));

        return resultList;

    }

    /** 
     * Method that carries out the simulation phase of the MCTS algorithm for bestPass until the game's end.
     * @param expandedNode The expanded node from the expansion phase.
     * @param rootPlayerNo The root's player number, i.e. the player, for whom bestPass was called.
     * @return List<Double> The game's result that is determined at the end of the simulation of the remaining game.
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
                
        int playerNoOfCurrentPlayer = rootPlayerNo;

        for (int playerNo = 0; playerNo <= 3; playerNo++){            
            //For each player, except the root player, make the equally distributed choice of 3 of his (playable) cards and save them
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
                players[playerNo].savePassedCards(tempPass);                            
            }
        }
        
        int[] passOfCurrentPlayer = new int[3];
        passOfCurrentPlayer[0] = expandedNode.getActionPassedCards()[0].getId();
        passOfCurrentPlayer[1] = expandedNode.getActionPassedCards()[1].getId();
        passOfCurrentPlayer[2] = expandedNode.getActionPassedCards()[2].getId();
        players[playerNoOfCurrentPlayer].savePassedCards(passOfCurrentPlayer);      
                                                 
        for (Player player : players){
            player.passCards(players[player.getReceiverNo()]);
        }
                            
        gameStateForSimulation.setCardsHaveBeenPassed(true);                         
    
        gameStateForSimulation.setTricksPlayed(gameStateForSimulation.getTricksPlayed() + 1);
        int numberOfStarter = gameStateForSimulation.getStarterOfRound().getPlayerNo();                              
        gameStateForSimulation.initTrick(numberOfStarter);

        // Play/simulate all tricks
        simulateRemainingTricksUntilRoundOver(gameStateForSimulation, gameControllerHelper);

        gameControllerHelper.completeRound();                                

        if (!gameStateForSimulation.isRoundOver()){
            System.out.println("Still open round. That shouldn't be the case! Program is terminated!");
            System.exit(0);
        }

        // Simulate remaining rounds.
        simulateRemainingRoundsUntilGameOver(gameStateForSimulation, gameControllerHelper);

        if (!gameStateForSimulation.atLeastOnePlayerHas100PointsOrMore()){ 
            System.out.println("Game not over yet. That shouldn't be the case! Program is terminated!");
            System.exit(0);
        }

        // Game is over.
        gameStateResultOfSimulation = gameStateForSimulation;

        //Evaluation
        resultList = evaluateGameEnd(gameStateResultOfSimulation, rootPlayerNo);

        return resultList;

    }    
    
}
