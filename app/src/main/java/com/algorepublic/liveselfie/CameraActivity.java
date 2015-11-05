package com.algorepublic.liveselfie;

        import android.content.res.Configuration;
        import android.hardware.Camera;
        import android.media.MediaRecorder;
        import android.os.Bundle;
        import android.os.Environment;
        import android.util.Log;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.Toast;

        import java.io.File;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.Date;

/**
 * Created by ahmad on 11/3/15.
 */
public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback , MediaRecorder.OnInfoListener {
    private final String VIDEO_PATH_NAME = "/mnt/sdcard/VGA_30fps_512vbrate.mp4";

    View view;
    Camera camera;
    private boolean frontCamera;
    MediaRecorder mediaRecorder;
    SurfaceHolder surfaceHolder;
    boolean mInitSuccesful;
    SurfaceView surfaceView;
    ImageView btn;
    private boolean cameraFront=false;
    Camera.Parameters parameters;
    File OutputFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        btn = (ImageView) findViewById(R.id.camera_button);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.start();
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            if (!mInitSuccesful)
                initRecorder(surfaceHolder.getSurface());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        shutdown();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getApplicationContext(), "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(getApplicationContext(), "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        OutputFile = new File(Environment.getExternalStorageDirectory().getPath());
        String video= "/DCIM/100MEDIA/Video";
        if(camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            camera.setDisplayOrientation(90);
            parameters = camera.getParameters();
//            surfaceView.getLayoutParams().width = parameters.getPreviewSize().width;
//            surfaceView.getLayoutParams().height = parameters.getPreviewSize().height;
            parameters.setPreviewSize(parameters.getPreviewSize().width,parameters.getPreviewSize().height);
            camera.setParameters(parameters);
            camera.unlock();
            cameraFront = true;
        }

        if(mediaRecorder == null)  mediaRecorder = new MediaRecorder();
        mediaRecorder.setPreviewDisplay(surface);
        mediaRecorder.setCamera(camera);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //       mMediaRecorder.setOutputFormat(8);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setMaxDuration(5000);
        mediaRecorder.setOnInfoListener(this);
        mediaRecorder.setOutputFile(VIDEO_PATH_NAME);
        mediaRecorder.setOrientationHint(270);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        camera.stopPreview();
        mediaRecorder.reset();
        mediaRecorder.release();
        camera.release();

        // once the objects have been released they can't be reused
        mediaRecorder = null;
        camera = null;
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
        if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
            Log.e("recording Finished","yes");
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
//            try {
//                initRecorder(surfaceHolder.getSurface());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
