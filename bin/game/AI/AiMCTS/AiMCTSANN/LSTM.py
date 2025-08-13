import pandas as pd
import numpy as np
import torch # installieren über pip3 install --pre torch torchvision torchaudio --index-url https://download.pytorch.org/whl/nightly/cpu
import torch.nn as nn
from sklearn.model_selection import train_test_split # installieren über pip install scikit-learn
from sklearn.preprocessing import MinMaxScaler

# Laden des trainierten Modells
model = LSTMModel(input_dim, hidden_dim, output_dim)
model.load_state_dict(torch.load("pfad_zum_speicherort/model.pth"))
# model.eval()  # Setzt das Modell in den Evaluationsmodus

# Evaluation des Modells
# model.eval()
# with torch.no_grad():
    # predicted = model(X_test)
    # Hier Metriken für Evaluierung berechnen/ Evtl bei Aufteilung in 2 Dateien Evaluationsteil weglassen

# Einen Spielzustand für die Vorhersage vorbereiten
# Beispiel: lstmGameState = [feature1, feature2, ...]
# Dies sollte dem Format der Trainingsdaten entsprechen
lstmGgameState = np.array([gameState])
lstmGgameState = scaler.transform(gameState)  # gleiche Skalierung zu verwenden
lstmGgameState = torch.tensor(gameState, dtype=torch.float32)

# Vorhersage mit dem trainierten Modell
model.eval()
with torch.no_grad():
    predicted_action = model(gameState)
    print("Vorhergesagte Aktion:", predicted_action.numpy())
