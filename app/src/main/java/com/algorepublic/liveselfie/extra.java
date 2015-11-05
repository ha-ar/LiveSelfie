package com.algorepublic.liveselfie;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("ALL")
public class Extra extends BaseActivity implements SurfaceHolder.Callback{


    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private boolean recording = false;
    private Camera camera;
    private Camera.Parameters parameters;
    private SurfaceView cameraView;
    private final String VIDEO_PATH_NAME = "/mnt/sdcard/VGA_30fps_512vbrate.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        recorder = new MediaRecorder();
        cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ImageView cameraButton = (ImageView) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    recorder.stop();
                    recording = false;

                    // Let's initRecorder so we can record again
                    initRecorder();
                    prepareRecorder();
                } else {
                    recording = true;
                    recorder.start();
                }
            }
        });
    }

    private void initRecorder( ) {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        File file = new File(path, "/" + "video.mp4");
        String video= "video.mp4";
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//        recorder.setProfile(cpHigh);
        if (camera == null){
            try {
                camera = Camera.open(getFrontCameraId());
                camera.setPreviewDisplay(holder);
                parameters = camera.getParameters();
                cameraView.getLayoutParams().width = parameters.getPreviewSize().width;
                cameraView.getLayoutParams().height = parameters.getPreviewSize().height;
                camera.startPreview();
                camera.setDisplayOrientation(90);
                camera.unlock();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (recorder == null) {
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setCamera(camera);
            recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoEncodingBitRate(512 * 1000);
            Log.e("file path",file.getAbsolutePath());
            recorder.setOutputFile(file.getAbsolutePath());
            recorder.setMaxDuration(5000); // 5 seconds
            recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
            recorder.setOnInfoListener(new StopRecordingListener());
            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                finish();
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }
    }


    private void prepareRecorder() {
//        recorder.setPreviewDisplay(surfaceHolder.getSurface());
//        try {
//            recorder.prepare();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//            finish();
//        } catch (IOException e) {
//            e.printStackTrace();
//            finish();
//        }
    }

    class StopRecordingListener implements MediaRecorder.OnInfoListener{

        @Override
        public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                mediaRecorder.stop();
            }
        }
    }

    class CameraClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if (recording) {
                recorder.stop();
                recording = false;

                // Let's initRecorder so we can record again
                initRecorder();
                prepareRecorder();
            } else {
                recording = true;
                recorder.start();
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initRecorder();
        prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();

    }

    private int getFrontCameraId() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    return camIdx;
                } catch (RuntimeException e) {
                    Log.e("Camera", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return 0;
    }

}
