package com.algorepublic.liveselfie;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.algorepublic.liveselfie.filter.IFRiseFilter;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class FilterActivity extends BaseActivity{


    ImageView gifImageView;
    long maxDur;
    ProgressDialog progressDialog;
    MediaMetadataRetriever mediaMetadataRetriever = null;
    BaseClass baseClass;
    AnimatedGifEncoder animatedGifEncoder;
    Uri videoUri;
    byte[] bytes ;
    private Bitmap[] bitmaps;
    int k =0;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        gifImageView =(ImageView) findViewById(R.id.gifVeiw);
        mediaMetadataRetriever = new MediaMetadataRetriever();
        baseClass = (BaseClass) getApplicationContext();
        bitmaps = new Bitmap[21];
//        File path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MOVIES);
//        File file = new File(path, "/" + "video.mp4");
//        Uri uri = Uri.fromFile(file);
        Bundle extra = getIntent().getExtras();
        videoUri = Uri.parse(extra.getString("VideoUri"));
        Log.e("URI",videoUri.toString());
        MediaMetadataRetriever tRetriever = new MediaMetadataRetriever();
        try{
            tRetriever.setDataSource(getBaseContext(), videoUri);

            mediaMetadataRetriever = tRetriever;
            //extract duration in millisecond, as String
            String DURATION = mediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION);
            Log.e("Duration: ", DURATION + " ms");
            //convert to us, as int
            maxDur = (long)(1000*Double.parseDouble(DURATION));

//                timeFrameBar.setProgress(0);
        }catch(RuntimeException e){
            e.printStackTrace();
            Toast.makeText(FilterActivity.this,
                    "Something Wrong!",
                    Toast.LENGTH_LONG).show();
        }
        genGIF();

        final AnimationDrawable animation = new AnimationDrawable();
        animation.setOneShot(false);
        for (int j=0; j<bitmaps.length;j++){
            animation.addFrame(new BitmapDrawable(getResources(),bitmaps[j]),100);
        }
         if (Build.VERSION.SDK_INT < 16){
             gifImageView.setBackgroundDrawable(animation);
         }else {
             gifImageView.setBackground(animation);
         }
        gifImageView.post(new Runnable() {
            @Override
            public void run() {
                Log.e("test","Yes");
                animation.start();
            }
        });
    }

    private void genGIF() {
        Sepia sepia = new Sepia();
        Bitmap bmFrame;
        GPUImage gpuImage = new GPUImage(FilterActivity.this);
        int k =0;
        for (int i = 0; i < 100; i += 5) {
            long frameTime = maxDur * i / 100;
            gpuImage.setFilter(new GPUImageSepiaFilter());
            bmFrame = mediaMetadataRetriever.getFrameAtTime(frameTime);
            gpuImage.setImage(bmFrame);
                    //sepia.setSepiaColorFilter(mediaMetadataRetriever.getFrameAtTime(frameTime));
            bitmaps[k] =bmFrame;
            k++;
        }
        //last from at end
        gpuImage.setFilter(new GPUImageGammaFilter());
        bmFrame = mediaMetadataRetriever.getFrameAtTime(maxDur);
        gpuImage.setImage(bmFrame);
        bitmaps[k]=bmFrame;
        k=0;
        Log.e("bitmap size", bitmaps.length + " size");
    }

}
