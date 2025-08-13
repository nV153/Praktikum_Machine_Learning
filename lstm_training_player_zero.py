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
This script prepares and trains an LSTM model for a card game prediction task. 
It processes game data, creates features and labels, trains an LSTM network, 
and saves the model and scaler for future use.
"""
# Load data
file_path = "playDataOfSimulationPlayerZero.csv"
df = pd.read_csv(file_path)


# Assume a full deck of cards (IDs from 1 to 52)
full_deck = set(range(1, 53))
max_cards_played = 52  


"""
Calculates the cards played based on the current row's data.
    
    Parameters:
    - row: The current row of DataFrame being processed.
    - full_deck: A set containing all card IDs.
    - max_length: The maximum number of cards that can be played.
    
Returns:
    A list of played cards, padded with -100 to indicate unused slots.
"""
def calculate_cards_played(row, full_deck, max_length):

    
    hand_cards = {row[f'hC{i}P0'] for i in range(13) if row[f'hC{i}P0'] != -100}
    opponent_cards = {row[f'C{i}'] for i in range(39) if row[f'C{i}'] != -100}
    played_cards = full_deck - hand_cards - opponent_cards
    played_cards_padded = list(played_cards) + [-100] * (max_length - len(played_cards))
    return played_cards_padded


# Apply functions to DataFrame
df['cards_played_until_now'] = df.apply(lambda row: calculate_cards_played(row, full_deck, max_cards_played), axis=1)


"""
    Determines the correct label for training based on the starting player number and the played card.
    
    Parameters:
    - row: The current row of DataFrame.
    
    Returns:
    The ID of the played card as the label.
    """

def determine_label(row):
    
    mapping = {1: 'trickCard3', 0: 'trickCard0', 2: 'trickCard2', 3: 'trickCard1'}
    return row[mapping[row['startPNo']]]

# Apply functions to DataFrame
df['label'] = df.apply(determine_label, axis=1)

# Prepare features and labels for training
cards_played_df = pd.DataFrame(df['cards_played_until_now'].tolist())
cards_played_columns = [f'card_played_{i}' for i in range(cards_played_df.shape[1])]
cards_played_df.columns = cards_played_columns
df = pd.concat([df.drop(['cards_played_until_now'], axis=1), cards_played_df], axis=1)
df.fillna(-100, inplace=True)

feature_columns = cards_played_columns + ['hCP0P0', 'hCP1P0', 'hCP2P0', 'hCP3P0', 'hCP4P0', 'hCP5P0', 'hCP6P0', 'hCP7P0', 'hCP8P0', 'hCP9P0', 'hCP10P0', 'hCP11P0', 'hCP12P0']
features = df[feature_columns]
print(feature_columns)
labels = df['label'].map(lambda x: int(x) if x != -100 else -100)

unique_labels = pd.unique(labels)
label_mapping = {original: new for new, original in enumerate(unique_labels)}
labels_mapped = labels.map(lambda x: label_mapping.get(x, -100))

X_train, X_test, y_train_mapped, y_test_mapped = train_test_split(features, labels_mapped, test_size=0.2, random_state=42)
scaler = MinMaxScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)


scaler_path_for_player_zero = 'scaler_LSTM_player_zero.pkl'  
dump(scaler, scaler_path_for_player_zero)


train_features = torch.Tensor(X_train_scaled).unsqueeze(1)
train_labels = torch.Tensor(y_train_mapped.values).long()
test_features = torch.Tensor(X_test_scaled).unsqueeze(1)
test_labels = torch.Tensor(y_test_mapped.values).long()

train_dataset = TensorDataset(train_features, train_labels)
test_dataset = TensorDataset(test_features, test_labels)

train_loader = DataLoader(train_dataset, batch_size=128, shuffle=True)
test_loader = DataLoader(test_dataset, batch_size=128, shuffle=False)


"""
    Defines an LSTM network for card game prediction.
    
    Parameters:
    - input_size: The number of input features.
    - hidden_size: The number of hidden units in the LSTM layers.
    - num_layers: The number of LSTM layers.
    - num_classes: The number of output classes (unique cards).
    """
class LSTMNet(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, num_classes):
        super(LSTMNet, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, num_classes)
    
    # Forward pass of the LSTM network.
    # Parameters: - x: Input features tensor.
    # Returns: The network's output tensor
    def forward(self, x):
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)
        c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)
        out, _ = self.lstm(x, (h0, c0))
        out = self.fc(out[:, -1, :])
        return out

model = LSTMNet(len(feature_columns), 25, 2, len(unique_labels))
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)

# Model training and evaluation
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

torch.save(model.state_dict(), "lstm_model_player_zero.pth")


"""
End of script for training the LSTM model for card game prediction.
The model and scaler are saved for deploying in the game's AI.
"""