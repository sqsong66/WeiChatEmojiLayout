package com.example.wechatemojilayout.utlis;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.model.EmotionCategoryItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 青松 on 2016/8/22.
 */
public class EmotionAssetDbHelper {

    public static final String DB_EMOTION = "emotion.db";
    public static final String TABLE_NAME_EMOTION = "emotion";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";

    private Context context;

    public EmotionAssetDbHelper(Context context) {
        this.context = context;
    }

    public SQLiteDatabase openDatabase(String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        if (!dbFile.exists()) {
            try {
                SQLiteDatabase checkDB = context.openOrCreateDatabase(dbName, context.MODE_PRIVATE, null);
                if(checkDB != null) {
                    checkDB.close();
                }
                copyDatabase(dbFile, dbName);
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    private void copyDatabase(File dbFile, String dbName) throws IOException {
        InputStream is = context.getAssets().open(dbName);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }
        os.flush();
        os.close();
        is.close();
    }

    public synchronized List<EmotionCategoryItem> queryEmotionList(String dbName) {
        long startTime = System.currentTimeMillis();
        List<EmotionCategoryItem> itemList = new ArrayList<>();
        SQLiteDatabase database = this.openDatabase(dbName);
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE_NAME_EMOTION, null, null, null, null, null, null);
            Map<String, List<Emotion>> map = new LinkedHashMap<>();
            List<String> descriptions = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));

                Emotion emotion = new Emotion();
                emotion.setCategory(category);
                emotion.setDescription(description);
                emotion.setName(name);
                emotion.setValue(value);

                List<Emotion> emotions = map.get(description);
                if (emotions == null) {
                    emotions = new ArrayList<>();
                    emotions.add(emotion);
                    descriptions.add(description);
                    map.put(description, emotions);
                } else {
                    emotions.add(emotion);
                }
            }

            for (String description : descriptions) {
                EmotionCategoryItem emotionCategoryItem = new EmotionCategoryItem();
                emotionCategoryItem.setCategory(description);
                emotionCategoryItem.setEmotionList(map.get(description));
                itemList.add(emotionCategoryItem);
            }
            Log.i("sqsong", "Query Emotion Items Cost Time: " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }
        return itemList;
    }

    public synchronized String queryEmotionValue(String dbName, String name) {
        String value = name;
        SQLiteDatabase database = this.openDatabase(dbName);
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("select * from emotion where name = ?", new String[]{name});
            if (cursor != null && cursor.moveToNext()) {
                value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.close();
        }
        return value;
    }

}
