package com.example.dell.wi_fi_direct_based_videostream_ltf.Service;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ClientAsynTask extends AsyncTask <String,String,Boolean>{
    String string;
    public ClientAsynTask(String s){
        this.string=s;
    }
    @Override
    protected Boolean doInBackground(String... string) {
        try{
            Socket socket=new Socket();
            InetAddress inetAddress=InetAddress.getByName("192.168.49.1");
            socket.connect(new InetSocketAddress(inetAddress,50000));
                Log.d(TAG, "doInBackground: strings"+string[0]);
            if (socket.isConnected()) {
                Log.d(TAG, "连接到服务器！！！ ");
            }else{
                Log.d(TAG, "连接到服务器失败！！！ ");}
            PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            printWriter.println(string[0]);

            printWriter.flush();
            socket.close();//传输一次socket就被关闭了
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"ClientAsynTask错误了");
            return false;
        }
       return true;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Log.d(TAG,values[0]);

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean){
            Log.d(TAG, "onPostExecute: 客户端连上了");
        }
            else{
            Log.d(TAG, "onPostExecute: 没连上");


        }
    }
}

