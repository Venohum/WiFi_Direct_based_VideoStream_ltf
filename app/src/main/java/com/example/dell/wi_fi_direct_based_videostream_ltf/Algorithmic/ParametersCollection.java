package com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic;

import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.WiFiDirectActivity;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ParametersCollection implements Runnable {

//    private TrafficStats trafficStats;
    private WiFiDirectActivity wiFiDirectActivity;
    public static final String TAG = ParametersCollection.class.getSimpleName();
    private boolean isalive=true;

    public ParametersCollection(WiFiDirectActivity wiFiDirectActivity){

        this.wiFiDirectActivity=wiFiDirectActivity;
    }


    @Override
    public void run() {
//        Log.d(TAG, "run: SSID:"+wiFiDirectActivity.getSSID());

//        trafficStats=new TrafficStats();
        ComputeBandwidth computeBandwidth=new ComputeBandwidth();
        int i=0;
        while(isalive){
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d(TAG, "run: RSSI----------:" + wiFiDirectActivity.getRSSI(wiFiDirectActivity.getSSID()));
            Log.d(TAG, "run: Bandwidth--------:"+ComputeBandwidth.throughtput+"kbps");
            Log.d(TAG, "run: Bandwidth--------"+ Arrays.toString(ComputeBandwidth.throughtput_queue.toArray()));

            //Log.d(TAG, "run: TrafficStats--:"+TrafficStats.getTotalTxBytes());
        }

    }
    public void setStatus(boolean isalive){
        this.isalive=isalive;

    }
}
