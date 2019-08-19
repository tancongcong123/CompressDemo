package com.example.compressdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.compressor.Compressor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class CompressUtils implements Handler.Callback{

    private Handler mHandler;
    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;
    private OnCompressorListener mCompressListener;
    private HashSet<String> hashSet;
    private String targetDir;

    private CompressUtils(Builder builder) {
        this.mCompressListener = builder.mCompressListener;
        this.hashSet = builder.hashSet;
        this.targetDir = builder.targetDir;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static class Builder {
        private Context context;
        private String targetDir;
        private HashSet<String> hashSet;
        private OnCompressorListener mCompressListener;

        public Builder(Context context) {
            this.context = context;
            hashSet = new HashSet<>();
        }

        public Builder setCompressListener(OnCompressorListener listener) {
            this.mCompressListener = listener;
            return this;
        }

        public Builder setTargetDir(String path) {
            this.targetDir = path;
            return this;
        }

        public Builder load(String path) {
            this.hashSet.add(path);
            return this;
        }

        public void launch() {
            build().doCompress(context);
        }

        private CompressUtils build(){
            return new CompressUtils(this);
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }



    private void doCompress(Context context){
        while (hashSet.size()>0) {
            final String path = hashSet.iterator().next();
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));

                        File result = new Compressor(context)
                                .setMaxWidth(1200)
                                .setMaxHeight(1200)
                                .setQuality(40)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setDestinationDirectoryPath(targetDir)
                                .compressToFile(new File(path));

                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
                    } catch (IOException e) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                    }
                }
            });
            hashSet.remove(path);
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        if (mCompressListener == null) return false;

        switch (message.what) {
            case MSG_COMPRESS_START:
                mCompressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                mCompressListener.onSuccess((File) message.obj);
                break;
            case MSG_COMPRESS_ERROR:
                mCompressListener.onError((Throwable) message.obj);
                break;
        }
        return false;
    }
}
