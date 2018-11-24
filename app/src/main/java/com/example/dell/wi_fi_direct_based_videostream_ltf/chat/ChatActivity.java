package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ServerAsyncTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ClientAsynTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.recorder.RecordService;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.example.dell.wi_fi_direct_based_videostream_ltf.R.id.Sendmessage;

public class ChatActivity extends AppCompatActivity {
    WifiP2pInfo wifiP2pInfo;
    WifiP2pManager wifiP2pManager;
    ClientThread clientThread=null;
    ServerThread serverThread=null;
   Boolean bool=true;
   Boolean isgroupowner;
    public static final String TAG = "ChatAcyivity";
    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE   = 103;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;//ScreenRcord button
    private Button play;
    private Button pause;
    private Button stop;
    private Button replay;
    private Thread thread;
    private Looper mylooper;
    private Handler myhandler;
    private MyHandler UIhandler = new MyHandler(this);
    private SurfaceHolder holder;
    /*
    以下变量的定义是为了实现本地视频显示在SurfaceView中
     */
    private SeekBar seekBar;
    private SurfaceView sv;
    private Boolean isplaying;
    private MediaPlayer mediaPlayer;
    private EditText et_path;
    private int currentPostition = 0;
    public static final int UPDATE_UI =1;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_chat);
//        wifiP2pInfo=new WifiP2pInfo();
//        if (wifiP2pInfo!=null){
//        Log.d(TAG, "哈哈哈哈哈,P2p地址是kong "+wifiP2pInfo.toString() );}
        //final SurfaceView surfaceView =(findViewById(R.id.surfaceView));
        Intent intent1=getIntent();
        isgroupowner=intent1.getBooleanExtra("ChatActivity",true);
        wifiP2pManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final EditText editText=(EditText)findViewById(R.id.Sendtext);
        Button btn_sedm=(Button)findViewById(Sendmessage);
        ServerAsyncTask serverAsyncTask=new ServerAsyncTask(editText);
        ClientAsynTask clientAsynTask=new ClientAsynTask("成功");
//        clientAsynTask.execute("192.168.49.1","5000","成功");
        if (isgroupowner==true){
        serverAsyncTask.execute(50000);}
       else {
            clientAsynTask.execute("成功");
        }
        /*
        if (serverThread==null){
        serverThread=new ServerThread("read",handler);
        new Thread(serverThread).start();
            Log.d(ChatActivity.TAG,"线程第一次被创建");}
        if (clientThread==null){
        clientThread=new ClientThread("write",handler);
        new Thread(clientThread).start();}*/
/**
 * 测试使用SurfaceView播放视频
 *
 */
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        sv=(SurfaceView)findViewById(R.id.surfaceView);
        //et_path=(EditText)findViewById(Sendmessage);
        play=(Button) findViewById(R.id.btn_play);
        pause=(Button)findViewById(R.id.btn_pause);
        stop=(Button)findViewById(R.id.btn_stop);
        replay=(Button)findViewById(R.id.btn_replay);


        play.setOnClickListener(click);
        pause.setOnClickListener(click);
        stop.setOnClickListener(click);
        replay.setOnClickListener(click);
        sv.getHolder().addCallback(callback);
        seekBar.setOnSeekBarChangeListener(change);
        btn_sedm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                message=Message.obtain();
//                message.what=345;
//                message.obj=editText.getText().toString();
//                editText.setText("");
//                if (clientThread.myhandler!=null){
//                    clientThread.myhandler.sendMessage(message);
//                }
            }
        });
        startBtn = (Button) findViewById(R.id.start_record);
//        startBtn.setEnabled(false);
        /**
         * 这是录制屏幕按钮
         */
        startBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (recordService.isRunning()) {
                    recordService.stopRecord();
                    startBtn.setText(R.string.start_record);
                } else {
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

                }
            }
        });
        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }//OnCreate
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder被创建！");
            if(currentPostition>0){
                //创建SurfaceHolder的时候，如果存在上次播放的位置，则按照i上次波安防位置进行播放。
               play(currentPostition);
                currentPostition=0;
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG,"SurfaceHolder changed!");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG,"SurcfaceHolder 被销毁");
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                currentPostition =mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }

        }
    };
    private SeekBar.OnSeekBarChangeListener change=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            int p1=mediaPlayer.getCurrentPosition();
