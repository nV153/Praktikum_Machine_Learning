## Projektplan - Erster Teil

### Aufgabenpaket 1: Benutzeroberfläche (Simon)
-	Erstellen der Assets: Spielkarten (textuell oder graphisch) (Lisa)

-	Benutzeroberfläche I (Spiel starten/beenden, Name des menschlichen Spielers, KI-Eigenschaften, Deckgröße, Weitergabe an/aus, Simulationsmodus an/aus) (Simon)

-	Benutzeroberfläche II (Spielablauf: Spielkarten anzeigen, Blatt des menschlichen Spielers, aktueller Stich, Punkte laufende Runde/Gesamtpunktzahl) (Simon + Kevin)

-   Benutzeroberfläche III (vorheriger Stich) (Kevin)

-   Benutzeroberfläche IV (Punkteanzeige gesamtes Spiel, bei Spielende: zusätzlich Sieger, Dialogfenster beenden /neu starten / Startbildschirm) (Kevin)

-   Benutzeroberfläche V (Simulationsfenster mit Buttons und Fortschrittsanzeige) (Kevin)

### Aufgabenpaket 2: Programmlogik (Kevin + Lisa)

- Grundlegende Klassen (HandCards, Trick, Player, GameState) (Lisa)

- Implementierung der Spielregeln (optische Unterscheidung, regelwidrige Aktionen ausschließen, New Moon) (Kevin)

- Grundlegende Spielfunktionen (Karten ausspielen, Punkte zählen...) (Kevin + Lisa)

- Spielablauf, Zusammenführung von Programmlogik und GUI, GUI-Updates über Threads, Multithreading (Kevin)

### Aufgabenpaket 3: regelbasierte, konfigurierbare KI (Julijan + Niklas)
- KI-Klasse (importierbar aus .class-Datei, regelbasiert)
 
- Spielstärke & Gedächtnis (Niklas + Julijan)

- KI-Interface & Utilklassen (Niklas + Julijan)

- bestMove() (Niklas + Julijan)
    - NewMoon() (Niklas + Julijan)
        - PlayforStM (Niklas)
        - StopStM (Julijan)
    - Standartregeln (Niklas)

- bestPass() (Julijan)



### Aufgabenpaket 4: Simulationsmodus (Lisa)
- Simulationsmodus (1 KI vom Typ x gegen 3-mal KI vom Typ y) 

- Optionen: KI-Stärke, Anzahl der zu simulierenden Spiele (Simon + Kevin)

- Loggerklasse: weitergegebene Karten, Handkarten nach Weitergabe, Stiche, Rundenpunkte, Spielpunkte (Lisa)

- Erzeugen der CSV-Logdatei (Lisa)

- Implementieren der Spiellogik im Simulationsfall (Lisa)

### Aufgabenpaket 5: Koordination und Testen
- Koordinieren der Arbeitsabläufe

- Testen: regelwidrige Aktionen sind ausgeschlossen, KI-Entscheidungen entsprechen Regelwerk,…

- Dokumentation, Git-Repo pflegen
 
### Meilensteine:

**Meilenstein I [bis 22.10.2023]:**

Grundlegende Benutzeroberfläche steht, einfache Aktionen sind möglich 

**Meilenstein II [bis 05.11.2023]:**

Eine Spielrunde kann absolviert werden

**Meilenstein III [bis 19.11.2023]:**

Spiele können simuliert werden


## Projektplan - Zweiter Teil

### Aufgabenpaket 1: MCTS Basic: "Cheating MCTS" und "PIMC" (Kevin)

- 4 KI-Varianten des Cheating MCTS
- 1 KI-Variante des PIMC
- siehe Datei Beitraege_Albrechts_Phase2.md für Auflistung der Beiträge von Kevin Albrechts in der 2. Phase


### Aufgabenpaket 2: MCTS-Erweiterung ... (Julijan)
- bestPass() und bestMove() 

- Simulationsphase
...

- Dokumentation: Abschneiden im Vergleich zu anderen KI-Varianten

### Aufgabenpaket 3: MCTS-Erweiterung ... (Simon)
...

- Dokumentation: Abschneiden im Vergleich zu anderen KI-Varianten


### Aufgabenpaket 4: CFR Basic (Niklas + Lisa)
- bestPass()

- bestMove()

- Gamestates aus Simulationsdaten ableiten

- Clusterbildung zur Reduktion der Gamestates

- Dokumentation: Abschneiden im Vergleich zu anderen KI-Varianten

### Aufgabenpaket 5: CFR-Erweiterung ... (Niklas)
...

- Dokumentation: Abschneiden im Vergleich zu anderen KI-Varianten


### Aufgabenpaket 6: CFR-Erweiterung ... (Lisa)
...

- Dokumentation: Abschneiden im Vergleich zu anderen KI-Varianten

### Aufgabenpaket 7: Zusammenführung, abschließende Arbeiten
- Auswahlmöglichkeiten für die KI-Varianten im UI implementieren (Simon)

- Anpassungen im GameController zur Auswahl verschiedener KIs (Lisa)

- Dokumentation

**Meilenstein I [bis 31.12.2023]:**

Basic Versionen von CFR und MCTS sind implementiert 

**Meilenstein II [bis 21.01.2024]:**

5 Erweiterungen der MCTS und CFR-Algorithmen sind implementiert

**Meilenstein III [bis 11.02.2024]:**

Die KI-Varianten sind ausreichend getestet, eine Dokumentation liegt vor. 
Anmerkungen aus der Zwischenpräsentation wurden umgesetzt.


