import pandas as pd
import numpy as np
import torch
import torch.nn as nn
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
from torch.utils.data import TensorDataset, DataLoader
import torch.optim as optim
from joblib import dump


"""
This script is part of a series for training LSTM models for card game prediction for different players. 
The detailed comments and documentation explaining the data preparation, model definition, training, 
and evaluation process can be found in the lstm_training_player_zero.py script. 

This script follows the same structure and methodology, with adjustments made for the specific player 
it is intended to train a model for. For a comprehensive understanding of the training process and 
the functions used, please refer to the lstm_training_player_zero.py script.
"""

file_path = "playDataOfSimulationPlayerThree.csv"
df = pd.read_csv(file_path)


full_deck = set(range(1, 53))
max_cards_played = 52  

def calculate_cards_played(row, full_deck, max_length):
    hand_cards = {row[f'hC{i}P3'] for i in range(13) if row[f'hC{i}P3'] != -100}
    opponent_cards = {row[f'C{i}'] for i in range(39) if row[f'C{i}'] != -100}
    played_cards = full_deck - hand_cards - opponent_cards
    played_cards_padded = list(played_cards) + [-100] * (max_length - len(played_cards))
    return played_cards_padded

df['cards_played_until_now'] = df.apply(lambda row: calculate_cards_played(row, full_deck, max_cards_played), axis=1)
df['playerNo'] = 1  

def determine_label(row):
    
    mapping = {0: 'trickCard3', 1: 'trickCard2', 2: 'trickCard1', 3: 'trickCard0'}
    return row[mapping[row['startPNo']]]


df['label'] = df.apply(determine_label, axis=1)


cards_played_df = pd.DataFrame(df['cards_played_until_now'].tolist())
cards_played_columns = [f'card_played_{i}' for i in range(cards_played_df.shape[1])]
cards_played_df.columns = cards_played_columns
df = pd.concat([df.drop(['cards_played_until_now'], axis=1), cards_played_df], axis=1)
df.fillna(-100, inplace=True)

feature_columns = cards_played_columns + ['hCP0P3', 'hCP1P3', 'hCP2P3', 'hCP3P3', 'hCP4P3', 'hCP5P3', 'hCP6P3', 'hCP7P3', 'hCP8P3', 'hCP9P3', 'hCP10P3', 'hCP11P3', 'hCP12P3']
features = df[feature_columns]
labels = df['label'].map(lambda x: int(x) if x != -100 else -100)

unique_labels = pd.unique(labels)
label_mapping = {original: new for new, original in enumerate(unique_labels)}
labels_mapped = labels.map(lambda x: label_mapping.get(x, -100))

X_train, X_test, y_train_mapped, y_test_mapped = train_test_split(features, labels_mapped, test_size=0.2, random_state=42)
scaler = MinMaxScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)


scaler_path_for_player_three = 'scaler_LSTM_player_three.pkl' 
dump(scaler, scaler_path_for_player_three)

train_features = torch.Tensor(X_train_scaled).unsqueeze(1)
train_labels = torch.Tensor(y_train_mapped.values).long()
test_features = torch.Tensor(X_test_scaled).unsqueeze(1)
test_labels = torch.Tensor(y_test_mapped.values).long()

train_dataset = TensorDataset(train_features, train_labels)
test_dataset = TensorDataset(test_features, test_labels)

train_loader = DataLoader(train_dataset, batch_size=128, shuffle=True)
test_loader = DataLoader(test_dataset, batch_size=128, shuffle=False)

class LSTMNet(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, num_classes):
        super(LSTMNet, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, num_classes)
    
    def forward(self, x):
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)
        c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)
        out, _ = self.lstm(x, (h0, c0))
        out = self.fc(out[:, -1, :])
        return out

model = LSTMNet(len(feature_columns), 25, 2, len(unique_labels))
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)

num_epochs = 50
for epoch in range(num_epochs):
    for inputs, labels in train_loader:
        outputs = model(inputs)
        loss = criterion(outputs, labels)
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
    print(f'Epoch [{epoch+1}/{num_epochs}], Loss: {loss.item():.4f}')

model.eval()
with torch.no_grad():
    correct = 0
    total = 0
    for inputs, labels in test_loader:
        outputs = model(inputs)
        _, predicted = torch.max(outputs.data, 1)
        total += labels.size(0)
        correct += (predicted == labels).sum().item()
print(f'Genauigkeit des Modells auf dem Testset: {100 * correct / total}%')

torch.save(model.state_dict(), "lstm_model_player_three.pth")
