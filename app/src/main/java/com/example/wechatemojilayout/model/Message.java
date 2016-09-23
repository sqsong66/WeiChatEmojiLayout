package com.example.wechatemojilayout.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;

/**
 * Created by 青松 on 2016/9/23.
 */
public class Message implements Parcelable {

    private String message;
    private String avatar;
    private SpannableString spannableMessage;

    public Message(String message, String avatar) {
        this.message = message;
        this.avatar = avatar;
    }

    protected Message(Parcel in) {
        message = in.readString();
        avatar = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(avatar);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public SpannableString getSpannableMessage() {
        return spannableMessage;
    }

    public void setSpannableMessage(SpannableString spannableMessage) {
        this.spannableMessage = spannableMessage;
    }
}
