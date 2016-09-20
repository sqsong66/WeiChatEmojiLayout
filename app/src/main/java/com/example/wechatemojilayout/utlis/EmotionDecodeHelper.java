package com.example.wechatemojilayout.utlis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.InputStream;

/**
 * Created by 青松 on 2016/9/20.
 */
public class EmotionDecodeHelper {

    private Context context;

    public EmotionDecodeHelper(Context context) {
        this.context = context;
    }

    public Bitmap getEmotonAssetBitmap(String emojiPicName, int zoomSize) {
        Bitmap resultBitmap = null;
        try {
            Bitmap cacheBitmap = ImageBitmapCache.getInstance().getBitmapFromMemCache(emojiPicName);
            if (cacheBitmap == null) {
                InputStream inputStream = context.getAssets().open(emojiPicName);
                cacheBitmap = BitmapFactory.decodeStream(inputStream);
                ImageBitmapCache.getInstance().addBitmapToMemCache(emojiPicName, cacheBitmap);
            }
            if (zoomSize > 0) {
                resultBitmap = zoomBitmap(cacheBitmap, DensityUtils.dip2px(zoomSize));
            } else {
                resultBitmap = cacheBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultBitmap;
    }

    public Bitmap zoomBitmap(Bitmap source, int width) {
        Matrix matrix = new Matrix();
        float scale = (float)width * 1.0F / (float)source.getWidth();
        matrix.setScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return result;
    }
}
