package com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic;

import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.WiFiDirectActivity;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class ParametersCollection implements Runnable {

//    private TrafficStats trafficStats;
    private WiFiDirectActivity wiFiDirectActivity;
    public static final String TAG = ParametersCollection.class.getSimpleName();
    private boolean isalive=true;
    private static int TIME=10;
    public static ArrayBlockingQueue<Integer>RSSI_queue= new ArrayBlockingQueue<>(TIME);

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
            Log.d(TAG, "run: 当前RSSI值:----------:" + wiFiDirectActivity.getRSSI(wiFiDirectActivity.getSSID()));
            RSSI_queue.add(wiFiDirectActivity.getRSSI(wiFiDirectActivity.getSSID()));


            if (RSSI_queue.size()>=TIME){
                Log.d(TAG, "run:"+TIME+"秒内 RSSI值----------:"+RSSI_queue.toString());
                RSSI_queue.poll();
            }

            Log.d(TAG, "run: 当前 throughtput值:--------:"+ComputeBandwidth.throughtput+"kbps");
            if (ComputeBandwidth.throughtput_queue.size()>=ComputeBandwidth.TIME)
            Log.d(TAG, "run: "+TIME+"秒内throughput值:--------"+ Arrays.toString(ComputeBandwidth.throughtput_queue.toArray()));



            //Log.d(TAG, "run: TrafficStats--:"+TrafficStats.getTotalTxBytes());
        }

    }
    public void setStatus(boolean isalive){
        this.isalive=isalive;

    }
}
