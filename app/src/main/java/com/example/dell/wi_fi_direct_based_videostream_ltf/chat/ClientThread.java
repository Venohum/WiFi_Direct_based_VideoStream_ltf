package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ClientThread implements Runnable {

public Handler handler,myhandler;
public Socket socket;
private String type,message="你好，我来自客户端线程";

public ClientThread(String type,Handler handler){
    this.handler=handler;
    this.type=type;
}
    @Override
    public void run(){
    if (socket==null){
        socket=new Socket();
    }
    try{
        if (!socket.isConnected()){
            InetAddress inetAddress=InetAddress.getByName("192.168.49.1");
            Log.d(TAG, "inetAddress: "+inetAddress.toString());
            socket.connect(new InetSocketAddress(inetAddress,50000));
        }
        if (socket.isConnected()) {
           // System.out.println("连接到服务器！！！");
            Log.d(TAG, "连接到服务器！！！ ");
        }
        if (type=="write"){
        new Thread(new ClientWriteMessage()).start();
            Log.d(TAG, "ClientWriteMessage()被执行了 ");
        }


    }catch (SocketTimeoutException e) {
        e.printStackTrace();
        Log.d(TAG, "超时了！！！ ");
    }
        catch (Exception e) {
        e.printStackTrace();
    System.out.print("异常");}
    }

    private class ClientWriteMessage implements Runnable{
    @Override
        public void run(){
        Looper.prepare();
        myhandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 345) {
                    try {
                        message = msg.obj.toString();
                        System.out.println(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                    Log.d(TAG, "ClientWrite：客户端写入message开始 ");
                    stream.writeUTF(message);
                    stream.flush();
                    Log.d(TAG, "ClientWrite：客户端写入message完毕");
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    System.out.println(e.getMessage()+"看这里ClientThread写");
                    e.printStackTrace();
                }
            }
        };
//        if (myhandler==null){
//            Log.d(TAG, "111111111111111111111111111失败 ");
//        }else {
//            Log.d(TAG, "111111111111111111111111111不空 ");
//        }
        Looper.loop();
    }
    }
}
