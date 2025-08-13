#Modul zum Speichern von Ergebnissen

import csv
import os

def save_best_pass(card_numbers_to_pass, file_path_result_log):
    
    print("Saving pass started.")    
    
    if os.path.exists(file_path_result_log):    
        with open(file_path_result_log, 'w+t') as log: #TODO: Oder +t oder nur w oder w+?
            print("resultLog.csv gefunden und wird neu geschrieben.")
            #writer = csv.writer(log)
            #writer.writerow(card_numbers_to_pass)
            log.write(str(card_numbers_to_pass[0]) + "\n" + str(card_numbers_to_pass[1]) + "\n" + str(card_numbers_to_pass[2]))
            log.close()
    else:
        with open(file_path_result_log, 'x') as log:
            print("Pfad zu resultLog.csv nicht gefunden. Datei wird neu erstellt.")
            #writer = csv.writer(log)
            #writer.writerow(card_numbers_to_pass)
            log.write(str(card_numbers_to_pass[0]) + "\n" + str(card_numbers_to_pass[1]) + "\n" + str(card_numbers_to_pass[2]))
            log.close()
    
    print("Saving pass ended.")   
        



def save_best_move(card_number_to_move, file_path_result_log):
    print("Saving move started.")    

    if os.path.exists(file_path_result_log):    
        with open(file_path_result_log, 'w+t') as log: #TODO: Oder +t oder nur w oder w+?
            print("resultLog.csv gefunden und wird neu geschrieben.")
            #writer = csv.writer(log)
            #writer.writerow(card_number_to_move)
            log.write(str(card_number_to_move))
            log.close()
    else:
        with open(file_path_result_log, 'x') as log:
            print("Pfad zu resultLog.csv nicht gefunden. Datei wird neu erstellt.")
            #writer = csv.writer(log)
            #writer.writerow(card_number_to_move)
            log.write(str(card_number_to_move))
            log.close()
    
    print("Saving move ended.")   

    