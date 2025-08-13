package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import game.AI.AiTypes;
import game.AI.AiCFR.DeepCFR;
import game.AI.AiCFR.AiCFRClustering;
import game.AI.AiCFR.MCCFR.AiMCCFR;

public class HeartsGameGUI {
    private JFrame frame;
    private JPanel inputPanel;
    private JTextField playerNameField;
    private JTextField numberOfSimulationGamesField;
    private JButton startButton;
    private JButton endButton;
    private JSlider difficultySlider;
    private JSlider memorySlider;
    private JSlider simulationDifficultySlider;
    private JSlider simulationMemorySlider;
    private JLabel simulationDifficultyLabel;
    private JLabel simulationMemoryLabel;
    private JRadioButton cards52RadioButton;
    private JRadioButton cards32RadioButton;
    private JRadioButton simulationRadioButton;
    private JRadioButton humanPlayerRadioButton;
    private JRadioButton passRadioButton;
    private JRadioButton nopassRadioButton;
    private JLabel nameLabel;
    private JLabel memoryLabel;
    private JLabel difficultyLabel;
    private JLabel simulationGamesLabel;
    private JLabel backgroundLabel;
    private ImageIcon backgroundIcon;
    private JComboBox AI1To3ComboBox;
    private JComboBox AI4ComboBox;
    private JLabel AI1To3Label;
    private JLabel AI4Label;
    private JButton testButton;
    private JFrame testFrame;
    private JPanel testPanel;
    Color moosgruen = new Color(51, 102, 0); // R, G, B values for "Moosgrün"

    private JPanel fpsPanel;
    //private JSlider framesPerSecondSlider;
    private JSpinner framesPerSecondSpinner;    

    private static class BackgroundPanel extends JPanel {
        private ImageIcon backgroundIcon;
    
