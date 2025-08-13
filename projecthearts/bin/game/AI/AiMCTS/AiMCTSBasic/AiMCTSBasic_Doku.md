# Dokumentation der KIs in AiMCTSBasic:

## Klassenhierarchie

- Abstrakte Klasse AiMCTSBasic implements AiInterface:
  - Attribute:
    - MCTSTree tree: Der (unvollständige) Spielbaum
    - MCTSNode root: Die Wurzel des (unvollständigen) Spielbaums
    - int numberOfIterations: Anzahl der Iteration
    - int expansionDepth: Maximale Expansionstiefe des Spielbaums                                         
    - int maxNumberOfPossiblePasses: Maximale Anzahl möglicher Weitergaben/Kombinationen von 3 Handkarten
  - bestPass:
    1. MCTS-Parameter in Abhängigkeit der Difficulty und der Memory setzen.
    2. Erzeugung des neuen Spielbaums für den aktuellen GameState
    3. Setzen einer festen Expansionstiefe expansionDepth von 1
    4. Setzen der Anzahl möglicher Weitergaben als Binomialkoeffizient in Abhängigkeit der Decksize.
    5. Aufruf des MCTS-Algorithmus runBestPassMCTS
    6. Rückgabe der weiterzugebenden 3 Karten des am meisten besuchten Kind-Knotens der Wurzel
  - runBestPassMCTS:
    - Zeitschranke für MCTS-Algorithmus setzen, ca. 5 Sekunden.
    - Schleife mit Anzahl der Iterationen numberOfIterations:
      1. selectionPhase
      2. expansionPhaseForBestPass
      3. simulationPhaseForBestPass
      4. backpropagationPhase
      5. Bei Überschreitung der obigen Zeitschranke wird, nur im Human-Mode (nicht im Simulation-Mode), der MCTS-Algorithmus, also diese Schleife, abgebrochen.
    - Wahl und Rückgabe des am meisten besuchten Kind-Knotens der Wurzel
  - bestMove:
    1. MCTS-Parameter in Abhängigkeit der Difficulty und der Memory setzen.
    2. Erzeugung des neuen Spielbaums für den aktuellen GameState
    3. Aufruf des MCTS-Algorithmus runBestMoveMCTS
    4. Rückgabe der zu spielenden Karte des am meisten besuchten Kind-Knotens der Wurzel
  - runBestMoveMCTS:
    - Zeitschranke für MCTS-Algorithmus setzen, ca. 5 Sekunden.
    - Schleife mit Anzahl der Iterationen numberOfIterations:
      1. selectionPhase
      2. expansionPhase
      3. simulationPhase
      4. backpropagationPhase
      5. Bei Überschreitung der obigen Zeitschranke wird, nur im Human-Mode (nicht im Simulation-Mode), der MCTS-Algorithmus, also diese Schleife, abgebrochen.
    - Wahl und Rückgabe des am meisten besuchten Kind-Knotens der Wurzel
  - Abstrakte Methode setDifficultyForBestMove zum Setzen der Difficulty für bestMove
  - Abstrakte Methode setDifficultyForBestPass zum Setzen der Difficulty für bestPass
  - Abstrakte Methode setMemoryForBestMove zum Setzen der Memory für bestMove
  - Abstrakte Methode setMemoryForBestPass zum Setzen der Memory für bestPass
  - selectionPhase:
    1. Wähle Kind-Knoten unter noch nicht besuchten gleichverteilt, also per Zufall.
    2. Sobald jeder Kind-Knoten besucht, wähle nach Selektionsstrategie, bspw. UCT oder UCB1, je nach Subklasse, über Methode select.
  - Abstrakte Methode expansionPhase für bestMove
  - Abstrakte Methode expansionPhaseForBestPass für bestPass
  - Abstrakte Methode simulationPhase für bestMove
  - Abstrakte Methode simulationPhaseForBestPass für bestPass
  - backpropagationPhase:
    - Update der folgenden Attribute der besuchten Knoten: visits, winScore, avgPayout
  - Abstrakte Methode select
  - determineNextGameStateOfNextPlayerOrNode:
    - Methode, die eine einmalige Übergangssimulation von einer gameState-Kopie des Parent-Knoten zum gameState des Kind-Knotens durchführt, also des Knotens des Spielers, der als nächster an der Reihe ist.
  - Innere Klasse MCTSNode extends Node
    - Geerbte Attribute von Node:
      - GameState gameState
      - Node parent
      - List< Node > children
      - int winScore
      - int visits
    - Weitere Attribute:
      - Card actionPlayedCard: "Aktion" im Falle von bestMove, also die Karte mit Codierung Id 1-52, welche in diesem Node/GameState von diesem Spieler gelegt wurde
      - List< Card > actionsPlayableCards: Enthält später die in diesem Gamestate spielbaren Karten, also die spielbaren Handkarten des Spielers mit Nummer playerNo
      - int playerNo: Nummer des Spielers, der in diesem GameState/Knoten als Nächster mit einer Aktion an der Reihe ist
      - double avgPayout: Mittlere Auszahlung
      - int depth: Knotentiefe zur Wurzel
      - Card[] actionPassedCards: "Aktion" im Falle von bestPass, also die 3 Karten, die im Parent zum Weitergeben ausgewählt wurden.
  - Innere Klasse MCTSTree extends Tree
    - Geerbte Attribute von Tree:
      - Node root: Die Wurzel des Baumes und somit der GameState, für den bestPass oder bestMove aufgerufen wird.
    - Methode selectBestNode: Bestimmung des nach "visits" am meisten besuchten Kind-Knotens der Wurzel "root" des Spielbaums

