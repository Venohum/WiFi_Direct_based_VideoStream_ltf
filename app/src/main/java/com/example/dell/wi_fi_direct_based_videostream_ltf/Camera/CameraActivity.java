package com.example.dell.wi_fi_direct_based_videostream_ltf.Camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.dell.wi_fi_direct_based_videostream_ltf.Algorithmic.ComputeBandwidth;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.AsyncEncoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Synchronization.Decoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.Synchronization.Encoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Coder.VideoDecoder;
import com.example.dell.wi_fi_direct_based_videostream_ltf.Multicast.MulticastServer;
import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.UDP.EchoServer;
import com.example.dell.wi_fi_direct_based_videostream_ltf.wifi_direct.DeviceDetailFragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机开发：
 */
public class CameraActivity extends AppCompatActivity {
    public static final String TAG = CameraActivity.class.getSimpleName();
    private CaptureRequest.Builder mCaptureRequestBuilder,mCaptureRequestBuilder2;
    private SurfaceView surfaceView,surfaceView2;
    private HandlerThread mCameraThread;
    private HandlerThread mCameraThread2;
    private Handler mCameraHandler,mCameraHandler2;
    private SurfaceHolder mSurfaceHolder,mSurfaceHolder2;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CaptureRequest mCaptureRequest,mCaptureRequest2;
    private CameraCaptureSession mCameraCaptureSession,mCameraCaptureSession2;
    private ImageReader mImageReader;
    private Button pill;
    private int framerate = 24;//每秒帧率
    private int bitrate = 1900000;//编码比特率，
    private final String MIME_TYPE = "video/avc";
//    private byte[] h264=new byte[width*height*3];
    private byte[][] data=new byte[3][];
    private byte[] buf={0,2,3};
    private Encoder encoder;
    private Decoder mDecoder;
    private AsyncEncoder asyncEncoder;
    private MulticastServer multicastServer;

    private EchoServer server;
    //public static File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/6.h264");

    public CameraActivity() throws IOException {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.sfvSurfaceView);
        surfaceView2=(SurfaceView)findViewById(R.id.sfvSurfaceView2);
        pill=(Button)findViewById(R.id.pipi);
        pill.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 是组主么"+DeviceDetailFragment.info.isGroupOwner);
//                if (DeviceDetailFragment.info.isGroupOwner)
               try{

                    server=new EchoServer();
                    new Thread(server).start();
                    multicastServer=new MulticastServer();
                    new Thread(multicastServer).start();
                    Log.d(TAG, "onClick: 这是UDP 服务端！");


                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           if (mSurfaceHolder2.getSurface()!=null){
                               Log.d(TAG, "run: surface2"+mSurfaceHolder2.getSurface().toString());
                           startdecode(MIME_TYPE,mSurfaceHolder2.getSurface(),16,32,server,multicastServer);
                           Log.d(TAG, "run: 解码开始！");}
                           else {
                               Log.d(TAG, "run: surface2为空无法解码！");
                           }
                       }
                   }).start();

                }catch (Exception e){
                    e.printStackTrace();
            }
            }
        });

        Log.d(TAG, "onCreate: onCreat执行了！");


    }
    @Override
    protected void onResume() {
        super.onResume();
        initCameraThread();
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder2=surfaceView2.getHolder();
        surfaceView.setZOrderMediaOverlay(true);//把控件放在窗口最顶层
        surfaceView2.setZOrderMediaOverlay(true);
        mSurfaceHolder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
        mSurfaceHolder2.addCallback(mSurfaceHolderCallback1);

        Log.d(TAG, "onResume: onResume 执行了");
        /*
         * 计算实时网速
         * */
        new Thread(new ComputeBandwidth()).start();
    }


    private void initCameraThread() {
        mCameraThread = new HandlerThread("CameraSurfaceViewThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }
    private SurfaceHolder.Callback mSurfaceHolderCallback1=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };
    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setupCamera(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());

            openCamera();
            Log.d(TAG, "surfaceCreated: openCamera 已经执行！");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (cameraManager != null)
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    //此处默认打开后置摄像头
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                        continue;
                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    assert map != null;
                    mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    Log.d(TAG, "setupCamera尺寸尺寸！！！宽是"+mPreviewSize.getWidth()+"高是"+mPreviewSize.getHeight());
                    mCameraId = cameraId;

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
    }

    //选择sizeMap中大于并且最接近width和height的size
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            Log.d(TAG, "getOptimalSize: "+sizeList.toString());
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "openCamera: 没有授权相机！");
            return;
        }
        try {
            Log.d(TAG, "mCameraId === " + mCameraId);
            if (cameraManager != null)
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    /**
     *该类是CameraDevice的内部类，其中定义了
     * onOpened， onDisconnected，onError三个方法，
     * 这三个方法需要用户来实现。系统会根据打开 Camera 设备的状态结果，回调三个不同的方法。
     *
     */
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            /**
             * 初始化编码器——同步方式
             */
            //initEncoder();
            /**
             * 初始化编码器_异步方式
             */
            initAsyncEncoder("video/avc",1920,1080);//调节分辨率
            /**
             * 开始预览
             */
            startPreView();
            //encoder.surface=surfaceView.getHolder().getSurface();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                camera.close();
                mCameraDevice = null;
            }

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                camera.close();
                mCameraDevice = null;
            }

        }
    };

    /**
     * 开始预览函数
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void startPreView() {
        try {
//            mImageReader = ImageReader.newInstance(surfaceView.getWidth(), surfaceView.getHeight(), ImageFormat.YUV_420_888, 2);
//            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mCameraHandler);
            Surface surface = mSurfaceHolder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);//视频帧率有区别
            if (surface != null) {
                Log.d(TAG, "SURFACE不为空");

                mCaptureRequestBuilder.addTarget(surface);//用于显示在SurfaceView中预览
//                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());//用于ImageReader获取数据
                mCaptureRequestBuilder.addTarget(asyncEncoder.mSurface);

            } else {
                Log.d(TAG, "SURFACE为空");
            }
            /**
             *注意，以下参数列表中有两个surface，其中分别为SurfaceView的
             * 以及用来获取byte[]的ImageReader的surface
             *
             *mImageReader.getSurface()
             */
            mCameraDevice.createCaptureSession(Arrays.asList(surface,asyncEncoder.mSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureRequest = mCaptureRequestBuilder.build();
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,  @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
//                                try {
//                                    startEncode();
//
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }

                            }
                        }, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        /**
         *  当有一张图片可用时会回调此方法，但有一点一定要注意：
         *  一定要调用 reader.acquireNextImage()和close()方法，否则画面就会卡住！！！！！我被这个坑坑了好久！！！
         *    很多人可能写Demo就在这里打一个Log，结果卡住了，或者方法不能一直被回调。
         **/
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireNextImage();
            /**
             *  因为Camera2并没有Camera1的Priview回调！！！所以该怎么能到预览图像的byte[]呢？就是在这里了！！！我找了好久的办法！！！
             **/
            //Log.d(TAG, "onImageAvailable: 所谓的像素平面数组的长度"+img.getPlanes().length);


            ByteBuffer []buffer=new ByteBuffer[img.getPlanes().length];
            for (int i=0;i<img.getPlanes().length;i++){
            buffer[i]= img.getPlanes()[i].getBuffer();
            data[i] = new byte[buffer[i].remaining()];
            buffer[i].get(data[i]);}