        public BackgroundPanel(ImageIcon backgroundIcon) {
            this.backgroundIcon = backgroundIcon;
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundIcon != null) {
                int width = getWidth();
                int height = getHeight();
    
                int imageWidth = backgroundIcon.getIconWidth();
                int imageHeight = backgroundIcon.getIconHeight();
                double scaleFactor = Math.min((double) width / imageWidth, (double) height / imageHeight);
                int scaledWidth = (int) (imageWidth * scaleFactor);
                int scaledHeight = (int) (imageHeight * scaleFactor);
    
                int x = (width - scaledWidth) / 2;
                int y = (height - scaledHeight) / 2;
    
                g.drawImage(backgroundIcon.getImage(), x, y, scaledWidth, scaledHeight, this);
            }
        }
    }
    


    public HeartsGameGUI() {
        frame = new JFrame("Hearts Card Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1350, 800);
        frame.setMinimumSize(new Dimension(1350, 800));
        frame.setMaximumSize(new Dimension(1350, 800));

        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/GUI/assets/heartsgameguibackground.png"));
        inputPanel = new BackgroundPanel(backgroundIcon);
        frame.setContentPane(inputPanel);
        inputPanel.setLayout(new GridBagLayout());
        backgroundLabel = new JLabel(backgroundIcon);
        frame.pack();
        frame.setVisible(true);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        

        JLabel titleLabel = new JLabel("Hearts Card Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        inputPanel.add(titleLabel, constraints);


        this.nameLabel = new JLabel("Player Name:");
        constraints.gridx = 0;
        constraints.gridy = 2;
        inputPanel.add(this.nameLabel, constraints);

        playerNameField = new JTextField(20);
        playerNameField.setMaximumSize(new Dimension(200, 30));
        constraints.gridx = 1;
        constraints.gridy = 2;
        inputPanel.add(playerNameField, constraints);


        this.simulationGamesLabel = new JLabel("No. of Simulation Games: ");
        constraints.gridx = 0;
        constraints.gridy = 2;
        inputPanel.add(this.simulationGamesLabel, constraints);
        simulationGamesLabel.setVisible(true);

        numberOfSimulationGamesField = new JTextField(20);
        numberOfSimulationGamesField.setMaximumSize(new Dimension(200, 30));
        constraints.gridx = 1;
        constraints.gridy = 2;
        inputPanel.add(numberOfSimulationGamesField, constraints);
        numberOfSimulationGamesField.setVisible(true);
        numberOfSimulationGamesField.setBackground(Color.WHITE);
        
        
        // SLIDER AND SLIDERLABEL
        //___________________________________
        


        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        

        difficultySlider = new JSlider(0, 3);
        difficultySlider.setMajorTickSpacing(1);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);
        difficultySlider.setBackground(Color.WHITE);
        difficultySlider.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel memoryLabel = new JLabel("Memory: ");
        memoryLabel.setHorizontalAlignment(JLabel.CENTER);
        memoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
       

        memorySlider = new JSlider(0, 3);
        memorySlider.setMajorTickSpacing(1);
        memorySlider.setPaintTicks(true);
        memorySlider.setPaintLabels(true);
        memorySlider.setBackground(Color.WHITE);
        memorySlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        


        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.Y_AXIS));
        difficultyPanel.setBackground(Color.WHITE);
        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(difficultySlider);


        JPanel memoryPanel = new JPanel();
        memoryPanel.setLayout(new BoxLayout(memoryPanel, BoxLayout.Y_AXIS));
        memoryPanel.setBackground(Color.WHITE);
        memoryPanel.add(memoryLabel);
        memoryPanel.add(memorySlider);

        JPanel AIPanel = new JPanel();
        AIPanel.setBackground(Color.WHITE);
        AIPanel.add(difficultyPanel);
        AIPanel.add(memoryPanel);
        constraints.gridx=0;
        constraints.gridy=3;
        inputPanel.add(AIPanel, constraints);

            

        // SIMULATIONMODE
        
        this.simulationDifficultyLabel = new JLabel ("Simulation Difficulty");
        simulationDifficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        simulationDifficultySlider = new JSlider(0, 3);
        simulationDifficultySlider.setMajorTickSpacing(1);
        simulationDifficultySlider.setPaintTicks(true);
        simulationDifficultySlider.setPaintLabels(true);
        simulationDifficultySlider.setBackground(Color.WHITE);
        simulationDifficultySlider.setAlignmentX(Component.CENTER_ALIGNMENT);
       

        this.simulationMemoryLabel = new JLabel ("Simulation Memory");
        simulationMemoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        

        simulationMemorySlider = new JSlider (0, 3);
        simulationMemorySlider.setMajorTickSpacing(1);
        simulationMemorySlider.setPaintTicks(true);
        simulationMemorySlider.setPaintLabels(true);
        simulationMemorySlider.setBackground(Color.WHITE);
        simulationMemorySlider.setAlignmentX(Component.CENTER_ALIGNMENT);
        

        JPanel simulationDifficultyPanel = new JPanel();
        simulationDifficultyPanel.setLayout(new BoxLayout(simulationDifficultyPanel, BoxLayout.Y_AXIS));
        simulationDifficultyPanel.setBackground(Color.WHITE);
        simulationDifficultyPanel.add(simulationDifficultyLabel);
        simulationDifficultyPanel.add(simulationDifficultySlider);


        JPanel simulationMemoryPanel = new JPanel();
        simulationMemoryPanel.setLayout(new BoxLayout(simulationMemoryPanel, BoxLayout.Y_AXIS));
        simulationMemoryPanel.setBackground(Color.WHITE);
        simulationMemoryPanel.add(simulationMemoryLabel);
        simulationMemoryPanel.add(simulationMemorySlider);

        JPanel simulationPanel = new JPanel();
        simulationPanel.setBackground(Color.WHITE);
        simulationPanel.add(simulationDifficultyPanel);
        simulationPanel.add(simulationMemoryPanel);
        constraints.gridx=0;
        constraints.gridy=4;
        inputPanel.add(simulationPanel, constraints);

        //___________________________
        // SLIDER AND SLIDERLABEL END

        // COMBOBOXES
        //___________________________
        

        

        AiTypes[] AI4Options = {AiTypes.RULE_BASED, AiTypes.CFR_CLUSTERING, AiTypes.Cheating_MCTS, AiTypes.PIMC, AiTypes.SO_ISMCTS, AiTypes.MO_ISMCTS , AiTypes.DeepCFR, AiTypes.MCCFR, AiTypes.ISMCTSANN};
        AI4ComboBox = new JComboBox<>(AI4Options);
        AI4ComboBox.setVisible(false);

        AI4Label = new JLabel("AI Player 4");
        AI4Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        AI4Label.setVisible(false);

        JPanel AI4ComboBoxPanel = new JPanel();
        AI4ComboBoxPanel.setLayout(new BoxLayout(AI4ComboBoxPanel, BoxLayout.Y_AXIS));
        AI4ComboBoxPanel.setBackground(Color.WHITE);
        AI4ComboBoxPanel.add(AI4Label);
        AI4ComboBoxPanel.add(AI4ComboBox);

        constraints.gridx = 1;
        constraints.gridy = 6;
        inputPanel.add(AI4ComboBoxPanel, constraints);

        AiTypes[] AI1To3Options = {AiTypes.RULE_BASED, AiTypes.CFR_CLUSTERING, AiTypes.Cheating_MCTS, AiTypes.PIMC, AiTypes.SO_ISMCTS, AiTypes.MO_ISMCTS , AiTypes.DeepCFR, AiTypes.MCCFR, AiTypes.ISMCTSANN};
        AI1To3ComboBox = new JComboBox<>(AI1To3Options);

        AI1To3Label = new JLabel("AI Player 1 to 3");
        AI1To3Label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel AI1to3ComboBoxPanel = new JPanel();
        AI1to3ComboBoxPanel.setLayout(new BoxLayout(AI1to3ComboBoxPanel, BoxLayout.Y_AXIS));
        AI1to3ComboBoxPanel.setBackground(Color.WHITE);
        AI1to3ComboBoxPanel.add(AI1To3Label);
        AI1to3ComboBoxPanel.add(AI1To3ComboBox);

        constraints.gridx = 1;
        constraints.gridy = 5;
        inputPanel.add(AI1to3ComboBoxPanel, constraints);
        
        //_____________________
        //COMBOBOXES END
        


        playerNameField.setVisible(true);
        playerNameField.setBackground(Color.WHITE); 
        nameLabel.setVisible(true);
        simulationDifficultySlider.setVisible(false);
        simulationMemorySlider.setVisible(false);
        simulationDifficultyLabel.setVisible(false);
        simulationMemoryLabel.setVisible(false);

        cards52RadioButton = new JRadioButton("52 Cards");
        cards52RadioButton.setSelected(true);
        cards52RadioButton.setBackground(Color.WHITE);

        cards32RadioButton = new JRadioButton("32 Cards");
        cards32RadioButton.setSelected(false);
        cards32RadioButton.setBackground(Color.WHITE);

        passRadioButton = new JRadioButton("pass");
        passRadioButton.setSelected(true);
        passRadioButton.setBackground(Color.WHITE);

        nopassRadioButton = new JRadioButton("no pass");
        nopassRadioButton.setSelected(false);
        nopassRadioButton.setBackground(Color.WHITE);

        ButtonGroup passing = new ButtonGroup();
        passing.add(passRadioButton);
        passing.add(nopassRadioButton);

        ButtonGroup cardGroup = new ButtonGroup();
        cardGroup.add(cards52RadioButton);
        cardGroup.add(cards32RadioButton);

        simulationRadioButton = new JRadioButton("Simulation");
        simulationRadioButton.setSelected(false);
        simulationRadioButton.setBackground(Color.WHITE);
        simulationRadioButton.setBorderPainted(false);
        humanPlayerRadioButton = new JRadioButton("Human Player");
        humanPlayerRadioButton.setBackground(Color.WHITE);
        humanPlayerRadioButton.setSelected(true);
        humanPlayerRadioButton.setBorderPainted(false);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(simulationRadioButton);
        modeGroup.add(humanPlayerRadioButton);

        JPanel cardsPanel = new JPanel();
        cardsPanel.add(cards52RadioButton);
        cardsPanel.add(cards32RadioButton);
        cardsPanel.setBackground(Color.WHITE);

        JPanel modePanel = new JPanel();
        modePanel.add(simulationRadioButton);
        modePanel.add(humanPlayerRadioButton);
        modePanel.setBackground(Color.WHITE);

        JPanel passPanel = new JPanel();
        passPanel.add(passRadioButton);
        passPanel.add(nopassRadioButton);
        passPanel.setBackground(Color.WHITE);

        

        constraints.gridx = 0;
        constraints.gridy = 11;
        constraints.gridwidth = 2;
        inputPanel.add(cardsPanel, constraints);

        constraints.gridy = 12;
        inputPanel.add(modePanel, constraints);

        constraints.gridy=13;
        inputPanel.add(passPanel, constraints);


        startButton = new JButton("Start Game");
        constraints.gridx = 0;
        constraints.gridy = 14;
        constraints.gridwidth = 2;
        inputPanel.add(startButton, constraints);

        endButton = new JButton("End Game");
        constraints.gridx = 0;
        constraints.gridy = 15;
        constraints.gridwidth = 2;
        inputPanel.add(endButton, constraints);

        testButton = new JButton("Test-Environment");
        constraints.gridx = 0;
        constraints.gridy = 16;
        constraints.gridwidth = 2;
        inputPanel.add(testButton, constraints);




        // TEST ENRIVONMENT
        //______________________________________________
        testFrame  = new JFrame("Test Environment");
        testFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        testFrame.setSize(700, 450);
        testFrame.setMinimumSize(new Dimension(700, 450));
        testFrame.setVisible(false);

        testPanel = new JPanel(new FlowLayout((FlowLayout.CENTER)));
        testPanel.setBackground(moosgruen);
        testFrame.setContentPane(testPanel);

        JButton button1 = new JButton("Train");
        JButton button2 = new JButton("Train");
        JButton button3 = new JButton("Train");
        JTextField textField1 = new JTextField();
        JTextField textField2 = new JTextField();
        JTextField textField3 = new JTextField();
        JTextField textField4 = new JTextField();
        JTextField textField5 = new JTextField();
        JTextField textField6 = new JTextField();

        for (int i = 1; i <= 3; i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            switch (i) {
                case 1:
                    JLabel l1 = new JLabel("AICFRClustering :");
                    panel.add(l1);
                    JLabel info = new JLabel("Iterations / Games");
                    panel.add(info);
                    textField1.setColumns(5);
                    textField2.setColumns(5);
                    textField1.setText("1000");
                    textField2.setText("1");
                    panel.add(textField1);
                    panel.add(textField2);
                    break;

                case 2:     
                    JLabel l2 = new JLabel("DeepCFR :" );     
                    panel.add(l2); 
                    JLabel info2 = new JLabel("Iterations / Games");
                    panel.add(info2);
                    textField3.setColumns(5);
                    textField4.setColumns(5);
                    textField3.setText("500");
                    textField4.setText("1");
                    panel.add(textField3);
                    panel.add(textField4);
                    break;

                case 3:
                    JLabel l3 = new JLabel("MCCFR :" );
                    panel.add(l3);
                    JLabel info3 = new JLabel("Iterations Play / Iterations Pass");
                    panel.add(info3);
                    textField5.setColumns(5);
                    textField6.setColumns(5);
                    textField5.setText("500");
                    textField6.setText("500");
                    panel.add(textField5);
                    panel.add(textField6);
                    break;  
                
            }

            switch (i) {
                case 1:
                    button1.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {                   
                            try {
                                int i1 = Integer.parseInt(textField1.getText());
                                int i2 = Integer.parseInt(textField2.getText());
                                System.out.println("Training with " + i1 + " iterations, each " + i2 + " games.");

                                AiCFRClustering cfr = new AiCFRClustering();
                                cfr.trainCFR(i2, i1);

                            } catch (Exception ee) {
                                JOptionPane.showMessageDialog(null, "Please only enter integers!");
                            }
                        }
                    });
                    panel.add(button1);
                    break;
                case 2:
                button2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {                   
                        try {
                            int i1 = Integer.parseInt(textField3.getText());
                            int i2 = Integer.parseInt(textField4.getText());
                            System.out.println("Training with " + i1 + " iterations, each " + i2 + " games.");

                            DeepCFR.trainDeepCFR(i2, i1);

                        } catch (Exception ee) {
                            JOptionPane.showMessageDialog(null, "Please only enter integers!");
                        }
                    }
                });
                panel.add(button2);
                break;

                case 3:
                button3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {                   
                        try {
                            int i1 = Integer.parseInt(textField5.getText());
                            int i2 = Integer.parseInt(textField6.getText());
                            
                            System.out.println("Training with " + i1 + " Play-Iterations and " + i2 + " Pass-Iterations.");

                            AiMCCFR mccfr = new AiMCCFR();
                            mccfr.trainMCCFR(i1);
                            mccfr.trainMCCFRPass(i2);

                        } catch (Exception ee) {
                            JOptionPane.showMessageDialog(null, "Please only enter integers!");
                        }
                    }
                });
                panel.add(button3);
                    break;

            }

            testPanel.add(panel);
        }
 
        //______________________________________________
        //END TEST ENVIRONMENT


        fpsPanel = new JPanel(new GridBagLayout());
        fpsPanel.setBackground(Color.WHITE);
        JLabel fpsSpinnerLabel = new JLabel("FPS: ");
        constraints.gridx = 0;
        constraints.gridy=0;
        fpsPanel.add(fpsSpinnerLabel, constraints);
        SpinnerNumberModel fpsModel = new SpinnerNumberModel(20, 10, 120, 10);
        framesPerSecondSpinner = new JSpinner(fpsModel);
        constraints.gridx = 2;
        constraints.gridy=0;
        fpsPanel.add(framesPerSecondSpinner, constraints);
        constraints.gridx = 0;
        constraints.gridy=17;
        //inputPanel.add(fpsPanel, constraints);


        frame.setContentPane(inputPanel);
        
    }

    public JFrame getFrame() {
        return frame;
    }

    public JPanel getInputPanel() {
        return inputPanel;

    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getEndButton() {
        return endButton;
    } 

    public JRadioButton getCards32RadioButton() {
        return cards32RadioButton;
    }

    public JRadioButton getCards52RadioButton() {
        return cards52RadioButton;
    }

    public JRadioButton getHumanPlayerRadioButton() {
        return humanPlayerRadioButton;
    }

    public JRadioButton getSimulationRadioButton() {
        return simulationRadioButton;
    }

    public JRadioButton getPassRadioButton() {
        return passRadioButton;
    }

    public JRadioButton getNoPassRadioButton() {
        return nopassRadioButton;
    }

    public JSlider getDifficultySlider() {
        return difficultySlider;
    }

    public JLabel getDifficultyLabel() {
        return difficultyLabel;
    }

    public JSlider getMemorySlider() {
        return memorySlider;
    }

    public JLabel getMemoryLabel() {
        return memoryLabel;
    }

    public JSlider getSimulationDifficultySlider(){
        return simulationDifficultySlider;
    }

    public JSlider getSimulationMemorySlider(){
        return simulationMemorySlider;
    }

    public JLabel getSimulationDifficultyLabel(){
        return simulationDifficultyLabel;
    }

    public JLabel getSimulationMemoryLabel(){
        return simulationMemoryLabel;
    }

    public JTextField getPlayerNameField() {
        return playerNameField;
    }

    public JLabel getNameLabel() {
        return nameLabel;
    }

    public JTextField getNumberOfSimulationGamesField() {
        return numberOfSimulationGamesField;
    }

    public JLabel getSimulationGamesLabel() {
        return simulationGamesLabel;
    }

    public JSpinner getFramesPerSecondSpinner() {
        return framesPerSecondSpinner;
    }

    public JPanel getFpsPanel() {
        return fpsPanel;
    }

    public JComboBox getAI1To3ComboBox(){
        return AI1To3ComboBox;
    }

    public JComboBox getAI4ComboBox(){
        return AI4ComboBox;
    }

    public JLabel getAI1To3Label() {
        return AI1To3Label;
    }

    public JLabel getAI4Label() {
        return AI4Label;
    }

    public JFrame getTestFrame(){
        return testFrame;
    }
    
    public JButton getTestButton(){
        return testButton;
    }
}



