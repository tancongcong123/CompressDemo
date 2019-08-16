package com.example.compressdemo;

import android.graphics.Bitmap;

public class BitmapTools {

    public static Bitmap getMonochrome(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width*height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                //分离三原色
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                //转化成灰度像素
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }
}