- Abstrakte Klasse AiMCTSCheatingPI extends AiMCTSBasic:
  - expansionPhase (Override): Expansionsphase für bestMove, die für einen Knoten bereits alle seine Kind-Knoten hinzufügt, sofern die maximale Expansionstiefe expansionDepth nicht überschritten und zudem nicht in eine nächste Runde hinein expandiert wird, wobei ein Kind-Knoten pro möglicher Aktion erzeugt wird. Die möglichen Aktionen sind hier vollständig vorhanden, da AiMCTSCheatingPI eine "Cheating"-Ai ist, die perfekte Information über das Spiel und somit über die Handkarten aller Spielers besitzt. Der Knoten für die anschließende Simulation wird zum Schluss gleichverteilt aus den hinzugefügten Kind-Knoten ausgewählt. Diese Methode nutzt die geerbte Methode determineNextGameStateOfNextPlayerOrNode zur Bestimmung des GameStates eines hinzugefügten Kind-Knotens.
  - expansionPhaseForBestPass (Override): Expansionsphase für bestPass, die für einen Knoten bereits alle seine Kind-Knoten hinzufügt, sofern die maximale Expansionstiefe expansionDepth nicht überschritten wird. Da diese auf 1 festgelegt wurde, hat der Spielbaum nur eine Tiefe von 1. Das wird mit der Vielzahl möglicher Aktionen und den damit verbundenen hohen Speicherplatz- und Laufzeitanforderungen begründet: Es wird ein Kind-Knoten pro möglicher Aktion erzeugt, wobei eine Aktion eine Teilmenge mit Größe 3 Karten von dem Menge der Handkarten, also 13 bzw. 8 Handkarten, bedeutet. Die Anzahl möglicher Teilmengen kann durch die Memory-Einstellungen begrenzt werden, bspw. sind bei höchster Stufe 4 und Deckgröße 52 alle insgesamt 286 Aktionen möglich, also der Binomialkoeffizient von 13 über 3, bei niedrigeren Memory-Einstellungen erfolgt eine Abstufung, bspw. auf die Hälfte. Die möglichen Aktionen sind hier vollständig bekannt, da AiMCTSCheatingPI eine "Cheating"-Ai ist, die perfekte Information über das Spiel und somit über die Handkarten aller Spielers besitzt. Der Knoten für die anschließende Simulation wird zum Schluss gleichverteilt aus den hinzugefügten Kind-Knoten ausgewählt. Diese Methode nutzt die Methode getAllPossibleSubsetsOfASizeOfArrayWithDistinctElements von AiMCTSBasicHelper.Permutation zur Bestimmung aller möglichen Teilmengen der Größe 3 Karten von den 13 bzw. 8 Handkarten.

