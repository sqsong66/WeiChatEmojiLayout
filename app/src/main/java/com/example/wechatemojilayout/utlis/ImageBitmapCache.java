package com.example.wechatemojilayout.utlis;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by 青松 on 2016/8/22.
 */
public class ImageBitmapCache {

    private static ImageBitmapCache bitmapCache;
    private LruCache<String, Bitmap> mMemoryCache;

    public ImageBitmapCache() {
        init();
    }

    private void init() {
        int cacheSize = ((int) (Runtime.getRuntime().maxMemory() / 1024 / 1024)) / 8; //MB
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public static ImageBitmapCache getInstance() {
        if (bitmapCache == null) {
            synchronized (ImageBitmapCache.class) {
                if (bitmapCache == null) {
                    bitmapCache = new ImageBitmapCache();
                }
            }
        }
        return bitmapCache;
    }

    public void addBitmapToMemCache(String key, Bitmap bitmap) {
        if(key != null && bitmap != null) {
            if(this.mMemoryCache != null) {
                this.mMemoryCache.put(key, bitmap);
            }

        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if(this.mMemoryCache != null) {
            Bitmap memBitmap = this.mMemoryCache.get(key);
            if(memBitmap != null) {
                return memBitmap;
            }
        }
        return null;
    }

    public void clearMemCache() {
        if(this.mMemoryCache != null) {
            this.mMemoryCache.evictAll();
        }
    }
}
