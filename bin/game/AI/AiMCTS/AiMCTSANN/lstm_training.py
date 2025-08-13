import pandas as pd
import numpy as np
import torch
import torch.nn as nn
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler

# Daten laden
file_path = "path_to_your_simulation_data.csv"
df = pd.read_csv(file_path)

# Hier Daten anpassen und vorbereiten
# Zum Beispiel: Umwandlung von Kartennummern in numerische Werte, Normalisierung usw.

# Angenommen: ein DataFrame `X` für Eingabedaten und `y` für Ausgabedaten (Labels)

# Daten in Trainings- und Testdatensätze aufteilen
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Daten normalisieren
scaler = MinMaxScaler(feature_range=(0, 1))
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

# Konvertieren zu PyTorch Tensoren
X_train = torch.tensor(X_train, dtype=torch.float32)
X_test = torch.tensor(X_test, dtype=torch.float32)
y_train = torch.tensor(y_train, dtype=torch.float32)
y_test = torch.tensor(y_test, dtype=torch.float32)

# LSTM-Modell in PyTorch definieren
class LSTMModel(nn.Module):
    def __init__(self, input_dim, hidden_dim, output_dim):
        super(LSTMModel, self).__init__()
        self.hidden_dim = hidden_dim
        self.lstm = nn.LSTM(input_dim, hidden_dim)
        self.linear = nn.Linear(hidden_dim, output_dim)

    def forward(self, x):
        lstm_out, _ = self.lstm(x)
        predictions = self.linear(lstm_out[-1])
        return predictions

# Modellinstanz erstellen
input_dim = X_train.shape[2]  # Anzahl der Merkmale
hidden_dim = 50  # Anzahl der versteckten Zustände
output_dim = 1  # Ausgabedimension

model = LSTMModel(input_dim, hidden_dim, output_dim)
loss_function = nn.MSELoss()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)

# Trainingsprozess
epochs = 10
for epoch in range(epochs):
    model.train()
    optimizer.zero_grad()
    output = model(X_train)
    loss = loss_function(output, y_train)
    loss.backward()
    optimizer.step()
    print(f'Epoch {epoch+1}, Loss: {loss.item()}')

    # Speichern des trainierten Modells
torch.save(model.state_dict(), "pfad_zum_speicherort/model.pth")
