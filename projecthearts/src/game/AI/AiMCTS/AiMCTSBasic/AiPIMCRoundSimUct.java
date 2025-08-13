package game.AI.AiMCTS.AiMCTSBasic;

import java.util.List;

import game.Difficulty;
import game.GameState;
import game.Player;
import game.AI.AiMCTS.UCT;

/**
 * This AI class works with MCTS, where only imperfect information about the playable cards 
 * in the opponents' hand is available in the expansion phase and 
 * UCT is used in the selection phase. The simulation phase lasts until the end of the round.
 * More precisely, this AI only has imperfect game information about the game state, in particular 
 * the opponent's hand cards. By determinizing the imperfect information, or more precisely, 
 * by estimating the opponent's hand cards, this AI can treat the game as one with perfect 
 * information. The opponent's hand cards are estimated depending on the memory level: 
 * the higher the level, the further back the AI can remember tricks and thus exclude these 
 * cards that have already been played from the distribution of possible hand cards to the opponents.
 * It can be referred to the following literature:
 * "... , determinization may involve guessing the cards an opponent might have in 
    their hand in a card game (see Fig. 5).
    Perfect Information MCTS (PIMC) assumes that all hidden information is determined 
    and then the game is treated as a perfect information one with respect to a certain assumed 
    state of the world." From: Swiechowski et al.: Monte Carlo Tree Search: a review of 
    recent modifcations and applications, 2022, page 2515 (chapter 4.1).
*/
public class AiPIMCRoundSimUct extends AiMCTSCheatingPIRoundSimUct {

    //Konstruktor, der den Superaufruf tätigen muss!
    public AiPIMCRoundSimUct(){
        super();
    }

    @Override
    public int[] bestPass(GameState gameState, int playerNo, int receiverNo) {

        //TODO: Nach TESTS, welche Werte am besten sind oder, hier evtl. andere Varianten für 
        //      Bestimmung der numberOfIterations und expansionDepth nehmen.
        //      D.h. pro konkreter KI-Klasse kann hier noch ein Unterschied gemacht werden! 
        //      Diese Werte müssen also nicht für alle KI-Klassen gleich sein!
        
        //System.out.println("maxNumberOfPossiblePasses: " + maxNumberOfPossiblePasses); //TODO: Test
        //Scanner in = new Scanner(System.in); //TEST        
        //System.out.println("Press Enter to continue..."); //TEST
        //String s = in.nextLine(); //TEST
        //System.out.println("Continuing... "); //TEST      

        //TODO: Determinization von gameState machen! determinizedGameState = ...
        //      -> siehe ...ISMCTS, GameState determinization, doMove -> PlayableCards setzen!!! Und Weiteres?
        //      , dann return super.bestPass(determinizedGameState, playerNo, receiverNo);

        //Einen möglichen gameState mithilfe des tatsächlichen gameState schätzen, d. h., den unbekannten gameState determinisieren.
        //  Genauergesagt, es werden die gegnerischen Handkarten (in Abhängigkeit der Memory, wobei hier bei bestPass letztlich doch unabhängig) geschätzt.
        //  Die eigenen Handkarten (und die eigenen PlayableHandCards) bleiben durch determinization unverändert!!!
        GameState determinizedGameState = gameState.deepCopy();
        determinizedGameState.determinization(playerNo);
        //  Die gegnerischen! PlayableHandCards sind noch nicht gesetzt worden. Dies muss im Folgenden noch gemacht werden.
        //System.out.println("AiPIMCRoundSimUct bestPass... for...");
        for (Player player : determinizedGameState.getPlayers()){
            if (player.getPlayerNo() != playerNo) {
                //PlayableHandCards des Gegners bestimmen.
                if (determinizedGameState.getTricksPlayed() == 0){
                    //Tritt bei bestPass immer ein.
                    player.setHandCardsPlayableFromRulesAndIsInHandInTrick0or1(determinizedGameState);
                } else {
                    //Ein anderer Fall existiert nicht!
                }
            }
            //System.out.println("AiPIMCRoundSimUct bestPass... for-nächste Iteration...");
        }
        
        return super.bestPass(determinizedGameState, playerNo, receiverNo);
    }

    // ****OKAY
    @Override
    protected void setDifficultyForBestPass(GameState gameState, int playerNo) {
        //Zunächst in Abhängigkeit der Difficulty die numberOfIterations setzen
        this.numberOfIterations = Difficulty.determineNumberOfIterationsVariant2(gameState, playerNo);
        
    }

    // ****OKAY
    @Override
    protected void setMemoryForBestPass(GameState gameState, int playerNo) {
        //Memory entfällt für bestPass  
        ;
    }

