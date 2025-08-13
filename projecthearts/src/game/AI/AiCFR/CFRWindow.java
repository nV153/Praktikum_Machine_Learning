package game.AI.AiCFR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CFRWindow {
    public CFRWindow() {
        // Erstelle das Hauptfenster
        JFrame fenster = new JFrame("CFR Einstellungen");
        fenster.setSize(600, 250);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenster.setResizable(false);
        fenster.setLocationRelativeTo(null);
        fenster.setLayout(new GridLayout(4, 3));

        for (int i = 1; i <= 3; i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            //Mehre Switch Anweisungen für Übersicht
            switch (i) {
                case 1:
                    JLabel l1 = new JLabel("CFRBasic :");
                    panel.add(l1);
                    break;

                case 2:     
                    JLabel l2 = new JLabel(" DeepCFR :" );     
                    panel.add(l2); 
                    break;

                case 3:
                    JLabel l3 = new JLabel("   MCCFR :" );
                    panel.add(l3);
                    break;  
                
            }

            //Textfelder evtl. anpassen
            JTextField textField1 = new JTextField();
            JLabel info = new JLabel("It. / Iter.G");
            panel.add(info);
            JTextField textField2 = new JTextField();
            textField1.setColumns(5);
            textField2.setColumns(5);
            textField1.setText("3");
            textField2.setText("10");

            
            JButton button1 = new JButton("Train");
            JButton button2 = new JButton("Delete Pass");
            JButton button3 = new JButton("Delete Play");
            //Hier Actionlistener einfügen
            switch (i) {
                case 1:
                    button1.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {                   
                            try {
                                int i1 = Integer.parseInt(textField1.getText());
                                int i2 = Integer.parseInt(textField2.getText());
                                System.out.println("Training mit " + i1  + i2);

                                //TODO: Hier Train Befehl
                                //BasicCFR a = new BasicCFR();
                                //a.trainCFR(3 , i1,i2);

                            } catch (Exception ee) {
                                JOptionPane.showMessageDialog(null, "Bitte nur Integer eingeben!");
                            }
                        }
                    });
                    button2.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {     
                            int antwort = JOptionPane.showConfirmDialog(null,"Sind Sie sicher, dass Sie  die Pass Ai löschen möchten?", "Bestätigung",JOptionPane.YES_NO_OPTION);
                            if (antwort == JOptionPane.YES_OPTION) {
                                //TODO: Basis CFR Play löschen
                                System.out.println("Lösche Basic CFRPass");
                            }
                        }
                    });
                    button3.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {     
                            int antwort = JOptionPane.showConfirmDialog(null,"Sind Sie sicher, dass Sie  die Play Ai löschen möchten?", "Bestätigung",JOptionPane.YES_NO_OPTION);
                            if (antwort == JOptionPane.YES_OPTION) {
                                //TODO: Basis CFR Play löschen
                                System.out.println("Lösche Basic CFRPLay");
                            }
                        }
                    });
                    break;

                case 2:
                button1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {                   
                        try {
                            int i1 = Integer.parseInt(textField1.getText());
                            int i2 = Integer.parseInt(textField2.getText());
                            System.out.println("Training mit " + i1  + i2);

                            DeepCFR.trainDeepCFR(i1, i2);


                        } catch (Exception ee) {
                            JOptionPane.showMessageDialog(null, "Bitte nur Integer eingeben!");
                        }
                    }
                });
                button2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {     
                        int antwort = JOptionPane.showConfirmDialog(null,"Sind Sie sicher, dass Sie  die Pass Ai löschen möchten?", "Bestätigung",JOptionPane.YES_NO_OPTION);
                        if (antwort == JOptionPane.YES_OPTION) {

                            String s = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser";
                            File f = new File(s);

                            if(f.exists()){
                                System.out.println("Lösche DeepCFR Pass");
                                try {
                                    Files.delete(Paths.get(s));   
                                    System.out.println("DeepCFR Pass wurde gelöscht");
                                } catch (Exception ee) {
                                    System.out.println("Fehöer beim löschen von DeepCFR Pass");
                                }                  
                            }else{
                                System.out.println("DeepCFR Pass existiert nicht und kann nicht gelöscht werden!");
                            }                                                    
                        }
                    }
                });
                button3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {     
                        int antwort = JOptionPane.showConfirmDialog(null,"Sind Sie sicher, dass Sie  die Play Ai löschen möchten?", "Bestätigung",JOptionPane.YES_NO_OPTION);
                        if (antwort == JOptionPane.YES_OPTION) {
                            
                            String s = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser";
                            File f = new File(s);

                            if(f.exists()){
                                System.out.println("Lösche DeepCFR Play");
                                try {
                                    Files.delete(Paths.get(s));   
                                    System.out.println("DeepCFR Play wurde gelöscht");
                                } catch (Exception ee) {
                                    System.out.println("Fehöer beim löschen von DeepCFR Play");
                                }                  
                            }else{
                                System.out.println("DeepCFR Play existiert nicht und kann nicht gelöscht werden!");
                            }                                                    

                        }
                    }
                });
                    break;

                case 3:
                    //TODO:MCCFR
                    break;

            }

            
            panel.add(textField1);
            panel.add(textField2);
            panel.add(button1);
            panel.add(button2);
            panel.add(button3);

            

            fenster.add(panel);
        }

        // Zeige das Fenster an
        fenster.setVisible(true);
    }
}