- Abstrakte Klasse AiMCTSCheatingPIGameSim extends AiMCTSCheatingPI:
  - simulationPhase (Override): Simulation mit perfekter Information über die spielbaren Handkarten aller Spieler bis zum Spielende mit anschließender Auswertung des Spielergebnisses, welche in der backPropagation zum Update von avgPayOut und winScore genutzt wird. Diese Methode nutzt folgende weitere Methoden dieser Klasse: simulateRemainingTricksUntilRoundOver, simulateRemainingRoundsUntilGameOver, evaluateGameEnd
  - simulateRemainingTricksUntilRoundOver: Simulation restlicher Tricks einer Runde
  - simulateRemainingRoundsUntilGameOver: Simulation weiterer Runden bis Spielende
  - evaluateGameEnd: Auswertung des Spielergebnisses
  - simulationPhaseForBestPass (Override): Simulation mit perfekter Information über die spielbaren Handkarten aller Spieler bis zum Spielende mit anschließender Auswertung des Spielergebnisses, welche in der backPropagation zum Update von avgPayOut und winScore genutzt wird. Diese Methode nutzt folgende weitere Methoden dieser Klasse: simulateRemainingTricksUntilRoundOver, simulateRemainingRoundsUntilGameOver, evaluateGameEnd. Die 3 beim Passing weitergebenen Karten der anderen Spieler werden gleichverteilt/zufällig aus ihren jeweiligen Handkarten gewählt.

- Konkrete Klasse AiMCTSCheatingPIGameSimUcb1 extends AiMCTSCheatingPIGameSim:
  - Dies ist eine spielbare KI-Klasse.
  - bestPass (Override): Aufruf von bestPass der Super-Klasse: super.bestPass(gameState, playerNo, receiverNo)  
  - setDifficultyForBestPass (Override): Setzen der Difficulty für bestPass. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant2.
  - setMemoryForBestPass (Override): Setzen der Memory für bestPass. Dies entfällt für bestPass.
  - bestMove (Override): Aufruf von bestMove der Super-Klasse: super.bestMove(g, playerNo)
  - setDifficultyForBestMove (Override): Setzen der Difficulty für bestMove. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant1.
  - setMemoryForBestMove (Override): Setzen der Memory für bestMove. Dies entfällt für bestMove.
  - select (Override): In der Selektionsphase soll nach der UCB1-Formel (UCB1.findBestNodeWithUCB1) ein Kind-Knoten selektiert werden.

- Konkrete Klasse AiMCTSCheatingPIGameSimUct extends AiMCTSCheatingPIGameSim:
  - Dies ist eine spielbare KI-Klasse.
  - bestPass (Override): Aufruf von bestPass der Super-Klasse: super.bestPass(gameState, playerNo, receiverNo)  
  - setDifficultyForBestPass (Override): Setzen der Difficulty für bestPass. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant2
  - setMemoryForBestPass (Override): Setzen der Memory für bestPass. Dies entfällt für bestPass.
  - bestMove (Override): Aufruf von bestMove der Super-Klasse: super.bestMove(g, playerNo)
  - setDifficultyForBestMove (Override): Setzen der Difficulty für bestMove. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant1.
  - setMemoryForBestMove (Override): Setzen der Memory für bestMove. Dies entfällt für bestMove.
  - select (Override): In der Selektionsphase soll nach der UCT-Formel (UCT.findBestNodeWithUCT) ein Kind-Knoten selektiert werden.

