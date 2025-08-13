package game;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JComboBox;


import GUI.GamePlayGUI;
import GUI.HeartsGameGUI;
import game.AI.AiInterface;
import game.AI.AiMCTS.AiISMCTS.MO_ISMCTS;
import game.AI.AiMCTS.AiISMCTS.SO_ISMCTS;
import game.AI.AiMCTS.AiISMCTSANN.ISMCTSANN;
import game.AI.AiMCTS.AiMCTSBasic.AiMCTSCheatingPIGameSimUcb1;
import game.AI.AiMCTS.AiMCTSBasic.AiMCTSCheatingPIGameSimUct;
import game.AI.AiMCTS.AiMCTSBasic.AiMCTSCheatingPIRoundSimUcb1;
import game.AI.AiMCTS.AiMCTSBasic.AiMCTSCheatingPIRoundSimUct;
import game.AI.AiMCTS.AiMCTSBasic.AiPIMCRoundSimUct;
import game.AI.AiRuleBased.AiRuleBased;
import game.AI.Utils.CardEvaluation;
import rules.Rules;
import game.AI.AiTypes;
import game.AI.AiCFR.AiCFRClustering;
import game.AI.AiCFR.DeepCFR;
import game.AI.AiCFR.MCCFR.AiMCCFR;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import java.awt.event.MouseEvent;

//import org.apache.commons.exec.CommandLine;
//import org.apache.commons.exec.DefaultExecutor;
//import org.apache.commons.exec.ExecuteException;

/**
 * The main class for the game: It controls the program/game flow
 * and executes all actions that occur during the game, e.g. those called by the user via the GUI.
 * There are 3 threads in both human mode and simulation mode:
 * The EDT/main thread is for interaction with the user.
 * Another thread (SwingWorker) regularly updates the GUI (components), e.g. with FPS 20/60 etc.
 * In human mode, it updates the game field ("frame") of the GamePlayGUI.
 * In simulation mode, it updates the ProgressBar.
 * A third thread (SwingWorker) performs "time-consuming" calculations without user interaction 
 * in the background and sets component attributes/properties.
 * In human mode, it performs the trick execution, for example.
 * In simulation mode, it carries out the actual simulation of the games.
 */
public class GameController {
    private Map<String, String> settingsBuffer; //Cache for the settings that are transferred to the current gameState at startup.
    private GameState gameState; //Game state, "a game"
    private HeartsGameGUI heartsGameGUI; //The HeartsGame GUI/Start screen
    private GamePlayGUI gamePlayGUI; //The GamePlay GUI
    private GamePlayGuiUpdateWorker gamePlayGuiUpdateWorker; //Thread for automatically updating the GameplayGUI, more precisely the GamePlayGUI's playing field.
    private boolean isGamePlayGuiUpdateWorkerNeededToRun;
    private int gamePlayGuiUpdateWorkerFPS; //The frames per second with which the GamePlayGUI playing field is to be updated.
    private Logger logger;
    private AiInterface ai1;
    private AiInterface ai2;    

    /**
     * Main constructor that is called from HeartsMain.java.
     */
    public GameController(GameState gameState, HeartsGameGUI heartsGameGUI, GamePlayGUI gamePlayGUI){ 
        this.gameState = gameState;
        this.heartsGameGUI = heartsGameGUI;
        this.gamePlayGUI = gamePlayGUI;
        this.gamePlayGuiUpdateWorker = null;
        this.isGamePlayGuiUpdateWorkerNeededToRun = false;
        this.settingsBuffer = new HashMap<String,String>();
        this.ai1 = new AiRuleBased();
        this.ai2 = new AiRuleBased();
        initView();       
    }

    /**
     * Constructor for the simulation mode without GUI 
     */
    public GameController(GameState gameState, AiTypes firstAi, AiTypes scndAi){
        this.gameState = gameState;
        this.ai1 = getAiClass(firstAi);
        this.ai2 = getAiClass(scndAi);
        
    }

    public void setAI1to3(AiTypes aiType) {
        this.ai1 = getAiClass(aiType);
    }

    public void setAI4(AiTypes aiType) {
        this.ai2 = getAiClass(aiType);
    }

    /**
     * Constructor for the simulation mode without GUI only with a gameState to have access to methods of GameController.
     */
    public GameController(GameState gameState){
        this.gameState = gameState;
    }

    private AiInterface getAiClass(AiTypes type){
        switch (type){
            
            case RULE_BASED:
                return new AiRuleBased();
            
            case CFR_CLUSTERING:
                return new AiCFRClustering();

            case MCCFR:
                return new AiMCCFR();

            case BASIC_MCTS_CHEATING_PI_GAME_SIM_UCB1:
                return new AiMCTSCheatingPIGameSimUcb1();

            case BASIC_MCTS_CHEATING_PI_GAME_SIM_UCT:
                return new AiMCTSCheatingPIGameSimUct();

            case BASIC_MCTS_CHEATING_PI_ROUND_SIM_UCB1:
                return new AiMCTSCheatingPIRoundSimUcb1();
            
            case Cheating_MCTS:
                return new AiMCTSCheatingPIRoundSimUct();

            case PIMC:
                return new AiPIMCRoundSimUct();

            case SO_ISMCTS:
                return new SO_ISMCTS();

            case MO_ISMCTS:
                return new MO_ISMCTS();

            case DeepCFR:
                return new DeepCFR();
                
            case ISMCTSANN:
                return new ISMCTSANN();
            // TODO weitere KI-Typen hier hinzufügen
            
            default:
                throw new IllegalArgumentException("Ungültiger KI-Typ: " + type);
        }

    }

    
    /**
     * Method to return to the start screen and reset everything.
     */
    public void backToHeartsGameGUI(){
        this.gameState = new GameState();
        this.isGamePlayGuiUpdateWorkerNeededToRun = false;
        this.gamePlayGuiUpdateWorker = null;
        this.settingsBuffer = new HashMap<String,String>();
        this.initView();
    }

    /**
     * Initializes the views to see only the start screen (HeartsGame GUI).
     */
    private void initView(){
        this.gamePlayGUI.getFrame().setVisible(false); //Hide GamePlayGUI
        this.gamePlayGUI.getPreviousTrickFrame().setVisible(false); //Hide additional frame of GamePlayGUI
        this.gamePlayGUI.getLegendFrame().setVisible(false); //Hide frame for the legend of the colored card markers
        this.gamePlayGUI.getGameFinishedFrame().setVisible(false); //Hide additional frame of GamePlayGUI
        this.gamePlayGUI.getSimulationModeFrame().setVisible(false); //Hide frame of the simulation mode
        this.heartsGameGUI.getFrame().setVisible(true); //Show start screen (HeartsGame GUI)
    }


