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
    static final int TIME=10;//测量10组数据
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

            if (throughtput_queue.size()>=TIME)
                throughtput_queue.poll();
        }
    }
//    public double getBandwidth() {
//        return bandwidth;
//    }
    public static float AVG_function(ArrayBlockingQueue arrayBlockingQueue){
        int temp=0;
        float result;
        for (int i=0;i<arrayBlockingQueue.size();i++){
            temp+=(Integer) arrayBlockingQueue.peek();

        }
        result= (float) (temp*1.0/arrayBlockingQueue.size());
        return result;
    }

    public void setStatus(boolean isalive){
        this.isalive=isalive;
        throughtput_queue.clear();
    }

//    public ArrayBlockingQueue getThroughtput_queue(){
//
//        return throughtput_queue;
//    }

}