- Konkrete Klasse AiMCTSCheatingPIRoundSimUcb1 extends AiMCTSCheatingPIRoundSim:
  - Dies ist eine spielbare KI-Klasse.
  - bestPass (Override): Aufruf von bestPass der Super-Klasse: super.bestPass(gameState, playerNo, receiverNo)  
  - setDifficultyForBestPass (Override): Setzen der Difficulty für bestPass. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant2
  - setMemoryForBestPass (Override): Setzen der Memory für bestPass. Dies entfällt für bestPass.
  - bestMove (Override): Aufruf von bestMove der Super-Klasse: super.bestMove(g, playerNo)
  - setDifficultyForBestMove (Override): Setzen der Difficulty für bestMove. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant1.
  - setMemoryForBestMove (Override): Setzen der Memory für bestMove. Dies entfällt für bestMove.
  - select (Override): In der Selektionsphase soll nach der UCB1-Formel (UCB1.findBestNodeWithUCB1) ein Kind-Knoten selektiert werden.

- Konkrete Klasse AiMCTSCheatingPIRoundSimUct extends AiMCTSCheatingPIRoundSim:
  - Dies ist eine spielbare KI-Klasse.
  - bestPass (Override): Aufruf von bestPass der Super-Klasse: super.bestPass(gameState, playerNo, receiverNo)  
  - setDifficultyForBestPass (Override): Setzen der Difficulty für bestPass. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant2
  - setMemoryForBestPass (Override): Setzen der Memory für bestPass. Dies entfällt für bestPass.
  - bestMove (Override): Aufruf von bestMove der Super-Klasse: super.bestMove(g, playerNo)
  - setDifficultyForBestMove (Override): Setzen der Difficulty für bestMove. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant1.
  - setMemoryForBestMove (Override): Setzen der Memory für bestMove. Dies entfällt für bestMove.
  - select (Override): In der Selektionsphase soll nach der UCT-Formel (UCT.findBestNodeWithUCT) ein Kind-Knoten selektiert werden.

- Konkrete Klasse AiPIMCRoundSimUct extends AiMCTSCheatingPIRoundSimUct:
  - Dies ist eine spielbare KI-Klasse.
  - bestPass (Override):
    1. Einen möglichen gameState mithilfe des tatsächlichen gameState schätzen, d. h., den unbekannten gameState determinisieren. Genauergesagt, es werden die gegnerischen Handkarten (in Abhängigkeit der Memory, wobei hier bei bestPass letztlich doch unabhängig) geschätzt.
    2. Die gegnerischen spielbaren Handkarten PlayableHandCards sind durch die Determinization noch nicht gesetzt worden. Dies wird hier gemacht.
    3. Aufruf von bestPass der Super-Klasse: super.bestPass(determinizedGameState, playerNo, receiverNo)
  - setDifficultyForBestPass (Override): Setzen der Difficulty für bestPass. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant2
  - setMemoryForBestPass (Override): Setzen der Memory für bestPass. Dies entfällt für bestPass.
  - bestMove (Override): 
    1. Einen möglichen gameState mithilfe des tatsächlichen gameState schätzen, d. h., den unbekannten gameState determinisieren. Genauergesagt, es werden die gegnerischen Handkarten (in Abhängigkeit der Memory) geschätzt.
    2. Die gegnerischen spielbaren Handkarten PlayableHandCards sind durch die Determinization noch nicht gesetzt worden. Dies wird hier gemacht.
    3. Aufruf von bestMove der Super-Klasse: super.bestMove(determinizedGameState, playerNo)
  - setDifficultyForBestMove (Override): Setzen der Difficulty für bestMove. In Abhängigkeit der Difficulty werden die Anzahl Iterationen numberOfIterations gesetzt: Difficulty.determineNumberOfIterationsVariant1.
  - setMemoryForBestMove (Override): Setzen der Memory für bestMove. Memory wird schon innerhalb der Determinization berücksichtigt und braucht daher hier nicht erneut berücksichtigt werden.
  - select (Override): In der Selektionsphase soll nach der UCT-Formel (UCT.findBestNodeWithUCT) ein Kind-Knoten selektiert werden.