//            if (mediaPlayer.isPlaying()){
//                mediaPlayer.seekTo(p1);
//            }
//
//            Log.d(TAG, "onProgressChanged: ");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTra");
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress =seekBar.getProgress();
            if (mediaPlayer!=null&& mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(progress);
                Log.d(TAG, "onStopTrackingTouch: I touched the seekbar!!!");
            }
        }
    };
    private View.OnClickListener click=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                switch (v.getId()){
                    case R.id.btn_replay:
                        replay();
                        break;
                    case R.id.btn_play:
                        play(0);
                        break;
                    case R.id.btn_pause:
                        pause();
                        break;
                    case R.id.btn_stop:
                        stop();
                        break;
                    default:
                        break;
                }
        }
    };

    static class MyHandler extends Handler{
        private final WeakReference<ChatActivity>mactivity;
        MyHandler( ChatActivity activity){

            mactivity=new WeakReference<ChatActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg){
            ChatActivity activity=mactivity.get();
            super.handleMessage(msg);
            if(activity!=null){
                switch (msg.what){
                    case UPDATE_UI:
//                        activity.UIhandler.sendMessageDelayed(msg,500);
                        int position=activity.mediaPlayer.getCurrentPosition();
                        int totalduration =activity.mediaPlayer.getDuration();
                        activity.seekBar.setMax(totalduration);
                        activity.seekBar.setProgress(position);
                        Log.d(TAG, "handleMessage: 更新了呀丫丫丫丫");
                        break;
                }
            }
        }
    }
    protected void play(final int msec){
        String path ="/storage/emulated/0/DCIM/Camera/b.mp4";
        File file =new File(path);
        if (!file.exists()){
            Toast.makeText(this,"Video path is error",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            //mediaPlayer.setDataSource();
            mediaPlayer.setDisplay(sv.getHolder());
            Log.i(TAG, "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {Log.i(TAG, "onPrepared: 装载完成");
                    mediaPlayer.start();
                    //按照初始位置播放
                    mediaPlayer.seekTo(msec);
                    //设置进度条最大进度为视频流的最大播放时长
                    seekBar.setMax(mediaPlayer.getDuration());
                    Log.d(TAG, "onPrepared: 最大播放时长"+mediaPlayer.getDuration());
                  new Thread(){
                        @Override
                        public void run(){
                            UIhandler.sendEmptyMessage(UPDATE_UI);
                            try{
                                isplaying=true;
                                while(isplaying){
                                    int current=mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(500);

                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    play.setEnabled(false);
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //在播放完时被回调
                    play.setEnabled(true);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    play(0);
                    isplaying=false;
                    return false;
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    protected void pause(){
        if (pause.getText().toString().equals("继续")){
            pause.setText("暂停");
            mediaPlayer.start();
            Toast.makeText(this,"继续播放",Toast.LENGTH_SHORT).show();
            return;
        }
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            pause.setText("继续");
            Toast.makeText(this,"暂停播放",Toast.LENGTH_SHORT).show();
        }
    }
    protected void stop(){
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            isplaying=false;
        }
        Log.d(TAG, "stop: click the button of stop!");
    }

    protected void replay(){
        Log.d(TAG, "replay: 99999999999999999999999999");
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(0);
            Toast.makeText(this,"重新播放",Toast.LENGTH_SHORT).show();
            //replay.setText("暂停");
        }
        isplaying=false;
        play(0);
        Log.d(TAG, "replay: zahuiushi ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
            startBtn.setText(R.string.stop_record);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                finish();
//            }
//        }
//    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            startBtn.setEnabled(true);
            startBtn.setText(recordService.isRunning() ? R.string.stop_record : R.string.start_record);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };
}

