# Beiträge von Kevin Albrechts in der 2. Phase

# Implementierte KIs:

- BASIC_MCTS_CHEATING_PI_GAME_SIM_UCB1:
- BASIC_MCTS_CHEATING_PI_GAME_SIM_UCT
- BASIC_MCTS_CHEATING_PI_ROUND_SIM_UCB1
- BASIC_MCTS_CHEATING_PI_ROUND_SIM_UCT

//TODO: *****
- Noch entfernen: ...UCB2
- Eventuell noch implementieren ...DeterminizedII : siehe Paper https://eprints.whiterose.ac.uk/75048/1/CowlingPowleyWhitehouse2012.pdf S. 125/126 IV. C/D


# Dateien:

//TODO: ****

- Alles unterhalb von AiMCTSBasic:
  - AiMCTSBasic.java
  - AiMCTSDeterminizedII.java (TODO: wahrscheinlich noch entfernen)
  - AiMCTSCheatingPI.java
  - AiMCTSCheatingPIGameSim.java (simulationPhase-Spielablauf noch in GameState-Methode auslagern?)
  - AiMCTSCheatingPIGameSimUcb1.java
  - AiMCTSCheatingPIGameSimUcb2.java (TODO: wahrscheinlich noch entfernen)
  - AiMCTSCheatingPIGameSimUct.java
  - AiMCTSCheatingPIRoundSim.java (simulationPhase-Spielablauf noch in GameState-Methode auslagern?)
  - AiMCTSCheatingPIRoundSimUcb1.java
  - AiMCTSCheatingPIRoundSimUcb2.java (TODO: wahrscheinlich noch entfernen)
  - AiMCTSCheatingPIRoundSimUct.java
  - AiMCTSBasicHelper.java
- Alles unterhalb von AiMLPython, zur Anbindung von Python an Java:
  - ...
- UCB1.java
- UCB2.java (TODO: wahrscheinlich noch entfernen)
- AiMCTSvariant1.java
- PythonExecutionHandler.java

# Weiteres, wie Methoden in sonstigen Klassen etc.

//TODO: ****
 
 - Deep-Copy-Methoden "deepCopy" und Clone-Methoden "clone" für GameState, Player, Trick, HandCards, Cards
 - Anbindung von Python an Java: Aufruf und Ausführung eines Python-Skripts aus Java heraus, Übergabe von Parametern über Konsole und Dateien zur Interprozesskomunikation.
 - GameState: 
   - Bestimmung der Liste spielbarer Handkarten eines Spielers: determineListOfPlayableCards
   - Bestimmung der Liste von Spielsiegern: determineListOfGameWinnerOrWinners
   - Bestimmung der Liste von Rundensiegern: determineListOfRoundWinnerOrWinners
   - New Moon durch Old Moon ersetzt bei der Rundenausertung: updateGamePoints
   - simulationPhase-Spielablauf für GameSim und RoundSim: Methodenrümpfe von den Methoden "..." und "..." (falls Julijan sie in GameState noch auslagert.)
 - GameController:
   - ... TODO (Python?, noch etwas? Konstruktor nur mit gameState?...)
 - einige kleinere Dinge an anderen Stellen


 