package GUI;
import javax.swing.*;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

import java.awt.*;
public class GamePlayGUI {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel lowerPanel;
    private JPanel playerHandPanel;
    private JPanel playerHandPanelSelectedCards;
    private JPanel nextActionPassMove;
    private JLabel cardToShowPreviousTrickLabel;
    private JFrame previousTrickFrame;
    private JPanel previousTrickFrameCenterPanel;
    private JLabel previousTrickCard1Label;
    private JLabel previousTrickCard2Label;
    private JLabel previousTrickCard3Label;
    private JLabel previousTrickCard4Label;
    private JPanel placeholder;
    private JPanel centerContainer;
    private JPanel centerPanel;
    private JLabel playerNameLabel;
    private JButton returnButton;
    private JPanel scoresPanel;
    private JLabel player1NameLabel;
    private JLabel player1ScoreLabel;
    private JLabel player1ScoreGameLabel;
    private JLabel player2NameLabel;
    private JLabel player2ScoreLabel;
    private JLabel player2ScoreGameLabel;
    private JLabel player3NameLabel;
    private JLabel player3ScoreLabel;
    private JLabel player3ScoreGameLabel;
    private JLabel player4NameLabel;
    private JLabel player4ScoreLabel;
    private JLabel player4ScoreGameLabel;
    private JFrame gameFinishedFrame;
    private JButton gameFinishedReturnButton;
    private JButton gameFinishedRestartButton;
    private JButton gameFinishedEndButton;
    private JPanel gameFinishedPanelUpperPanel;
    private JPanel gameFinishedPanelCenterPanel;
    private JPanel gameFinishedPanelLowerPanel;
    private JLabel player1NameFinishedLabel;
    private JLabel player1ScoreGameFinishedLabel;
    private JLabel player2NameFinishedLabel;
    private JLabel player2ScoreGameFinishedLabel;
    private JLabel player3NameFinishedLabel;
    private JLabel player3ScoreGameFinishedLabel;
    private JLabel player4NameFinishedLabel;
    private JLabel player4ScoreGameFinishedLabel;
    private JLabel gameWinnerNameFinishedLabel;
    private JLabel gameWinnerAnotherFinishedLabel;
    private JLabel gameWinnerScoreGameFinishedLabel;
    private JFrame simulationModeFrame;
    private JPanel simulationModePanel;
    private JPanel simulationModePanelCenterPanel;
    private JButton simulationModeReturnButton;
    private JButton simulationModeRestartButton;
    private JButton simulationModeEndButton;
    private JButton simulationModeAbortButton;
    private JProgressBar simulationModeProgressBar;   
    private JButton nextActionPassMoveButton;
    private JPanel upperPanel;
    private JPanel leftCenterCardPanel;
    private JPanel rightCenterCardPanel;
    private JPanel topCenterCardPanel;
    private JPanel bottomCenterCardPanel;
    private JPanel legendPanel;
    private JFrame legendFrame;
    private JButton legendButton;
    private JLabel unplayableLabel;
    private JLabel firstCardOfTheTrickLabel;
    private JLabel roundWinningCardLabel;
    private JLabel receivedCardsLabel;
    private JLabel player4Label;
    private JLabel player1ResultsLabel;
    private JLabel player2ResultsLabel;
    private JLabel player3ResultsLabel;
    private JLabel player4ResultsLabel;
    Color moosgruen = new Color(51, 102, 0); // R, G, B-Werte for "Moosgrün"
    private void showSimulationResults() {
    try {
        String filePath = "dataOfSimulation.csv";
        Desktop.getDesktop().open(new File(filePath));
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}

    public GamePlayGUI() {
        frame = new JFrame("Hearts Card Game - Gameplay");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1350, 800);
        frame.setMinimumSize(new Dimension(1350, 800));
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(moosgruen);
  
        //Button for returning to the start screen - Start
        returnButton = new JButton("Return to Start Screen (the game will be lost)");
        //Button for returning to the start screen - End

        //Panel and label for the round points - start
        // - Player1
        JPanel player1ScoresPanel = new JPanel(new GridBagLayout());
        player1ScoresPanel.setBackground(moosgruen);
        player1ScoresPanel.setPreferredSize(new Dimension(240, 50));
        JLabel player1LeftLabel = new JLabel("Player 1 (Left): ");
        player1LeftLabel.setForeground(Color.WHITE);
        player1NameLabel = new JLabel("NamePlayer1  ");
        player1NameLabel.setForeground(Color.WHITE);
        player1ScoreLabel = new JLabel(String.valueOf(0) + " (R)| ");
        player1ScoreLabel.setForeground(Color.WHITE);
        player1ScoreGameLabel = new JLabel(String.valueOf(0) + " (G) ");
        player1ScoreGameLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer1ScoresPanel = new GridBagConstraints();
        gridBagConstraintsPlayer1ScoresPanel.gridx = 0;
        gridBagConstraintsPlayer1ScoresPanel.gridy = 0;
        player1ScoresPanel.add(player1LeftLabel, gridBagConstraintsPlayer1ScoresPanel);
        gridBagConstraintsPlayer1ScoresPanel.gridx = 1;
        gridBagConstraintsPlayer1ScoresPanel.gridy = 0;
        player1ScoresPanel.add(player1NameLabel, gridBagConstraintsPlayer1ScoresPanel);
        gridBagConstraintsPlayer1ScoresPanel.gridx = 2;
        gridBagConstraintsPlayer1ScoresPanel.gridy = 0;
        player1ScoresPanel.add(player1ScoreLabel, gridBagConstraintsPlayer1ScoresPanel);     
        gridBagConstraintsPlayer1ScoresPanel.gridx = 3;
        gridBagConstraintsPlayer1ScoresPanel.gridy = 0;
        player1ScoresPanel.add(player1ScoreGameLabel, gridBagConstraintsPlayer1ScoresPanel);  
        // - Player2
        JPanel player2ScoresPanel = new JPanel(new GridBagLayout());
        player2ScoresPanel.setBackground(moosgruen);
        player2ScoresPanel.setPreferredSize(new Dimension(240, 50));
        JLabel player2LeftLabel = new JLabel("    Player 2 (Top): ");
        player2LeftLabel.setForeground(Color.WHITE);
        player2NameLabel = new JLabel("NamePlayer2  ");
        player2NameLabel.setForeground(Color.WHITE);
        player2ScoreLabel = new JLabel(String.valueOf(0) + " (R)| ");
        player2ScoreLabel.setForeground(Color.WHITE);
        player2ScoreGameLabel = new JLabel(String.valueOf(0) + " (G) ");
        player2ScoreGameLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer2ScoresPanel = new GridBagConstraints();
        gridBagConstraintsPlayer2ScoresPanel.gridx = 0;
        gridBagConstraintsPlayer2ScoresPanel.gridy = 0;
        player2ScoresPanel.add(player2LeftLabel, gridBagConstraintsPlayer2ScoresPanel);
        gridBagConstraintsPlayer2ScoresPanel.gridx = 1;
        gridBagConstraintsPlayer2ScoresPanel.gridy = 0;
        player2ScoresPanel.add(player2NameLabel, gridBagConstraintsPlayer2ScoresPanel);
        gridBagConstraintsPlayer2ScoresPanel.gridx = 2;
        gridBagConstraintsPlayer2ScoresPanel.gridy = 0;
        player2ScoresPanel.add(player2ScoreLabel, gridBagConstraintsPlayer2ScoresPanel);        
        gridBagConstraintsPlayer2ScoresPanel.gridx = 3;
        gridBagConstraintsPlayer2ScoresPanel.gridy = 0;
        player2ScoresPanel.add(player2ScoreGameLabel, gridBagConstraintsPlayer2ScoresPanel);
        // - Player3
        JPanel player3ScoresPanel = new JPanel(new GridBagLayout());
        player3ScoresPanel.setBackground(moosgruen);
        player3ScoresPanel.setPreferredSize(new Dimension(260, 40));
        JLabel player3LeftLabel = new JLabel("    Player 3 (Right): ");
        player3LeftLabel.setForeground(Color.WHITE);
        player3NameLabel = new JLabel("NamePlayer3  ");
        player3NameLabel.setForeground(Color.WHITE);
        player3ScoreLabel = new JLabel(String.valueOf(0) + " (R)| ");
        player3ScoreLabel.setForeground(Color.WHITE);
        player3ScoreGameLabel = new JLabel(String.valueOf(0) + " (G) ");
        player3ScoreGameLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer3ScoresPanel = new GridBagConstraints();
        gridBagConstraintsPlayer3ScoresPanel.gridx = 0;
        gridBagConstraintsPlayer3ScoresPanel.gridy = 0;
        player3ScoresPanel.add(player3LeftLabel, gridBagConstraintsPlayer3ScoresPanel);
        gridBagConstraintsPlayer3ScoresPanel.gridx = 1;
        gridBagConstraintsPlayer3ScoresPanel.gridy = 0;
        player3ScoresPanel.add(player3NameLabel, gridBagConstraintsPlayer3ScoresPanel);
        gridBagConstraintsPlayer3ScoresPanel.gridx = 2;
        gridBagConstraintsPlayer3ScoresPanel.gridy = 0;
        player3ScoresPanel.add(player3ScoreLabel, gridBagConstraintsPlayer3ScoresPanel);
        gridBagConstraintsPlayer3ScoresPanel.gridx = 3;
        gridBagConstraintsPlayer3ScoresPanel.gridy = 0;
        player3ScoresPanel.add(player3ScoreGameLabel, gridBagConstraintsPlayer3ScoresPanel);
        // - Player4 (Human)
        JPanel player4ScoresPanel = new JPanel(new GridBagLayout());
        player4ScoresPanel.setBackground(moosgruen);
        player4ScoresPanel.setPreferredSize(new Dimension(380, 50));
        JLabel player4LeftLabel = new JLabel("    Player 4 (Bottom, You): ");
        player4LeftLabel.setForeground(Color.WHITE);
        player4LeftLabel.setFont(new Font(getPlayer3NameLabel().getFont().getName(), Font.BOLD,getPlayer3NameLabel().getFont().getSize()));
        player4NameLabel = new JLabel("NamePlayer4  ");
        player4NameLabel.setForeground(Color.WHITE);
        player4ScoreLabel = new JLabel(String.valueOf(0) + " (R)| ");
        player4ScoreLabel.setForeground(Color.WHITE);
        player4ScoreGameLabel = new JLabel(String.valueOf(0) + " (G) ");
        player4ScoreGameLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer4ScoresPanel = new GridBagConstraints();
        gridBagConstraintsPlayer4ScoresPanel.gridx = 0;
        gridBagConstraintsPlayer4ScoresPanel.gridy = 0;
        player4ScoresPanel.add(player4LeftLabel, gridBagConstraintsPlayer4ScoresPanel);
        gridBagConstraintsPlayer4ScoresPanel.gridx = 1;
        gridBagConstraintsPlayer4ScoresPanel.gridy = 0;
        player4ScoresPanel.add(player4NameLabel, gridBagConstraintsPlayer4ScoresPanel);
        gridBagConstraintsPlayer4ScoresPanel.gridx = 2;
        gridBagConstraintsPlayer4ScoresPanel.gridy = 0;
        player4ScoresPanel.add(player4ScoreLabel, gridBagConstraintsPlayer4ScoresPanel);       
        gridBagConstraintsPlayer4ScoresPanel.gridx = 3;
        gridBagConstraintsPlayer4ScoresPanel.gridy = 0;
        player4ScoresPanel.add(player4ScoreGameLabel, gridBagConstraintsPlayer4ScoresPanel);        
        // -Merge in one panel
        scoresPanel = new JPanel(new GridBagLayout());
        scoresPanel.setBackground(moosgruen);
        GridBagConstraints gridBagConstraintsScoresPanel = new GridBagConstraints();
        gridBagConstraintsScoresPanel.gridx = 0;
        gridBagConstraintsScoresPanel.gridy = 0;//Player1
        scoresPanel.add(player1ScoresPanel, gridBagConstraintsScoresPanel);
        gridBagConstraintsScoresPanel.gridx = 1;
        gridBagConstraintsScoresPanel.gridy = 0;//Player2
        scoresPanel.add(player2ScoresPanel, gridBagConstraintsScoresPanel);
        gridBagConstraintsScoresPanel.gridx = 2;
        gridBagConstraintsScoresPanel.gridy = 0;//Player3
        scoresPanel.add(player3ScoresPanel, gridBagConstraintsScoresPanel);
        gridBagConstraintsScoresPanel.gridx = 3;
        gridBagConstraintsScoresPanel.gridy = 0;//Player4
        scoresPanel.add(player4ScoresPanel, gridBagConstraintsScoresPanel);
        //Panel and label for the round points - End

        //Panel containing the return button and the round points - Start
        upperPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraintsUpperPanel = new GridBagConstraints();
        gridBagConstraintsUpperPanel.gridx = 0;
        gridBagConstraintsUpperPanel.gridy = 0;
        upperPanel.add(returnButton, gridBagConstraintsUpperPanel);
        gridBagConstraintsUpperPanel.gridy = 1;
        upperPanel.add(scoresPanel, gridBagConstraintsUpperPanel);
        upperPanel.setBackground(moosgruen);
        upperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        //Panel containing the return button and the round points - End

        //-------------------------------------------------------------------
        // Lower Panel - Start
        // -Lower panel and its upper and lower panel
        lowerPanel = new JPanel(new GridBagLayout());
        JPanel lowerPanelTopPanel = new JPanel(new GridBagLayout());
        lowerPanelTopPanel.setBackground(moosgruen);
        JPanel lowerPanelBottomPanel = new JPanel(new GridBagLayout());
        lowerPanelBottomPanel.setBackground(moosgruen);        
        // -Of the lower panel ("lowerPanelBottomPanel" of the "lowerPanel") additionally a left and right panel
        JPanel lowerPanelLeftPanel = new JPanel(new GridBagLayout());
        lowerPanelLeftPanel.setBackground(moosgruen);
        JPanel lowerPanelRightPanel = new JPanel(new GridBagLayout());
        lowerPanelRightPanel.setBackground(moosgruen);
        // -In lowerPanelTopPanel: nextActionPassMoveButton
        //  -PassMove button
        nextActionPassMove = new JPanel(new GridBagLayout());
        JPanel nextActionPassMoveInnerPanelWithButton = new JPanel(new GridBagLayout());
        nextActionPassMoveButton = new JButton("Pass/Move Cards");
        nextActionPassMoveInnerPanelWithButton.add(nextActionPassMoveButton);
        nextActionPassMoveInnerPanelWithButton.setBackground(moosgruen);
        nextActionPassMove.add(nextActionPassMoveInnerPanelWithButton);
        nextActionPassMove.setBorder(BorderFactory.createEmptyBorder(0,0,15,0)); 
        nextActionPassMove.setBackground(moosgruen);
        //  -Add the PassMove button to the lowerPanelTopPanel
        GridBagConstraints gridBagConstraintsLowerPanelTopPanel = new GridBagConstraints();
        gridBagConstraintsLowerPanelTopPanel.gridx = 0;
        gridBagConstraintsLowerPanelTopPanel.gridy = 0;
        lowerPanelTopPanel.add(nextActionPassMove, gridBagConstraintsLowerPanelTopPanel);
        // -In lowerPanelRightPanel: playerHandPanel, playerHandPanelSelectedCards, playerNameLabel
        // -playerHandPanel
        playerHandPanel = new JPanel(new GridBagLayout());
        playerHandPanel.setBackground(moosgruen);
        for (int i = 1; i <= 13; i++){
            JLabel card = new JLabel();
            JPanel cardSlot = new JPanel(new BorderLayout());
            cardSlot.setBackground(moosgruen);
            cardSlot.add(card);
            cardSlot.setPreferredSize(new Dimension(82, 120));
            cardSlot.setBorder(BorderFactory.createLineBorder(moosgruen)); 
            playerHandPanel.add(cardSlot,i-1);
        }
        // -playerHandPanelSelectedCards
        playerHandPanelSelectedCards = new JPanel(new GridBagLayout());
        playerHandPanelSelectedCards.setBackground(moosgruen);
        for (int i = 1; i <= 13; i++){
            JLabel card = new JLabel();
            JPanel cardSlot = new JPanel(new BorderLayout());
            cardSlot.setBackground(moosgruen);
            cardSlot.add(card);
            cardSlot.setPreferredSize(new Dimension(82, 120));
            cardSlot.setBorder(BorderFactory.createLineBorder(moosgruen)); 
            playerHandPanelSelectedCards.add(cardSlot,i-1);
        }        
 
        legendButton = new JButton ("Legend");
        legendButton.setPreferredSize(new Dimension(80, 20));
        legendButton.setMaximumSize(new Dimension(80, 20));
        legendButton.setSize(80, 20);
        
        // -Player Name Label
        playerNameLabel = new JLabel();
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setHorizontalAlignment(JLabel.CENTER);
        // -lowerPanelRightPanel merging
        GridBagConstraints gridBagConstraintsLowerPanelRight = new GridBagConstraints();
        gridBagConstraintsLowerPanelRight.gridx = 0;
        gridBagConstraintsLowerPanelRight.gridy = 0;
        lowerPanelRightPanel.add(playerHandPanelSelectedCards, gridBagConstraintsLowerPanelRight);
        gridBagConstraintsLowerPanelRight.gridy = 1;
        lowerPanelRightPanel.add(playerHandPanel, gridBagConstraintsLowerPanelRight);
        gridBagConstraintsLowerPanelRight.gridy = 2;
        lowerPanelRightPanel.add(playerNameLabel, gridBagConstraintsLowerPanelRight);
        // -In lowerPanelLeftPanel: Card back and a label for displaying the last tricks in a new window + two "empty" panels
        //  -Card back
        cardToShowPreviousTrickLabel = new JLabel();
        JPanel cardToShowPreviousTrickPanel = new JPanel(new BorderLayout());
        cardToShowPreviousTrickPanel.setBackground(moosgruen);
        cardToShowPreviousTrickPanel.add(cardToShowPreviousTrickLabel);
        cardToShowPreviousTrickPanel.setPreferredSize(new Dimension(82, 120));
        cardToShowPreviousTrickPanel.setBorder(BorderFactory.createLineBorder(moosgruen)); 
        //  -an empty panel so that everything is aligned correctly
        JPanel emptyPanel1 = new JPanel(new BorderLayout());
        emptyPanel1.add(legendButton, BorderLayout.SOUTH);
        emptyPanel1.setBackground(moosgruen);
        emptyPanel1.setPreferredSize(new Dimension(82, 120));
        emptyPanel1.setBorder(BorderFactory.createLineBorder(moosgruen)); 
        // -lowerPanelLeftPanel merging
        GridBagConstraints gridBagConstraintsLowerPanelLeft = new GridBagConstraints();
        gridBagConstraintsLowerPanelLeft.gridx = 0;
        gridBagConstraintsLowerPanelLeft.gridy = 0;
        lowerPanelLeftPanel.add(emptyPanel1, gridBagConstraintsLowerPanelLeft);
        gridBagConstraintsLowerPanelLeft.gridy = 1;
        lowerPanelLeftPanel.add(cardToShowPreviousTrickPanel, gridBagConstraintsLowerPanelLeft);
        gridBagConstraintsLowerPanelLeft.gridy = 2;
        JLabel clickToShowPreviousTrick = new JLabel("Click to show previous Trick");
        clickToShowPreviousTrick.setForeground(Color.WHITE);
        lowerPanelLeftPanel.add(clickToShowPreviousTrick, gridBagConstraintsLowerPanelLeft);

        // merge the lowerPanelLeftPanel and lowerPanelRightPanel on the lowerPanelBottomPanel
        GridBagConstraints gridBagConstraintsLowerPanelBottomPanel = new GridBagConstraints();
        gridBagConstraintsLowerPanelBottomPanel.gridx = 0;
        gridBagConstraintsLowerPanelBottomPanel.gridy = 0;
        lowerPanelBottomPanel.add(lowerPanelLeftPanel, gridBagConstraintsLowerPanelBottomPanel);       
        gridBagConstraintsLowerPanelBottomPanel.gridx = 1;
        gridBagConstraintsLowerPanelBottomPanel.gridy = 0;
        lowerPanelBottomPanel.add(lowerPanelRightPanel, gridBagConstraintsLowerPanelBottomPanel);

        // merge lowerPanelBottomPanel and lowerPanelTopPanel to lowerPanel
        GridBagConstraints gridBagConstraintsLowerPanel = new GridBagConstraints();
        gridBagConstraintsLowerPanel.gridx = 0;//oben
        gridBagConstraintsLowerPanel.gridy = 0;
        lowerPanel.add(lowerPanelTopPanel, gridBagConstraintsLowerPanel);//oben        
        gridBagConstraintsLowerPanel.gridx = 0;//unten
        gridBagConstraintsLowerPanel.gridy = 1;
        lowerPanel.add(lowerPanelBottomPanel, gridBagConstraintsLowerPanel);//unten
        lowerPanel.setBackground(moosgruen);


        // Lower Panel - End
        //-------------------------------------------------------------------
        
        
        centerContainer = new JPanel();
        centerContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        centerContainer.setPreferredSize(new Dimension(240, 360));
        centerContainer.setBackground(moosgruen);
        
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3,3));
        centerPanel.setPreferredSize(new Dimension(240, 360));
        centerPanel.setMaximumSize(new Dimension(240, 360));

        
        centerContainer.add(centerPanel);
        centerPanel.setBackground(moosgruen);

        
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        leftCenterCardPanel = new JPanel(new BorderLayout());
        leftCenterCardPanel.add(new JLabel());
        leftCenterCardPanel.setPreferredSize(new Dimension(80, 120));
        leftCenterCardPanel.setBackground(moosgruen);
        centerPanel.add(leftCenterCardPanel, BorderLayout.WEST);

        

        topCenterCardPanel = new JPanel(new BorderLayout());
        topCenterCardPanel.add(new JLabel());
        topCenterCardPanel.setPreferredSize(new Dimension(80, 120));
        topCenterCardPanel.setBackground(moosgruen);
        centerPanel.add(topCenterCardPanel, BorderLayout.NORTH);

        rightCenterCardPanel = new JPanel(new BorderLayout());
        rightCenterCardPanel.add(new JLabel());
        rightCenterCardPanel.setPreferredSize(new Dimension(80, 120));
        rightCenterCardPanel.setBackground(moosgruen);
        centerPanel.add(rightCenterCardPanel, BorderLayout.EAST);

        bottomCenterCardPanel = new JPanel(new BorderLayout());
        bottomCenterCardPanel.add(new JLabel());
        bottomCenterCardPanel.setPreferredSize(new Dimension(80, 120));
        bottomCenterCardPanel.setBackground(moosgruen);
        centerPanel.add(bottomCenterCardPanel, BorderLayout.SOUTH);

        placeholder = new JPanel();
        placeholder.setPreferredSize(new Dimension(80, 120));
        placeholder.setBackground(moosgruen);

        
        centerPanel.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setBackground(moosgruen);
            }
        });
        centerPanel.add(topCenterCardPanel);
        centerPanel.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setBackground(moosgruen);
            }
        });
        centerPanel.add(leftCenterCardPanel);
        centerPanel.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setBackground(moosgruen);
            }
        });
        centerPanel.add(rightCenterCardPanel);
        centerPanel.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setBackground(moosgruen);
            }
        });
        centerPanel.add(bottomCenterCardPanel);
        centerPanel.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                this.setBackground(moosgruen);
            }
        });


        mainPanel.add(lowerPanel, BorderLayout.SOUTH);
        mainPanel.add(upperPanel, BorderLayout.NORTH);

        frame.setContentPane(mainPanel);

        //---------------------------------------------------------------
        //2. Frame for displaying the last trick
        previousTrickFrame = new JFrame("Previous Trick");
        previousTrickFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        previousTrickFrame.setSize(700, 450);
        previousTrickFrame.setMinimumSize(new Dimension(700, 450));
        previousTrickFrame.setVisible(false);

        JPanel previousTrickPanel = new JPanel(new BorderLayout());
        previousTrickPanel.setBackground(moosgruen);
        previousTrickFrame.setContentPane(previousTrickPanel);
        
        JPanel previousTrickFrameCenterContainer = new JPanel();
        previousTrickFrameCenterContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        previousTrickFrameCenterContainer.setPreferredSize(new Dimension(500, 360));
        previousTrickFrameCenterContainer.setBackground(moosgruen);
        previousTrickPanel.add(previousTrickFrameCenterContainer, BorderLayout.CENTER);
        
        previousTrickFrameCenterPanel = new JPanel();
        previousTrickFrameCenterPanel.setLayout(new GridBagLayout());
        previousTrickFrameCenterPanel.setPreferredSize(new Dimension(500, 360));
        previousTrickFrameCenterPanel.setMaximumSize(new Dimension(500, 360));
        previousTrickFrameCenterPanel.setBackground(moosgruen);
        previousTrickFrameCenterContainer.add(previousTrickFrameCenterPanel);

        previousTrickCard1Label = new JLabel();
        JPanel previousTrickCard1Panel = new JPanel(new BorderLayout());
        previousTrickCard1Panel.setBackground(moosgruen);
        previousTrickCard1Panel.add(previousTrickCard1Label);
        previousTrickCard1Panel.setPreferredSize(new Dimension(82, 150));
        previousTrickCard1Panel.setBorder(BorderFactory.createLineBorder(moosgruen));
        JLabel player1Label = new JLabel("Frodo");
        player1Label.setForeground(Color.WHITE);
        JPanel player1LabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player1LabelPanel.setBackground(moosgruen);
        player1LabelPanel.add(player1Label);
        previousTrickCard1Panel.add(player1LabelPanel, BorderLayout.SOUTH);
        previousTrickFrameCenterPanel.add(previousTrickCard1Panel,0);

        previousTrickCard2Label = new JLabel();
        JPanel previousTrickCard2Panel = new JPanel(new BorderLayout());
        previousTrickCard2Panel.setBackground(moosgruen);
        previousTrickCard2Panel.add(previousTrickCard2Label);
        previousTrickCard2Panel.setPreferredSize(new Dimension(82, 150));
        previousTrickCard2Panel.setBorder(BorderFactory.createLineBorder(moosgruen));
        JLabel player2Label = new JLabel("Sam");
        player2Label.setForeground(Color.WHITE);
        JPanel player2LabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player2LabelPanel.setBackground(moosgruen);
        player2LabelPanel.add(player2Label);
        previousTrickCard2Panel.add(player2LabelPanel, BorderLayout.SOUTH);
        previousTrickFrameCenterPanel.add(previousTrickCard2Panel,1);
        

        previousTrickCard3Label = new JLabel();
        JPanel previousTrickCard3Panel = new JPanel(new BorderLayout());
        previousTrickCard3Panel.setBackground(moosgruen);
        previousTrickCard3Panel.add(previousTrickCard3Label);
        previousTrickCard3Panel.setPreferredSize(new Dimension(82, 150));
        previousTrickCard3Panel.setBorder(BorderFactory.createLineBorder(moosgruen));
        JLabel player3Label = new JLabel("Gandalf");
        player3Label.setForeground(Color.WHITE);
        JPanel player3LabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player3LabelPanel.setBackground(moosgruen);
        player3LabelPanel.add(player3Label);
        previousTrickCard3Panel.add(player3LabelPanel, BorderLayout.SOUTH);
        previousTrickFrameCenterPanel.add(previousTrickCard3Panel,2);

        previousTrickCard4Label = new JLabel();
        JPanel previousTrickCard4Panel = new JPanel(new BorderLayout());
        previousTrickCard4Panel.setBackground(moosgruen);
        previousTrickCard4Panel.add(previousTrickCard4Label);
        previousTrickCard4Panel.setPreferredSize(new Dimension(82, 150));
        previousTrickCard4Panel.setBorder(BorderFactory.createLineBorder(moosgruen));
        player4Label = new JLabel();
        player4Label.setForeground(Color.WHITE);
        JPanel player4LabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player4LabelPanel.setBackground(moosgruen);
        player4LabelPanel.add(player4Label);
        previousTrickCard4Panel.add(player4LabelPanel, BorderLayout.SOUTH);
        previousTrickFrameCenterPanel.add(previousTrickCard4Panel,3);

        
        //Legend Frame

        legendFrame = new JFrame("Legend");
        legendFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        legendFrame.setMinimumSize(new Dimension(300,570));
        legendFrame.pack();
        legendFrame.setVisible(false);

        legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBackground(moosgruen);
        legendFrame.setContentPane(legendPanel);

        JPanel legendCenterContainer = new JPanel();
        legendCenterContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        legendCenterContainer.setBackground(moosgruen);
        legendPanel.add(legendCenterContainer, BorderLayout.WEST);

        JPanel legendCenterPanel = new JPanel();
        legendCenterPanel.setLayout(new BoxLayout(legendCenterPanel, BoxLayout.Y_AXIS));
        legendCenterPanel.setBackground(moosgruen);
        legendCenterContainer.add(legendCenterPanel);
        

        ImageIcon originalUnplayableIcon = new ImageIcon(getClass().getResource("/GUI/assets/backside.png"));
        Image unplayableImage = originalUnplayableIcon.getImage();
        Image newUnplayableImage = unplayableImage.getScaledInstance(80, 120, java.awt.Image.SCALE_SMOOTH);
        ImageIcon unplayableIcon = new ImageIcon(newUnplayableImage);
        unplayableLabel = new JLabel();
        unplayableLabel.setIcon(unplayableIcon);
        unplayableLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(255,127,127)));
        JPanel unplayablePanel = new JPanel();
        unplayablePanel.setLayout(new BorderLayout());
        unplayablePanel.setBackground(moosgruen);
        unplayablePanel.add(unplayableLabel, BorderLayout.WEST);
        JLabel unplayableTextLabel = new JLabel("Unplayable Card");
        unplayableTextLabel.setForeground(Color.WHITE);
        unplayableTextLabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        unplayablePanel.add(unplayableTextLabel, BorderLayout.CENTER);
        unplayablePanel.setBorder(BorderFactory.createLineBorder(moosgruen));
        legendCenterPanel.add(unplayablePanel);

        ImageIcon originalFirstCardOfTheTrickIcon = new ImageIcon(getClass().getResource("/GUI/assets/backside.png"));
        Image firstCardOfTheTrickImage = originalFirstCardOfTheTrickIcon.getImage();
        Image newFirstCardOfTheTrickImage = firstCardOfTheTrickImage.getScaledInstance(80, 120, java.awt.Image.SCALE_SMOOTH);
        ImageIcon firstCardOfTheTrickIcon = new ImageIcon(newFirstCardOfTheTrickImage);
        firstCardOfTheTrickLabel = new JLabel();
        firstCardOfTheTrickLabel.setIcon(firstCardOfTheTrickIcon);
        firstCardOfTheTrickLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(222, 255, 0)));
        JPanel firstCardOfTheTrickPanel = new JPanel();
        firstCardOfTheTrickPanel.setLayout(new BorderLayout());
        firstCardOfTheTrickPanel.setBackground(moosgruen);
        firstCardOfTheTrickPanel.add(firstCardOfTheTrickLabel, BorderLayout.WEST);
        JLabel firstCardTextLabel= new JLabel("First card of the trick");
        firstCardTextLabel.setForeground(Color.WHITE);
        firstCardTextLabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        firstCardOfTheTrickPanel.add(firstCardTextLabel, BorderLayout.CENTER);
        firstCardOfTheTrickPanel.setBorder(BorderFactory.createLineBorder(moosgruen));
        legendCenterPanel.add(firstCardOfTheTrickPanel);

        ImageIcon originalRoundWinningCardIcon = new ImageIcon(getClass().getResource("/GUI/assets/backside.png"));
        Image roundWinningCardImage = originalRoundWinningCardIcon.getImage();
        Image newRoundWinningCardImage = roundWinningCardImage.getScaledInstance(80, 120, java.awt.Image.SCALE_SMOOTH);
        ImageIcon roundWinningCardIcon = new ImageIcon(newRoundWinningCardImage);
        roundWinningCardLabel = new JLabel();
        roundWinningCardLabel.setIcon(roundWinningCardIcon);
        roundWinningCardLabel.setBorder(BorderFactory.createMatteBorder(5,0,0,0,new Color(0,0,255)));
        JPanel roundWinningCardPanel = new JPanel();
        roundWinningCardPanel.setLayout(new BorderLayout());
        roundWinningCardPanel.setBackground(moosgruen);
        roundWinningCardPanel.add(roundWinningCardLabel, BorderLayout.WEST);
        JLabel roundWinningTextLabel = new JLabel("Trick winning card");
        roundWinningTextLabel.setForeground(Color.WHITE);
        roundWinningTextLabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
        roundWinningCardPanel.add(roundWinningTextLabel, BorderLayout.CENTER);
        roundWinningCardPanel.setBorder(BorderFactory.createLineBorder(moosgruen));
        legendCenterPanel.add(roundWinningCardPanel);

        ImageIcon originalReceivedCardsIcon = new ImageIcon(getClass().getResource("/GUI/assets/backside.png"));
        Image receivedCardsImage = originalReceivedCardsIcon.getImage();
        Image newReceivedCardsImage = receivedCardsImage.getScaledInstance(80, 120, java.awt.Image.SCALE_SMOOTH);
        ImageIcon receivedCardsIcon = new ImageIcon(newReceivedCardsImage);
        receivedCardsLabel = new JLabel();
        receivedCardsLabel.setIcon(receivedCardsIcon);
        receivedCardsLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 5, 0, Color.ORANGE));
        JPanel receivedCardsPanel = new JPanel();
        receivedCardsPanel.setLayout(new BorderLayout());
        receivedCardsPanel.setBackground(moosgruen);
        receivedCardsPanel.add(receivedCardsLabel, BorderLayout.WEST);
        JLabel receivedCardsTextLabel = new JLabel ("Received Cards");
        receivedCardsTextLabel.setForeground(Color.WHITE);
        receivedCardsTextLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        receivedCardsPanel.add(receivedCardsTextLabel, BorderLayout.CENTER);
        receivedCardsPanel.setBorder(BorderFactory.createLineBorder(moosgruen));
        legendCenterPanel.add(receivedCardsPanel);







        //---------------------------------------------------------------
        //3. Frame for displaying the winner etc. at the end of the game and the buttons Return, Restart, End
        gameFinishedFrame = new JFrame("Game Finished");
        gameFinishedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFinishedFrame.setSize(800, 500);
        gameFinishedFrame.setMinimumSize(new Dimension(800, 500));
        gameFinishedFrame.setVisible(false);

        JPanel gameFinishedPanel = new JPanel(new GridBagLayout());
        gameFinishedPanel.setBackground(moosgruen);
        gameFinishedFrame.setContentPane(gameFinishedPanel);  

        //Top: Game Score
        gameFinishedPanelUpperPanel = new JPanel();
        gameFinishedPanelUpperPanel.setLayout(new GridBagLayout());
        gameFinishedPanelUpperPanel.setPreferredSize(new Dimension(500, 140));
        gameFinishedPanelUpperPanel.setMinimumSize(new Dimension(500, 140));
        gameFinishedPanelUpperPanel.setBackground(moosgruen);
        GridBagConstraints gridBagConstraintsGameFinishedPanel = new GridBagConstraints();
        gridBagConstraintsGameFinishedPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanel.gridy = 0;
        gameFinishedPanel.add(gameFinishedPanelUpperPanel, gridBagConstraintsGameFinishedPanel);
        // - Player1
        JPanel player1ScoresFinishedPanel = new JPanel(new GridBagLayout());
        player1ScoresFinishedPanel.setBackground(moosgruen);
        player1ScoresFinishedPanel.setPreferredSize(new Dimension(380, 20));
        player1ScoresFinishedPanel.setMinimumSize(new Dimension(380, 20));
        player1ScoresFinishedPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0)); 
        JLabel player1LeftFinishedLabel = new JLabel("Player 1 (Left): ");
        player1LeftFinishedLabel.setForeground(Color.WHITE);
        player1NameFinishedLabel = new JLabel("NamePlayer1  ");
        player1NameFinishedLabel.setForeground(Color.WHITE);
        player1ScoreGameFinishedLabel = new JLabel(String.valueOf(0) + " (G) ");
        player1ScoreGameFinishedLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer1ScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridx = 0;
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridy = 0;
        player1ScoresFinishedPanel.add(player1LeftFinishedLabel, gridBagConstraintsPlayer1ScoresFinishedPanel);
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridx = 1;
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridy = 0;
        player1ScoresFinishedPanel.add(player1NameFinishedLabel, gridBagConstraintsPlayer1ScoresFinishedPanel);   
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridx = 2;
        gridBagConstraintsPlayer1ScoresFinishedPanel.gridy = 0;
        player1ScoresFinishedPanel.add(player1ScoreGameFinishedLabel, gridBagConstraintsPlayer1ScoresFinishedPanel);  
        // - Player2
        JPanel player2ScoresFinishedPanel = new JPanel(new GridBagLayout());
        player2ScoresFinishedPanel.setBackground(moosgruen);
        player2ScoresFinishedPanel.setPreferredSize(new Dimension(380, 20));
        player2ScoresFinishedPanel.setMinimumSize(new Dimension(380, 20));
        player2ScoresFinishedPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel player2LeftFinishedLabel = new JLabel("    Player 2 (Top): ");
        player2LeftFinishedLabel.setForeground(Color.WHITE);
        player2NameFinishedLabel = new JLabel("NamePlayer2  ");
        player2NameFinishedLabel.setForeground(Color.WHITE);
        player2ScoreGameFinishedLabel = new JLabel(String.valueOf(0) + " (G) ");
        player2ScoreGameFinishedLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer2ScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridx = 0;
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridy = 0;
        player2ScoresFinishedPanel.add(player2LeftFinishedLabel, gridBagConstraintsPlayer2ScoresFinishedPanel);
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridx = 1;
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridy = 0;
        player2ScoresFinishedPanel.add(player2NameFinishedLabel, gridBagConstraintsPlayer2ScoresFinishedPanel);   
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridx = 2;
        gridBagConstraintsPlayer2ScoresFinishedPanel.gridy = 0;
        player2ScoresFinishedPanel.add(player2ScoreGameFinishedLabel, gridBagConstraintsPlayer2ScoresFinishedPanel);  
        // - Player3
        JPanel player3ScoresFinishedPanel = new JPanel(new GridBagLayout());
        player3ScoresFinishedPanel.setBackground(moosgruen);
        player3ScoresFinishedPanel.setPreferredSize(new Dimension(380, 20));
        player3ScoresFinishedPanel.setMinimumSize(new Dimension(380, 20));
        player3ScoresFinishedPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel player3LeftFinishedLabel = new JLabel("    Player 3 (Right): ");
        player3LeftFinishedLabel.setForeground(Color.WHITE);
        player3NameFinishedLabel = new JLabel("NamePlayer3  ");
        player3NameFinishedLabel.setForeground(Color.WHITE);
        player3ScoreGameFinishedLabel = new JLabel(String.valueOf(0) + " (G) ");
        player3ScoreGameFinishedLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer3ScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridx = 0;
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridy = 0;
        player3ScoresFinishedPanel.add(player3LeftFinishedLabel, gridBagConstraintsPlayer3ScoresFinishedPanel);
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridx = 1;
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridy = 0;
        player3ScoresFinishedPanel.add(player3NameFinishedLabel, gridBagConstraintsPlayer3ScoresFinishedPanel);   
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridx = 2;
        gridBagConstraintsPlayer3ScoresFinishedPanel.gridy = 0;
        player3ScoresFinishedPanel.add(player3ScoreGameFinishedLabel, gridBagConstraintsPlayer3ScoresFinishedPanel);  
        // - Player4 (Human)
        JPanel player4ScoresFinishedPanel = new JPanel(new GridBagLayout());
        player4ScoresFinishedPanel.setBackground(moosgruen);
        player4ScoresFinishedPanel.setPreferredSize(new Dimension(380, 20));
        player4ScoresFinishedPanel.setMinimumSize(new Dimension(380, 20));
        player4ScoresFinishedPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel player4LeftFinishedLabel = new JLabel("    Player 4 (Bottom, You): ");
        player4LeftFinishedLabel.setFont(new Font(getPlayer3NameFinishedLabel().getFont().getName(), Font.BOLD,getPlayer3NameFinishedLabel().getFont().getSize()));
        player4LeftFinishedLabel.setForeground(Color.WHITE);
        player4NameFinishedLabel = new JLabel("NamePlayer4  ");
        player4NameFinishedLabel.setForeground(Color.WHITE);
        player4ScoreGameFinishedLabel = new JLabel(String.valueOf(0) + " (G) ");
        player4ScoreGameFinishedLabel.setForeground(Color.WHITE);
        GridBagConstraints gridBagConstraintsPlayer4ScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridx = 0;
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridy = 0;
        player4ScoresFinishedPanel.add(player4LeftFinishedLabel, gridBagConstraintsPlayer4ScoresFinishedPanel);
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridx = 1;
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridy = 0;
        player4ScoresFinishedPanel.add(player4NameFinishedLabel, gridBagConstraintsPlayer4ScoresFinishedPanel);   
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridx = 2;
        gridBagConstraintsPlayer4ScoresFinishedPanel.gridy = 0;
        player4ScoresFinishedPanel.add(player4ScoreGameFinishedLabel, gridBagConstraintsPlayer4ScoresFinishedPanel);      
        // -Merging in the gameFinishedPanelUpperPanel panel
        GridBagConstraints gridBagConstraintsScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsScoresFinishedPanel.gridx = 0;
        gridBagConstraintsScoresFinishedPanel.gridy = 0;//Player1
        gameFinishedPanelUpperPanel.add(player1ScoresFinishedPanel, gridBagConstraintsScoresFinishedPanel);
        gridBagConstraintsScoresFinishedPanel.gridx = 0;
        gridBagConstraintsScoresFinishedPanel.gridy = 1;//Player2
        gameFinishedPanelUpperPanel.add(player2ScoresFinishedPanel, gridBagConstraintsScoresFinishedPanel);
        gridBagConstraintsScoresFinishedPanel.gridx = 0;
        gridBagConstraintsScoresFinishedPanel.gridy = 2;//Player3
        gameFinishedPanelUpperPanel.add(player3ScoresFinishedPanel, gridBagConstraintsScoresFinishedPanel);
        gridBagConstraintsScoresFinishedPanel.gridx = 0;
        gridBagConstraintsScoresFinishedPanel.gridy = 3;//Player4
        gameFinishedPanelUpperPanel.add(player4ScoresFinishedPanel, gridBagConstraintsScoresFinishedPanel);


        //In the center: Display of the winner
        gameFinishedPanelCenterPanel = new JPanel();
        gameFinishedPanelCenterPanel.setLayout(new GridBagLayout());
        gameFinishedPanelCenterPanel.setPreferredSize(new Dimension(500, 120));
        gameFinishedPanelCenterPanel.setMinimumSize(new Dimension(500, 120));
        gameFinishedPanelCenterPanel.setBackground(moosgruen);
        gridBagConstraintsGameFinishedPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanel.gridy = 1;
        gameFinishedPanel.add(gameFinishedPanelCenterPanel, gridBagConstraintsGameFinishedPanel);
        JLabel gameWinnerLeftFinishedLabel = new JLabel("The winner or winners: ");
        gameWinnerLeftFinishedLabel.setForeground(Color.WHITE);
        gameWinnerNameFinishedLabel = new JLabel("NameOfWinner/s");
        gameWinnerNameFinishedLabel.setForeground(Color.WHITE);
        gameWinnerAnotherFinishedLabel =  new JLabel("With game points: ");
        gameWinnerAnotherFinishedLabel.setForeground(Color.WHITE);
        gameWinnerScoreGameFinishedLabel = new JLabel(String.valueOf(0) + " ");
        gameWinnerScoreGameFinishedLabel.setForeground(Color.WHITE);
        //Add to panel gameFinishedPanelCenterPanel
        GridBagConstraints gridBagConstraintsGameWinnerScoresFinishedPanel = new GridBagConstraints();
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridx = 0;
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridy = 0;
        gameFinishedPanelCenterPanel.add(gameWinnerLeftFinishedLabel, gridBagConstraintsGameWinnerScoresFinishedPanel);
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridx = 1;
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridy = 0;
        gameFinishedPanelCenterPanel.add(gameWinnerNameFinishedLabel, gridBagConstraintsGameWinnerScoresFinishedPanel);
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridx = 0;
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridy = 1;
        gameFinishedPanelCenterPanel.add(gameWinnerAnotherFinishedLabel, gridBagConstraintsGameWinnerScoresFinishedPanel);
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridx = 1;
        gridBagConstraintsGameWinnerScoresFinishedPanel.gridy = 1;
        gameFinishedPanelCenterPanel.add(gameWinnerScoreGameFinishedLabel, gridBagConstraintsGameWinnerScoresFinishedPanel);
        
        //Bottom: The buttons
        gameFinishedPanelLowerPanel = new JPanel();
        gameFinishedPanelLowerPanel.setLayout(new GridBagLayout());
        gameFinishedPanelLowerPanel.setPreferredSize(new Dimension(500, 150));
        gameFinishedPanelLowerPanel.setMinimumSize(new Dimension(500, 150));
        gameFinishedPanelLowerPanel.setBackground(moosgruen);
        gridBagConstraintsGameFinishedPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanel.gridy = 2;
        gameFinishedPanel.add(gameFinishedPanelLowerPanel, gridBagConstraintsGameFinishedPanel);
        //Button for returning to the start screen    
        gameFinishedReturnButton = new JButton("Return to Start Screen");
        JPanel gameFinishedPanelLowerPanel1stButtonPanel = new JPanel();
        gameFinishedPanelLowerPanel1stButtonPanel.setLayout(new GridBagLayout());
        gameFinishedPanelLowerPanel1stButtonPanel.setPreferredSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel1stButtonPanel.setMinimumSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel1stButtonPanel.setBackground(moosgruen);
        GridBagConstraints gridBagConstraintsGameFinishedPanelLowerPanel1stButtonPanel = new GridBagConstraints();
        gridBagConstraintsGameFinishedPanelLowerPanel1stButtonPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanelLowerPanel1stButtonPanel.gridy = 0;
        gameFinishedPanelLowerPanel1stButtonPanel.add(gameFinishedReturnButton, gridBagConstraintsGameFinishedPanelLowerPanel1stButtonPanel);        
        //Button for starting a new game with the same settings
        gameFinishedRestartButton = new JButton("Restart Game With Same Settings");
        JPanel gameFinishedPanelLowerPanel2ndButtonPanel = new JPanel();
        gameFinishedPanelLowerPanel2ndButtonPanel.setLayout(new GridBagLayout());
        gameFinishedPanelLowerPanel2ndButtonPanel.setPreferredSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel2ndButtonPanel.setMinimumSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel2ndButtonPanel.setBackground(moosgruen);
        GridBagConstraints gridBagConstraintsGameFinishedPanelLowerPanel2ndButtonPanel = new GridBagConstraints();
        gridBagConstraintsGameFinishedPanelLowerPanel2ndButtonPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanelLowerPanel2ndButtonPanel.gridy = 0;
        gameFinishedPanelLowerPanel2ndButtonPanel.add(gameFinishedRestartButton, gridBagConstraintsGameFinishedPanelLowerPanel2ndButtonPanel);        
        //Button to exit/terminate the program.
        gameFinishedEndButton = new JButton("End Hearts");
        JPanel gameFinishedPanelLowerPanel3rdButtonPanel = new JPanel();
        gameFinishedPanelLowerPanel3rdButtonPanel.setLayout(new GridBagLayout());
        gameFinishedPanelLowerPanel3rdButtonPanel.setPreferredSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel3rdButtonPanel.setMinimumSize(new Dimension(380, 40));
        gameFinishedPanelLowerPanel3rdButtonPanel.setBackground(moosgruen);
        GridBagConstraints gridBagConstraintsGameFinishedPanelLowerPanel3rdButtonPanel = new GridBagConstraints();
        gridBagConstraintsGameFinishedPanelLowerPanel3rdButtonPanel.gridx = 0;
        gridBagConstraintsGameFinishedPanelLowerPanel3rdButtonPanel.gridy = 0;
        gameFinishedPanelLowerPanel3rdButtonPanel.add(gameFinishedEndButton, gridBagConstraintsGameFinishedPanelLowerPanel3rdButtonPanel);        
        //Add the buttons to the gameFinishedPanelLowerPanel panel
        GridBagConstraints gridBagConstraintsButtonsFinishedPanel = new GridBagConstraints();
        gridBagConstraintsButtonsFinishedPanel.gridx = 0;
        gridBagConstraintsButtonsFinishedPanel.gridy = 0;
        gameFinishedPanelLowerPanel.add(gameFinishedPanelLowerPanel1stButtonPanel, gridBagConstraintsButtonsFinishedPanel);
        gridBagConstraintsButtonsFinishedPanel.gridy = 1;
        gameFinishedPanelLowerPanel.add(gameFinishedPanelLowerPanel2ndButtonPanel, gridBagConstraintsButtonsFinishedPanel);
        gridBagConstraintsButtonsFinishedPanel.gridy = 2;
        gameFinishedPanelLowerPanel.add(gameFinishedPanelLowerPanel3rdButtonPanel, gridBagConstraintsButtonsFinishedPanel);

        //---------------------------------------------------------------
        //4. Frame for simulation mode
        simulationModeFrame = new JFrame("Simulation Mode");
        simulationModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        simulationModeFrame.setSize(1400, 700);
        simulationModeFrame.setMinimumSize(new Dimension(1400, 700));
        simulationModeFrame.setVisible(false);

        simulationModePanel = new JPanel(new BorderLayout());
        simulationModePanel.setBackground(moosgruen);
        simulationModeFrame.setContentPane(simulationModePanel);    
        
        //Center: Simulation info: progress etc.
        simulationModePanelCenterPanel = new JPanel();
        simulationModePanelCenterPanel.setLayout(new GridBagLayout());
        simulationModePanelCenterPanel.setPreferredSize(new Dimension(1400, 600));
        simulationModePanelCenterPanel.setBackground(moosgruen);
        simulationModePanel.add(simulationModePanelCenterPanel, BorderLayout.CENTER);
        JLabel simulationModeProgressLabel1 = new JLabel("Simulation Progress: ");
        simulationModeProgressLabel1.setForeground(Color.WHITE);
        simulationModeProgressBar = new JProgressBar(0, 100);
        simulationModeProgressBar.setStringPainted(true);
        JLabel results = new JLabel("<html><u><b>Results:</u></b></html>");
        results.setForeground(Color.WHITE);
        results.setBorder(BorderFactory.createEmptyBorder(40,0,0,0));
        player1ResultsLabel = new JLabel("Player 1:");
        player1ResultsLabel.setForeground(Color.WHITE);
        player1ResultsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        player2ResultsLabel = new JLabel("Player 2:");
        player2ResultsLabel.setForeground(Color.WHITE);
        player2ResultsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        player3ResultsLabel = new JLabel("Player 3:");
        player3ResultsLabel.setForeground(Color.WHITE);
        player3ResultsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        player4ResultsLabel = new JLabel("Player 4:");
        player4ResultsLabel.setForeground(Color.WHITE);
        player4ResultsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        //Add to panel simulationModePanelCenterPanel
        GridBagConstraints gridBagConstraintsProgressSimulationModePanel = new GridBagConstraints();
        gridBagConstraintsProgressSimulationModePanel.gridx = 0;
        gridBagConstraintsProgressSimulationModePanel.gridy = 0;
        simulationModePanelCenterPanel.add(simulationModeProgressLabel1, gridBagConstraintsProgressSimulationModePanel);
        gridBagConstraintsProgressSimulationModePanel.gridx = 1;
        gridBagConstraintsProgressSimulationModePanel.gridy = 0;
        simulationModePanelCenterPanel.add(simulationModeProgressBar, gridBagConstraintsProgressSimulationModePanel); 
        gridBagConstraintsProgressSimulationModePanel.gridx=1;
        gridBagConstraintsProgressSimulationModePanel.gridy=1;
        simulationModePanelCenterPanel.add(results, gridBagConstraintsProgressSimulationModePanel);  
        gridBagConstraintsProgressSimulationModePanel.gridx=1;
        gridBagConstraintsProgressSimulationModePanel.gridy=2;
        simulationModePanelCenterPanel.add(player1ResultsLabel, gridBagConstraintsProgressSimulationModePanel);
        gridBagConstraintsProgressSimulationModePanel.gridx=1;
        gridBagConstraintsProgressSimulationModePanel.gridy=3;
        simulationModePanelCenterPanel.add(player2ResultsLabel, gridBagConstraintsProgressSimulationModePanel);
        gridBagConstraintsProgressSimulationModePanel.gridx=1;
        gridBagConstraintsProgressSimulationModePanel.gridy=4;
        simulationModePanelCenterPanel.add(player3ResultsLabel, gridBagConstraintsProgressSimulationModePanel);
        gridBagConstraintsProgressSimulationModePanel.gridx=1;
        gridBagConstraintsProgressSimulationModePanel.gridy=5;
        simulationModePanelCenterPanel.add(player4ResultsLabel, gridBagConstraintsProgressSimulationModePanel);

        //Bottom: Buttons
        JPanel simulationModePanelLowerPanel = new JPanel();
        simulationModePanelLowerPanel.setLayout(new GridBagLayout());
        simulationModePanelLowerPanel.setPreferredSize(new Dimension(500, 360));
        simulationModePanelLowerPanel.setBackground(moosgruen);
        simulationModePanel.add(simulationModePanelLowerPanel, BorderLayout.SOUTH);
        //Button for returning to the start screen
        simulationModeReturnButton = new JButton("Return");
        //Button for starting a new game with the same settings
        simulationModeRestartButton = new JButton("Restart");
        //Button to exit/terminate the program.
        simulationModeEndButton = new JButton("End");
        //Button to abort the simulation.
        simulationModeAbortButton = new JButton("Abort"); 
       //Button to display the results    
        JButton resultsButton = new JButton("Results");
        

        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSimulationResults();
            }
        });

        
        //Add the buttons to the gameFinishedPanelLowerPanel panel
        GridBagConstraints gridBagConstraintsButtonsSimulationModePanel = new GridBagConstraints();
        gridBagConstraintsButtonsSimulationModePanel.gridx = 0;
        gridBagConstraintsButtonsSimulationModePanel.gridy = 0;
        simulationModePanelLowerPanel.add(simulationModeReturnButton, gridBagConstraintsButtonsSimulationModePanel);
        gridBagConstraintsButtonsSimulationModePanel.gridx = 1;
        simulationModePanelLowerPanel.add(simulationModeRestartButton, gridBagConstraintsButtonsSimulationModePanel);
        gridBagConstraintsButtonsSimulationModePanel.gridx = 2;
        simulationModePanelLowerPanel.add(simulationModeEndButton, gridBagConstraintsButtonsSimulationModePanel);
        gridBagConstraintsButtonsSimulationModePanel.gridx = 3;
        simulationModePanelLowerPanel.add(simulationModeAbortButton, gridBagConstraintsButtonsSimulationModePanel);
        gridBagConstraintsButtonsSimulationModePanel.gridx=4;
        simulationModePanelLowerPanel.add(resultsButton, gridBagConstraintsButtonsSimulationModePanel);

        
    }

    public JFrame getFrame() {
        return frame;
    }
    
    public JLabel getPlayerNameLabel() {
        return playerNameLabel;
    }

    public JPanel getPlayerHandPanel() {
        return playerHandPanel;
    }

    public JPanel getPlayerHandPanelSelectedCards() {
        return playerHandPanelSelectedCards;
    }

    public JPanel getLowerPanel() {
        return lowerPanel;
    }

    public JButton getReturnButton() {
        return returnButton;
    }

    public JPanel getNextActionPassMove() {
        return nextActionPassMove;
    }

    public JButton getNextActionPassMoveButton() {
        return nextActionPassMoveButton;
    }
    
    public Color getmoosgruen() {
        return moosgruen;
    }

    //PlayerNo 0
    public JPanel getLeftCenterCardPanel() {
        return leftCenterCardPanel;
    }

    //PlayerNo 1
    public JPanel getTopCenterCardPanel() {
        return topCenterCardPanel;
    }

    //PlayerNo2
    public JPanel getRightCenterCardPanel() {
        return rightCenterCardPanel;
    }

    //PlayerNo3 (human player)
    public JPanel getBottomCenterCardPanel() {
        return bottomCenterCardPanel;
    }

    public JLabel getPlayer1NameLabel() {
        return player1NameLabel;
    }

    public JLabel getPlayer2NameLabel() {
        return player2NameLabel;
    }
    public JLabel getPlayer3NameLabel() {
        return player3NameLabel;
    }
    public JLabel getPlayer4NameLabel() {
        return player4NameLabel;
    }

    public JLabel getPlayer1ScoreLabel() {
        return player1ScoreLabel;
    }

    public JLabel getPlayer2ScoreLabel() {
        return player2ScoreLabel;
    }
    public JLabel getPlayer3ScoreLabel() {
        return player3ScoreLabel;
    }
    
    public JLabel getPlayer4ScoreLabel() {
        return player4ScoreLabel;
    }

    public JLabel getPlayer1ScoreGameLabel() {
        return player1ScoreGameLabel;
    }

    public JLabel getPlayer2ScoreGameLabel() {
        return player2ScoreGameLabel;
    }

    public JLabel getPlayer3ScoreGameLabel() {
        return player3ScoreGameLabel;
    }

    public JLabel getPlayer4ScoreGameLabel() {
        return player4ScoreGameLabel;
    }

    public JLabel getCardToShowPreviousTrickLabel() {
        return cardToShowPreviousTrickLabel;
    }

    public JFrame getPreviousTrickFrame() {
        return previousTrickFrame;
    }

    public JLabel getPreviousTrickCard1Label() {
        return previousTrickCard1Label;
    }

    public JLabel getPreviousTrickCard2Label() {
        return previousTrickCard2Label;
    }

    public JLabel getPreviousTrickCard3Label() {
        return previousTrickCard3Label;
    }

    public JLabel getPreviousTrickCard4Label() {
        return previousTrickCard4Label;
    }

    public JPanel getPreviousTrickFrameCenterPanel() {
        return previousTrickFrameCenterPanel;
    }
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getScoresPanel() {
        return scoresPanel;
    }

    public JPanel getCenterContainer() {
        return centerContainer;
    }

    public JPanel getCenterPanel() {
        return centerPanel;
    }

    public JFrame getGameFinishedFrame() {
        return gameFinishedFrame;
    }

    public JButton getGameFinishedReturnButton() {
        return gameFinishedReturnButton;
    }

    public JButton getGameFinishedRestartButton() {
        return gameFinishedRestartButton;
    }

    public JButton getGameFinishedEndButton() {
        return gameFinishedEndButton;
    }

    public JPanel getGameFinishedPanelUpperPanel() {
        return gameFinishedPanelUpperPanel;
    }

    public JPanel getGameFinishedPanelCenterPanel() {
        return gameFinishedPanelCenterPanel;
    }

    public JPanel getGameFinishedPanelLowerPanel() {
        return gameFinishedPanelLowerPanel;
    }

    public JLabel getGameWinnerNameFinishedLabel() {
        return gameWinnerNameFinishedLabel;
    }

    public JLabel getGameWinnerAnotherFinishedLabel() {
        return gameWinnerAnotherFinishedLabel;
    }

    public JLabel getGameWinnerScoreGameFinishedLabel() {
        return gameWinnerScoreGameFinishedLabel;
    }

    public JLabel getPlayer1NameFinishedLabel() {
        return player1NameFinishedLabel;
    }

    public JLabel getPlayer2NameFinishedLabel() {
        return player2NameFinishedLabel;
    }

    public JLabel getPlayer3NameFinishedLabel() {
        return player3NameFinishedLabel;
    }    

    public JLabel getPlayer4NameFinishedLabel() {
        return player4NameFinishedLabel;
    }

    public JLabel getPlayer1ScoreGameFinishedLabel() {
        return player1ScoreGameFinishedLabel;
    }

    public JLabel getPlayer2ScoreGameFinishedLabel() {
        return player2ScoreGameFinishedLabel;
    }

    public JLabel getPlayer3ScoreGameFinishedLabel() {
        return player3ScoreGameFinishedLabel;
    }

    public JLabel getPlayer4ScoreGameFinishedLabel() {
        return player4ScoreGameFinishedLabel;
    }

    public JFrame getSimulationModeFrame() {
        return simulationModeFrame;
    }

    public JButton getSimulationModeReturnButton() {
        return simulationModeReturnButton;
    }

    public JButton getSimulationModeRestartButton() {
        return simulationModeRestartButton;
    }

    public JButton getSimulationModeEndButton() {
        return simulationModeEndButton;
    }

    public JButton getSimulationModeAbortButton() {
        return simulationModeAbortButton;
    }

    public JPanel getSimulationModePanel() {
        return simulationModePanel;
    }
       
    public JProgressBar getSimulationModeProgressBar() {
        return simulationModeProgressBar;
    }

    public JPanel getSimulationModePanelCenterPanel() {
        return simulationModePanelCenterPanel;
    }

    public JButton getLegendButton() {
        return legendButton;
    }

    public JFrame getLegendFrame() {
        return legendFrame;
    }

    public JLabel getPlayer4Label() {
        return player4Label;
    }

    public JLabel getPlayer1ResultsLabel() {
        return player1ResultsLabel;
    }


    public JLabel getPlayer2ResultsLabel() {
        return player2ResultsLabel;
    }

    public JLabel getPlayer3ResultsLabel() {
        return player3ResultsLabel;
    }

    public JLabel getPlayer4ResultsLabel() {
        return player4ResultsLabel;
    }


}
