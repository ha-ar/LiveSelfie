package com.algorepublic.liveselfie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class FilterActivity extends BaseActivity{


    GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        gifImageView =(GifImageView) findViewById(R.id.gifVeiw);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File outFile = new File(extStorageDirectory, "test.GIF");
        try {
            GifDrawable gifPath = new GifDrawable(outFile);
            gifImageView.setImageDrawable(gifPath);
            gifPath.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