    /**
     * Method that performs necessary actions when the start button is pressed.
     */
    private void startButtonPressed(){
        String deckSize = null;
        if (this.heartsGameGUI.getCards52RadioButton().isSelected() == true){
            deckSize = "52";                
        } else if (this.heartsGameGUI.getCards32RadioButton().isSelected() == true){
            deckSize = "32";
        }
        String gameMode = null;
        String playerName = null;
        String numberOfSimulationGames = null;
        if (this.heartsGameGUI.getSimulationRadioButton().isSelected() == true){
            gameMode = "simulation";
            playerName = "Ai";
            numberOfSimulationGames = this.heartsGameGUI.getNumberOfSimulationGamesField().getText().trim();
        } else if (heartsGameGUI.getHumanPlayerRadioButton().isSelected() == true){
            gameMode = "human";
            playerName = this.heartsGameGUI.getPlayerNameField().getText().trim();
            numberOfSimulationGames = null;
        } 
        String passOrNoPass = null;
        if (this.heartsGameGUI.getPassRadioButton().isSelected() == true){
            passOrNoPass = "pass";                
        } else if (this.heartsGameGUI.getNoPassRadioButton().isSelected() == true){
            passOrNoPass = "nopass";
        }      
        this.bufferAllSettings(playerName,
            numberOfSimulationGames,
            String.valueOf(this.heartsGameGUI.getDifficultySlider().getValue()),
            String.valueOf(this.heartsGameGUI.getMemorySlider().getValue()),
            String.valueOf(this.heartsGameGUI.getSimulationDifficultySlider().getValue()),
            String.valueOf(this.heartsGameGUI.getSimulationMemorySlider().getValue()),
            deckSize,
            gameMode,
            passOrNoPass);
        this.initNewGame(); //Initializes new game, i.e. gameState      
        this.heartsGameGUI.getFrame().setVisible(false); //Hide start screen
        //Branching according to the Simulation or Human game mode
        if (this.gameState.getGameMode() == GameMode.SIMULATION){
            //Disable buttons
            this.gamePlayGUI.getSimulationModeReturnButton().setEnabled(false);
            this.gamePlayGUI.getSimulationModeRestartButton().setEnabled(false);
            this.gamePlayGUI.getSimulationModeEndButton().setEnabled(false);//TODO: Oder true, falls/sobald Simulationsabbruch funktioniert.
            //Reset progress bar
            this.gamePlayGUI.getSimulationModeProgressBar().setValue(0);
            this.gamePlayGUI.getSimulationModeProgressBar().setString("  " + String.valueOf(0) + " % ");
            //Show simulation mode frame
            this.gamePlayGUI.getSimulationModeFrame().setVisible(true);            
            //Start simulation thread to simulate the game/games.
            new SimulationWorker().execute();

            this.gamePlayGUI.getPlayer1ResultsLabel().setText("Player1: "+"1st place: "+"0 times, "+"2nd place: "+"0 times, "+"3rd place: "+"0 times, "+"4th place: "+"0 times");
            this.gamePlayGUI.getPlayer2ResultsLabel().setText("Player2: "+"1st place: "+"0 times, "+"2nd place: "+"0 times, "+"3rd place: "+"0 times, "+"4th place: "+"0 times");
            this.gamePlayGUI.getPlayer3ResultsLabel().setText("Player3: "+"1st place: "+"0 times, "+"2nd place: "+"0 times, "+"3rd place: "+"0 times, "+"4th place: "+"0 times");
            this.gamePlayGUI.getPlayer4ResultsLabel().setText("Player4: "+"1st place: "+"0 times, "+"2nd place: "+"0 times, "+"3rd place: "+"0 times, "+"4th place: "+"0 times");

            //Enable the abort button
            this.gamePlayGUI.getSimulationModeAbortButton().setEnabled(true);
            //Update GUI
            this.gamePlayGUI.getSimulationModePanel().paintImmediately(this.gamePlayGUI.getSimulationModePanel().getBounds());
        } else if(this.gameState.getGameMode() == GameMode.HUMAN){      
            this.gamePlayGUI.getPlayer1NameLabel().setText(this.gameState.getPlayers()[0].getName() + "  ");
            this.gamePlayGUI.getPlayer1ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[0].getRoundPts()) + " (R)| ");
            //The game points must be set/reset on the GUI.
            this.gamePlayGUI.getPlayer1ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[0].getGamePts()) + " (G) ");
            this.gamePlayGUI.getPlayer2NameLabel().setText(this.gameState.getPlayers()[1].getName() + "  ");
            this.gamePlayGUI.getPlayer2ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[1].getRoundPts()) + " (R)| ");
            this.gamePlayGUI.getPlayer2ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[1].getGamePts()) + " (G) ");
            this.gamePlayGUI.getPlayer3NameLabel().setText(this.gameState.getPlayers()[2].getName() + "  ");
            this.gamePlayGUI.getPlayer3ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[2].getRoundPts()) + " (R)| ");
            this.gamePlayGUI.getPlayer3ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[2].getGamePts()) + " (G) ");
            this.gamePlayGUI.getPlayer4NameLabel().setText(this.gameState.getPlayers()[3].getName() + "  ");
            this.gamePlayGUI.getPlayer4ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[3].getRoundPts()) + " (R)| ");
            this.gamePlayGUI.getPlayer4ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[3].getGamePts()) + " (G) ");            
            this.gamePlayGUI.getPlayerNameLabel().setText(this.gameState.getHumanPlayerName()); 
            this.gamePlayGUI.getPlayer4Label().setText(this.gameState.getHumanPlayerName());             
            String pathToFile = Card.getImagePathOfBacksideCard(); //Kartenrücken
            ImageIcon cardImage = new ImageIcon(getClass().getResource(pathToFile));
            // Down scale image
            cardImage.setImage(cardImage.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH));
            this.gamePlayGUI.getCardToShowPreviousTrickLabel().setIcon(cardImage);
            this.clearCenterSlotsInGUI();
            this.gamePlayGUI.getFrame().setVisible(true); //Show GamePlayGUI.
            this.gamePlayGUI.getFrame().getContentPane().setBackground(this.gamePlayGUI.getmoosgruen());
            //Instantiate and start the worker/thread for automatically updating the GUI:
            this.gamePlayGuiUpdateWorkerFPS = (int) this.heartsGameGUI.getFramesPerSecondSpinner().getValue(); //default 20 fps
            this.gamePlayGuiUpdateWorker = new GamePlayGuiUpdateWorker(this.gamePlayGuiUpdateWorkerFPS);
            this.isGamePlayGuiUpdateWorkerNeededToRun = true;
            this.gamePlayGuiUpdateWorker.execute();            
            this.initRoundUntilPass();
            //Game has started and is controlled via events.
            //From now on, the first action that triggers an event is the passing of 3 cards by the human player.
        } else{
            System.exit(0);
        } 
    }

    /**
     * Method that terminates the program when the end button is pressed.
     */
    private void endButtonPressed(){        
        System.exit(0); 
    }
    
    private void testButtonPressed(){
        this.heartsGameGUI.getTestFrame().setVisible(true);

    }

    /**
     * Method that performs actions when the simulation radio button is pressed.
     */
    private void simulationRadioButtonPressed(){        
        this.heartsGameGUI.getPlayerNameField().setVisible(false);
        this.heartsGameGUI.getNameLabel().setVisible(false);
        this.heartsGameGUI.getNumberOfSimulationGamesField().setVisible(true);
        this.heartsGameGUI.getSimulationGamesLabel().setVisible(true);    
        this.heartsGameGUI.getSimulationDifficultySlider().setVisible(true);
        this.heartsGameGUI.getSimulationMemorySlider().setVisible(true);
        this.heartsGameGUI.getSimulationDifficultyLabel().setVisible(true);
        this.heartsGameGUI.getSimulationMemoryLabel().setVisible(true);
        this.heartsGameGUI.getFpsPanel().setVisible(false);
        this.heartsGameGUI.getAI4ComboBox().setVisible(true);
        this.heartsGameGUI.getAI4Label().setVisible(true);
    }

    /**
     * Method that performs actions when the human player radio button is pressed.
     */
    private void humanPlayerRadioButtonPressed(){        
        this.heartsGameGUI.getPlayerNameField().setVisible(true);
        this.heartsGameGUI.getNameLabel().setVisible(true);
        this.heartsGameGUI.getNumberOfSimulationGamesField().setVisible(false); 
        this.heartsGameGUI.getSimulationGamesLabel().setVisible(false); 
        this.heartsGameGUI.getSimulationDifficultySlider().setVisible(false);
        this.heartsGameGUI.getSimulationMemorySlider().setVisible(false);
        this.heartsGameGUI.getSimulationDifficultyLabel().setVisible(false);
        this.heartsGameGUI.getSimulationMemoryLabel().setVisible(false);
        this.heartsGameGUI.getFpsPanel().setVisible(true);
        this.heartsGameGUI.getAI4ComboBox().setVisible(false);
        this.heartsGameGUI.getAI4Label().setVisible(false);
    }

    /**
     * Method that performs actions when the return button is pressed.
     */
    private void returnButtonPressed() {        
        this.backToHeartsGameGUI();
    }

    /**
     * Method that performs actions when the legend button is pressed.
     */
    private void legendButtonPressed() {        
        this.gamePlayGUI.getLegendFrame().setVisible(true);        
    }

    /**
     * Method that performs actions when the game finished return button is pressed.
     */
    private void gameFinishedReturnButtonPressed() { 
        this.returnButtonPressed();
    }

    /**
     * Method that performs actions when the game finished restart button is pressed.
     */
    private void gameFinishedRestartButtonPressed() { 
        this.isGamePlayGuiUpdateWorkerNeededToRun = false;
        this.gamePlayGuiUpdateWorker = null;
        this.gamePlayGUI.getGameFinishedFrame().setVisible(false); 
        this.startButtonPressed();
    }

    /**
     * Method that performs actions when the game finished end button is pressed.
     */
    private void gameFinishedEndButtonPressed() {
        this.endButtonPressed(); 
    }

    /**
     * Method that performs actions when the simulation mode return button is pressed.
     */
    private void simulationModeReturnButtonPressed() { 
        this.gameState.setIsSimulationModeAbortButtonPressed(false);
        this.returnButtonPressed();
    }

    /**
     * Method that performs actions when the simulation mode restart button is pressed.
     */
    private void simulationModeRestartButtonPressed() { 
        this.gameState.setIsSimulationModeAbortButtonPressed(false);
        this.startButtonPressed();
    }

    /**
     * Method that performs actions when the simulation mode end button is pressed.
     */
    private void simulationModeEndButtonPressed() { 
        this.endButtonPressed(); 
    }
    
     /**
     * Method that performs actions when the simulation mode abort button is pressed. More precisely, it
     * stores in the gameState that the abort button was pressed in the simulation.
     */
    private void simulationModeAbortButtonPressed(){
        this.gameState.setIsSimulationModeAbortButtonPressed(true);
    }


    /**
     * Method for the actual (i.e. the actual playing) start of a round, 
     * which carries out the possible passing of all players and the moving of the computer players until it is the human player's turn.
     */
    private void nextActionPassingUntilHumanPlayersTurnToMove(){
        //Button is in the passing status/mode
        if ((this.gameState.getIsGameWithPassing() == true
            && this.gameState.getIsRoundWithPassing() == true 
            &&this.gamePlayGUI.getNextActionPassMoveButton().isEnabled()==true 
            && this.gameState.getCardsHaveBeenPassed()==false
            && this.gameState.getNumberOfSelectedCards() == 3) //Pass only if 3 cards have been selected.
            ||
            (
            (this.gameState.getIsGameWithPassing() == false
            || this.gameState.getIsRoundWithPassing() == false) 
            &&this.gamePlayGUI.getNextActionPassMoveButton().isEnabled()==false 
            && this.gameState.getCardsHaveBeenPassed()==false
            )){
            if (this.gameState.getIsGameWithPassing() == true
                && this.gameState.getIsRoundWithPassing() == true){ //In case you are playing with passing.
                //Disable button first.
                this.gamePlayGUI.getNextActionPassMoveButton().setEnabled(false);
                this.gamePlayGUI.getNextActionPassMoveButton().setVisible(false); 
                //passCards() passes the 3 cards for each player, updates the GUI in case of a human player
                // and sets cardsHaveBeenPassed true.
                this.passCards();
            } else if (this.gameState.getIsGameWithPassing() == false
                        || this.gameState.getIsRoundWithPassing() == false){//In case of playing without passing.
                this.gameState.setCardsHaveBeenPassed(true);   
            }            
            //Passing is over.
            //Next, the first trick is played. To do this, set the number of tricksPlayed 1
            this.gameState.setTricksPlayed(1);
            //Determine first player.
            Player starterOfRound = this.gameState.getStarterOfRound();
            //Set the playable cards for each player
            this.gameState.setPlayableCardsForEachPlayerInTrick0or1();            
            //Update the GUI to the playable cards of the human player (enable/disable/mark) and, if necessary, mark the cards received by passing.
            this.prepareGUIWithHumanPlayableCards(this.gameState.getPlayers()[3].getHandCardsPlayable().getIsInHand()); //OKAY
            //Initialize first trick
            this.gameState.initTrick(starterOfRound.getPlayerNo());
            //Next, play the cards until it is the human player's turn
            // If the first player is a computer, then prepare the GUI up to the human, i.e. play cards from the previous computer player. If human is first, then easier.
            //If the first player is a computer:
            if (this.gameState.getGameMode() == GameMode.HUMAN && starterOfRound.getPlayerNo() != 3){
                //Play from the starter of the round, i.e. the 1st trick, until it is the human player's turn.
                for (int playerNumber = starterOfRound.getPlayerNo(); playerNumber < 3; playerNumber++){
                    //Determine the computer card to be played.
                    int cardNumberIdToMove = ai1.bestMove(gameState, playerNumber);
                    //Play this card: Remove this card from the player's hand and add it to the trick.
                    this.gameState.playCard(cardNumberIdToMove, playerNumber);                    
                    //Place this card on the GUI in this player's center slot
                    this.addCardToCenterSlotInGUI(cardNumberIdToMove, playerNumber, this.gameState.getStarterOfNewestTrick());
                }
                //The human player's turn to play a card (move, not pass)                
                this.gameState.setHumanPlayersTurnToMoveCard(true); //Statt Klicken auf Move-Button erfolgt die Event-Auslösung jetzt über Klicken auf eine Karte.
                // Any computers playing first have played their cards, 
                // these lie in the middle of the GUI, 
                // next it is the turn of the human player,
                // whose button and cards have been enabled.                
                // Now it is the human player's turn.
                // Next, the button is pressed and the event is handled again.
            } else if (this.gameState.getGameMode() == GameMode.HUMAN && starterOfRound.getPlayerNo() == 3){
                //The human player's turn to play a card (move, not pass)
                this.gameState.setHumanPlayersTurnToMoveCard(true); 
            } else {} 
        //The program is ready ("waits") for the player's next action.
        }
        else{} 
        //The program is ready ("waits") for the player's next action:
        //  Before the end of the game: Passing/Moving, i.e. clicking on one of his cards.
        //  At the end of the game: Clicking the restart, return or end button.
    }

    /**
     * Method that carries out the playing of tricks, starting from the human player, 
     * who calls this method by clicking/pressing a card until it is the human player's turn again.
     * The method also handles end of round and end of game.
     */
    private void nextActionHumanMoveCardPressed(){
        //Game is in Move state (i.e. to throw/play a card into the trick, i.e. this.gameState.isHumanPlayersTurnToMoveCard == true) 
        // and a playable card has been clicked on/pressed by the human player.
        if (this.gameState.getIsHumanPlayersTurnToMoveCard()==true 
                && this.gameState.getCardsHaveBeenPassed()==true
                && this.gameState.getNumberOfSelectedCards() == 1){ //Play card only if 1 card has been selected.
            //It is the turn of the human player, who has selected a card to move (Press). This is played in the following.
            // So that he cannot press a card again in the meantime, the following variable is set to false, i.e. he is blocked until it is his turn again and cannot press another card until then.
            this.gameState.setHumanPlayersTurnToMoveCard(false);           
            //Determine the card selected by the human player via the GUI as card number 1-52
            Player player4 = this.gameState.getPlayers()[3];
            int cardNumberIdToMove = 0; //1-52
            int cardSlotNo = 0;
            if (gameState.getGameMode() == GameMode.HUMAN){
                for (int i = 1; i<= this.gameState.getIsSelectedCardInGUI().length; i++){ 
                    cardSlotNo++;
                    if (this.gameState.getIsSelectedCardInGUI()[i-1] == true){
                        cardNumberIdToMove = this.gameState.getCardNumbersInGUI()[i-1];
                        break; 
                    }
                }
            } else {}                   
            //Remove this card from the player's hand and add it to the trick
            this.gameState.playCard(cardNumberIdToMove, 3);
            //Hide this card in its upper slot
            ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
            //Place this card on the GUI in this player's center slot
            this.addCardToCenterSlotInGUI(cardNumberIdToMove, 3, this.gameState.getStarterOfNewestTrick());
            //The human player has played his card up to this point.
            //Let any remaining computer players play this trick.
            for (int playerNumber = 0; playerNumber < this.gameState.getStarterOfNewestTrick(); playerNumber++){
                //In case of 2nd trick or later, i.e., tricksPlayed >=2, and that the human player was the starter of this trick,
                //the playable cards of the computers must still be determined and set:
                if (this.gameState.getTricksPlayed() >= 2 && this.gameState.getStarterOfNewestTrick() == 3){
                    this.gameState.setPlayableCardsForNonStarterInTrick2orLater(this.gameState.getPlayers()[playerNumber], this.gameState.getFirstCardOfNewestTrick());
                }
                //Determine the computer card to be played.
                cardNumberIdToMove = ai1.bestMove(gameState, playerNumber); 
                //Remove this card from the player's hand and add it to the trick
                this.gameState.playCard(cardNumberIdToMove, playerNumber);
                //Place this card on the GUI in this player's center slot
                this.addCardToCenterSlotInGUI(cardNumberIdToMove, playerNumber, this.gameState.getStarterOfNewestTrick());
            }
            //Trick evaluation:
            // - Then evaluate the trick:
            // - If heart has not been broken/played in current round, but was played in this trick, then set isHeartBroken true.
            updateHeartsBroken();
            // - Then credit the points of the trick to the winner's round account. (not yet to the game account!) 
            // - Then complete the trick: Determine the winner, count the points of the trick, credit them to the winner's round account and return the winner.
            Player currentTrickWinner = this.completeTrick(); 
            // - Save the player number (PlayerNo) of the trick winner so that it can be found as the starter for the next trick.
            int currentTrickWinnerPlayerNo = currentTrickWinner.getPlayerNo();
            // - Then mark the winner and add a time delay so that the winner is visible to the human player.
            this.markTrickWinnerInCenterSlotInGUI(currentTrickWinnerPlayerNo);
            // - And update the round account on the GUI for this player (only he gets points for this trick).
            this.updateRoundScoresInGUI(currentTrickWinnerPlayerNo);
            //Short timeout so that the human player can take another look at the trick and the winner.
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
            //Trick evaluation completed. The rest of the game follows.
            // - Then, - if tricksPlayed < 13 or 8, depending on deck size,
            if (this.gameState.isRoundOver() == false) {            
                //First set the frame of the previous trick visible false so that the human player cannot leave it open in the background,
                // as he is only allowed to see the last trick.
                this.gamePlayGUI.getPreviousTrickFrame().setVisible(false);
                //Play again up to the human player etc., whereby the trick starter is determined first and must be set in the new trick instance (there in starter via constructor).
                //First "clean up"/prepare the GUI: Empty the CenterSlots (center of the table) and rebuild the human player cards/hand from the left (so that there are no gaps)
                this.clearCenterSlotsInGUI();
                this.setHumanPlayerCardsInGUI(this.gameState.getPlayers()[3].getHandCards().getIsInHand()); //Die verbleibenden Karten des Human-Players in der GUI anzeigen.                       
                //Next, the next trick is played. To do this, increase the number of tricksPlayed by 1
                this.gameState.setTricksPlayed(this.gameState.getTricksPlayed()+1);
                //Determine the first player of the new trick: The starter is the winner of the last trick.
                Player starterOfTrick = currentTrickWinner; //siehe oben
                //Initialize the new trick:
                this.gameState.initTrick(starterOfTrick.getPlayerNo());
                //Determine and set the playable cards for the starter.
                this.gameState.setPlayableCardsForStarterInTrick2orLater(starterOfTrick);                
                //If the starter is a computer:
                if (this.gameState.getGameMode() == GameMode.HUMAN && starterOfTrick.getPlayerNo() != 3){
                    //Determine the computer card to be played.
                    cardNumberIdToMove = ai1.bestMove(gameState, starterOfTrick.getPlayerNo()); 
                    //Remove this card from the player/starter's hand and add it to the trick
                    this.gameState.playCard(cardNumberIdToMove, starterOfTrick.getPlayerNo());
                    //Place this card on the GUI in this player's center slot
                    this.addCardToCenterSlotInGUI(cardNumberIdToMove, starterOfTrick.getPlayerNo(), this.gameState.getStarterOfNewestTrick());            
                    //Determine the playable cards of all other players:
                    for (int playerNumber = 0; playerNumber <= 3; playerNumber++){
                        if (playerNumber != starterOfTrick.getPlayerNo()){ //not for the starter                         
                            this.gameState.setPlayableCardsForNonStarterInTrick2orLater(this.gameState.getPlayers()[playerNumber], cardNumberIdToMove);                            
                        }
                    }                                          
                    //Update the GUI to the playable cards of the human player (enable/disable/mark):
                    this.prepareGUIWithHumanPlayableCards(this.gameState.getPlayers()[3].getHandCardsPlayable().getIsInHand());
                    //Now let the computers play until it is the human player's turn, if there are any in between.
                    for (int playerNumber = starterOfTrick.getPlayerNo()+1; playerNumber < 3; playerNumber++){
                        //Determine the computer card to be played.
                        cardNumberIdToMove = ai1.bestMove(gameState, playerNumber);
                        //Remove this card from the player/starter's hand and add it to the trick
                        this.gameState.playCard(cardNumberIdToMove, playerNumber);
                        //Place this card on the GUI in this player's center slot
                        this.addCardToCenterSlotInGUI(cardNumberIdToMove, playerNumber, this.gameState.getStarterOfNewestTrick());
                    }
                    //The human player's turn:
                    this.gameState.setHumanPlayersTurnToMoveCard(true);
                    //Now it is the human player's turn. From here, you "wait" for a card to be clicked/pressed.
                } else{ //The starter is the human player
                    //The playable cards have already been determined and set for the starter. See above if.
                    //The GUI still needs to be updated to the playable cards of the human player (enablen/disablen/mark):
                    this.prepareGUIWithHumanPlayableCards(this.gameState.getPlayers()[3].getHandCardsPlayable().getIsInHand());
                    this.gameState.setHumanPlayersTurnToMoveCard(true);
                }
            //Another trick is played until it is the human player's turn to click/press his card, 
            //  either after one or more computer players or as a starter.          
            } else { //so tricksPlayed == 13 or 8, depending on deck size
                //End the round and credit all round points to the respective game accounts.
                this.completeRound();
                //Update the game accounts/game points scores in the GUI.
                this.updateGameScoresInGUIForAllPlayers();
                //Short timeout so that the human player can take another look at the trick and the winner.
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } 
                // - if at least one player has 100 points or more:
                if (this.gameState.atLeastOnePlayerHas100PointsOrMore()){ //Game over
                    //End the game and display the ranking + restart/return/end buttons
                    this.updateGameFinishedFrame();
                    //Display the game finished frame.
                    this.gamePlayGUI.getGameFinishedFrame().setVisible(true);
                    //If a new game is started or the player returns to the start screen, the data of the 
                    // winner display need not necessarily be reset, as the player will not be able to see the frame until the next end of the game at the earliest. 
                } else{ //Game not over yet -> Start next round.
                    // - Initiate/initialize next round until passing                                
                    this.initRoundUntilPass();
                }
            }             
        } 
        else{} 
        //The program is ready ("waits") for the player's next action:
        //  Before the end of the game: Passing/Moving, i.e. clicking on one of his cards.
        //  At the end of the game: Clicking the restart, return or end button.
    }

    public void setHeartsGameGui(HeartsGameGUI heartsGameGUI) {
        this.heartsGameGUI = heartsGameGUI;
    }

    public void setGamePlayGUI(GamePlayGUI gamePlayGUI) {
        this.gamePlayGUI = gamePlayGUI;
    }
    
    /**
     * Method for initializing a new game
     */
    private void initNewGame(){
        this.gameState.init();
        //Difficulty
        if (settingsBuffer.get("difficulty").equals("0")) {
            this.gameState.setDifficulty(Difficulty.EASY);
        } else if (settingsBuffer.get("difficulty").equals("1")) {
            this.gameState.setDifficulty(Difficulty.MEDIUM);
        } else if (settingsBuffer.get("difficulty").equals("2")) {
            this.gameState.setDifficulty(Difficulty.HARD);
        } else if (settingsBuffer.get("difficulty").equals("3")) {
            this.gameState.setDifficulty(Difficulty.EXPERT);
        }
        // Memory
        if (settingsBuffer.get("memory").equals("0")) {
            this.gameState.setMemory(Memory.LOW);
        } else if (settingsBuffer.get("memory").equals("1")) {
            this.gameState.setMemory(Memory.NORMAL);
        } else if (settingsBuffer.get("memory").equals("2")) {
            this.gameState.setMemory(Memory.HIGH);
        } else if (settingsBuffer.get("memory").equals("3")) {
            this.gameState.setMemory(Memory.SUPER);
        }
        //simulation or human:
        if (settingsBuffer.get("gameMode").equals("human")) {        
            //GameMode
            this.gameState.setGameMode(GameMode.HUMAN);//WICHTIG
            //Set the human player name
            this.gameState.setHumanPlayer(settingsBuffer.get("humanPlayerName"));
        } else if (settingsBuffer.get("gameMode").equals("simulation")) {
            //GameMode
            this.gameState.setGameMode(GameMode.SIMULATION);
            if (!gameState.getIsSimulationModeRunning()){ // only set numberOfSimulationGames if not already in the middle of simulation
                //Set number of games to be simulated: numberOfSimulationGames
                try {           
                    this.gameState.setNumberOfSimulationGames(Integer.parseInt(settingsBuffer.get("numberOfSimulationGames")));
                }
                catch (NumberFormatException e) {
                    this.gameState.setNumberOfSimulationGames(0);
                }            
            }            
            //Difficulty of the 4th AI player
            if (settingsBuffer.get("simDifficulty").equals("0")) {
                this.gameState.setDifficulty4thAi(Difficulty.EASY);
            } else if (settingsBuffer.get("simDifficulty").equals("1")) {
                this.gameState.setDifficulty4thAi(Difficulty.MEDIUM);
            } else if (settingsBuffer.get("simDifficulty").equals("2")) {
                this.gameState.setDifficulty4thAi(Difficulty.HARD);
            } else if (settingsBuffer.get("simDifficulty").equals("3")) {
                this.gameState.setDifficulty4thAi(Difficulty.EXPERT);
            }
            //Memory of the 4th AI player
            if (settingsBuffer.get("simMemory").equals("0")) {
                this.gameState.setMemory4thAi(Memory.LOW);
            } else if (settingsBuffer.get("simMemory").equals("1")) {
                this.gameState.setMemory4thAi(Memory.NORMAL);
            } else if (settingsBuffer.get("simMemory").equals("2")) {
                this.gameState.setMemory4thAi(Memory.HIGH);
            } else if (settingsBuffer.get("simMemory").equals("3")) {
                this.gameState.setMemory4thAi(Memory.SUPER);
            }          
            //Initialize the 4th player as AI
            this.gameState.setSimulationAI();       
        }
        //Deck Size
        if (settingsBuffer.get("deckSize").equals("52")) {
            this.gameState.createDeck(true);
            if (settingsBuffer.get("gameMode").equals("human")) { 
                this.gameState.setIsSelectedCardInGUI(new boolean[13]); 
                this.gameState.setCardNumbersInGUI(new int[13]);
                for (int index = 1; index<=this.gameState.getCardNumbersInGUI().length; index++){
                    this.gameState.getCardNumbersInGUI()[index-1] = -1;
                }
            }
        } else if (settingsBuffer.get("deckSize").equals("32")) {
            this.gameState.createDeck(false);
            if (settingsBuffer.get("gameMode").equals("human")) { 
                this.gameState.setIsSelectedCardInGUI(new boolean[13]); 
                this.gameState.setCardNumbersInGUI(new int[13]); 
                for (int index = 1; index<=this.gameState.getCardNumbersInGUI().length; index++){
                    this.gameState.getCardNumbersInGUI()[index-1] = -1; 
                }
            }
        }
        // Pass or No Pass
        if (settingsBuffer.get("passOrNoPass").equals("pass")) {
            this.gameState.setIsGameWithPassing(true);
        } else if (settingsBuffer.get("passOrNoPass").equals("nopass")) {
            this.gameState.setIsGameWithPassing(false);
        }
        if (!this.gameState.getIsSimulationModeRunning()) this.printAllSettingsInGameState();
        //The new "game" has been initialized up to this point.
    }

    /**
     * Method that updates the simulation progress bar's value.
     * @param numberOfSimulatedGames The number of already simulated games.
     * @param totalNumberOfGamesToSimulate The total number of games to simulate.
     */
    private void updateSimulationProgress(int numberOfSimulatedGames, int totalNumberOfGamesToSimulate){
        float progress = (((float) numberOfSimulatedGames)/((float) totalNumberOfGamesToSimulate))*100;
        this.gamePlayGUI.getSimulationModeProgressBar().setValue((int) progress);               
        this.gamePlayGUI.getSimulationModeProgressBar().setString("  " + String.valueOf(this.gamePlayGUI.getSimulationModeProgressBar().getValue()) + " % ");
    }

    /** 
     * Method that starts the simulation mode.
     * 
     * @param numberOfGamesToSimulate The number of games to simulate.
     * @param dataMode Either "data" or "readable", determines how the data is logged and exported to the csv.
     * @param viewPlayerNo The number of the players, whos view shall be logged (influences the logged cards of the current trick), -1 to logg the whole trick.
     */
    public void startSimulationModeGame(int numberOfGamesToSimulate, String dataMode, int viewPlayerNo){

        gameState.setNumberOfSimulationGames(numberOfGamesToSimulate);
        
        logger = new Logger(numberOfGamesToSimulate, gameState.getDeckSize(), dataMode, viewPlayerNo);

        initSimulalationMode();
        
        int numberOfSimulatedGames = 0;
        
        // one game
        while (numberOfSimulatedGames < numberOfGamesToSimulate 
                && !gameState.getIsSimulationModeAbortButtonPressed()){             
            
            if (gamePlayGUI != null) initNewGame();
            else initNewSimulationGameWithoutGUI(numberOfGamesToSimulate);
        
            logger.initNewGame();
            // one round
            while (!gameState.atLeastOnePlayerHas100PointsOrMore() && !gameState.getIsSimulationModeAbortButtonPressed()) {

                initSimulationRoundUntilFirstCard();
                // one trick
                while (!gameState.isRoundOver() && !gameState.getIsSimulationModeAbortButtonPressed()){
                    
                    logger.logHandCards(gameState.getPlayers());
                    if (viewPlayerNo != -1){
                        logger.logAvailableCards(CardEvaluation.calculateAvailableCards(gameState.getCardsPlayedInThisRound(), gameState.getPlayers()[viewPlayerNo].getHandCards()));
                    }
                    logger.logCardsPlayedInThisRound(gameState.getCardsPlayedInThisRound());
                    
                    starterPlaysCardInSimulationMode();
                    logger.logNumberOfTricksPlayed(gameState.getTricksPlayed());
                    
                    for (int playerCount = 1; playerCount < 4; playerCount++){ 
                        nonStarterPlaysCardInSimulationMode(gameState.getStarterOfNewestTrick(), playerCount);
                    }
                      
                    completeTrickInSimulation();                      
                }
                completeRoundInSimulation();
            }
            
            logger.logGameStatistic(gameState.getPlayers());

            numberOfSimulatedGames++; 

            // update progress bar, if simulation with GUI
            if (gamePlayGUI != null) updateSimulationProgress(numberOfSimulatedGames, numberOfGamesToSimulate);  

            // complete simulation
            if (numberOfSimulatedGames == numberOfGamesToSimulate){
                gameState.setIsSimulationModeRunning(false);
                logger.exportData("passDataOfSimulation.csv", "playDataOfSimulation.csv");   
            }
        }
    }

    /**
     * Method to initialize the simulation mode. 
     */
    private void initSimulalationMode(){
        gameState.setIsSimulationModeRunning(true);
        if (gamePlayGUI != null) updateSimulationProgress(0, gameState.getNumberOfSimulationGames());
    }

    /**
     * Method that inits game if simulation mode is started outside GUI. 
     * GameState must be initiated before, memory and difficulty set and deck created.
     * @param numberOfGamesToSimulate The number of games to simulate.
     */
    private void initNewSimulationGameWithoutGUI(int numberOfGamesToSimulate){
        // get last settings
        Difficulty diff = gameState.getDifficulty();
        Memory mem = gameState.getMemory();
        Difficulty diff4 = gameState.getDifficulty4thAi();
        Memory mem4 = gameState.getMemory4thAi();
        boolean isStandardDeck = (gameState.getDeckSize() == 52);
        boolean isGameWithPassing = gameState.getIsGameWithPassing();

        this.gameState.init();

        gameState.createDeck(isStandardDeck);
        gameState.setDifficulty(diff);
        gameState.setMemory(mem);
        gameState.setDifficulty4thAi(diff4);
        gameState.setMemory4thAi(mem4);
        gameState.setIsGameWithPassing(isGameWithPassing);

        this.gameState.setGameMode(GameMode.SIMULATION);
        if (!gameState.getIsSimulationModeRunning()) this.gameState.setNumberOfSimulationGames(numberOfGamesToSimulate);
        this.gameState.setSimulationAI();
    }

    /** 
     * Method to initialize a simulated round until the first card to play. 
     */
    public void initSimulationRoundUntilFirstCard(){
        
        initRoundUntilPass();
        logger.logHandCardsBeforePassing(gameState.getPlayers()); 
        
        int player1ReceiverNo = Rules.getPassReceiverNo(0, gameState.getRoundNo());
        
        if (gameState.getIsGameWithPassing() && (player1ReceiverNo != 0)) {
            
            passCards();
            logger.logPassReceiverNo(player1ReceiverNo);
        }
        
        else logger.handleNoPass();
              
        gameState.setTricksPlayed(gameState.getTricksPlayed() + 1);
    }

    /**
     * Method that plays the first card in a new trick.
     */
    public void starterPlaysCardInSimulationMode() throws IllegalArgumentException{
        
        int numberOfStarter;
                    
        if (gameState.isFirstTrick()) {
                        
            numberOfStarter = gameState.getStarterOfRound().getPlayerNo();            
            gameState.setPlayableCardsForEachPlayerInTrick0or1();          
            gameState.initTrick(numberOfStarter);
        }
        
        else {
            numberOfStarter = gameState.getStarterOfNewestTrick(); 
            gameState.setPlayableCardsForStarterInTrick2orLater(gameState.getPlayers()[numberOfStarter]);
        }
        
        logger.logHandCardsPlayable(gameState.getPlayers()[numberOfStarter]); 
        
        int firstCardId;
        if (numberOfStarter == 3) firstCardId = ai2.bestMove(gameState, numberOfStarter);
        else firstCardId = ai1.bestMove(gameState, numberOfStarter);
        
        if (firstCardId < 1 || firstCardId > 52){
            throw new IllegalArgumentException("Wrong value of bestMove() for starting player: " + firstCardId);
        }
                
        gameState.playCard(firstCardId, numberOfStarter);
        
    }

    /**
     * Method to play a card, if the player, which is not the starting player, plays a card in simulation mode.
     * @param numberOfStarter The number of the starter.
     * @param playerCount Counting number for players.
     */
    private void nonStarterPlaysCardInSimulationMode(int numberOfStarter, int playerCount) throws IllegalArgumentException{
        int currentPlayerNumber = (numberOfStarter + playerCount) % 4;
                        
        // set playable cards 
        if (!gameState.isFirstTrick()) gameState.setPlayableCardsForNonStarterInTrick2orLater(gameState.getPlayers()[currentPlayerNumber], gameState.getFirstCardOfNewestTrick());
        logger.logHandCardsPlayable(gameState.getPlayers()[currentPlayerNumber]); 

        int cardIdToMove;
        if (currentPlayerNumber == 3) cardIdToMove = ai2.bestMove(gameState, currentPlayerNumber);
        else cardIdToMove = ai1.bestMove(gameState, currentPlayerNumber); 
                        
        if (cardIdToMove < 1 || cardIdToMove > 52){     
            throw new IllegalArgumentException("Invalid value of bestMove(): " + cardIdToMove);
        }    
        
        gameState.playCard(cardIdToMove, currentPlayerNumber); 
    }

    /**
     * Method that completes a trick in simulation mode: determine winner and log data.
     */
    private void completeTrickInSimulation(){
        
        Player trickWinner = completeTrick();
        int numberOfTrickWinner = trickWinner.getPlayerNo();
                    
        updateHeartsBroken();
                    
        logger.logTrickPlayed(gameState.getNewestTrick(), gameState.getStarterOfNewestTrick());
        logger.logRoundPoints(gameState.getPlayers());
        
                    
        if (!gameState.isRoundOver()){
            logger.logGamePoints(gameState.getPlayers());
            logger.logPayoff(gameState.getPlayers());
            gameState.setTricksPlayed(gameState.getTricksPlayed() + 1);
            gameState.initTrick(numberOfTrickWinner);
        } 
    }

    /**
     * Method that completes a round in simulation mode and logs the data. 
     */
    private void completeRoundInSimulation(){
        completeRound();
        
        if (gameState.getIsMoonShot()){
            int playerNo = -1;
            for (Player player : gameState.getPlayers()){
                //player who shot the moon has 26 or 21 roundpoints 
                int shootTheMoonPoints = (gameState.getDeckSize() == 52) ? 26 : 21;
                if (player.getRoundPts() == shootTheMoonPoints) {
                    playerNo = player.getPlayerNo();
                    break;
                }  
            }
            logger.logShotTheMoon(playerNo);
        }
        
        logger.logGamePoints(gameState.getPlayers());
        logger.logPayoff(gameState.getPlayers());
        logger.logRoundAndGameNumber();
    }

    /**
     * Method that prints the game statistics after completing a simulation. 
     * Prints how often which player reached which position and what was the average and median placing. 
     */
    private void printGameStatisticAfterSimulation(){

        System.out.println("");
        System.out.println("Simulation abgeschlossen. Die Spieler haben wie folgt abgeschnitten: ");
        for (int i = 0; i < 4; i++){
            
            System.out.println("Spieler " + i + " :");
            
            int first = logger.getNumberOfWins()[i];
            int scnd = logger.getNumber2ndPlaces()[i];
            int third = logger.getNumberOf3rdPlaces()[i];
            int fourth = logger.getNumberOf4thPlaces()[i];
            System.out.println(first + " Mal auf dem 1. Platz.");
            System.out.println(scnd + " Mal auf dem 2. Platz.");
            System.out.println(third + " Mal auf dem 3. Platz.");
            System.out.println(fourth + " Mal auf dem 4. Platz.");

            double average = ((double) (first + scnd*2 + third*3 + fourth*4) / logger.getNumberOfGames());
            System.out.println("Durchschnittliche Platzierung: " + (Math.round(average * 10.0) / 10.0));

            int median = 1;
            int count = first;
            if (scnd > count){
                median = 2;
            }
            else if (third > count){
                median = 3;
            }
            else if (fourth > count){
                median = 4;
            }
            System.out.println("Meistens auf dem " + median + ". Platz.");
            System.out.println("Shoot the moon erzielt: " + logger.getNumberOfShootTheMoons()[i] + " Mal.");
            System.out.println("");
        }
    }

    /**
     * Method to show game statistics after simulation has been completed.
     */
    private void showGameStatisticInGUIAfterSimulation(){

        int[] wins = logger.getNumberOfWins();
        int[] scnd = logger.getNumber2ndPlaces();
        int[] third = logger.getNumberOf3rdPlaces();
        int[] fourth = logger.getNumberOf4thPlaces();
        double[] avgGamePts = logger.getAvgGamePts();
        double[] avgGamePtsPayoff = logger.getAvgGamePtsPayoff();
        long[] totalGamePts = logger.getTotalGamePts();
        long[] totalGamePtsPayoff = logger.getTotalGamePtsPayoff();

        this.gamePlayGUI.getPlayer1ResultsLabel().setText("Player1: "+"1st place: "+ wins[0] + " times, "+"2nd place: "+ scnd[0] +" times, "+ "3rd place: "+ third[0] + " times, "+"4th place: "+ fourth[0] + " times, "
            +"Avg. Game Points: "+ String.format("%.2f",avgGamePts[0]) + " (Total: " + totalGamePts[0] + ")" + " , "+"Avg. Game Points Payoff: "+ String.format("%.2f",avgGamePtsPayoff[0]) + " (Total: " + totalGamePtsPayoff[0] + ")");
        this.gamePlayGUI.getPlayer2ResultsLabel().setText("Player2: "+"1st place: "+ wins[1] +" times, "+"2nd place: "+ scnd[1] + " times, "+ "3rd place: "+ third[1] + " times, "+"4th place: "+ fourth[1] + " times, "
            +"Avg. Game Points: "+ String.format("%.2f",avgGamePts[1]) + " (Total: " + totalGamePts[1] + ")" + " , "+"Avg. Game Points Payoff: "+ String.format("%.2f",avgGamePtsPayoff[1]) + " (Total: " + totalGamePtsPayoff[1] + ")");
        this.gamePlayGUI.getPlayer3ResultsLabel().setText("Player3: "+"1st place: "+ wins[2] +" times, "+"2nd place: "+ scnd[2] + " times, "+ "3rd place: "+ third[2] + " times, "+"4th place: "+ fourth[2] + " times, "
            +"Avg. Game Points: "+ String.format("%.2f",avgGamePts[2]) + " (Total: " + totalGamePts[2] + ")" + " , "+"Avg. Game Points Payoff: "+ String.format("%.2f",avgGamePtsPayoff[2]) + " (Total: " + totalGamePtsPayoff[2] + ")");
        this.gamePlayGUI.getPlayer4ResultsLabel().setText("Player4: "+"1st place: "+ wins[3] +" times, "+"2nd place: "+ scnd[3] + " times, "+ "3rd place: "+ third[3] + " times, "+"4th place: "+ fourth[3] + " times, "
            +"Avg. Game Points: "+ String.format("%.2f",avgGamePts[3]) + " (Total: " + totalGamePts[3] + ")" + " , "+"Avg. Game Points Payoff: "+ String.format("%.2f",avgGamePtsPayoff[3]) + " (Total: " + totalGamePtsPayoff[3] + ")");
    }

    /**
     * Method to initialize a new round until passing, which can then be started by the human player.
     */
    private void initRoundUntilPass(){
        //General initialization independent of the game mode
        this.gameState.addRoundTricksToGameRoundTricks(gameState.createAndSetNewRoundTricks());
        this.gameState.setTricksPlayed(0); 
        this.gameState.resetCardsPlayedInThisRound();
        this.gameState.resetRoundPoints(); 
        this.gameState.setIsHeartBroken(false); 
        this.gameState.setCardsHaveBeenPassed(false); 
        this.gameState.resetCardsPassed(); 
        this.gameState.resetHandCardsOfPlayers(); 
        this.gameState.setRoundNo(this.gameState.getRoundNo()+1);
        this.gameState.shuffleDeck();
        this.gameState.handOutCards();

        //Further inits for human mode
        if (gameState.getGameMode() == GameMode.HUMAN){
            this.gameState.setHumanPlayersTurnToMoveCard(false);
            this.clearCenterSlotsInGUI();
            this.gamePlayGUI.getPreviousTrickFrame().setVisible(false); 
            this.updateRoundScoresInGUIForAllPlayers(); 
            this.gamePlayGUI.getNextActionPassMoveButton().setEnabled(false); 
            this.gamePlayGUI.getNextActionPassMoveButton().paintImmediately(this.gamePlayGUI.getNextActionPassMoveButton().getBounds());
            this.setHumanPlayerCardsInGUI(this.gameState.getPlayers()[3].getHandCards().getIsInHand()); 
            //this.printOutAllHandCardsForEachPlayer();    
        }

        //In a game with passing, passing is omitted in every 4th round.
        if (this.gameState.getIsGameWithPassing() == true){
            if (this.gameState.getRoundNo()%4 == 0){
                this.gameState.setRoundWithPassing(false);
            } else{
                this.gameState.setRoundWithPassing(true);
            }
        }
                                 
        if (this.gameState.getIsGameWithPassing() == true
            && this.gameState.getIsRoundWithPassing() == true){
            this.gameState.setPlayableCardsForEachPlayerInTrick0or1();    
            if (gameState.getGameMode() == GameMode.HUMAN){
                this.prepareGUIWithHumanPlayableCards(this.gameState.getPlayers()[3].getHandCardsPlayable().getIsInHand());                
                this.gamePlayGUI.getNextActionPassMoveButton().setText("Pass these cards (3)");
                this.gamePlayGUI.getNextActionPassMoveButton().setVisible(true);
                this.gamePlayGUI.getNextActionPassMoveButton().setEnabled(true);  
                this.gamePlayGUI.getNextActionPassMoveButton().paintImmediately(this.gamePlayGUI.getNextActionPassMoveButton().getBounds());    
            }
            
        } else if (this.gameState.getIsGameWithPassing() == false
                    || this.gameState.getIsRoundWithPassing() == false){ 
            if (this.gameState.getGameMode() == GameMode.HUMAN){
                this.gamePlayGUI.getNextActionPassMoveButton().setVisible(false); 
                this.gamePlayGUI.getNextActionPassMoveButton().setEnabled(false);
                this.gamePlayGUI.getNextActionPassMoveButton().paintImmediately(this.gamePlayGUI.getNextActionPassMoveButton().getBounds());
                this.nextActionPassingUntilHumanPlayersTurnToMove();
            }
        }   
        //Next: Passing by clicking on pass button.
    }

    /**
     * Method that checks if hearts is already broken and, 
     * if false and hearts is played in the current trick, sets isHeartBroken to true.
     */
    public void updateHeartsBroken(){
        if (this.gameState.getIsHeartBroken() == false){
                for (int cardPlayed : this.gameState.getNewestTrick().getCardsPlayed()){
                    if (cardPlayed >= 14 && cardPlayed <= 26){
                        this.gameState.setIsHeartBroken(true);
                    }
                }
            }
    }

    /**
     * Method that updates the game finished frame.
     */
    private void updateGameFinishedFrame(){
        for (int playerNo = 0; playerNo<=3; playerNo++){
            //Set name and game points.
            if (playerNo == 0){
                this.gamePlayGUI.getPlayer1NameFinishedLabel().setText(this.gameState.getPlayers()[playerNo].getName() + "  "); 
                this.gamePlayGUI.getPlayer1ScoreGameFinishedLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) "); 
            } else if (playerNo == 1){
                this.gamePlayGUI.getPlayer2NameFinishedLabel().setText(this.gameState.getPlayers()[playerNo].getName() + "  "); 
                this.gamePlayGUI.getPlayer2ScoreGameFinishedLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) "); 
            } else if (playerNo == 2){
                this.gamePlayGUI.getPlayer3NameFinishedLabel().setText(this.gameState.getPlayers()[playerNo].getName() + "  "); 
                this.gamePlayGUI.getPlayer3ScoreGameFinishedLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");
            } else if (playerNo == 3){
                this.gamePlayGUI.getPlayer4NameFinishedLabel().setText(this.gameState.getPlayers()[playerNo].getName() + "  "); 
                this.gamePlayGUI.getPlayer4ScoreGameFinishedLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");
            } else {} 
        }
        //Setting the winner or winners!: Name and game points
        //Determine the name or names and the game points
        String gameWinnerNameOrGameWinnersNames = "";
        int winnersGamePoints = 125; //Wikipedia: "The highest score that can be achieved is 125."
        for (Player player : this.gameState.determineListOfGameWinnerOrWinners()){
            gameWinnerNameOrGameWinnersNames = gameWinnerNameOrGameWinnersNames + "    Player " + String.valueOf(player.getPlayerNo()+1) + ": " + player.getName();
            winnersGamePoints = player.getGamePts();
        };
        gameWinnerNameOrGameWinnersNames = gameWinnerNameOrGameWinnersNames.trim();
        this.gamePlayGUI.getGameWinnerNameFinishedLabel().setText(gameWinnerNameOrGameWinnersNames);
        this.gamePlayGUI.getGameWinnerScoreGameFinishedLabel().setText(String.valueOf(winnersGamePoints));
    }

    /**
     * Method that clears the Game Play GUI's center slots from cards.
     */
    private void clearCenterSlotsInGUI(){
        //Player 0
        ((JLabel) this.gamePlayGUI.getLeftCenterCardPanel().getComponent(0)).setIcon(null);
        ((JLabel) this.gamePlayGUI.getLeftCenterCardPanel().getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        //Player 1
        ((JLabel) this.gamePlayGUI.getTopCenterCardPanel().getComponent(0)).setIcon(null);
        ((JLabel) this.gamePlayGUI.getTopCenterCardPanel().getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        //Player 2
        ((JLabel) this.gamePlayGUI.getRightCenterCardPanel().getComponent(0)).setIcon(null);
        ((JLabel) this.gamePlayGUI.getRightCenterCardPanel().getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        //Player 3 (human player)
        ((JLabel) this.gamePlayGUI.getBottomCenterCardPanel().getComponent(0)).setIcon(null);        
        ((JLabel) this.gamePlayGUI.getBottomCenterCardPanel().getComponent(0)).setBorder(BorderFactory.createEmptyBorder());        
    }

    /**
     * Method that adds a played card to a Game Play GUI's center slot.
     * @param cardNumber The number of the played card.
     * @param playerNo The number of the player who played the card.
     * @param startingPlayerNo The number of the player who started the current trick.
     */
    private void addCardToCenterSlotInGUI(int cardNumber, int playerNo, int startingPlayerNo){        
        JLabel centerSlotLabel = null;
        if (playerNo == 0){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getLeftCenterCardPanel().getComponent(0);
        } else if (playerNo == 1){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getTopCenterCardPanel().getComponent(0);
        } else if (playerNo == 2){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getRightCenterCardPanel().getComponent(0);
        } else if (playerNo == 3){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getBottomCenterCardPanel().getComponent(0);
        } else {} 
        String pathToFile = (new Card(cardNumber)).getImagePath();
        ImageIcon cardImage = new ImageIcon(getClass().getResource(pathToFile));
        cardImage.setImage(cardImage.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH));
        centerSlotLabel.setIcon(cardImage);
        if (playerNo == startingPlayerNo){
            centerSlotLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(222, 255, 0)));
        }
        //A small time delay to make the computers appear natural.        
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }
        

    /**
     * Method that marks the trick winner in the center of the table.
     * @param trickWinner The winner of the trick.
     */
    private void markTrickWinnerInCenterSlotInGUI(int trickWinner){
        JLabel centerSlotLabel = null;
        if (trickWinner == 0){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getLeftCenterCardPanel().getComponent(0);
        } else if (trickWinner == 1){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getTopCenterCardPanel().getComponent(0);
        } else if (trickWinner == 2){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getRightCenterCardPanel().getComponent(0);
        } else if (trickWinner == 3){
            centerSlotLabel = (JLabel) this.gamePlayGUI.getBottomCenterCardPanel().getComponent(0);
        } else {} 
        centerSlotLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(0, 0, 255))); //blue
        try { 
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that updates the round points on the GUI for all players.
     */
    private void updateRoundScoresInGUIForAllPlayers(){
        for (int playerNo = 0; playerNo<=3; playerNo++){
            this.updateRoundScoresInGUI(playerNo);
        }
    }

    /**
     * Method that updates the round points on the GUI for a player.
     * @param playerNo The number of the player whose round points (score) will be updated in the GUI.
     */
    private void updateRoundScoresInGUI(int playerNo){
        if (playerNo == 0){
            this.gamePlayGUI.getPlayer1ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getRoundPts()) + " (R)| ");            
        } else if (playerNo == 1){
            this.gamePlayGUI.getPlayer2ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getRoundPts()) + " (R)| ");             
        } else if (playerNo == 2){
            this.gamePlayGUI.getPlayer3ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getRoundPts()) + " (R)| ");            
        } else if (playerNo == 3){
            this.gamePlayGUI.getPlayer4ScoreLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getRoundPts()) + " (R)| ");            
        } else {}         
    }

    /**
     * Method that updates the game points on the GUI for all players.
     */
    private void updateGameScoresInGUIForAllPlayers(){
        for (int playerNo = 0; playerNo<=3; playerNo++){
            if (playerNo == 0){
                this.gamePlayGUI.getPlayer1ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");                 
            } else if (playerNo == 1){
                this.gamePlayGUI.getPlayer2ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");                 
            } else if (playerNo == 2){
                this.gamePlayGUI.getPlayer3ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");                
            } else if (playerNo == 3){
                this.gamePlayGUI.getPlayer4ScoreGameLabel().setText(String.valueOf(this.gameState.getPlayers()[playerNo].getGamePts()) + " (G) ");                
            } else {}             
        }
    }
    
    /**
     * Method for displaying the cards isInHand of the human player on the GUI in the corresponding slots.
     * @param isInHand The array of cards in hand of the human player.
     */
    private void setHumanPlayerCardsInGUI(boolean[] isInHand){
        int cardSlotNo = 0;
        JPanel currentPlayerHandPanel = null; //lower
        JLabel currentCardLabel = null; //lower
        JPanel currentPlayerHandPanelSelectedCards = null; //upper
        JLabel currentCardLabelSelected = null; //upper
        String pathToFile = "";
        for (int index = 1; index<=this.gameState.getCardNumbersInGUI().length; index++){
            this.gameState.getCardNumbersInGUI()[index-1] = -1; //Reset array of size 13 (13 slots) to "-1"
        }
        //Empty/reset all card slots 
        for (int i = 1; i<= this.gamePlayGUI.getPlayerHandPanel().getComponents().length; i++){ 
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(i-1)).getComponent(0)).setIcon(null);        
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(i-1)).getComponent(0)).setIcon(null);
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(i-1)).getComponent(0)).setVisible(false);
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(i-1)).getComponent(0)).setVisible(false);
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(i-1)).getComponent(0)).setBorder(BorderFactory.createLineBorder(new Color(51, 102, 0)));                 
        }
        //Refill the card slots -> from left to right, so that with each trick there is one card less in the hand, 
        // but all the cards in the hand are left-aligned, so there are no gaps.
        for (int cardNumber = 1; cardNumber<=isInHand.length; cardNumber++){
            if (isInHand[cardNumber-1]==true){
                cardSlotNo++;
                pathToFile = (new Card(cardNumber)).getImagePath();
                ImageIcon cardImage = new ImageIcon(getClass().getResource(pathToFile));
                cardImage.setImage(cardImage.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH));
                currentPlayerHandPanel = (JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1); 
                currentCardLabel = (JLabel) currentPlayerHandPanel.getComponent(0);
                currentCardLabel.setIcon(cardImage);
                currentCardLabel.setVisible(true);
                if (this.gameState.getCardsHaveBeenPassed() == false){ 
                    currentPlayerHandPanelSelectedCards = (JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1); //obere
                    currentCardLabelSelected = (JLabel) currentPlayerHandPanelSelectedCards.getComponent(0); 
                    currentCardLabelSelected.setIcon(cardImage);                    
                    currentCardLabelSelected.setVisible(false);                    
                }
                //Update the gameState so that the GUI is up-to-date, i.e. the slot now contains the corresponding card number:
                this.gameState.getCardNumbersInGUI()[cardSlotNo-1] = cardNumber;                                        
            }
        }    
        //For the sake of completeness, set the remaining slots on the right to "disable"/visible false so that no event can be fired there in the first place.
        for (int i = cardSlotNo+1; i <= this.gamePlayGUI.getPlayerHandPanel().getComponents().length; i++){             
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(i-1)).getComponent(0)).setVisible(false);
            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(i-1)).getComponent(0)).setVisible(false);
        }   
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int index = 1; index <= this.gameState.getIsSelectedCardInGUI().length; index++){
            this.gameState.getIsSelectedCardInGUI()[index-1] = false; 
        }        
        this.gameState.setNumberOfSelectedCards(0); 
    }

    
    /**
     * Method that customizes the GUI so that the human player can only select/click on playable cards.
     * @param isInHandPlayable The array of cards in hand of the human player that are playable only.
     */
    private void prepareGUIWithHumanPlayableCards(boolean[] isInHandPlayable){
        int[] cardNumbersReceivedFromPassing = new int[3];
        if (this.gameState.getIsGameWithPassing()
            && this.gameState.getIsRoundWithPassing() 
            && this.gameState.getCardsHaveBeenPassed() 
            && (this.gameState.getTricksPlayed() == 1)){
            for (int i = 0; i < 3; i++){
                cardNumbersReceivedFromPassing[i] = this.gameState.getPlayers()[3].getCardsReceivedFromPassing().get(i);
            }            
        }                        
        for (int cardSlotNo = 1; cardSlotNo <= this.gameState.getCardNumbersInGUI().length; cardSlotNo++){
            int cardSlotNoHelper = cardSlotNo; 
            //If the slot contains a card, the array getCardNumbersInGUI() contains the CardNumber (1-52). If not, there is a -1.
            //So check whether the slot contains a card. Only process slots with cards: Possibly enable, possibly gray out
            if (this.gameState.getCardNumbersInGUI()[cardSlotNo-1] != -1){ 
                //Check whether the contained card may be played.
                if (isInHandPlayable[this.gameState.getCardNumbersInGUI()[cardSlotNo-1]-1] == true){ 
                    if (this.gameState.getIsGameWithPassing()
                        && this.gameState.getIsRoundWithPassing()
                        && this.gameState.getCardsHaveBeenPassed() 
                        && (this.gameState.getTricksPlayed() == 1)
                        && (IntStream.of(cardNumbersReceivedFromPassing).anyMatch(x -> x == this.gameState.getCardNumbersInGUI()[cardSlotNoHelper-1]))){                            
                            ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0))
                            .setBorder(BorderFactory.createMatteBorder(0, 0, 5, 0, Color.ORANGE));                               
                    } else{                         
                    }
                } else{
                    if (this.gameState.getIsGameWithPassing()
                        && this.gameState.getIsRoundWithPassing()
                        && this.gameState.getCardsHaveBeenPassed() 
                        && (this.gameState.getTricksPlayed() == 1)
                        && (IntStream.of(cardNumbersReceivedFromPassing).anyMatch(x -> x == this.gameState.getCardNumbersInGUI()[cardSlotNoHelper-1]))){
                        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0))
                            .setBorder(new CompoundBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(255, 127, 127)),
                                                            BorderFactory.createMatteBorder(0, 0, 5, 0, Color.ORANGE)));                    
                    } else{
                        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(255, 127, 127)));                    
                    }
                }
            }
        }          
    }

    /**
     * Method that passes three cards for every player in this game.
     */
    public void passCards(){  
    
        Player[] players = gameState.getPlayers(); 
        Player player1 = players[0]; 
        Player player2 = players[1]; 
        Player player3 = players[2]; 
        Player player4 = players[3];
        
        //Determine and save the receiver of each player's cards.
        int roundNo = gameState.getRoundNo();
        int player1ReceiverNo = Rules.getPassReceiverNo(player1.getPlayerNo(), roundNo);
        player1.setReceiverNo(player1ReceiverNo);
        int player2ReceiverNo = Rules.getPassReceiverNo(player2.getPlayerNo(), roundNo);
        player2.setReceiverNo(player2ReceiverNo);
        int player3ReceiverNo = Rules.getPassReceiverNo(player3.getPlayerNo(), roundNo);
        player3.setReceiverNo(player3ReceiverNo);
        int player4ReceiverNo = Rules.getPassReceiverNo(player4.getPlayerNo(), roundNo);
        player4.setReceiverNo(player4ReceiverNo);

        //Determine and save bestPass() for each player
        int[] passOfPlayer1 = ai1.bestPass(gameState, player1.getPlayerNo(), player1ReceiverNo);
        player1.savePassedCards(passOfPlayer1);
        int[] passOfPlayer2 = ai1.bestPass(gameState, player2.getPlayerNo(), player2ReceiverNo);
        player2.savePassedCards(passOfPlayer2);
        int[] passOfPlayer3 = ai1.bestPass(gameState, player3.getPlayerNo(), player3ReceiverNo);
        player3.savePassedCards(passOfPlayer3);

        //Log cards in simulation mode and determine bestPass for player 4
        int[] passOfPlayer4 = new int[3];
        if (gameState.getGameMode() == GameMode.SIMULATION){

            passOfPlayer4 = ai2.bestPass(gameState, player4.getPlayerNo(), player4ReceiverNo); 
            player4.savePassedCards(passOfPlayer4);
            
            int[][] passOfPlayers = {passOfPlayer1, passOfPlayer2, passOfPlayer3, passOfPlayer4};
            logger.logCardsPassed(passOfPlayers);
        }

        else if (gameState.getGameMode() == GameMode.HUMAN){
            int ithCardToPass = 0;
            for (int i = 1; i<= this.gameState.getIsSelectedCardInGUI().length; i++){
                if (this.gameState.getIsSelectedCardInGUI()[i-1] == true){
                    ithCardToPass++;
                    passOfPlayer4[ithCardToPass-1] = this.gameState.getCardNumbersInGUI()[i-1];
                }
            }
            player4.savePassedCards(passOfPlayer4);

        } else {}       
        
        // Pass cards in hand to other players for each player
        for (Player player : players){
            player.passCards(players[player.getReceiverNo()]);
        }
        
        if (gameState.getGameMode() == GameMode.HUMAN){
            //Deposit the cards that the human player has received, i.e. that have been passed on to him, as such with him (important for marking).
            for (Player player : players){
                if (player.getReceiverNo() == 3){
                    player4.setCardsReceivedFromPassing(player.getCardsToPass());
                    break;
                }                
            }
            this.setHumanPlayerCardsInGUI(players[3].getHandCards().getIsInHand());                    
        }       
        this.gameState.setCardsHaveBeenPassed(true);  
                    
    }

    /**
     * Method to print out all hand cards for each player.
     */
    public void printOutAllHandCardsForEachPlayer(){
        System.out.println();
        System.out.println("Player 0 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[0].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        }
        System.out.println();
        System.out.println("Player 0 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[0].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        }

        System.out.println();
        System.out.println("Player 1 getHandCards: "); 
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[1].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        }     
        System.out.println();
        System.out.println("Player 1 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[1].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        }  

        System.out.println();
        System.out.println("Player 2 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[2].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        } 
        System.out.println();
        System.out.println("Player 2 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[2].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        } 

        System.out.println();
        System.out.println("Player 3 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[3].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        }    
        System.out.println();
        System.out.println("Player 3 getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[3].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        }        
        System.out.println();
    }

    /**
     * Method to print out all hand cards of a player.
     * @param player The player whose hand cards will be printed out.
     */
    public void printOutAllHandCardsOfPlayer(Player player){
        System.out.println();
        System.out.println("Player (playerNo " + player.getPlayerNo() + ") getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[player.getPlayerNo()].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        }
        System.out.println();
        System.out.println("Player (playerNo " + player.getPlayerNo() + ") getHandCards: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[player.getPlayerNo()].getHandCards().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        }
        System.out.println();
    }

    /**
     * Method to print out all hand cards of a player that are playable only.
     * @param player The player whose playable hand cards will be printed out.
     */
    public void printOutAllHandCardsPlayableOfPlayer(Player player){
        System.out.println();
        System.out.println("Player (playerNo " + player.getPlayerNo() + ") getHandCardsPlayable: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[player.getPlayerNo()].getHandCardsPlayable().getIsInHand()[i-1]==true){
                System.out.print(card.getCardName() + ", ");
            }
        }
        System.out.println();
        System.out.println("Player (playerNo " + player.getPlayerNo() + ") getHandCardsPlayable: ");
        for (int i = 1; i<=52; i++) {
            Card card = new Card(i);
            if (this.gameState.getPlayers()[player.getPlayerNo()].getHandCardsPlayable().getIsInHand()[i-1]==true){
                System.out.print(card.getId() + ", ");
            }
        }
        System.out.println();
    }  
    

    /**
     * Method that caches or buffers the settings.
     * @param humanPlayerName The name of the human player.
     * @param numberOfSimulationGames The number of games to simulate.
     * @param difficulty The difficulty level picked by the user.
     * @param memory The memory capacity picked by the user.
     * @param simDifficulty The simulation difficulty level picked by the user, i.e. for the 4th computer player.
     * @param simMemory  The simulation memory capacity picked by the user, i.e. for the 4th computer player.
     * @param deckSize The deck size picked by the user.
     * @param gameMode The game mode picked by the user: simulation mode or human player mode.
     * @param passOrNoPass The mode of passing or no passing picked by the user.
     */
    private void bufferAllSettings(String humanPlayerName, String numberOfSimulationGames, String difficulty, String memory, 
                                    String simDifficulty, String simMemory, String deckSize, String gameMode, String passOrNoPass){
        this.settingsBuffer.put("numberOfSimulationGames", numberOfSimulationGames);
        this.settingsBuffer.put("humanPlayerName", humanPlayerName);
        this.settingsBuffer.put("difficulty", difficulty);
        this.settingsBuffer.put("memory", memory);
        this.settingsBuffer.put("deckSize", deckSize);
        this.settingsBuffer.put("gameMode", gameMode);
        this.settingsBuffer.put("passOrNoPass", passOrNoPass);
        if (gameMode.equals("simulation")){
            this.settingsBuffer.put("simDifficulty", simDifficulty);
            this.settingsBuffer.put("simMemory", simMemory);
        }
    }

        
    /**
     * Method to print out all settings.
     */
    private void printAllSettingsInGameState(){
        System.out.println("Name: " + this.gameState.getHumanPlayerName());

        System.out.println("numberOfSimulationGames: " + this.gameState.getNumberOfSimulationGames());

        System.out.println("Memory: " + this.gameState.getMemory());
        System.out.println("Difficulty:" + this.gameState.getDifficulty());

        if (this.gameState.getGameMode() == GameMode.SIMULATION == true){
            System.out.println("Memory4thAi: " + this.gameState.getMemory4thAi());
            System.out.println("Difficulty4thAi:" + this.gameState.getDifficulty4thAi());
            System.out.println("Simulation Mode Running: "+ gameState.getIsSimulationModeRunning());
        }

        System.out.println("Deck Size: " + this.gameState.getDeckSize());        
        System.out.println("Game Mode: " + this.gameState.getGameMode());
        
        System.out.println("Is Game With Passing: " + String.valueOf(this.gameState.getIsGameWithPassing()));
    }

    
    /**
     * Method to complete the trick: 
     * Determine the winner, count the points of the trick, credit them to the winner's round account 
     * and return the winner.
     * @return Player The winner of the trick is returned.
     */
    public Player completeTrick(){
        Player trickWinner = gameState.determineTrickWinner(); 
        this.gameState.updateRoundPoints(trickWinner);
        return trickWinner;
    }

    /**
     * Method that complete the round: Update game points and payoff.
     */
    public void completeRound(){
        this.gameState.updateGamePoints();
        this.gameState.updatePayoff();
    }

    
    /**
     * Method to get the last trick with the 4 card numbers and starter and winner in this order.
     */
    public int[] getPreviousTrickInfo(){
        int[] previousTrickInfo = new int[6];
        if (this.gameState.getTricksPlayed() >= 2){
            int previousTrickNumber = this.gameState.getTricksPlayed() - 1;
            for (int i = 0; i <= 3; i++){
                previousTrickInfo[i] = this.gameState.getRoundTricks()[previousTrickNumber-1].getCardsPlayed()[i];
            }            
            Trick previousTrick = this.gameState.getRoundTricks()[previousTrickNumber-1];
            previousTrickInfo[4] = previousTrick.getStarter(); //Starter
            previousTrickInfo[5] = previousTrick.getWinner(); //Winner
            this.gamePlayGUI.getPlayer4Label().setText(this.gameState.getHumanPlayerName());
        } else{ //Return the card Id of the card back 4 times.
            for (int i = 0; i <= 3; i++){
                previousTrickInfo[i] = -1;
                this.gamePlayGUI.getPlayer4Label().setText(this.gameState.getHumanPlayerName());
            }
            previousTrickInfo[4] = -1; //Starter
            previousTrickInfo[5] = -1; //Winner
        }
        return previousTrickInfo;
    }

    public Logger getLogger(){
        return this.logger;
    }

    /**
     * Method that checks whether the human player is allowed to select/click on/press a card in a lower slot 
     * in the GUI and then move it upwards.
     * @param cardSlotNo The card slot number which is pressed on.
     */
    private boolean isClickOnSlotOfLowerPanelPermitted(int cardSlotNo){        
        return 
            (
                (gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == false) //Not yet selected, i.e. card only visible at the bottom.
                && 
                (
                    (gameState.getCardNumbersInGUI()[cardSlotNo-1] >= 1) //There is a card 1-52 in the slot. This also covers deck size 32.
                    && 
                    (gameState.getPlayers()[3].getHandCardsPlayable().getIsInHand()[gameState.getCardNumbersInGUI()[cardSlotNo-1]-1] == true) //And the card in the slot can be played at all by the human player according to the rules of the game
                )
                && 
                (
                    (gameState.getCardsHaveBeenPassed() == false && gameState.getNumberOfSelectedCards()<3) //Pass: 3 cards selectable possible
                    || 
                    (gameState.getCardsHaveBeenPassed() == true && this.gameState.getIsHumanPlayersTurnToMoveCard() && gameState.getNumberOfSelectedCards()<1) //Possible to select only 1 card for tricks
                )
            );                    
    }

    
    /**
     * Inner class for executing moves (method nextActionHumanMoveCardPressed()) when clicking/pressing on a card label, i.e. a card to be played.
     * This keeps the event dispatch thread (EDT, the calling "main" thread) available for further GUI inputs/actions/interactions.
     */
    class TrickWorker extends SwingWorker<Integer, Integer>
    {
        boolean isWorkerDone = false;

        protected Integer doInBackground() throws Exception
        {
            Thread.sleep(300);
            nextActionHumanMoveCardPressed();
            isWorkerDone = true;
            
            Thread.sleep(10);            

            return 42;
        }

        protected void done()
        {
            try
            {                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Inner class for running the simulation including updating the progress bar.
     * This keeps the event dispatch thread (EDT, the calling "main" thread) available for further GUI inputs/actions/interactions.
     */
    class SimulationWorker extends SwingWorker<Integer, Integer>
    {
        boolean isWorkerDone = false;

        protected Integer doInBackground() throws Exception
        {
            Thread.sleep(300);
            
            //Thread for updating the progress bar
            Thread updateProgressBarInGuiThread = new Thread("updateProgressBarInGuiThread"){
                @Override
                public void run(){

                    while (!isWorkerDone){
                        System.out.println("SimulationWorker still ongoing: " + gamePlayGUI.getSimulationModeProgressBar().getValue() + " %");
                        //GUI update, i.e. buttons and especially the progress bar.
                        // Otherwise the GUI of the progress bar will not be updated.
                        gamePlayGUI.getSimulationModePanel().paintImmediately(gamePlayGUI.getSimulationModePanel().getBounds());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }                
                    showGameStatisticInGUIAfterSimulation();
                    System.out.println(this.getName() + " ended.");               
                }
            };
            updateProgressBarInGuiThread.start();       
            
            try {
                // dataMode "readable" to get a csv with less information, viewPlayerNo = -1 to logg all cards played in a trick
                startSimulationModeGame(gameState.getNumberOfSimulationGames(), "data", 3);
            }
            catch (IllegalArgumentException e){
                System.out.println("Invalid parameter values in simulation mode: ");
                e.printStackTrace();
                isWorkerDone = true;
            }
            catch (IndexOutOfBoundsException e){
                e.printStackTrace();
                isWorkerDone = true;
            }
            isWorkerDone = true;
                    
            //After the end of the simulation, enable the buttons again.
            gamePlayGUI.getSimulationModeReturnButton().setEnabled(true);  
            gamePlayGUI.getSimulationModeRestartButton().setEnabled(true);
            gamePlayGUI.getSimulationModeEndButton().setEnabled(true);
            //Update progress bar, but do not change the value.
            gamePlayGUI.getSimulationModePanel().paintImmediately(gamePlayGUI.getSimulationModePanel().getBounds());
            Thread.sleep(100);

            return 42;
        }

        protected void done()
        {
            try
            {
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Inner class for automatically updating the GUI, more precisely the game field ("frame") of the GamePlayGUI.
     * This keeps the event dispatch thread (EDT, the calling "main" thread) available for further GUI inputs/actions/interactions.
     */
    class GamePlayGuiUpdateWorker extends SwingWorker<Integer, Integer>
    {
        int fps;
        int sleepingTimeInMillis;

        GamePlayGuiUpdateWorker(int framesPerSecondFPS){
            super();
            this.fps = framesPerSecondFPS;
            this.sleepingTimeInMillis = 1000 / framesPerSecondFPS; //Note: Integer division, at FPS 60 this results in 1000/60 = 16 (.66... is truncated)
        }


        protected Integer doInBackground() throws Exception
        {
            Thread.sleep(300);
            System.out.println("GamePlayGuiUpdateWorker started with FPS: " + this.fps + " (sleepingTimeInMillis: " + this.sleepingTimeInMillis + ")");            

            while (isGamePlayGuiUpdateWorkerNeededToRun){
                //The GUI components to be updated
                gamePlayGUI.getPlayerHandPanel().paintImmediately(gamePlayGUI.getPlayerHandPanel().getBounds());
                gamePlayGUI.getPlayerHandPanelSelectedCards().paintImmediately(gamePlayGUI.getPlayerHandPanelSelectedCards().getBounds());
                gamePlayGUI.getCenterContainer().paintImmediately(gamePlayGUI.getCenterContainer().getBounds());                
                gamePlayGUI.getMainPanel().paintImmediately(gamePlayGUI.getMainPanel().getBounds());
                //A pause
                Thread.sleep(sleepingTimeInMillis);
            }               

            return 42;
        }

        protected void done()
        {
            try
            {
                System.out.println("GamePlayGuiUpdateWorker ended");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    

    /**
     * Method that initializes the program's game controller.
     */
    public void initController(){       
        //HeartsGameGUI (start screen)
        //ActionListener for start game button
        this.heartsGameGUI.getStartButton().addActionListener(e -> this.startButtonPressed());
        //ActionListener for end button
        this.heartsGameGUI.getEndButton().addActionListener(e -> this.endButtonPressed());
        //ActionListener for test environment
        this.heartsGameGUI.getTestButton().addActionListener (e -> this.testButtonPressed());
        
        //ActionListener for AI1To3ComboBox
        this.heartsGameGUI.getAI1To3ComboBox().addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<AiTypes> cb = (JComboBox<AiTypes>) e.getSource();
                AiTypes selectedType = (AiTypes) cb.getSelectedItem();
                GameController.this.setAI1to3(selectedType); // Verwenden Sie hier die Instanz `gameController`
                }
        });

        //ActionListener für AI2to4ComboBox
        this.heartsGameGUI.getAI4ComboBox().addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<AiTypes> cb = (JComboBox<AiTypes>) e.getSource();
                AiTypes selectedType = (AiTypes) cb.getSelectedItem();
                GameController.this.setAI4(selectedType); // Verwenden Sie hier die Instanz `gameController`
                }
        });
        //ActionListener for simulation radio button
        this.heartsGameGUI.getSimulationRadioButton().addActionListener(e -> this.simulationRadioButtonPressed());
        //ActionListener for human player radio button
        this.heartsGameGUI.getHumanPlayerRadioButton().addActionListener(e -> this.humanPlayerRadioButtonPressed());

        //GamePlayGUI: Frame to display the winners at game's end.
        //Button for returning to the start screen
        this.gamePlayGUI.getGameFinishedReturnButton().addActionListener(e -> this.gameFinishedReturnButtonPressed());
        //Button for starting a new game with the same settings
        this.gamePlayGUI.getGameFinishedRestartButton().addActionListener(e -> this.gameFinishedRestartButtonPressed());
        //Button to terminate the program
        this.gamePlayGUI.getGameFinishedEndButton().addActionListener(e -> this.gameFinishedEndButtonPressed());        

        //GamePlayGUI: Frame for the simulation mode
        //Button for returning to the start screen
        this.gamePlayGUI.getSimulationModeReturnButton().addActionListener(e -> this.simulationModeReturnButtonPressed());
        //Button for starting a new game with the same settings
        this.gamePlayGUI.getSimulationModeRestartButton().addActionListener(e -> this.simulationModeRestartButtonPressed());
        //Button to terminate the program
        this.gamePlayGUI.getSimulationModeEndButton().addActionListener(e -> this.simulationModeEndButtonPressed());
        //Button to abort the simulation
        this.gamePlayGUI.getSimulationModeAbortButton().addActionListener(e -> this.simulationModeAbortButtonPressed());

        //GamePlayGUI: The frame of the playing surface/table
        //Return button (back to the start screen)
        this.gamePlayGUI.getReturnButton().addActionListener(e -> this.returnButtonPressed());
        //nextActionPassMoveButton for passing or moving cards or a card.
        this.gamePlayGUI.getNextActionPassMoveButton().addActionListener(e -> this.nextActionPassingUntilHumanPlayersTurnToMove());
        //cardToShowPreviousTrickLabel to show the previous trick
        this.gamePlayGUI.getCardToShowPreviousTrickLabel().addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int[] previousTrick = getPreviousTrickInfo();
                JPanel currentPreviousTrickPanel;
                JLabel currentCardLabel;
                String pathToFile;
                //Fill the frame: Create card images etc., place in the 4 slots etc. and set visible
                for (int i = 0; i<=3; i++){
                        currentPreviousTrickPanel = (JPanel) gamePlayGUI.getPreviousTrickFrameCenterPanel().getComponent(i);
                        currentCardLabel = (JLabel) currentPreviousTrickPanel.getComponent(0);
                        if (previousTrick[i] == -1){ 
                            pathToFile = Card.getImagePathOfBacksideCard();
                        } else{
                            pathToFile = (new Card(previousTrick[i])).getImagePath();
                        }
                        ImageIcon cardImage = new ImageIcon(getClass().getResource(pathToFile));
                        cardImage.setImage(cardImage.getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH));
                        currentCardLabel.setIcon(cardImage);             
                        currentCardLabel.setBorder(BorderFactory.createEmptyBorder());
                }
                //Mark starter (yellow)
                if (previousTrick[4] != -1){                
                    JPanel starterPanel = (JPanel) gamePlayGUI.getPreviousTrickFrameCenterPanel().getComponent(previousTrick[4]);
                    JLabel starterLabel = (JLabel) starterPanel.getComponent(0);
                    starterLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(222, 255, 0)));
                }
                //Mark winner (blue)
                if (previousTrick[5] != -1){                
                    JPanel winnerPanel = (JPanel) gamePlayGUI.getPreviousTrickFrameCenterPanel().getComponent(previousTrick[5]);
                    JLabel winnerLabel = (JLabel) winnerPanel.getComponent(0);
                    winnerLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(0, 0, 255)));
                }
                gamePlayGUI.getPreviousTrickFrame().setVisible(true);
            }
        });

        //GamePlayGUI: Frame PreviousTrickFrame, which contains a button legendButton to display the meaning of the colored markings of cards
        //Button legendButton
        this.gamePlayGUI.getLegendButton().addActionListener(e -> this.legendButtonPressed());


        // Add MouseListener for all JLabel/Cardslots on the playerHandPanel and playerHandPanelSelectedCards.
        // Lower slots
        // 1
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(0)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 1;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        //Hide card first
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                                
            }
        });

        // 2
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(1)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 2;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ 
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                                
            }
        });

        // 3
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(2)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 3;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                                
            }
        });

        // 4
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(3)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 4;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                                
            }
        });

        // 5
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(4)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 5;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                                
            }
        });

        // 6
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(5)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 6;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 7
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(6)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 7;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 8
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(7)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 8;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 9
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(8)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 9;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 10
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(9)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 10;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 11
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(10)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 11;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });
        
        // 12
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(11)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 12;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });

        // 13
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanel().getComponent(12)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 13;
                if (isClickOnSlotOfLowerPanelPermitted(cardSlotNo)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = true;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()+1);
                    if (gameState.getCardsHaveBeenPassed()==false){ //Status Passing
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    } else { // Status Moving
                        ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                        new TrickWorker().execute();
                    }                    
                }                                
            }
        });


        // Upper slots
        // 1
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(0)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 1;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 2
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(1)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 2;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);                    
                }                               
            }
        });        

        // 3
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(2)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 3;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 4
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(3)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 4;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 5
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(4)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 5;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 6
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(5)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 6;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 7
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(6)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 7;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 8
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(7)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 8;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 9
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(8)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 9;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                               
            }
        });        

        // 10
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(9)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 10;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 11
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(10)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 11;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                                
            }
        });        

        // 12
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(11)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 12;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                               
            }
        });        

        // 13
        ((JLabel) ((JPanel) this.gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(12)).getComponent(0)).addMouseListener(
            new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int cardSlotNo = 13;
                if ((gameState.getIsSelectedCardInGUI()[cardSlotNo-1] == true)){
                    gameState.getIsSelectedCardInGUI()[cardSlotNo-1] = false;
                    gameState.setNumberOfSelectedCards(gameState.getNumberOfSelectedCards()-1);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanel().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(true);
                    ((JLabel) ((JPanel) gamePlayGUI.getPlayerHandPanelSelectedCards().getComponent(cardSlotNo-1)).getComponent(0)).setVisible(false);
                }                              
            }
        });        
       
    }
    

}
