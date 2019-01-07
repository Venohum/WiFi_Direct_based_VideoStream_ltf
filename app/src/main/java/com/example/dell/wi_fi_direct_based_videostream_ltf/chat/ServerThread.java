package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ServerThread implements Runnable {
//    private Socket socket;
    private String[] type;
    private ServerSocket serverSocket;
    public ChatActivity.MyHandler myhandler;
    protected ServerHandler serverHandler;
    private Thread serverRead;
    private Thread serverWrite;
     ServerThread(String type[], ChatActivity.MyHandler handler){
    this.myhandler=handler;
    this.type=type;
    }
    @Override
    public void run(){
        Log.d(TAG, "run: 组主线程ServerThread创建成功！");
        Log.d(TAG, "onCreate: 这个线程的名字叫做（期待是组主线程）："+Thread.currentThread().getName());


        try{
            if (serverSocket==null){
                serverSocket=new ServerSocket(50000);
                serverSocket.setReuseAddress(true);
            }
//            Socket client;
            while(true){
                Socket client=serverSocket.accept();
                if(client!=null){
                Log.d(TAG,"新客户端"+client.getInetAddress().getHostAddress());}
               serverRead= new Thread(new ServerRead(client));
                serverRead.start();
                serverWrite=new Thread(new ServerWrite(client));
                serverWrite.start();



            }

        }catch (IOException e){
            e.printStackTrace();
        }
        if(serverSocket.isClosed()){
            serverWrite.interrupt();
            serverRead.interrupt();
        }


    }
    class ServerRead implements Runnable{
         private Socket socket;
         ServerRead(Socket socket){this.socket=socket;}
        @Override
        public void run(){
            try{
                while(socket.isConnected()){
                    DataInputStream inputStream=new DataInputStream(socket.getInputStream());
                    Log.d(TAG,"开始读");
                    String message =inputStream.readUTF();
                    if (message.equals("")){
                        Log.d(TAG, "message 是空的！ ");
                    }
                    Log.d(TAG, message);
                    Message message1=Message.obtain();
                    message1.what=1;
                    message1.obj="客户端"+socket.getInetAddress().getHostName()+"说:"+ message;
                    myhandler.sendMessage(message1);
                    Log.d(TAG, "run: "+(String)message1.obj);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            finally {
                try {
                    if(socket != null && !socket.isClosed()){
                        socket.close();
                        Log.d(TAG, "run: socket 关闭！");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "ServerThread 233");
                    e.printStackTrace();
                }
            }
        }
    }

    static class ServerHandler extends Handler{
        private WeakReference<ServerThread>mserverthread;
        private Socket socket;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ServerThread serverThread=mserverthread.get();
            String message=null;
            switch(msg.what){
//                case 35:
//                    message=msg.obj.toString();
//                    break;
                case 346:
                    message=msg.obj.toString();
                    break;
                default:
                    break;
            }try{
                DataOutputStream stream=new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "ServerWrite: 组主写入message开始！");
                if (message!=null)
                    stream.writeUTF(message);
                stream.flush();
                Log.d(TAG, "ServerWrite:组主写入message完毕！ ");}catch (IOException e){
                e.printStackTrace();
            }
        }
        ServerHandler(ServerThread serverThread,Socket socket){
            mserverthread=new WeakReference<ServerThread>(serverThread);
            this.socket=socket;

        }
    }
    class ServerWrite implements Runnable{
         Socket socket;
         ServerWrite(Socket socket){this.socket=socket;}
        @Override
        public void run(){
            Looper.prepare();
            serverHandler=new ServerHandler(ServerThread.this,socket);
            Looper.loop();
        }
    }
}
