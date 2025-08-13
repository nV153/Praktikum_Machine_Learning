import javax.swing.SwingUtilities;
import GUI.GamePlayGUI;
import GUI.HeartsGameGUI;
import game.GameController;
import game.GameState;


/**
* This class is the program entry class for the game Hearts and contains the program entry point, i.e. the main method.
* <p>
* The program corresponds to the MVP (model-view-presenter) pattern with passive view, 
* see Wikipedia: https://de.wikipedia.org/wiki/Model_View_Presenter#Passive_View
* Compare with MVC pattern.
* The model is an instance of the GameState class.
* There are two views, one instance of the HeartsGameGUI class as the start screen and one instance of the
* class GamePlayGUI as the game screen.
* The presenter or controller is an instance of the GameController class, which controls the program and game flow.
* <p>
*/
public class HeartsMain {
    /**
     * This method represents the entry point of the program.
     * It creates the components model, two views and the presenter (or controller) of the MVP pattern 
     * and initializes the controller until the start screen is displayed.
     * <p>
     * @param args An array containing strings.
     */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {                
                  
                GameState gameState = new GameState();
                HeartsGameGUI heartsGameGUI = new HeartsGameGUI();
                GamePlayGUI gamePlayGUI = new GamePlayGUI();
                GameController gameController = new GameController(gameState, heartsGameGUI, gamePlayGUI);
                gameController.initController();   

            }
         });
    }
}

