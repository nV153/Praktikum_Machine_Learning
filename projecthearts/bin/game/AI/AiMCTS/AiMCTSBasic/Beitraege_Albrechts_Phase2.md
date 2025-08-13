# Beiträge von Kevin Albrechts in der 2. Phase

# Implementierte KIs:

- "Cheating MCTS"-KIs: Nach Literatur "Cheating UCT": siehe Paper https://eprints.whiterose.ac.uk/75048/1/CowlingPowleyWhitehouse2012.pdf S. 125/126 IV. C
  - BASIC_MCTS_CHEATING_PI_GAME_SIM_UCB1 (nicht in GUI-Auswahl aufgenommen)
  - BASIC_MCTS_CHEATING_PI_GAME_SIM_UCT (nicht in GUI-Auswahl aufgenommen)
  - BASIC_MCTS_CHEATING_PI_ROUND_SIM_UCB1 (nicht in GUI-Auswahl aufgenommen)
  - BASIC_MCTS_CHEATING_PI_ROUND_SIM_UCT, in GUI wählbar als "Cheating_MCTS"
- "PIMC"-KI: Nach Literatur "Perfect Information MCTS": siehe Paper "Swiechowski et al.: Monte Carlo Tree Search: a review of recent modifcations and applications, 2022, S. 2515 (Kapitel 4.1)
  - PIMC_ROUND_SIM_UCT, in GUI wählbar als "PIMC"

# Dateien:

- Alles unterhalb von AiMCTSBasic:
  - AiMCTSBasic.java
  - AiMCTSBasicHelper.java
  - AiMCTSCheatingPI.java
  - AiMCTSCheatingPIGameSim.java
  - AiMCTSCheatingPIGameSimUcb1.java
  - AiMCTSCheatingPIGameSimUct.java
  - AiMCTSCheatingPIRoundSim.java
  - AiMCTSCheatingPIRoundSimUcb1.java
  - AiMCTSCheatingPIRoundSimUct.java
  - AiPIMCRoundSimUct.java
- Alles unterhalb von AiMLPython, zur Anbindung von Python an Java:
  - ...
- UCB1.java
- AiMCTSvariant1.java
- PythonExecutionHandler.java

//TODO: Einige Dateien noch entfernen!!!

# Weiteres, wie Methoden in sonstigen Klassen etc.

//TODO: ****
 
 - Deep-Copy-Methoden "deepCopy" und Clone-Methoden "clone" für GameState, Player, Trick, HandCards, Cards
 - Anbindung von Python an Java: Aufruf und Ausführung eines Python-Skripts aus Java heraus, Übergabe von Parametern über Konsole und Dateien zur Interprozesskomunikation. //TODO: Am Ende entfernt.
 - GameState: 
   - Bestimmung der Liste spielbarer Handkarten eines Spielers: determineListOfPlayableCards
   - Bestimmung der Liste von Spielsiegern: determineListOfGameWinnerOrWinners
   - Bestimmung der Liste von Rundensiegern: determineListOfRoundWinnerOrWinners
   - New Moon durch Old Moon ersetzt bei der Rundenauswertung: updateGamePoints
 - GameController:
   - ... //TODO (Python?, noch etwas? Konstruktor nur mit gameState?...)
 - Rules:
   - Methode getPlayableCardsInTrick0or1ForAiDeterminization zur Bestimmung spielbarer Karten in Trick 0 oder 1 speziell für die AI-Determinization: Durch die AI-Determinization funktionierte der Aufruf gameState.getStarterOfRound() in der ansonsten identischen Methode getPlayableCardsInTrick0or1 nicht mehr. Das Problem wird durch einen Übergabeparameter umgangen.
 - Im Simulationsmodus die Berechnung der Spielerstatistik für das Simulationsende: Total und Average Game Points sowie Total und Average Payoff der Game Points (gemäß Auszahlungsfunktion Zwischenpräsentation): Anpassungen in Logger, GameController (und GamePlayGUI)
 - einige kleinere Dinge an anderen Stellen
 - Memory:
   - Methode determineExpansionDepthVariant1 zu einer möglichen Einschränkung der Expansionstiefe.
   - Methode determineMaxNumberOfPossiblePassesVariant1 zu einer möglichen Einschränkung der bei bestPass maximalen Anzahl möglicher Weitergaben.
 - Difficulty:
   - Methode determineNumberOfIterationsVariant1 zur Bestimmung der Iterationsanzahl.
   - Methode determineNumberOfIterationsVariant2 zur Bestimmung der Iterationsanzahl.



 