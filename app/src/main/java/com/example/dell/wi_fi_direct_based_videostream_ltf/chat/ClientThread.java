package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;
public class ClientThread implements Runnable {
protected ClientHandler handler;
protected ChatActivity.MyHandler myHandler;

public Socket socket=new Socket();
private String []type;

    ClientThread(String type[], ChatActivity.MyHandler myHandler){
    this.myHandler=myHandler;
    this.type=type;
}
    @Override
    public void run(){
        try{
        if (!socket.isConnected()&&type[0].equals("group_client")){
            InetAddress inetAddress=InetAddress.getByName("192.168.49.1");
            Log.d(TAG, "inetAddress: "+inetAddress.toString());
            socket.connect(new InetSocketAddress(inetAddress,50000));
        }
        if (socket.isConnected()) {
        switch (type[0]){
            case "group_client":
                Log.d(TAG, "连接到GroupOwner！！！ ");
                break;
            case "group_owner":
                Log.d(TAG, "run:GroupOwner中的写线程成功启动 ");
        }

            
        }
//        if (type.equals("write")){
//        new Thread(new ClientWriteMessage()).start();
//            Log.d(TAG, "ClientWriteMessage()被执行了 "); }
            switch (type[0]){
                case "group_client":
                    new Thread(new ClientWriteMessage()).start();
                    new Thread(new ClientReadMessage()).start();
                    break;
                case"group_owner":
                    //new Thread(new ClientReadMessage()).start();
                    new Thread(new ClientWriteMessage()).start();
                    Log.d(TAG, "run:GroupOwner中的写线程成功启动lalalal");
                    break;
                    default:
                        break;

            }
    }catch (SocketTimeoutException e) {
        e.printStackTrace();
        Log.d(TAG, "超时了！！！ ");
    }
        catch (Exception e) {
        e.printStackTrace();
            Log.d(TAG, "ClientThread run: 异常！");}

        while(Thread.currentThread().isAlive()){
            Log.d(TAG, "run: "+Thread.currentThread().getName()+"在运行");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 弱引用解决内存泄漏问题
     */
    protected  static class ClientHandler extends Handler{
    private WeakReference<ClientThread>mclientThread;
    ClientHandler(ClientThread clientThread){
        mclientThread=new WeakReference<ClientThread>(clientThread);
    }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ClientThread clientThread=mclientThread.get();
            String message=null;
            switch(msg.what){
                case 345:
                    message=msg.obj.toString();
                    break;
//                case 346:
//                    message=msg.obj.toString();
//                    break;
                default:
                    break;
            }try{
                DataOutputStream stream=new DataOutputStream(clientThread.socket.getOutputStream());
                Log.d(TAG, "ClientWrite: 客户端写入message开始");
                if (message!=null)
                    stream.writeUTF(message);
                stream.flush();
                Log.d(TAG, "handleMessage:客户端写入message完毕！ ");}catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * 若不采用弱引用，则可能会引起内存泄漏：
     */

    private Handler handler_test=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message=null;
            switch (msg.what){

                case 345:
                    message=msg.obj.toString();
                    break;
//                case 346:
//                    message=msg.obj.toString();
//                    break;
                default:
                    break;

            }
            try{
                DataOutputStream stream=new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "ClientWrite: 客户端写入message开始");
                if (message!=null)
                    stream.writeUTF(message);
                stream.flush();
                Log.d(TAG, "handleMessage:客户端写入message完毕！ ");}catch (IOException e){
                e.printStackTrace();
            }
        }
    };

    private class ClientWriteMessage implements Runnable{
        @Override
        public void run(){
        Looper.prepare();
        handler=new ClientHandler(ClientThread.this);
        Looper.loop();
    }
    }
    private class ClientReadMessage implements Runnable{
        @Override
        public void run() {
            Looper.prepare();
            try{
                while(socket.isConnected()){
                    DataInputStream stream=new DataInputStream(socket.getInputStream());
                    String m_from_server=stream.readUTF();
                    Log.d(TAG, "run: "+m_from_server);
                    Message message=Message.obtain();
                    message.what=1;
                    message.obj=m_from_server;
                    myHandler.sendMessage(message);
                }

            }catch (IOException e){
                e.printStackTrace();
            }

            Looper.loop();
        }
    }


}
