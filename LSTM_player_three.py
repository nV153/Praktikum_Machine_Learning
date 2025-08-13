"""
This script is part of a series designed to predict the next move in a card game using LSTM networks, 
specifically tailored for different players within the game. It shares the same structure and functionality 
as LSTM_player_zero.py but is customized for another player in the game.

For a detailed explanation of the code structure, methodology, and comments, please refer to the LSTM_player_zero.py script. 
That script contains comprehensive documentation on the LSTM network implementation, data preprocessing, model prediction, 
and other critical components relevant to all scripts in this series.
"""


import torch
import torch.nn as nn
import numpy as np
import sys
import joblib
import pandas as pd

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


model_path = "lstm_model_player_three.pth"
input_size = 65
hidden_size = 25
num_layers = 2
num_classes = 52


model = LSTMNet(input_size, hidden_size, num_layers, num_classes)
model.load_state_dict(torch.load(model_path, map_location=torch.device('cpu')))
model.eval()


scaler_path = 'scaler_LSTM_player_three.pkl'
scaler = joblib.load(scaler_path)


feature_columns = ['card_played_0', 'card_played_1', 'card_played_2', 'card_played_3', 'card_played_4', 'card_played_5', 'card_played_6', 'card_played_7', 'card_played_8', 'card_played_9', 'card_played_10', 'card_played_11', 'card_played_12', 'card_played_13', 'card_played_14', 'card_played_15', 'card_played_16', 'card_played_17', 'card_played_18', 'card_played_19', 'card_played_20', 'card_played_21', 'card_played_22', 'card_played_23', 'card_played_24', 'card_played_25', 'card_played_26', 'card_played_27', 'card_played_28', 'card_played_29', 'card_played_30', 'card_played_31', 'card_played_32', 'card_played_33', 'card_played_34', 'card_played_35', 'card_played_36', 'card_played_37', 'card_played_38', 'card_played_39', 'card_played_40', 'card_played_41', 'card_played_42', 'card_played_43', 'card_played_44', 'card_played_45', 'card_played_46', 'card_played_47', 'card_played_48', 'card_played_49', 'card_played_50', 'card_played_51', 
                 'hCP0P3', 'hCP1P3', 'hCP2P3', 'hCP3P3', 'hCP4P3', 'hCP5P3', 'hCP6P3', 'hCP7P3', 'hCP8P3', 'hCP9P3', 'hCP10P3', 'hCP11P3', 'hCP12P3']


input_features_string = sys.stdin.readline().strip()
input_features_list = list(map(float, input_features_string.split(',')))


input_df = pd.DataFrame([input_features_list], columns=feature_columns)


scaled_features = scaler.transform(input_df)


scaled_features_tensor = torch.Tensor(scaled_features).float().unsqueeze(1)


with torch.no_grad():
    outputs = model(scaled_features_tensor)
    _, predicted = torch.max(outputs.data, 1)


print(predicted.item())
