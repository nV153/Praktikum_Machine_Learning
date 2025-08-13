package game.AI.AiCFR;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import game.Logger;

/** This class provides methods for clustering simulation data. */

public class KMeansClusterer {

    /**
    * Initializes a cluster model based on the specified mode ("play" or "pass") using the provided Logger.
    *
    * @param mode   The mode of the cluster model ("play" or "pass").
    * @param logger The Logger object containing the necessary data for model generation.
    * @throws Exception If an error occurs during the initialization or generation of the cluster model.
    */
    public static void initClusterModel(String mode, Logger logger){      
        try {
            if(mode.equals("play")){
                generateModel("play", logger);
                System.out.println("Playmodell wurde erfolgreich erstellt!");
 
            }else if(mode.equals("pass")){          
                generateModel("pass", logger);
                System.out.println("Passmodell wurde erfolgreich erstellt!");
                
            }else if (mode.equals("playLow")){
                generateModel("playLow", logger);
                System.out.println("Playmodell low wurde erfolgreich erstellt!");
            }else{
                System.out.println("Der angegebene Modus in initClusterModel() existiert nicht!");
            }                 
        } catch (Exception e) {
            System.out.println("Fehler beim Initialisieren des Clustermodells " + mode);
            e.printStackTrace();
        }          
    }

    /**
    * Generates a cluster model based on the specified action ("play" or "pass") using the provided Logger.
    * This method builds the cluster model using the Weka library and saves it to a file. 
    * A corresponding HashMap with regret and strategy values for the built model is initialized and saved to a file.
    */
    private static void generateModel (String action, Logger logger) throws Exception{
        String filename;
        String filenameHashMap;
    
        if (action.equals("play")) {
            filename = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\playCluster.model";
            filenameHashMap = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPlay.json";
        }
        else if (action.equals("playLow")){
            filename = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\playLowCluster.model";
            filenameHashMap = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPlayLow.json";
        } else {
            filename = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\passCluster.model";
            filenameHashMap = "projecthearts\\src\\game\\AI\\AiCFR\\Data\\clusterInfoSetsPass.json";
        }

        Instances dataset;
        if(action.equals("play") || action.equals("playLow")){
            dataset = loadGameInfoPlay(logger);
        }else{
            dataset = loadGameInfoPass(logger);
        }

        // Generate a SimpleKMeans object
        SimpleKMeans kmeans = new SimpleKMeans();
        int numClusterCent;
        if (action.equals("play")){
            numClusterCent = 5000;  
        }
        else if (action.equals("playLow")){
            numClusterCent = 1000;   
        }
        else {
            numClusterCent = 1000;  
        }
         
        kmeans.setNumClusters(numClusterCent);
        kmeans.setDistanceFunction(new weka.core.ManhattanDistance());
        kmeans.buildClusterer(dataset);

        // Generate the hash map for regret and strategy
        HashMap<String, double[][]> infoSets = new HashMap<String, double[][]>();

        for (int i = 0; i <=numClusterCent-1; i++){
            int numActions;
            if (action.equals("pass")) numActions = 13;
            else numActions = getNumActionsOfCluster(kmeans, i);
            double[][] values = CFRUtil.initClusterInfoSetValues(numActions);
            infoSets.put(Integer.toString(i), values);
        }

        printClusterCentroidsAndSize(kmeans);

        // save cluster and hash map
        SerializationHelper.write(filename, kmeans);
        CFRUtil.serializeInfoSets(infoSets, filenameHashMap);   
    }

    /**
    * Loads game information for the "play" mode from the provided Logger object and creates Instances accordingly.
    * This method constructs Instances based on attributes defined for the "play" mode and fills them with data extracted from the Logger.
    */
    private static Instances loadGameInfoPlay(Logger logger){    
        ArrayList<Attribute> attributes = createATTPlay();
        Instances data = new Instances("dataset", attributes, 0);  
        int numberOfInstances = logger.getTricksPlayed().size(); 
        
        for (int i = 0; i < numberOfInstances; i++) {
            
            double[] temp = CFRUtil.getValuesFromLogger(logger, "play", i, -1, 3);

            Instance randomInstance = new DenseInstance(1.0, temp);
            randomInstance.setDataset(data);
            data.add(randomInstance);
        }
        return data;   
    }

