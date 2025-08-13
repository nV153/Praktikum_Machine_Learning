package game.AI.AiCFR;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationReLU;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import game.Card;
import game.Difficulty;
import game.GameState;
import game.HandCards;
import game.Logger;
import game.Memory;
import game.Player;
import game.Trick;
import game.AI.AiInterface;
import game.AI.Utils.CardEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class DeepCFR implements AiInterface {
    static int rows;
    private static final int playerNo = 3;
    static int inputSizePlay = 21;      //TrickNo , 13 Handkarten , StartpNo , 4 Trickarten , Anzahl Qos + Hearts
    static int OutputSizePlay = 13;     //13 Handkarten
    static int inputSizePass = 14;      //Direction to Pass , 13 Handkarten
    static int OutputSizePass =13;      //13 Handkarten

    private final static String fileNamePass = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPass.json";
    private final static String fileNamePlay = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPlay.json";
    private static Logger DLogger;
    private Difficulty difficulty = Difficulty.EXPERT;
    private Memory memory = Memory.HIGH;
    


  
    /**
    * Trains a Deep Counterfactual Regret Minimization (Deep CFR) model.
    *
    * @param numIT         The total number of iterations the training should go through.
    * @param numITperG     The number of iterations per game (batch) to run through in each training cycle.
    */
    public static void trainDeepCFR(int numIT ,int numITperG){
        File f1  = new File("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser");
        File f2  = new File("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");
        boolean PassExits;
        boolean PlayExits;


        if (f1.exists()) {
            PassExits = true;
        } else {
            PassExits =false;
        }

        if (f2.exists()) {
            PlayExits = true;
        } else {
            PlayExits =false;
        }

        BasicNetwork BackUpPass = null;
        BasicNetwork BackUpPlay = null; 
        //BackUps laden
        if(PassExits){
            BackUpPass = loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser");
        }
        if(PlayExits){
            BackUpPlay = loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");
        }
       
        //Deaktiviert das vergleichen mit der alten Version
        PassExits = false;
        PlayExits = false;


        AiCFRClustering t = new AiCFRClustering();
        for(int i = 0; i < numIT; i++){    
            t.simulateGames(-1, numITperG, Difficulty.EASY, Memory.LOW, Difficulty.EASY, Memory.LOW);
            DLogger = t.getLog();
                     

            trainPass();       
            trainPlay();
        }  

        //Teste die einzelnen Netzwerke
        //Bereite neue Testdaten vor
        if(PlayExits || PassExits){
            t.simulateGames(3, 50, Difficulty.EASY, Memory.LOW, Difficulty.EASY, Memory.LOW);  
        }


        if(PlayExits){
            rows = DLogger.getTricksPlayed().size();           //wird in readCSV mit gezählt
            MLDataSet trainingSetPlay = prepareDataPlay();
            System.out.println("Test Alt / Neu Play");
            double HROldPlay = testNeuralNetwork(BackUpPlay, trainingSetPlay , rows);
            double HRNewPlay = testNeuralNetwork(loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser"), trainingSetPlay, rows);
    
            if(HROldPlay > HRNewPlay){
                //System.out.println("Das Play-Training war nicht sinnvoll => Verwerfe");
                saveNeuralNetwork(BackUpPlay, "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");
            }else{
                //System.out.println("Das Play-Training war sinnvoll!");
            }
        }


        if(PassExits){
            rows = DLogger.getTricksPlayed().size();
            MLDataSet trainingSetPass = prepareDataPass();
            System.out.println("Test Alt / Neu Pass");
            double HROldPass = testNeuralNetwork(BackUpPass, trainingSetPass , rows);
            double HRNewPass = testNeuralNetwork(loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser"), trainingSetPass, rows);
    
            if(HROldPass > HRNewPass){
                //System.out.println("Das Pass-Training war nicht sinnvoll => Verwerfe");
                saveNeuralNetwork(BackUpPass, "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser");
            }else{
                //System.out.println("Das Pass-Training war sinnvoll!");
            }    
        }
     


        Encog.getInstance().shutdown();
    }

    private static void trainPass(){
        String s = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser";
        File f  = new File("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser");
        BasicNetwork network;

        if (f.exists()) {
            network = loadNeuralNetwork(s);
        } else {
            network = createNeuralNetworkPass();
        }

        MLDataSet trainingSet = prepareDataPass();
        trainNeuralNetwork(network, trainingSet);

        System.out.println("Pass Training Iteration abgeschlossen!");
        saveNeuralNetwork(network, s);

        Encog.getInstance().shutdown();

    }

    private static void trainPlay(){
        String s = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser";
        File f  = new File("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");
        BasicNetwork network;

        if (f.exists()) {
            network = loadNeuralNetwork(s);
        } else {
            network = createNeuralNetworkPlay();
        }
       
        //MLDataSet trainingSet = readCSV("projecthearts\\src\\game\\AI\\AiCFR\\Data\\newPlayDataOfSim.csv" , 1);
        MLDataSet trainingSet = prepareDataPlay();
        trainNeuralNetwork(network, trainingSet);

        System.out.println("Play Training Iteration abgeschlossen!");
        saveNeuralNetwork(network, s);
        Encog.getInstance().shutdown();

    }

 
    private static MLDataSet prepareDataPass(){
        MLDataSet dataSet = new BasicMLDataSet();
        double[] output = new double[13];

        int size = DLogger.getTricksPlayed().size();
        int passLineCounter = 0;
        int numTricks = DLogger.getDeckSize() / 4;

        

        SimpleKMeans Cluster = null;
        Instances dataset = null;   
        BasicNetwork network = null;  
        HashMap<String, double[][]> Map = null;
        try {
            Cluster = KMeansClusterer.loadClusterModel("pass");
            Map = CFRUtil.deserializeInfoSets(fileNamePass);         
            dataset = new Instances("dataset", KMeansClusterer.createATTPass(), 0);                 
        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Clusters + Map in DeepCFR");
            e.printStackTrace();
        }

        //Durchlaufe Logger Pass Daten
        while (passLineCounter < size/numTricks) {
            //Input Daten
            double[] values = CFRUtil.getValuesFromLogger(DLogger, "pass", passLineCounter, passLineCounter, playerNo);

            //Output Daten
            double[] Gameinfo = new double[inputSizePass +3];   //3 sind die Karten welche gepasst wurden
            for(int i = 0; i < inputSizePass; i++){
                Gameinfo[i] = values[i];
            } 

            output = getGoodProb(values, 0, Cluster, Map, dataset, network);

            //Erstelle DataPair und füge es hinzu
            MLDataPair pair = new BasicMLDataPair(new BasicMLData(values), new BasicMLData(output));
            dataSet.add(pair);

            //System.out.println(passLineCounter);
            passLineCounter++;
        }


        return dataSet; 
    }

    private static MLDataSet prepareDataPlay(){
        MLDataSet dataSet = new BasicMLDataSet();
        double[] output = new double[13];

        int size = DLogger.getTricksPlayed().size();
        int playLineCounter = 0;
        

        SimpleKMeans Cluster = null;
        Instances dataset = null;   
        BasicNetwork network = loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");     ;  
        HashMap<String, double[][]> Map = null;
        try {
            Cluster = KMeansClusterer.loadClusterModel("play");
            Map = CFRUtil.deserializeInfoSets(fileNamePlay);         
            dataset = new Instances("dataset", KMeansClusterer.createATTPlay(), 0);                 
        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Clusters + Map in DeepCFR");
            e.printStackTrace();
        }

        
        //Durchlaufe Logger Pass Daten
        while (playLineCounter < size) {
            //Input Daten
            double[] values = CFRUtil.getValuesFromLogger(DLogger, "play", playLineCounter, -1, playerNo);

            //Output Daten
            double[] Gameinfo = new double[inputSizePass +3];   //3 sind die Karten welche gepasst wurden
            for(int i = 0; i < inputSizePass; i++){
                Gameinfo[i] = values[i];
            } 

            output = getGoodProb(values, 1,  Cluster, Map, dataset, network);

            //Erstelle DataPair und füge es hinzu
            MLDataPair pair = new BasicMLDataPair(new BasicMLData(values), new BasicMLData(output));
            dataSet.add(pair);

            //System.out.println(playLineCounter);
            playLineCounter++;
        }

        return dataSet; 
    }


    private static BasicNetwork createNeuralNetworkPass() {
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(null, true, inputSizePass));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 20));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 20));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, OutputSizePass));
        network.getStructure().finalizeStructure();
        network.reset();
        return network;
    }

    private static BasicNetwork createNeuralNetworkPlay() {
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(null, true, inputSizePlay));   
        network.addLayer(new BasicLayer(new ActivationReLU(), true, OutputSizePlay)); 
        network.addLayer(new BasicLayer(new ActivationReLU(), true, OutputSizePlay));       
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, OutputSizePlay));
        network.getStructure().finalizeStructure();
        network.reset(); 
        return network;
    }

    private static void saveNeuralNetwork(BasicNetwork network, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(network);
            //System.out.println("Neural Network saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static BasicNetwork loadNeuralNetwork(String filePath) {
        BasicNetwork loadedNetwork = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            loadedNetwork = (BasicNetwork) in.readObject();
            //System.out.println("Neural Network loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loadedNetwork;
    }

    private static void trainNeuralNetwork(BasicNetwork network, MLDataSet trainingSet) {
        ResilientPropagation train = new ResilientPropagation(network, trainingSet);
        int it = 1;
        int maxIterations = 100000; 

        do {
            train.iteration();
            //System.out.println("Iteration #" + it + " Error: " + train.getError());
             
            if(it % 10000 == 0){
                System.out.println("Iteration #" + it + " Error: " + train.getError());
            }
            

            if(it >= maxIterations){
                System.out.println("Maximale Anzahl von Iterationen erreicht. Abbruch.");
                
                break;
            } 

            if(train.getError() < 0.000005){
                System.out.println("Fehlerrate ausreichend reduziert. Abbruch.");
                break;
            }
            
            it++;
        } while (true);

        train.finishTraining();
    }

    private static double testNeuralNetwork(BasicNetwork network, MLDataSet trainingSet , int rows) {
        double correctPredictions = 0;
        for (MLDataPair pair : trainingSet) {
            MLData output = network.compute(pair.getInput());
            double predictedValue = output.getData(0);
            double expectedValue = pair.getIdeal().getData(0);
       
            //System.out.println("Input: " + pair.getInput().getData(0) + ", " + pair.getInput().getData(1)+ " Actual: " + output.getData(0) + " Expected: " + pair.getIdeal().getData(0));

        //Überprüfe, ob die Vorhersage korrekt ist
        if (Math.abs(predictedValue - expectedValue) < 0.5) {
            correctPredictions++;
        }
        }

        double hR = correctPredictions ;
        //Gib die Anzahl der korrekten Vorhersagen aus
        System.out.println("Number of correct predictions: " + hR  + " aus " + rows );

        return hR/rows;
    }

    @Override
    /**
    * Determines the best pass option according to the DeepCFR for a given game state, player number, and receiver number.
    *
    * @param g             The current game state.
    * @param playerNo      The number representing the player making the pass.
    * @param receiverNo    The number representing the intended receiver of the pass.
    * @return              An array representing the best pass option, typically containing the coordinates or index of the pass.
    */
    public int[] bestPass(GameState g, int playerNo, int receiverNo) {
        ArrayList<Card> listOfHandCards = CardEvaluation.getPlaybleCards(g.getPlayers()[playerNo].getHandCards());

        BasicNetwork network  = loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPass.ser");

        
        double[] input = new double[14];

        int t = 1;
        for(Card x : listOfHandCards){
            input[t] = x.getId();
            t++;
        }

        //Eingabe  umwandeln
        MLData inputData = new BasicMLData(input);
        MLDataPair inputPair = new BasicMLDataPair(inputData, null);

        //Netzwerk Vorhersage
        MLData predictedOutput = network.compute(inputPair.getInput());

        double[] probabilities = new double[13];
        for (int i = 0; i < predictedOutput.size(); i++) {
            probabilities[i] = predictedOutput.getData(i);
        }

        int[] temp = getTopIndices(probabilities , 3);      
        
        //Kartenids auslesen
        int[] passCards = new int[3];
        passCards[0] = listOfHandCards.get(temp[0]).getId();
        passCards[1] = listOfHandCards.get(temp[1]).getId();
        passCards[2] = listOfHandCards.get(temp[2]).getId();
       

         
        for(int i = 0; i <= 2 ; i++){
            //System.out.println("DeepCFR will Karte passen " + passCards[i]);
        } 
            
        return passCards;

    }

    
    @Override
    /**
    * Determines the best move option according to the DeepCFR for a given game state and player number.
    *
    * @param g             The current game state.
    * @param playerNo      The number representing the player making the pass.
    * @return              An array representing the best pass option, typically containing the coordinates or index of the pass.
    */
    public int bestMove(GameState g, int playerNo) {
        difficulty = g.getDifficultyOfPlayer(playerNo);
        memory = g.getMemoryOfPlayer(playerNo);
        //Lade Informationen um eigene Handkarten einzusehen
        Player aiPlayer = g.getPlayers()[playerNo];
        HandCards currentHandCards = aiPlayer.getHandCardsPlayable();
        ArrayList<Card> playableCards = CardEvaluation.getPlaybleCards(currentHandCards);
        Trick currentTrick = g.getNewestTrick();

        if (playableCards.size() == 1){             //Falls nur eine spielbare Karte
            return playableCards.get(0).getId();
        }


        double[] values = new double[inputSizePlay];  //21 Eingangsknoten

        for(int i = 0; i< 21;i++){
            values[i] = -100;
        }

        values[0] = g.getTricksPlayed();      //Trickplayed

        int a = 1;
        for(Card x : playableCards){    //Spielbare Handkarten
            values[a] = x.getId();
            a++;
        }

        List<Integer> cardsToConsider = g.getCardsPlayedWithMemory(playerNo);
        values[14] = g.getStarterOfNewestTrick();;     //StartPNo
        values[19] = CFRUtil.getQueenOfSpadesPlayedWithMemory(cardsToConsider);;    //Qos played
        values[20] = CFRUtil.getNumHeartsPlayedWithMemory(cardsToConsider);         //Count played Cards
        
        int[] Tc = currentTrick.getCardsPlayed();
        for(int i = 0; i< 4; i++){
            if(Tc[i] != -100){
                values[15+i] = Tc[i];
            }
        }

        BasicNetwork network  = loadNeuralNetwork("projecthearts\\src\\game\\AI\\AiCFR\\Data\\DeepCFRNeuralPlay.ser");

        //Eingabe  umwandeln
        MLData inputData = new BasicMLData(values);
        MLDataPair inputPair = new BasicMLDataPair(inputData, null);

        //Netzwerk Vorhersage
        MLData predictedOutput = network.compute(inputPair.getInput());

        
        double[] probabilities = new double[13];
        for (int i = 0; i < predictedOutput.size(); i++) {      
            probabilities[i] = predictedOutput.getData(i);
            //System.out.print(probabilities[i]+ "  ") ;
        }


        int[] CardstoPlay = getTopIndices(probabilities, 4);
        int CardtoPlayIndex = 0;

        switch (difficulty) {
            case EASY:
                CardtoPlayIndex = CardstoPlay[3];
                break;

            case MEDIUM:
                CardtoPlayIndex = CardstoPlay[2];           
                break;

            case HARD:
                CardtoPlayIndex = CardstoPlay[1];           
                break;

            case EXPERT:
                CardtoPlayIndex = CardstoPlay[0];           
                break;
        
            default:
                break;
        }
        
        int CardtoPlay;

        try {
            CardtoPlay = playableCards.get(CardtoPlayIndex).getId();   
        
        } catch (Exception e) {
            CardtoPlay = playableCards.get(CardstoPlay[0]).getId(); 
        }
                    
        return CardtoPlay; 


    }

    //Gibt die Werte mit der höchsten Wahrscheinlichkeit zurück
    private static int[] getTopIndices(double[] prob, int num) {
        if (prob == null || num <= 0 || num > prob.length) {
            throw new IllegalArgumentException("Ungültige Eingabeparameter");
        }

        // Erstelle ein Array mit Indizes von 0 bis prob.length-1
        Integer[] indices = new Integer[prob.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        // Sortiere die Indizes basierend auf den Werten im prob-Array
        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(prob[b], prob[a]);
            }
        });

        // Extrahiere die ersten 'num' Indizes
        return Arrays.stream(indices).mapToInt(Integer::intValue).limit(num).toArray();
    }

    //modi 0 = pass , 1 = play
    private static double[] getGoodProb(double[] gameInfo , int modi  , SimpleKMeans cluster , HashMap<String, double[][]>  Map ,Instances dataset , BasicNetwork network ){     
        double[] r = new double[13]; 

        if(modi == 0){      //Pass  , directiontoPlay + 13Handkarten
            try {
                Instance newInstance = new DenseInstance(1.0, gameInfo);     
                newInstance.setDataset(dataset);
                int ClusterNo =  cluster.clusterInstance(newInstance);              
    
                double[] strat = Map.get(Integer.toString(ClusterNo))[1];
                Instance clusterCenter = cluster.getClusterCentroids().instance(ClusterNo);

                double[] clustervalues = new double[14];        //Betrachte zugeorndetes Clusterzentrum
                for(int k = 0; k <14;k++){
                    clustervalues[k] = clusterCenter.value(k);
                }

                //Schaue ob Karte im Cluster existiert , Handkarten beginnen bei 1
                for(int i = 1; i < gameInfo.length;i++){     // Durchlaufe Handkarten              
                    boolean temp = false;
                    int index = -1;                           
                    
                    for(int j  = 1 ; j < gameInfo.length;j++){          //Durchläuft Cluster Handkarten
                        if(gameInfo[i] == clustervalues[j]  &&  gameInfo[i] !=-100){
                            temp = true;
                            index = j;
                            break;
                        }
                    }

                    //System.out.println("HandKarte  " + gameInfo[i]  +"am Index " + index +  " temp  " + temp) ;
                    if(temp){       //Karte exisitiert im Cluster => übernehme Werte
                        r[i-1] = strat[index-1];        //Dies sind nur Arrays der Größe 13 also um 1 versetzt
                    }else{          //Karte exisitiert nicht im Cluster => suche ähnliche Karte und übernehme
                        index = findClosestIndex(gameInfo[i], clustervalues);       //In Methode wird eine maximal Abstand von 4 vorrausgesetzt
                        //System.out.println("Wir haben ein ähnliche karte gefunden am Index" + index);
                        if(index != -1){
                            r[i-1] = strat[index-1];
                        }else{
                            r[i-1] = 0;
                        }                        
                    }                   
                }

                 
                /* 
                System.out.println("GameInfo: ");
                for(int i = 0; i < gameInfo.length;i++){
                    System.out.print(gameInfo[i]+ "  ");             
                }
                System.out.println("Zu Cluster : "  +ClusterNo);
                System.out.println("Wir erhalten die Prob ");
                for(int i = 1; i < gameInfo.length;i++){
                    System.out.print(r[i-1]+ "  ");             
                }  */


                
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Fehler im getgoodProb im Pass bei DeepCFR");
            }                     

        }else{      //Play 
            try {
                Instance newInstance = new DenseInstance(1.0, gameInfo);

                newInstance.setDataset(dataset);
                int ClusterNo = cluster.clusterInstance(newInstance);       //über alle 19 Werte wie Clusterbildung
               
                double[] strat = Map.get(Integer.toString(ClusterNo))[1];
                Instance clusterCenter = cluster.getClusterCentroids().instance(ClusterNo);

                 
                //Eingabe  umwandeln
                MLData inputData = new BasicMLData(gameInfo);
                MLDataPair inputPair = new BasicMLDataPair(inputData, null);
                //Netzwerk Vorhersage
                MLData predictedOutput = network.compute(inputPair.getInput());
                
                double[] probabilities = new double[13];
                for (int i = 0; i < predictedOutput.size(); i++) {      
                    probabilities[i] = predictedOutput.getData(i);
                }
   

                for(int i = 1; i <= 13;i++){      //Schaue ob Karte im Cluster existiert  , durchläuft Handkarten              
                    boolean temp = false;
                    int index = -1;             

                    double[] clustervalues = new double[13];
                    for(int k = 1; k <= 13;k++){                     //Handkarten beginnen bei 1
                        clustervalues[k-1] = clusterCenter.value(k);     
                    }
                    
                    for(int j  = 1 ; j <= 13;j++){          //durchläuft Cluster Handkarten
                        if(gameInfo[i] == clustervalues[j-1]  &&  gameInfo[i] !=-100){          //Achtung clustervalues beginnt bei 0 !
                            temp = true;
                            index = j;
                            break;
                        }
                    }

                    if(temp){       //Karte exisitiert im Cluster => übernehme Werte        
                        r[i-1] = strat[index-1];
                    }else{          //Karte exisitiert nicht im Cluster => suche ähnliche Karte und übernehme
                        if(gameInfo[i] == -100){
                            r[i-1] = 0.0;
                        }else{
                            index = findClosestIndex(gameInfo[i], clustervalues);
                            if(index != -1 ){
                                r[i-1] = strat[index-1];
                            }else{
                                if(probabilities[i-1] != 0.0 && probabilities[i-1] != 1.0){     //Um Daten nicht zu überschreiben
                                    r[i-1] = probabilities[i-1];
                                }else{
                                    r[i-1] = 0;
                                }                             
                            }  
                        }
                                             
                    }                   

                }


                /* 
                System.out.println("GameInfo: ");
                for(int i = 0; i < gameInfo.length;i++){
                    System.out.print(gameInfo[i]+ "  ");             
                }
                System.out.println("Zu Cluster : "  +ClusterNo);
                System.out.println("Wir erhalten die Prob ");
                for(int i = 1; i < 14;i++){
                    System.out.print(r[i-1]+ "  ");             
                }  
                System.out.println("  "); */
            

                
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Fehler im getgoodProb im Play bei DeepCFR");
            }
               
        }

        return r;
    }


    //Such naheliegende Karte im Array um im nachhinein deren Clusterprob zu übernehmen
    private static int findClosestIndex(double targetValue, double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array darf nicht null oder leer sein.");
        }

        double minDistance = Double.MAX_VALUE;
        int closestIndex = -1;

        for (int i = 1; i < array.length; i++) {
            double distance = Math.abs(targetValue - array[i]);

            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        if(minDistance < 5){        //Distanz soll nicht zu groß sein
            return closestIndex;
        }else{
            return -1;
        }
    }



    
}