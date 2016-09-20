package com.example.wechatemojilayout.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by 青松 on 2016/8/25.
 */
public class EmotionCategoryItem implements Parcelable{
    private String category;
    private List<Emotion> emotionList;

    public EmotionCategoryItem(){}

    protected EmotionCategoryItem(Parcel in) {
        category = in.readString();
        emotionList = in.createTypedArrayList(Emotion.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeTypedList(emotionList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmotionCategoryItem> CREATOR = new Creator<EmotionCategoryItem>() {
        @Override
        public EmotionCategoryItem createFromParcel(Parcel in) {
            return new EmotionCategoryItem(in);
        }

        @Override
        public EmotionCategoryItem[] newArray(int size) {
            return new EmotionCategoryItem[size];
        }
    };

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Emotion> getEmotionList() {
        return emotionList;
    }

    public void setEmotionList(List<Emotion> emotionList) {
        this.emotionList = emotionList;
    }
}
