import numpy as np
import matplotlib.pyplot as plt
import pandas as pd 
import yfinance as yf
import talib as ta
import tensorflowjs as tfjs
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd 
import yfinance as yf
import talib as ta
import tensorflowjs as tfjs
from sklearn.preprocessing import MinMaxScaler
from keras.models import Sequential
from keras.layers import LSTM
from keras.layers import Dropout
from keras.layers import Dense
from keras.layers import TimeDistributed
import tensorflow as tf
import keras
from keras import optimizers
from keras.callbacks import History, EarlyStopping
from keras.models import Model
from keras.layers import Dense, Dropout, LSTM, Input, Activation, concatenate
import argparse

parser = argparse.ArgumentParser(description="Oracle")
parser.add_argument('--coinname', type=str, help='Token Name.', required=True)
parser.add_argument('--period', type=str, help='Data Window.', default='60d')
parser.add_argument('--interval', type=str, help='Candle Interval. Use 1h, 15m, etc.', default='1h')

args = parser.parse_args()

coin_name = args.coinname.upper()
coin_pair = coin_name + "-USD"
prd=args.period
interv=args.interval

ticker = yf.Ticker( coin_pair )
the_data = ticker.history(period=prd,interval=interv)

# Adding indicators
data=the_data.copy()
data.columns = data.columns.get_level_values(0)

# Adding indicators
data=the_data.copy()
data.columns = data.columns.get_level_values(0)
data['RSI_15'] = ta.RSI(data.Close, timeperiod=15)
data['RSI_7'] = ta.RSI(data.Close, timeperiod=7)
data['EMAF'] = ta.EMA(data.Close, timeperiod=20)
data['EMAM'] = ta.EMA(data.Close, timeperiod=100)
data['EMAS'] = ta.EMA(data.Close, timeperiod=150)
data['ADX'] = ta.ADX(data['High'], data['Low'], data['Close'])
data = data.dropna()
# print(data)

data['Target'] = data['Close']-data.Open
data['Target'] = data['Target'].shift(-1)
data['TargetClass'] = [1 if data.Target[i]>0 else 0 for i in range(len(data))]
data['TargetNextClose'] = data['Close'].shift(-1)
data.dropna(inplace=True)
data.reset_index(inplace = True)
data.drop(['Datetime','Volume','Dividends','Stock Splits'], axis=1, inplace=True)
data = data.loc[:, ['Close','High','Low','Open','RSI_15','RSI_7','EMAF','EMAM','EMAS','ADX','Target','TargetClass','TargetNextClose']]
data_set = data.iloc[:, 0:13]#.values
pd.set_option('display.max_columns', None)

sc = MinMaxScaler(feature_range=(0,1))
data_set_scaled = sc.fit_transform(data_set)


X = []
backcandles = 30
total_indicators = 10
for j in range(total_indicators):
    X.append([])
    for i in range(backcandles, data_set_scaled.shape[0]):
        X[j].append(data_set_scaled[i-backcandles:i, j])

X=np.moveaxis(X, [0], [2])
X, yi =np.array(X), np.array(data_set_scaled[backcandles:,-1])
y=np.reshape(yi,(len(yi),1))

splitlimit = int(len(X)*0.8)
X_train, X_test = X[:splitlimit], X[splitlimit:]
y_train, y_test = y[:splitlimit], y[splitlimit:]

np.random.seed(10)

lstm_input = Input(shape=(backcandles, total_indicators), name='lstm_input')

inputs = LSTM(150,name='first_layer')(lstm_input)

inputs = Dense(1, name='dense_layer')(inputs)
output = Activation('linear', name='output')(inputs)

model = Model(inputs=lstm_input, outputs=output)
adam = optimizers.Adam()
model.compile(optimizer=adam, loss='mse')

early_stopping = EarlyStopping(monitor='val_loss', patience=15, restore_best_weights=True)

model.fit(x=X_train, y=y_train, batch_size=15, epochs=30, shuffle=True, validation_split = 0.1, callbacks=[early_stopping])

model.save('/data/oracle/' + coin_pair + '_' + prd + '_' + interv + '.keras') 

y_pred = model.predict(X_test)
y_pred_original = sc.inverse_transform(np.concatenate([np.zeros((y_pred.shape[0], data_set_scaled.shape[1] - 1)), y_pred], axis=1))[:, -1]
plt.figure(figsize=(16,8))
plt.plot(y_test, color = 'black', label = 'Test')
plt.plot(y_pred, color = 'green', label = 'Pred')
plt.legend()
plt.savefig('/data/oracle/' + coin_pair + '.png')