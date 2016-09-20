package com.example.wechatemojilayout.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 青松 on 2016/8/25.
 */
public class Emotion implements Parcelable {

    private String category;
    private String description;
    private String name;
    private String value;

    public Emotion(){}

    protected Emotion(Parcel in) {
        category = in.readString();
        description = in.readString();
        name = in.readString();
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(description);
        dest.writeString(name);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Emotion> CREATOR = new Creator<Emotion>() {
        @Override
        public Emotion createFromParcel(Parcel in) {
            return new Emotion(in);
        }

        @Override
        public Emotion[] newArray(int size) {
            return new Emotion[size];
        }
    };

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