//TODO: Über alle, aber insbesondere die konkreten Klassen nochmal drüberschauen und anpassen: bspw. select -> normalisiert/nicht-normalisiert, UCT, ...

## Beschreibung der konkreten KI-Verfahren/-Klassen

- AiMCTSCheatingPIGameSimUcb1:
  - KI, die mit MCTS arbeitet und perfekte Information über das Spiel mit eigentlich imperfekter Information besitzt.
  - Die Wahl der Aktion nach Abschluss des MCTS-Algorithmus erfolgt nach der höchsten Anzahl Besuche eines Kind-Knotens der Wurzel. Bei Gleichheit der Besuchsanzahl wird der erste dieser Kind-Knoten gewählt.
  - In der Expansions- und Simulationsphase wird perfekte Information über die spielbaren Karten auf der Hand aller Spieler zur Bestimmung möglicher Aktionen genutzt.
  - Die Expansionsphase geht bis maximal zum Rundenende, es erfolgt keine Expansion in die nächste Runde. (Begründung: Durch neue Handkarten und eventuelles Weitergeben wäre der Suchraum sehr groß, was zu Einbußen in Speicherplatz und Laufzeit führen würde.)
  - Die Simulationsphase geht bis zum Spielende.
  - UCB1 wird in der Selektionsphase genutzt.
  - Difficulty-Abstufung bestimmt bei bestPass und bestMove die Anzahl der Iterationen.
    - bestPass: Der Binomialkoeffizient (13,3) = 286 bzw. (8,3) = 56 wird mit 1, 20, 40 oder 60 multipliziert. Das heißt:
      - Deck Size 52: Stufe 0: 286, Stufe 1: 5.720, Stufe 2: 11.440, Stufe 3: 17.160
      - Deck Size 32: Stufe 0: 56, Stufe 1: 1.120, Stufe 2: 2.240, Stufe 3: 3.360
    - bestMove: Stufe 0: 30, Stufe 1: 600, Stufe 2: 1.200, Stufe 3: 1.800
  - Memory-Abstufung entfällt im Falle von bestPass und im Falle von bestMove entfällt sie auch.
  - Im Falle von bestPass ist die Expansionstiefe auf 1 beschränkt, d. h., lediglich für die Wurzel werden Kind-Knoten für mögliche Aktionen erzeugt. Immer von diesen Kind-Knoten ausgehend wird eine Simulation gestartet. (Begründung: Durch die große Vielzahl an möglichen Weitergaben wäre der Suchraum sehr groß, was zu Einbußen in Speicherplatz und Laufzeit führen würde.)
  - Eine Zeitschranke von 5 Sekunden, nur im Human-Mode (nicht im Simulation-Mode), bewirkt, dass ein bestPass- oder bestMove-Aufruf für einen Spieler, also eine Weitergabe oder ein Spielzug, höchstens ca. 5 Sekunden dauert.

- AiMCTSCheatingPIGameSimUct:
  - KI, die mit MCTS arbeitet und perfekte Information über das Spiel mit eigentlich imperfekter Information besitzt.
  - Genau gleich zu AiMCTSCheatingPIGameSimUcb1 mit dem einzigen Unterschied, dass in der Selektionsphase UCT genutzt wird.

- AiMCTSCheatingPIRoundSimUcb1:
  - KI, die mit MCTS arbeitet und perfekte Information über das Spiel mit eigentlich imperfekter Information besitzt.
  - Genau gleich zu AiMCTSCheatingPIGameSimUcb1 mit dem einzigen Unterschied, dass die Simulationsphase bis zum Ende der aktuellen Runde geht.

