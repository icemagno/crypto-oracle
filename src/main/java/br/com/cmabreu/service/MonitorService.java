package br.com.cmabreu.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class MonitorService {

    @Autowired RESTService restService;
    private final String coinName = "AAVE";
    private final String interval = "15m";
    private double lastCheckedPrice = 0.0;
    private double valorPrevisto = 0;
    private double ultimoPrevisto = 0;


    @Scheduled( fixedDelay = 1000 * 60 )
    private void check(){
        try{
            //String url = "https://api.binance.com/api/v1/ticker/price?symbol="+coinName+"USDT";
            //String response = restService.doRequestGet(url);
            //JSONObject res = new JSONObject( response );
            //String currentPrice = res.getString("price");
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            String url = "https://api.binance.com/api/v1/klines?symbol="+coinName+"USDT&interval=" + interval;
            String response = restService.doRequestGet(url);
            JSONArray res = new JSONArray(response);
            
            JSONArray lastCandle = res.getJSONArray( res.length() -2);
            JSONArray currentCandle = res.getJSONArray( res.length() -1);
    

            double currentPrice = Double.parseDouble(currentCandle.getString(4));
            Date currentTime = new Date(currentCandle.getLong(6));
            String formattedCurrentTime = df.format(currentTime);


            double closePrice = Double.parseDouble(lastCandle.getString(4));


            System.out.println(coinName+"/USDT: Fecha em " + formattedCurrentTime + " GMT (" + interval + ")" );
            if( lastCheckedPrice != closePrice ) {
                lastCheckedPrice = closePrice;
                ultimoPrevisto = valorPrevisto;
                fazPrevisao();
            }
            double diff = valorPrevisto - currentPrice;
            String dir = "U";
            if( diff < 0 ) dir = "D";

            System.out.println(" > Atual    : " + currentPrice  );
            System.out.println(" > Anterior : " + closePrice + " ( Foi Previsto: " + ultimoPrevisto + " )");
            System.out.println(" > Previsao : [" + dir + "] " + valorPrevisto + " ( " + diff + " )");

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

    private void fazPrevisao() throws Exception {
        String intr = "--interval=" + interval;
        String cn = "--coinname=" + coinName;
		String[] command = { "python", "oracle-use.py", cn, "--period=60d", intr };
        double res = 0;
		try {
			String[] environments = null;
		    Process process = Runtime.getRuntime().exec(command, environments, new File( "/home/oracle" ) );                    
		    
		    BufferedReader stdInput = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
		    BufferedReader stdError = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );		    

		    String s = null;
		    while ((s = stdInput.readLine()) != null) {
                try {
                    JSONArray arr = new JSONArray( s );
                    res = arr.getDouble(0);
                } catch ( Exception e){
                    // 
                }
		    }

		    while ((s = stdError.readLine()) != null) {
		        //System.out.println(" > ERR " + s );
		    }		    
		    
		    process.waitFor();

            valorPrevisto = res;
		} catch ( Exception e ) {
			e.printStackTrace();
		}	





    }


        /*
            [
                [
                    1591258320000,      	// Open time
                    "9640.7",       	 	// Open
                    "9642.4",       	 	// High
                    "9640.6",       	 	// Low
                    "9642.0",      	 	 	// Close (or latest price)
                    "206", 			 		// Volume
                    1591258379999,       	// Close time
                    "2.13660389",    		// Base asset volume
                    48,             		// Number of trades
                    "119",    				// Taker buy volume
                    "1.23424865",      		// Taker buy base asset volume
                    "0" 					// Ignore.
                ]
            ] 
        */


}
