package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Object2Array;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Packet.HeartBeatPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class HeartBeatClient implements Runnable {
    private boolean is_GO;
    private HeartBeatPacket heartBeatPacket;
    private DatagramSocket socket;
    private volatile boolean runnning;

    public HeartBeatClient(HeartBeatPacket heartBeatPacket,boolean is_Go){

        this.heartBeatPacket=heartBeatPacket;
        this.is_GO=is_Go;
        try {
            socket=new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        runnning=true;
        while (runnning){
            Log.d(TAG, "run: 发送心跳包");
            send_heart(Object2Array.objectToByteArray(heartBeatPacket));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void send_heart(byte[] data){

        try {
            InetAddress address=InetAddress.getByName("192.168.49.1");
            DatagramPacket datagramPacket=new DatagramPacket(data,data.length,address,4447);
            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop_send_heartbeta_packet(){

        runnning=false;
    }
}
