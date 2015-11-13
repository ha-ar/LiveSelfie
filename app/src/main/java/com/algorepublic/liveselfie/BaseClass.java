package com.algorepublic.liveselfie;

import android.app.Application;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by hasanali on 25/10/2015.
 */
public class BaseClass extends Application {

    private File bytes;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public void setBytes(File bytes){
        this.bytes=bytes;
    }

    public File getBytes(){
        return bytes;
    }

}