    @Override
    public int bestMove(GameState g, int playerNo) {

        //System.out.println("AiPIMCRoundSimUct bestMove...");

        //TODO: Nach TESTS, welche Werte am besten sind oder, hier evtl. andere Varianten für 
        //      Bestimmung der numberOfIterations und expansionDepth nehmen.
        //      D.h. pro konkreter KI-Klasse kann hier noch ein Unterschied gemacht werden! 
        //      Diese Werte müssen also nicht für alle KI-Klassen gleich sein!

        //Einen möglichen gameState mithilfe des tatsächlichen gameState schätzen, d. h., den unbekannten gameState determinisieren.
        //  Genauergesagt, es werden die gegnerischen Handkarten (in Abhängigkeit der Memory) geschätzt.
        //  Die eigenen Handkarten (und die eigenen PlayableHandCards) bleiben durch determinization unverändert!!!
        //System.out.println("AiPIMCRoundSimUct bestMove... deepCopy...");
        GameState determinizedGameState = g.deepCopy();
        //System.out.println("AiPIMCRoundSimUct bestMove... determinization...");
        determinizedGameState.determinization(playerNo);
        //TODO: In der determinization wird nicht berücksichtigt, 
        //      *****ob Pik Dame schon gespielt wurde und wie viele Herzkarten bereits gespielt wurden und wer der Starter der Runde war.
        //           Diese Sachen müssten bei der Determinization berücksichtigt werden. Wegen fehlendem Starter of Round habe ich vorerst eine
        //           weitere Rules-Methode für Trick0Or1 hinzugefügt.
        //      Dies ist aber für die Punkte-Auswertung, insbesondere Shoot The Moon, wichtig! An diese Dinge sollte sich ein Spieler auch in jedem Fall,
        //      egal welcher Memory, erinnern können oder zur Not aus den Punkten annähernd schließen können.
        //      //TODO: Also mit Julijan hierüber nochmal reden. D.h., entweder in Methode determinization ändern
        //              oder eigene Methode determinization schreiben!!!
        //  Die gegnerischen! PlayableHandCards sind noch nicht gesetzt worden. Dies muss im Folgenden noch gemacht werden.
        //Zunächst Starter bestimmen
        int starterNo = determinizedGameState.getStarterOfNewestTrick();
        //Jetzt gegnerische PlayableHandCards setzen
        //System.out.println("AiPIMCRoundSimUct bestMove... for...");
        for (Player player : determinizedGameState.getPlayers()){
            if (player.getPlayerNo() != playerNo) {
                //PlayableHandCards des Gegners bestimmen.
                if (determinizedGameState.getTricksPlayed() == 0){
                    //Tritt bei bestMove nie ein, da eventuelles Passing bereits geschehen ist!
                    ;
                } else if (determinizedGameState.getTricksPlayed() == 1){
                    //Trick 1
                    //System.out.println("AiPIMCRoundSimUct bestMove... for... getTricksPlayed() == 1...");                    
                    if (player.getPlayerNo() < starterNo || player.getPlayerNo() > playerNo){
                        //System.out.println("AiPIMCRoundSimUct bestMove... for... getTricksPlayed() == 1... player.getPlayerNo():" + player.getPlayerNo());
                        //determinizedGameState.getPlayers()[player.getPlayerNo()].setHandCardsPlayableFromRulesAndIsInHandInTrick0or1(determinizedGameState);
                        Player starterOfRound = determinizedGameState.getPlayers()[starterNo];
                        player.setHandCardsPlayableFromRulesAndIsInHandInTrick0or1ForAiDeterminization(determinizedGameState, starterOfRound);
                        //int firstCardNumberPlayed = determinizedGameState.getFirstCardOfNewestTrick();
                        //determinizedGameState.setPlayableCardsForNonStarterInTrick2orLater(player, firstCardNumberPlayed);
                        //System.out.println("AiPIMCRoundSimUct bestMove... for... getTricksPlayed() == 1... Karten gesetzt für player.getPlayerNo():" + player.getPlayerNo());
                    }
                    //System.out.println("AiPIMCRoundSimUct bestMove... for... getTricksPlayed() == 1... PlayableCards wurden gesetzt.");
                    //OKAY
                } else if (determinizedGameState.getTricksPlayed() > 1) {
                    //Trick größer 1       
                    if (playerNo == starterNo){ //Spieler, für den der MCTS aufgerufen wird, ist der Starter des neuen Tricks.
                        //Nichts machen, da als erster Spieler eines Tricks die zu legende Karte erst noch durch den
                        // MCTS gewählt werden muss und aus den verscheidenen Möglichkeiten sich erst später
                        // die spielbaren Karten der Gegner ergeben.
                        ;
                    } else { //Spieler, für den der MCTS aufgerufen wird, ist nicht der Starter des neuen Tricks.
                        // Es wurde bereits die erste Karte des Tricks gelegt und somit können die spielbaren Karten
                        //  der Gegner auch bestimmt werden.
                        int firstCardNumberPlayed = determinizedGameState.getFirstCardOfNewestTrick();
                        determinizedGameState.setPlayableCardsForNonStarterInTrick2orLater(player, firstCardNumberPlayed);
                    }                
                } else {
                    //Fall existiert nicht!
                }
            }
            //System.out.println("AiPIMCRoundSimUct bestMove... for-nächste Iteration...");
        }
                
        //System.out.println("AiPIMCRoundSimUct bestMove... for-Ende...return super...");
        return super.bestMove(determinizedGameState, playerNo);
    }

    // ****OKAY
    @Override
    protected void setDifficultyForBestMove(GameState gameState, int playerNo) {
        //Zunächst in Abhängigkeit der Difficulty die numberOfIterations setzen
        this.numberOfIterations = Difficulty.determineNumberOfIterationsVariant1(gameState, playerNo);
    }

    // ****OKAY
    @Override
    protected void setMemoryForBestMove(GameState gameState, int playerNo) {
        //Memory wird schon innerhalb der Determinization berücksichtigt 
        //  und braucht daher hier nicht erneut berücksichtigt werden.
        ;
    }

    //TODO: Die Methode findBestNodeWithUCT in UCT.java nochmal prüfen: node.getParent().getVisits(); 
    //      Aufruf für den Parent??? Was ist mit root? Müsste doch die Visits von "node" lediglich nehmen, 
    //      nicht die vom Parent von "node", oder??? -> Julijan mal fragen
    // ****OKAY
    @Override
    protected MCTSNode select(MCTSNode parentNode) {
        MCTSNode childNode = (MCTSNode) UCT.findBestNodeWithUCT(parentNode);
        return childNode;
    }
    
}