    /**
    * Loads game information for the "pass" mode from the provided Logger object and creates Instances accordingly.
    * This method constructs Instances based on attributes defined for the "pass" mode and fills them with data extracted from the Logger.
    */
    private static Instances loadGameInfoPass(Logger logger){    
        ArrayList<Attribute> attributes = createATTPass();
        Instances data = new Instances("dataset", attributes, 0);  
        int numberOfInstances = logger.getCardsPassed().size();

        for (int i = 0; i < numberOfInstances; i++) {
            double[] temp = CFRUtil.getValuesFromLogger(logger, "pass", -1, i,3);

            Instance randomInstance = new DenseInstance(1.0, temp);
            randomInstance.setDataset(data);
            data.add(randomInstance);
        }

        return data;   
    }

    /**
    * Creates and returns an ArrayList of Attribute objects for the "play" mode.
    * The attributes include the trick number, player cards, start player number, and trick cards.
    *
    * @return An ArrayList of Attribute objects representing the attributes for the "play" mode.
    */
    public static  ArrayList<Attribute> createATTPlay(){
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("Trick Number"));

        for (int i = 1; i <= 13; i++) {
            Attribute attribute = new Attribute("PCard " + i);
            attributes.add(attribute);
        }
        attributes.add(new Attribute("Start P. Number"));
        for (int i = 1; i <= 4; i++) {
            Attribute attribute = new Attribute("Trick Card " + i);
            attributes.add(attribute);
        }
        attributes.add(new Attribute("QueenOfSpadesPlayed"));
        attributes.add(new Attribute("num Hearts Played"));

        return attributes;
    } 

    /**
    * Creates and returns an ArrayList of Attribute objects for the "pass" mode.
    * The attributes include the direction to pass, the passed cards and hand cards before passing.
    *
    * @return An ArrayList of Attribute objects representing the attributes for the "pass" mode.
    */
    public static  ArrayList<Attribute> createATTPass(){
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("Direction to Pass"));

        for (int i = 1; i <= 13; i++) {
            Attribute attribute = new Attribute("HCard " + i);
            attributes.add(attribute);
        }

        return attributes;
    }

    /**
    * Loads a cluster model based on the specified mode ("play" or "pass").
    *
    * @param mode The mode of the cluster model to be loaded ("play" or "pass").
    * @return The loaded cluster model as a SimpleKMeans object.
    */
    protected static SimpleKMeans loadClusterModel(String mode){
        SimpleKMeans kmeans = null;
        
        try{
           if(mode.equals("play")){
                kmeans = (SimpleKMeans) SerializationHelper.read(new FileInputStream("projecthearts\\src\\game\\AI\\AiCFR\\Data\\playCluster.model"));
        
            }else if(mode.equals("pass")){
                kmeans = (SimpleKMeans) SerializationHelper.read(new FileInputStream("projecthearts\\src\\game\\AI\\AiCFR\\Data\\passCluster.model"));   
        
            }else if(mode.equals("playLow")){
                kmeans = (SimpleKMeans) SerializationHelper.read(new FileInputStream("projecthearts\\src\\game\\AI\\AiCFR\\Data\\playLowCluster.model"));
            }
            else {
                System.out.println("Modus existiert nicht, gültig sind 'play' oder 'pass'!");
            }  
        } catch (Exception e){
            System.out.println("Fehler beim Auslesen der Modeldatei");
        }
        return kmeans;    
    }

    private static int getNumActionsOfCluster(SimpleKMeans kmeans, int index){
        Instance instance = kmeans.getClusterCentroids().instance(index);
        int num = 0;

        for (int i=0; i<13; i++){
            if (instance.value((CFRUtil.ACTION_START_IDX + i)) != -100) num++;
            else break;
        }
        return num;
    }

    /**
    * Prints the centroids and sizes of clusters in the given SimpleKMeans clusterer.
    *
    */
    private static void printClusterCentroidsAndSize(SimpleKMeans kmeans){
        Instances centroids = kmeans.getClusterCentroids();
        for (int i = 0; i < centroids.numInstances(); i++) {
            System.out.println("Cluster " + i + ": " + centroids.instance(i));
        }

        double[] clusterSizes = kmeans.getClusterSizes();
        for (int i = 0; i < clusterSizes.length; i++) {
            System.out.println("Cluster " + i + " hat " + clusterSizes[i] + " Instanzen.");
        }
    }

    //zum Testen, TODO später entfernen
    public static void printOutHashMapDouble(HashMap<String, double[][]> infoSets){
        for (Map.Entry<String, double[][]> entry : infoSets.entrySet()) {
            String key = entry.getKey();
            double[][] values = entry.getValue();
            double[] regret = values[0];
            double[] strat = values[1];
            
            System.out.println("Schlüssel: " + key);
            System.out.print("Regret: ");
            for (double value : regret) {
                System.out.print(value + " ");
            }
            System.out.print("Strategy: ");
            for (double value : strat) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
  

  
}