- AiMCTSCheatingPIRoundSimUct:
  - KI, die mit MCTS arbeitet und perfekte Information über das Spiel mit eigentlich imperfekter Information besitzt.
  - Genau gleich zu AiMCTSCheatingPIGameSimUcb1 mit den beiden einzigen Unterschieden, dass in der Selektionsphase UCT genutzt wird und die Simulationsphase bis zum Ende der aktuellen Runde geht.
//TODO: Am besten AiMCTSCheatingPIRoundSimUct an erster Stelle beschreiben und AiMCTSCheatingPIGameSimUcb1 am Ende. Die Reihenfolge daher anpassen und die Beschreibungen dann auch korrekt machen!!!

- AiPIMCRoundSimUct:
  - Literatur: Świechowski et al.: Monte Carlo Tree Search: a review of recent modifcations and applications, 2022, page 2515 (chapter 4.1)
  - KI, die mit MCTS arbeitet. Sie besitzt lediglich imperfekte Spielinformationen über den GameState, insbesondere die gegnerischen Handkarten. Durch Determinization der imperfekten Information, genauergesagt, durch Schätzen der gegnerischen Handkarten, kann diese AI das Spiel als eines mit perfekter Information behandeln. Die gegnerischen Handkarten werden in Abhängigkeit der Memory-Stufe geschätzt: Je höher die Stufe, an desto weiter zurückliegende Tricks/Stiche kann sich die AI erinnern und diese somit bereits gelegten Karten von der Verteilung noch möglicher Handkarten an die Gegner ausschließen.
  - Die Wahl der Aktion nach Abschluss des MCTS-Algorithmus erfolgt nach der höchsten Anzahl Besuche eines Kind-Knotens der Wurzel. Bei Gleichheit der Besuchsanzahl wird der erste dieser Kind-Knoten gewählt.
  - Die Selektionsphase verwendet die UCT-Formel.
  - Die Expansionsphase expandiert bis maximal Rundenende.
  - Die Simulationsphase simuliert ein Spiel bis maximal zum Ende der aktuellen Runde.
  - Difficulty-Abstufung bestimmt bei bestPass und bestMove die Anzahl der Iterationen.
    - bestPass: Der Binomialkoeffizient (13,3) = 286 bzw. (8,3) = 56 wird mit 1, 20, 40 oder 60 multipliziert. Das heißt:
      - Deck Size 52: Stufe 0: 286, Stufe 1: 5.720, Stufe 2: 11.440, Stufe 3: 17.160
      - Deck Size 32: Stufe 0: 56, Stufe 1: 1.120, Stufe 2: 2.240, Stufe 3: 3.360
    - bestMove: Stufe 0: 30, Stufe 1: 600, Stufe 2: 1.200, Stufe 3: 1.800
  - Memory-Abstufung entfällt im Falle von bestPass und im Falle von bestMove wird sie in der Determinization der gegnerischen Handkarten behandelt, genauergesagt: Je höher die Stufe, an desto weiter zurückliegende Tricks/Stiche kann sich die AI erinnern und diese somit bereits gelegten Karten von der Verteilung noch möglicher Handkarten an die Gegner ausschließen.
    - Konkrete Anzahl der zu erinnernden zurückliegenden Tricks für bestMove:
      - Deck Size 52: Stufe 0: 1, Stufe 1: 3, Stufe 2: 7, Stufe 3: 14
      - Deck Size 32: Stufe 0: 1, Stufe 1: 2, Stufe 2: 4, Stufe 3: 10
  - Eine Zeitschranke von 5 Sekunden, nur im Human-Mode (nicht im Simulation-Mode), bewirkt, dass ein bestPass- oder bestMove-Aufruf für einen Spieler, also eine Weitergabe oder ein Spielzug, höchstens ca. 5 Sekunden dauert.
  - Im Endeffekt genau gleich zu AiMCTSCheatingPIRoundSimUct, allerdings mit dem Unterschied, dass die imperfekte Information über die gegnerischen Handkarten in Abhängigkeit der Memory-Stufe geschätzt werden (Determinization des GameState).

