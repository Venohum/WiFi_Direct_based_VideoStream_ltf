package com.example.dell.wi_fi_direct_based_videostream_ltf.Camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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

import com.example.dell.wi_fi_direct_based_videostream_ltf.R;
import com.example.dell.wi_fi_direct_based_videostream_ltf.recorder.RecordService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机开发：
 */
public class CameraActivity extends AppCompatActivity {
    private Camera camera;
    private static final String TAG=CameraActivity.class.getSimpleName();
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private SurfaceView surfaceView;
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;
    private SurfaceHolder mSurfaceHolder;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView=(SurfaceView)findViewById(R.id.sfvSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCameraThread();
        mSurfaceHolder=surfaceView.getHolder();
        surfaceView.setZOrderMediaOverlay(true);//把控件放在窗口最顶层
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
        Log.d(TAG, "onResume: onResume 执行了");
    }
    private void initCameraThread(){
        mCameraThread=new HandlerThread("CameraSurfaceViewThread");
        mCameraThread.start();
        mCameraHandler=new Handler(mCameraThread.getLooper());
    }
    private SurfaceHolder.Callback mSurfaceHolderCallback=new SurfaceHolder.Callback() {
       @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setupCamera(holder.getSurfaceFrame().width(),holder.getSurfaceFrame().height());
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera(int width,int height){
        CameraManager cameraManager=(CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics=cameraManager.getCameraCharacteristics(cameraId);
                Integer facing =cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                //此处默认打开后置摄像头
                if (facing!=null&&facing==CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap map=cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map!=null;
                mPreviewSize=getOptimalSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                mCameraId=cameraId;

            }
        }catch (CameraAccessException e){
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
    private void openCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        try {
            Log.d(TAG,"mCameraId === " + mCameraId);
            if (cameraManager!=null)
            cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api=Build.VERSION_CODES.LOLLIPOP)
    private CameraDevice.StateCallback mCameraDeviceStateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice=camera;
            /**
             * 开始预览
             */
            startPreView();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice!=null){
                mCameraDevice.close();
                camera.close();
                mCameraDevice=null;
            }

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (mCameraDevice!=null){
                mCameraDevice.close();
                camera.close();
                mCameraDevice=null;
            }

        }
    };

    /**
     * 开始预览函数
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private void startPreView(){
        try {
            Surface surface = mSurfaceHolder.getSurface();

            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if (surface!=null){
                Log.d(TAG,"SURFACE不为空");
                mCaptureRequestBuilder.addTarget(surface);
            }else {
                Log.d(TAG,"SURFACE为空");
            }

            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureRequest = mCaptureRequestBuilder.build();
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest,null,mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause(){
        super.onPause();
        if (mCameraCaptureSession!=null){
            mCameraCaptureSession.close();
            mCameraCaptureSession=null;
        }
        if (mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice=null;
        }

    }
}
