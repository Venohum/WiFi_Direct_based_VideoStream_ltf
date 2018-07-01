package com.example.dell.wi_fi_direct_based_videostream_ltf.Service;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.chat.ChatActivity.TAG;

public class ClientAsynTask extends AsyncTask <String,Void,String>{
    @Override
    protected String doInBackground(String... strings) {
        try{
            Socket socket=new Socket(strings[0],Integer.parseInt(strings[1]));
            if (socket.isConnected()) {
                Log.d(TAG, "连接到服务器！！！ ");
            }
            PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            printWriter.println(strings[2]);
            printWriter.flush();
            socket.close();
        }catch (Exception e){
            Log.d("Server","ClientAsynTask错误了");
        }

        return "Ok";
    }
}