- Auf drei weitere konkrete KI-Klassen für PIMC, die analog zu AiPIMCRoundSimUct wären, aber sich wie beim CheatingMCTS nur durch die Simulationsphase bis Runden- bzw. Spielende und die in der Selektionshase verwendete Formel UCT bzw. UCB1 unterscheiden würden, wurde aufgrund der Übersichtlichkeit und dem geringen Mehrwert verzichtet.

- Anmerkung zu den beiden Formeln UCT und UCB1 und ihren Klassen UCT.java bzw. UCB1.java: Beide Formeln sind im Prinzip gleich und entsprechen der Formel der Zwischenpräsentation, allerdings mit dem Unterschied, dass bei der Formel in UCT.java der Explorationsparameter außerhalb des Wurzelterms steht und bei UCB1.java innerhalb. Dies rührt daher, dass in der Literatur eine unheitliche Benutzung der Bezeichnungen UCT und UCB1 für Formel und Algorithmus existiert. Für UCB1.java wurde die Formel in der Literatur Auer, Peter; Cesa-Bianchi, Nicolò; Fischer, Paul (2002). "Finite-time Analysis of the Multiarmed Bandit Problem". Machine Learning. 47 (2/3): 235–256. ( https://link.springer.com/content/pdf/10.1023/A:1013689704352.pdf , Figure 1 ) verwendet, wo der Explorationsparameter fest ist. UCT.java kann auf die Zwischenpräsentation und die Literatur Cowling et al. (2012) "Information Set Monte Carlo Tree Search" in IEEE TRANSACTIONS ON COMPUTATIONAL INTELLIGENCE AND AI IN GAMES, VOL. 4, NO. 2, JUNE 2012 (IV. A (Seite 125)) zurückgeführt werden. Darüber hinaus werden UCT und UCB1 von den konkreten AIs unterschiedlich verwendet, normalisiert und nicht-normalisierter Payoff bzw. Payout und unterschiedliche Attribute und Datentypen bzgl. Knoten. Um die Aussagekraft des Vergleichs der implementierten MCTS-KIs zu erhöhen, wird auf eine Auswertung der UCB1-verwendenden KIs verzichtet und nur die UCT-verwendenden KIs für den Vergleich herangezogen. //TODO: Noch die beiden Formeln angeben!!!

Im Folgenden werden die zwei KIs "Cheating MCTS" (in GUI "Cheating_MCTS") und "PIMC" (in GUI "PIMC") durch Simulationen gegen die "RuleBased"-KI aus der ersten Phase zu einem Vergleich herangezogen. "Cheating MCTS" ist dabei eine Instanz der Klasse "AiMCTSCheatingPIRoundSimUct" und kann als Benchmark für andere MCTS-KIs gesehen werden, da diese perfekte Information über ein Spiel mit eigentlich imperfekter Information besitzt. "PIMC" ist eine Instanz der Klasse "AiPIMCRoundSimUct". Das heißt, beide KIs selektieren nach UCT auf Basis der normalisierten Payoff-/Rundenpunkte und simulieren bis Rundenende. Wir erwarten, dass die die Benchmark-KI "Cheating MCTS" besser als die KI "PIMC" abschneidet.

//TODO: Hier im Folgenden die Auswertung/den Vergleich durchführen!!! (Cheating MCTS gg. RuleBased und PIMC gegen RuleBased bei 100 oder 1000 ??? Spielen) und abschließend ein Fazit ziehen. Auch bzgl. unserer Erwartung, dass "Cheating MCTS" besser als die KI "PIMC" abschneiden müsste.


//TODO: *************
-Unterschiede herausstellen. Vor- und Nachteile herausstellen. Bspw. GameSim schwächer als RoundSim (?) !!!


Formatierung in Ordnung?

