package com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic;

import android.content.Context;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic.ParametersCollection.TAG;


public class ComputeBandwidth implements Runnable {
    private long preDataSize= 0L;
    private boolean isalive=true;
    private static final int TIME=20;//测量20组数据
    public static ArrayBlockingQueue<Integer> throughtput_queue= new ArrayBlockingQueue<>(TIME);
    static int throughtput =-1;


    @Override
    public void run() {
        while (isalive){
            preDataSize=TrafficStats.getTotalTxBytes();
            try {
                Thread.sleep(1000);
            }catch(Exception e){

                e.printStackTrace();
            }
            long temp=TrafficStats.getTotalTxBytes()-preDataSize;
            //    private double bandwidth=0;
            throughtput = (int) temp * 8 / 1024;
            throughtput_queue.add(throughtput);

//            Log.d(TAG, "run: Bandwidth-----："+throughtput+"kbps");
//            Log.d(TAG, "run: throughtput_queue.size()"+throughtput_queue.size());
            if (throughtput_queue.size()>=TIME)
                throughtput_queue.poll();
        }
    }
//    public double getBandwidth() {
//        return bandwidth;
//    }

    public void setStatus(boolean isalive){
        this.isalive=isalive;
        throughtput_queue.clear();
    }

//    public ArrayBlockingQueue getThroughtput_queue(){
//
//        return throughtput_queue;
//    }

}