//            Util.save(data[0], 0, data[0].length, file, true);
//            Util.save(data[1], 0, data[1].length, file, true);
//            Util.save(data[2], 0, data[2].length, file, true);
            img.close();
            //Log.d(TAG, "onImageAvailable: YUV_420_888" + Arrays.toString(data));


        }
    };

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

    }
    /**
     * Mediacodec编码部分
     */
    //MediaCodec codec = MediaCodec.createByCodecName("Test");
    private void startEncode() throws IOException {
//        int in=encoder.input(data[0],data[0].length,System.nanoTime()/1000);
//        Log.d(TAG, "startEncode: index"+in);
//        Log.d(TAG, "startEncode: 编码前，像素平面一"+Arrays.toString(data[0]));
//        Log.d(TAG, "startEncode: 编码前，像素平面二"+Arrays.toString(data[1]));
//        Log.d(TAG, "startEncode: 编码前，像素平面三"+Arrays.toString(data[2]));

         encoder.output();
//        if (nb==0)
//        Log.d(TAG, "startEncode:编码可以输出了");
//        Log.d(TAG, "startEncode: "+Arrays.toString(encoder.outputBuffers));

    }

    private void startDncode()throws Exception{
//        int result_input=-2;
        FileInputStream inputStream=new FileInputStream(Encoder.file);
        byte [] inputbyte=new byte[10*1024];

        if(inputStream.read(inputbyte)!=-1){
            Log.d(TAG, "startDncode长度: "+inputbyte.length);
            Log.d(TAG, "startDncode: 内容！"+Arrays.toString(inputbyte));
            mDecoder.input(inputbyte,inputbyte.length,System.nanoTime()/1000);}
            mDecoder.flush();
//            Log.d(TAG, "startDncode: 输入失败！");

        inputStream.close();
    }
    private void startshow()throws Exception{
        EchoServer echoServer=new EchoServer();

        mDecoder.input(echoServer.packet.getData(),echoServer.packet.getData().length,System.nanoTime()/1000);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startdecode(String mimeType, Surface surface, int viewwidth, int viewheight, EchoServer echoServer,MulticastServer multicastServer){
        VideoDecoder videoDecoder=new VideoDecoder(mimeType,surface,viewwidth,viewheight);
//        Log.d(TAG, "startdecode: sps"+Arrays.toString(encoder.mSps));
//        Log.d(TAG, "startdecode: pps"+Arrays.toString(encoder.mPps));
        videoDecoder.setechoServer(echoServer,multicastServer);
        videoDecoder.startDecoder();
    }
    /**
     * 编码器初始化_同步方式
     */

    private void initEncoder(){

        encoder=new Encoder();
        try {
            encoder.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
        encoder.configure(surfaceView.getWidth(),surfaceView.getHeight(),bitrate,framerate);
        encoder.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "initEncoder: 编码器初始化完成！");
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initAsyncEncoder(String MIME_TYPE, int with, int hight){
        asyncEncoder=new AsyncEncoder(MIME_TYPE,with,hight);
        asyncEncoder.startEncoder();

    }

    /**
     * 解码器初始化
     */
    private void initDncoder(){
        mDecoder=new Decoder();
        try{
            mDecoder.init();
        }catch (IOException e){
            e.printStackTrace();
        }
        try{
            Log.d(TAG, "initDncoder: mSps"+Arrays.toString(encoder.mSps));
            Log.d(TAG, "initDncoder: Pps"+Arrays.toString(encoder.mPps));
            mDecoder.configure(encoder.mSps,encoder.mPps,mSurfaceHolder2.getSurface());
            mDecoder.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "initDncoder: 解码器初始化完成！");
    }


}
