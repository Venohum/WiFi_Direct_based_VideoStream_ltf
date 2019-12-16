package com.example.dell.wi_fi_direct_based_videostream_ltf.UDP;

import android.util.Log;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Object2Array;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Packet.HeartBeatPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class HeartBeatServer implements Runnable {
    private final boolean is_GO;
    private byte[] heart_buff = new byte[1024 * 60];
    private volatile boolean running;
    private DatagramSocket socket = null;
    private Hashtable<String, HeartBeatPacket> hashtable=new Hashtable<>();

    public HeartBeatServer(boolean is_GO) {

        this.is_GO=is_GO;
        try {
            socket = new DatagramSocket(4449);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        running=true;
        Send_MemberList send_memberList=new Send_MemberList();
        ReceiveHeartbeatPacket receiveHeartbeatPacket=new ReceiveHeartbeatPacket();
        if (is_GO){
            new Thread(send_memberList).start();//为组员广播成员列表
            new Thread(receiveHeartbeatPacket).start();//接收组员的心跳包
        }
        while (running) {//接收成员列表
            DatagramPacket heartbeat_packet = new DatagramPacket(heart_buff, heart_buff.length);
            Log.d(TAG, "run: 开始接收成员列表MeberList");
            try {
                socket.receive(heartbeat_packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                hashtable=getMeberList(heartbeat_packet.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        send_memberList.stop_meberlist();
        receiveHeartbeatPacket.stop_receive_heartbeat();
    }
    @SuppressWarnings(value = {"unchecked"})
    private Hashtable<String,HeartBeatPacket> getMeberList(byte []data){
        Hashtable<String,HeartBeatPacket> hashtable;
        if( Object2Array.byteArrayToObject(data) instanceof Hashtable){
            hashtable= (Hashtable<String,HeartBeatPacket>) Object2Array.byteArrayToObject(data);
            if(!hashtable.isEmpty()){
                for (Iterator<Map.Entry<String,HeartBeatPacket>> iterator=hashtable.entrySet().iterator();iterator.hasNext();){
                    Map.Entry<String,HeartBeatPacket> item=iterator.next();
                    Log.d(TAG, "getMeberList: Key:-----"+item.getKey());
                    Log.d(TAG, "getMeberList: 设备名:----"+item.getValue().getDevice_name()+"服务类型——————"+item.getValue().getService_tyep());
                }

            }
            return hashtable;
        }
        return null;
    }
    /**
     * @param data 序列化的心跳包
     */
    private void Packet_decoder(byte[] data) {

        HeartBeatPacket heartBeatPacket = (HeartBeatPacket) Object2Array.byteArrayToObject(data);
        String device_name = heartBeatPacket.getDevice_name();
        String IP = heartBeatPacket.getIP();
        String service_type = heartBeatPacket.getService_tyep();
        hashtable.put(IP,heartBeatPacket);
        Log.d(TAG, "Packet_decoder: " + "IP是" + IP + ", 设备名称是：" + device_name + ", service_type是：" + service_type);
    }

    private class Send_MemberList implements Runnable{

        private DatagramSocket socket1;
        private volatile boolean isrunning;
        Send_MemberList(){
            try {
                socket1 = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            isrunning=true;
            while (isrunning){
                try {
                    InetAddress address=InetAddress.getByName("192.168.49.255");

                    if (!hashtable.isEmpty()) {
                        DatagramPacket datagramPacket=new DatagramPacket(Object2Array.objectToByteArray(hashtable),Object2Array.objectToByteArray(hashtable).length,address,4449);
                        socket1.send(datagramPacket);
                    }
                    Log.d(TAG, "run:广播发送成员列表！");
                    clear_Hashtable();

                    Thread.sleep(10000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void stop_meberlist(){

            isrunning=false;
            Log.d(TAG, "stop_Running: 停止广播成员列表！");
        }
    }

    private class ReceiveHeartbeatPacket implements Runnable{
        private DatagramSocket socket;
        private volatile boolean running;
        ReceiveHeartbeatPacket(){
            try {
                socket=new DatagramSocket(4447);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            running=true;
            while (running){
                DatagramPacket heartbeat_packet = new DatagramPacket(heart_buff, heart_buff.length);
                Log.d(TAG, "run: 开始接收心跳包");
                try {
                    socket.receive(heartbeat_packet);
                    Packet_decoder(heartbeat_packet.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void stop_receive_heartbeat(){
            
            running=false;
            Log.d(TAG, "stop_receive_heartbeat: 停止接收心跳包");
        }
    }

    private void clear_Hashtable(){
        if (!hashtable.isEmpty()){
            for (Iterator<Map.Entry<String,HeartBeatPacket>> iterator = hashtable.entrySet().iterator(); iterator.hasNext();){
                Map.Entry<String,HeartBeatPacket> item=iterator.next();
                //to do with item
                iterator.remove();
            }
        }
    }
    public void stop_Running() {
        running=false;
        Log.d(TAG, "stop_Running: 停止HeartBeatServer接收线程！");
    }
}
