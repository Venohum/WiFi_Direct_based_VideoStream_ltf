package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;

public class ChatActivity extends AppCompatActivity {
    ClientThread clientThread=null;
    ServerThread serverThread=null;
   Boolean bool=true;
    Message message;
private Handler handler=new Handler(){
    public void handleMessage(Message msg){

    }
};
    public static final String TAG = "ChatAcyivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        serverThread=new ServerThread("read");
        new Thread(serverThread).start();
        clientThread=new ClientThread("write",handler);
        new Thread(clientThread).start();
        message=Message.obtain();
        message.what=345;
        message.obj="成功了";
        while(bool){
            if(clientThread.myhandler!=null) {
                Log.d(ChatActivity.TAG,"77777777777777777");
            clientThread.myhandler.sendMessage(message);
            bool=false;}
        }
        Log.d(ChatActivity.TAG,"halellelelellelel");
    }
}
