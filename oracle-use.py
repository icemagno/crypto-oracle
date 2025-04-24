import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import yfinance as yf
import talib as ta
from sklearn.preprocessing import MinMaxScaler
from keras.models import load_model
from sklearn.preprocessing import MinMaxScaler
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
data['RSI_15'] = ta.RSI(data.Close, timeperiod=15)
data['RSI_7'] = ta.RSI(data.Close, timeperiod=7)
data['EMAF'] = ta.EMA(data.Close, timeperiod=20)
data['EMAM'] = ta.EMA(data.Close, timeperiod=100)
data['EMAS'] = ta.EMA(data.Close, timeperiod=150)
data['ADX'] = ta.ADX(data['High'], data['Low'], data['Close'])

data.dropna(inplace=True)
data.reset_index(inplace = True)
data.drop(['Datetime','Volume','Dividends','Stock Splits'], axis=1, inplace=True)
data = data.loc[:, ['Close','High','Low','Open','RSI_15','RSI_7','EMAF','EMAM','EMAS','ADX']]
data_set = data.iloc[:, 0:10]#.values
pd.set_option('display.max_columns', None)
sc = MinMaxScaler(feature_range=(0,1))
data_set_scaled = sc.fit_transform(data_set)

backcandles = 30
last_window = data_set_scaled[-backcandles:, :]
X_input = np.expand_dims(last_window, axis=0)

model = load_model('/data/oracle/' + coin_pair + '_' + prd + '_' + interv + '.keras')

y_pred = model.predict(X_input)
#y_pred_original = sc.inverse_transform(np.concatenate([np.zeros((y_pred.shape[0], data_set_scaled.shape[1] - 1)), y_pred], axis=1))[:, -1]

y_pred_adjusted = np.concatenate([y_pred, np.zeros((y_pred.shape[0], data_set_scaled.shape[1] - 1))], axis=1)
y_pred_original = sc.inverse_transform(y_pred_adjusted)[:, 0]

print(y_pred_original)
