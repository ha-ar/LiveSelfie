package com.algorepublic.liveselfie;

        import android.app.Dialog;
        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.content.res.Configuration;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.ColorMatrix;
        import android.graphics.ColorMatrixColorFilter;
        import android.graphics.Paint;
        import android.hardware.Camera;
        import android.media.MediaMetadataRetriever;
        import android.media.MediaRecorder;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.util.Log;
        import android.view.Display;
        import android.view.Surface;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.View;
        import android.view.Window;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.SeekBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.bumptech.glide.Glide;

        import java.io.BufferedOutputStream;
        import java.io.ByteArrayOutputStream;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
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
    ImageView btn ,flash;
    private boolean cameraFront=false;
    Camera.Parameters parameters;
    File finalPath;
//    SeekBar timeFrameBar;
    long maxDur;
    ProgressDialog progressDialog;
    MediaMetadataRetriever mediaMetadataRetriever = null;
    BaseClass baseClass;
    ImageView imageView;
    byte[] bytes ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        btn = (ImageView) findViewById(R.id.camera_button);
        flash = (ImageView) findViewById(R.id.flash);
//        timeFrameBar = (SeekBar) findViewById(R.id.timeframe);
        imageView = (ImageView) findViewById(R.id.gallery);
        baseClass = (BaseClass) getApplicationContext();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mediaMetadataRetriever = new MediaMetadataRetriever();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.start();
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CameraActivity.this,FilterActivity.class));
            }
        });

//        timeFrameBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {}
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                updateFrame();
//            }});
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
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        File file = new File(path, "/" + "video.mp4");
        String video= "video.mp4";
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
//        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setMaxDuration(4000);
        mediaRecorder.setOnInfoListener(this);
        finalPath = file;
        mediaRecorder.setOutputFile(file.getAbsolutePath());
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
            Log.e("recording Finished", "yes");
            mediaRecorder.stop();
        //    mediaRecorder.reset();
           // mediaRecorder.release();
//            progressDialog.show();
            Uri uri = Uri.fromFile(finalPath);
            Log.e("uri",uri.toString());
            Intent intent = new Intent(CameraActivity.this,FilterActivity.class);
            intent.putExtra("VideoUri",uri.toString());
            startActivity(intent);
            this.finish();

//            MediaMetadataRetriever tRetriever = new MediaMetadataRetriever();
//            try{
//                tRetriever.setDataSource(getBaseContext(), uri);
//
//                mediaMetadataRetriever = tRetriever;
//                //extract duration in millisecond, as String
//                String DURATION = mediaMetadataRetriever.extractMetadata(
//                        MediaMetadataRetriever.METADATA_KEY_DURATION);
//                Log.e("Duration: ",DURATION + " ms");
//                //convert to us, as int
//                maxDur = (long)(1000*Double.parseDouble(DURATION));
//
////                timeFrameBar.setProgress(0);
//                updateFrame();
//            }catch(RuntimeException e){
//                e.printStackTrace();
//                Toast.makeText(CameraActivity.this,
//                        "Something Wrong!",
//                        Toast.LENGTH_LONG).show();
//            }
//            TaskSaveGIF myTaskSaveGIF = new TaskSaveGIF();
//            myTaskSaveGIF.execute();



//            try {
//                initRecorder(surfaceHolder.getSurface());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void showDailog() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_popup);
        dialog.show();
    }

    private void updateFrame(){
//        int frameProgress = timeFrameBar.getProgress();

//        long frameTime = maxDur * frameProgress/100;

//        textCurDur.setText(String.valueOf(frameTime) + " us");
//        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime);
//        capturedImageView.setImageBitmap(bmFrame);
    }

    public class TaskSaveGIF extends AsyncTask<Void, Integer, String> {

//        SeekBar bar;

        public TaskSaveGIF(){
//            bar = sb;
//            Toast.makeText(CameraActivity.this,
//                    "Generate GIF animation",
//                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... params) {
//            File path = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_MOVIES);
//            File file = new File(path, "/" + "video.mp4");
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File outFile = new File(extStorageDirectory, "test.GIF");
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
                genGIF();
//                bos.write(genGIF());
//                bos.flush();
//                bos.close();

                return(outFile.getAbsolutePath() + " Saved");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Glide.with(CameraActivity.this).fromBytes().load(bytes).into(imageView);
            Toast.makeText(CameraActivity.this,
                    result,
                    Toast.LENGTH_LONG).show();
            progressDialog.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            bar.setProgress(values[0]);
            updateFrame();
        }

        private byte[] genGIF(){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
            animatedGifEncoder.setDelay(100);
            Log.e("before", "For loop");
            Bitmap bmFrame;
            ArrayList<Bitmap> array = new ArrayList<>();
            animatedGifEncoder.start(bos);
            int k = 0;
            for(int i=0; i<100; i+=5){
                long frameTime = maxDur * i/100;
                bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime);
                array.add(bmFrame);
//                animatedGifEncoder.addFrame(bmFrame);
                publishProgress(i);
                k++;
            }
            Log.e("after","for loop");
//            float[] colorTransform = {
//                    0.33f, 0.33f, 0.33f, 0, 0,
//                    0.50f, 0.59f, 0.59f, 0, 0,
//                    0.11f, 0.11f, 0.11f, 0, 0};
//
//            ColorMatrix colorMatrix = new ColorMatrix();
//            colorMatrix.setSaturation(0f); //Remove Colour
//            colorMatrix.set(colorTransform); //Apply the Red
//
//            ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
//            Paint paint = new Paint();
//            paint.setColorFilter(colorFilter);
//            Display display = getWindowManager().getDefaultDisplay();
//            Bitmap resultBitmap;
//            for (int l=0;l<=array.size();l++) {
//                resultBitmap = Bitmap.createBitmap(array.get(l), 0, (int) (display.getHeight() * 0.15), display.getWidth(), (int) (display.getHeight() * 0.75));
//
//                Canvas canvas = new Canvas(resultBitmap);
//                canvas.drawBitmap(resultBitmap, 0, 0, paint);
//                animatedGifEncoder.addFrame(resultBitmap);
//                publishProgress(l);
//            }


            bmFrame = mediaMetadataRetriever.getFrameAtTime(maxDur);
//            animatedGifEncoder.addFrame(bmFrame);
            publishProgress(100);
            animatedGifEncoder.finish();

//            byte[] a = new byte[bos.toByteArray().length];
//            a = bos.toByteArray();
//            Byte[] temp = new Byte[bos.toByteArray().length];
//            for (int i = 0; i < a.length; i++) {
//                temp[i] = a[i];
//            }
            bytes = new byte[bos.toByteArray().length];
            bytes = bos.toByteArray();
            return bos.toByteArray();
        }
    }
}
