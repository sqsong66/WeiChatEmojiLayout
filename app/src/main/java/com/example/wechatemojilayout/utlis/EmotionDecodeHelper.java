package com.example.wechatemojilayout.utlis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by 青松 on 2016/9/20.
 */
public class EmotionDecodeHelper {

    private Context context;

    public EmotionDecodeHelper(Context context) {
        this.context = context;
    }

    /**
     * 从asset目录decode出所需emoji表情
     * @param emojiPicName emoji表情名称
     * @param zoomSize 缩放大小
     * @return emoji表情图片的bitmap对象
     */
    public Bitmap getEmotonAssetBitmap(String emojiPicName, int zoomSize) {
        Bitmap resultBitmap = null;
        try {
            //先取缓存中的bitmap对象，如果缓存没有，则从asset中加载，并存放到缓存中
            Bitmap cacheBitmap = ImageBitmapCache.getInstance().getBitmapFromMemCache(emojiPicName);
            if (cacheBitmap == null) {
                InputStream inputStream = context.getAssets().open(emojiPicName);
                cacheBitmap = BitmapFactory.decodeStream(inputStream);
                ImageBitmapCache.getInstance().addBitmapToMemCache(emojiPicName, cacheBitmap);
                Log.e("sqsong", "Get bitmap from assets folder!");
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

    /**
     * 对bitmap对象进行缩放
     * @param source 源bitmap对象
     * @param size  缩放比例
     * @return 缩放后的新bitmap对象
     */
    public Bitmap zoomBitmap(Bitmap source, int size) {
        Matrix matrix = new Matrix();
        float scale = (float)size * 1.0F / (float)source.getWidth();
        matrix.setScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return result;
    }
}
