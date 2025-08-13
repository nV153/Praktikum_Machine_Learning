#Ai MCTS Variant 1 Inference Module

#----------Allgemeine Imports----------
import sys
import numpy as np

#----------Import der Funktionen zum Speichern der Ergebnisse von best_pass und best_move, also der Kartennummer(n)----------
#Auf die Funktionen erfolgt über Modul PythonIOHelper.py . Bei Änderung des Verzeichnisses von PythonIOHelper.py
#  muss sys.path.append("projecthearts/src/game/AI/AiMLPython") an den Pfad zu dem entsprechenden Verzeichnis angepasst werden.
sys.path.append("projecthearts/src/game/AI/AiMLPython")
from PythonIOHelper import save_best_move, save_best_pass

#----------Funktionen bestPass und bestMove----------
#Die Funktionen für bestPass und bestMove entweder hier in einem Skript definieren, dann Aufruf in Java mit einem zusätzlichen
#  "ID"-Parameter, um die Funktion später zu wählen, das heißt, ob bestPass oder bestMove gewählt werden soll,
#  oder in getrennten Skripten, was evtl. übersichtlicher wäre, dann würde Java entweder das bestPass-Skript oder bestMove-Skript aufrufen,
#  oder eine Mischung aus beiden: Ein Skript, dem Java als Parameter eine "ID" übergibt und dieses Skript ruft dann das entsprechende
#  Skript/Methode in separatem Skirpt zu dieser ID auf, also koordinert in diesem Fall nur.

def best_pass():
    print("Hello from best_pass")
    result = np.array([31, 32, 33]) #Kartennummern
    return result

def best_move():
    print("Hello from best_move")
    result = 52 #Kartennummer
    return result


#---------------------------------------------------
#----------Eigentlicher Beginn des Skritps----------
#---------------------------------------------------

#----------Daten einlesen----------
#Argumente aus PythonExecutionHandler.java
#args[0] = scriptPath;
#args[1] = String.valueOf(functionId);
#args[2] = filePathCurrentGameState;
#args[3] = String.valueOf(playerNo);
#args[4] = String.valueOf(receiverNo);
#args[5] = "projecthearts/src/game/AI/AiMLPython/resultLog.csv";

#Name des Skripts einlesen, wobei überflüssig und nur zum Test hier gemacht.
script_name = sys.argv[0]#"scriptname"#sys.argv[0]
#Die "ID", damit das Skript weiß, welche Funktion benötigt wird, also bestPass oder bestMove:
function_id_string = sys.argv[1]#"1"#sys.argv[1]
function_id = int(function_id_string)
#Der gameState könnte als JSON-String als Kommandozeilenparameter übergeben werden. 
# Müsste dann entweder direkt darauf zugegriffen werden oder auf Objekte gemappt werden. 
# Diese müssten dann noch im Python-Ordner einmal definiert werden. 
# Das Mapping oder der direkte Zugriff ist auch bei anderen Möglickeiten nötig, siehe im Folgenden.
# Alternativ kann gameState als Json-Datei übergeben werden, was evtl. am einfachsten wäre, aber sicherlich zeitaufwendiger beim Zugriff ist. 
#   -> Evtl. ein Nachteil im Simulationsmodus und für Bewertung.
file_path_current_gamestate = sys.argv[2]#"projecthearts/src/game/AI/AiMLPython/currentGameState.json"#sys.argv[2] 
#Die Spielernummer als String. Diese müsste dann noch in int konvertiert werden.
player_number_string = sys.argv[3]#"2"#sys.argv[3]
player_number = int(player_number_string)
#Die Spielernummer als String. Diese müsste dann noch in int konvertiert werden.
receiver_number_string = sys.argv[4]#"3"#sys.argv[4]
receiver_number = int(receiver_number_string)
#Pfad für die Ergebnisdatei
file_path_result_log = sys.argv[5]#"projecthearts/src/game/AI/AiMLPython/resultLog.csv"#sys.argv[5] 

#TODO: Test, ob Parameter-Übergabe funktioniert.
print("Eingabe-Parameter in Python: \n")
print("script_name: " + script_name + " \n")
print("function_id: " + str(function_id) + " \n")
print("file_path_current_gamestate: " + file_path_current_gamestate + " \n")
print("player_number: " + str(player_number) + " \n")
print("receiver_number: " + str(receiver_number) + " \n")
print("file_path_result_log: " + file_path_result_log + " \n")


#----------Daten verarbeiten, Ergebnis berechnen----------
result_of_best_pass = np.empty(3, dtype=int)
result_of_best_pass[:] = 0 #Als Test mit 0en instantiieren. Evtl. als Fehlerprüfung durch Java oder hier noch nützlich.
result_of_best_move = 0 #Als Test mit 0 instantiieren. Evtl. als Fehlerprüfung durch Java oder hier noch nützlich.

if function_id == 1:
    result_of_best_pass = best_pass()
elif function_id == 2:
    result_of_best_move = best_move()

#----------Ergebnis übermitteln----------
# Übergabe über die Konsole:
if function_id == 1:
    for number in  result_of_best_pass:
        print(number)
elif function_id == 2:
    print(result_of_best_move)
else:
    print(0)

#Alternativ kann das Ergebnis in eine Datei geschrieben werden, die Java dann ausliest, evtl. einfacher. Bei 1 bzw. 3 Integern wäre das aber unverhältnismäßig -> Verlangsamung!
if function_id == 1:
    save_best_pass(result_of_best_pass, file_path_result_log)
elif function_id == 2:
    save_best_move(result_of_best_move, file_path_result_log)


#----------Skript-Ende signalisieren---------- #TODO: Evtl. unnötig
sys.exit(0)



