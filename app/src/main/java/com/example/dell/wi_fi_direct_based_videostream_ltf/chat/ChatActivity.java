package com.example.dell.wi_fi_direct_based_videostream_ltf.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Camera.CameraActivity;
import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ServerAsyncTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Service.ClientAsynTask;
import com.example.dell.wi_fi_direct_based_videostream_ltf.recorder.RecordService;

import java.io.IOException;

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
    private static final int CAMERA_REQUEST_CODE=104;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;
    private Button startBtn;//ScreenRcord button
    private Button play;
    private Button pause;
    private Button stop;
    private Button replay;
    private Button btn_sedm;
    private Thread thread;
    private CameraDevice camera;
    private  MediaRecorder mediaRecorder;
    private boolean isRecording=false;
    private CameraDevice cameraDevice;

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
        verifyPermission(new String[]{Manifest.permission.CAMERA});
        Intent intent1=getIntent();
        isgroupowner=intent1.getBooleanExtra("ChatActivity",false);
        wifiP2pManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final TextView textView=(TextView) findViewById(R.id.chat_content);

        ServerAsyncTask serverAsyncTask=new ServerAsyncTask(textView);
        ClientAsynTask clientAsynTask=new ClientAsynTask("成功");
       // clientAsynTask.execute("192.168.49.1","5000","成功");
        if (isgroupowner){
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
        btn_sedm=(Button)findViewById(Sendmessage);

        play.setOnClickListener(click);
        pause.setOnClickListener(click);
        stop.setOnClickListener(click);
        replay.setOnClickListener(click);

        sv.getHolder().addCallback(callback);
        seekBar.setOnSeekBarChangeListener(change);

//        btn_sedm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                message=Message.obtain();
////                message.what=345;
////                message.obj=editText.getText().toString();
////                editText.setText("");
////                if (clientThread.myhandler!=null){
////                    clientThread.myhandler.sendMessage(message);
////                }
//            }
//        });
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
                    /**
                     * 用该方法启动活动，可以将结果返回到OnActivityResult()方法中
                     */

                }
            }
        });
        /*
        这是点击的相机按钮
         */
        btn_sedm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        if (ContextCompat.checkSelfPermission(ChatActivity.this,Manifest.permission_group.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission_group.CAMERA},CAMERA_REQUEST_CODE);
        }
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
}//OnCreate

    private void verifyPermission(String[] permissions) {



    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback stateCallback=new CameraDevice.StateCallback(){
      @Override
      public void onOpened(CameraDevice camera){
          cameraDevice=camera;
          videoRecorder();
      }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
          camera.close();
          cameraDevice=null;

        }
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
          camera.close();
          cameraDevice=null;

        }
    };

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
                Log.d(TAG, "onStopTrackingTouch: I touched the seekbar!!!!");
            }
        }
    };
    private View.OnClickListener click=new View.OnClickListener() {
       // @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View v) {

                switch (v.getId()){
                    case R.id.btn_replay:
                        //replay();
                        Intent intent=new Intent(ChatActivity.this, CameraActivity.class);
                        startActivity(intent);
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

    /**
     * 采用静态内部类+弱引用的方式，处理利用因使用Handler引起的内存泄漏问题
     */
//    static class MyHandler extends Handler{
//        private final WeakReference<ChatActivity>mactivity;
//        MyHandler( ChatActivity activity){
//
//            mactivity=new WeakReference<ChatActivity>(activity);
//        }
//        @Override
//        public void handleMessage(Message msg){
//            ChatActivity activity=mactivity.get();
//            super.handleMessage(msg);
//            if(activity!=null){
//                switch (msg.what){
//                    case UPDATE_UI:
//                        int position=activity.mediaPlayer.getCurrentPosition();
//                        int totalduration =activity.mediaPlayer.getDuration();
//                        activity.seekBar.setMax(totalduration);
//                        activity.seekBar.setProgress(position);
//                        Log.d(TAG, "handleMessage: 更新了呀丫丫丫丫");
//                        break;
//                }
//            }
//        }
//    }
    protected void play(final int msec){
//        String path ="/storage/emulated/0/DCIM/Camera/b.mp4";
//        File file =new File(path);
//        if (!file.exists()){
//            Toast.makeText(this,"Video path is error",Toast.LENGTH_SHORT).show();
//            return;
//        }

        try{
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mediaPlayer.setDataSource(file.getAbsolutePath());
           mediaPlayer.setDataSource(this, Uri.parse("rtmp://58.200.131.2:1935/livetv/hunantv"));
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
              thread=new Thread(){
                        @Override
                        public void run(){
                            try{
                                isplaying=true;
                                while(isplaying){
                                    int current=mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(current);
                                    sleep(500);
                                    Log.d(TAG, "run: 这是子线程里得run");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    };
              thread.start();
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
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(0);
            Toast.makeText(this,"重新播放",Toast.LENGTH_SHORT).show();
            //replay.setText("暂停");
        }
        isplaying=false;
        play(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        if (thread!=null){
        thread.interrupt();
        thread=null;}
        if (camera!=null){
//            camera.release();
        }
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
        if (requestCode==CAMERA_REQUEST_CODE){
            btn_sedm.setText("又他妈出错了");
            Log.d(TAG, "onActivityResult: 打开了录像功能");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }


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
    /**
     * 相机开发部分：
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(){
//         Camera camera = null;
//
//        try {
//            camera=Camera.open();
//            if (camera==null){Log.d(TAG, "getCamerainstance: 没得到相机");
//
//            }
//        }catch (Exception e){
//            camera=null;
//            Log.d(TAG, "getCamerainstance:看异常"+e.toString());
//            e.printStackTrace();
//        }
//        return camera;
        CameraManager cameraManager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission_group.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                return;
            }
            cameraManager.openCamera("1",stateCallback,null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }


    }

    //RequiresApi(api = Build.VERSION_CODES.O)
    private boolean prepareVideoRecorder(CameraDevice cameraDevice){
        camera=cameraDevice;
       mediaRecorder=new MediaRecorder();
        /**
         * 第1步：解锁摄像头并指响MediaRecorder
         */
        if(camera!=null){
//        mediaRecorder.setCamera();
        }
        /**
         * 第2步：指定音/视频源
         */
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        /**
         * 第3步：指定CamcorderProfile(相机配置文件)
         */
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        /**
         * 第4步：指定输出文件
         */
        mediaRecorder.setOutputFile(new RecordService().getsaveDirectory()+System.currentTimeMillis()+".mp4");
        /**
         * 第5步：指定预览输出
         */
        mediaRecorder.setPreviewDisplay(sv.getHolder().getSurface());
        /**
         * 第6步：根据以上配置准备MediaRecorder
         */
        try{
            mediaRecorder.prepare();
        }catch (IllegalStateException e){
            Log.d(TAG, "IllegealStateException preparing MediaRecorder: "+e.getMessage());
            releaseMediaRecorder();
            return false;
        }catch (IOException e){
            Log.d(TAG, "prepareVideoRecorder: "+e.getMessage());
        }
        return true;
    }
    private void releaseMediaRecorder(){

        if (mediaRecorder!= null) {

            mediaRecorder.reset(); // 清除recorder配置

            mediaRecorder.release(); // 释放recorder对象

            mediaRecorder= null;

//            camera.lock();           // 为后续使用锁定摄像头

        }

    }
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void videoRecorder(){
        if (isRecording){
            //Stop recorder and release the camera
            mediaRecorder.stop();
            releaseMediaRecorder();
            Toast.makeText(this,"录像已经停止",Toast.LENGTH_SHORT).show();
        }else {
            if (true){
                mediaRecorder.start();
                Toast.makeText(this,"录像已经开始",Toast.LENGTH_SHORT).show();
            }else {
                releaseMediaRecorder();
                Toast.makeText(this,"出错，录像已经停止！",Toast.LENGTH_SHORT).show();
            }
        }

    }

}

