package com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic.ParametersCollection.TAG;


public class ComputeBandwidth extends AppCompatActivity implements Runnable {
    private long preDataSize= 0L;
//    private double bandwidth=0;



    public int getRSSI(String regrex)  {
        int rssi = -1000;
        if(!("").equals(regrex)){

            WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            Pattern pattern = Pattern.compile(regrex);
            //Log.d(WiFiDirectActivity.TAG,""+scanResults.size());

            if(null != pattern){
                for(ScanResult scanResult : scanResults){

                    Matcher matcher = pattern.matcher(scanResult.SSID);
                    if(matcher.matches()){

               /* Log.d(WiFiDirectActivity.TAG, scanResult.BSSID);
                Log.d(WiFiDirectActivity.TAG, ""+scanResult.level);*/
                        rssi = scanResult.level;

                    }
                }
            }
        }

        return rssi;
    }

    @Override
    public void run() {
        while (true){
            preDataSize=TrafficStats.getTotalTxBytes();
            try {
                Thread.sleep(1000);
            }catch(Exception e){

                e.printStackTrace();
            }
            long temp=TrafficStats.getTotalTxBytes()-preDataSize;
            Log.d(TAG, "run: Bandwidth-----："+temp/8/1024+"kbps");
        }





//        synchronized (dataSize){
//            bandwidth = (dataSize - preDataSize)/1000000.0;
//            Log.d("带宽", "ComputeBandwidth" + dataSize/1000000.0+" "+preDataSize/1000000.0+" "+bandwidth+"Mbps" );
//            preDataSize = dataSize;
//
//
//        }
    }
//    public double getBandwidth() {
//        return bandwidth;
//    }
}
