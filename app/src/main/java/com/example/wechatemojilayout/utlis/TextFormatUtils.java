package com.example.wechatemojilayout.utlis;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import com.example.wechatemojilayout.BuildConfig;
import com.example.wechatemojilayout.model.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 青松 on 2016/9/23.
 */
public class TextFormatUtils {

    //Pattern
    public static final Pattern WEB_URL = Pattern.compile(Patterns.WEB_URL.pattern(), Pattern.CASE_INSENSITIVE);
    public static final Pattern TOPIC_URL = Pattern.compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
    public static final Pattern MENTION_URL = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");

    //Scheme
    public static final String WEB_SCHEME = "http://";
    public static final String TOPIC_SCHEME = BuildConfig.APPLICATION_ID + ".topic://";
    public static final String MENTION_SCHEME = BuildConfig.APPLICATION_ID + ".mention://";


    public static SpannableString convertNormalStringToSpannableString(String text) {
        SpannableString spannable = new SpannableString(text);
        Linkify.addLinks(spannable, WEB_URL, WEB_SCHEME);
        Linkify.addLinks(spannable, MENTION_URL, MENTION_SCHEME);
        Linkify.addLinks(spannable, TOPIC_URL, TOPIC_SCHEME);
        URLSpan[] urlSpans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        MyURLSpan weiboSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboSpan = new MyURLSpan(urlSpan.getURL());
            int start = spannable.getSpanStart(urlSpan);
            int end = spannable.getSpanEnd(urlSpan);
            spannable.removeSpan(urlSpan);
            spannable.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public static void setSpannableText(final TextView textView, final Message message) {
        final Context context = textView.getContext();
        final long startTime = System.currentTimeMillis();
        new AsyncTask<String, Void, SpannableString>() {
            @Override
            protected SpannableString doInBackground(String... strings) {
                SpannableString spannableText = convertNormalStringToSpannableString(strings[0]);
                Matcher matcher = Pattern.compile("\\[(\\S+?)\\]").matcher(spannableText);
                EmotionDecodeHelper decodeHelper = new EmotionDecodeHelper(context);
                EmotionAssetDbHelper dbHelper = new EmotionAssetDbHelper(context);
                while (matcher.find()) {
                    if (isCancelled()) break;
                    String key = matcher.group(0);
                    int k = matcher.start();
                    int m = matcher.end();

                    String emojiPicName = dbHelper.queryEmotionValue(EmotionAssetDbHelper.DB_EMOTION, key);
                    Bitmap bitmap = decodeHelper.getEmotonAssetBitmap(emojiPicName, 23);
                    ImageSpan imageSpan = new ImageSpan(context, bitmap, ImageSpan.ALIGN_BOTTOM);
                    spannableText.setSpan(imageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return spannableText;
            }

            @Override
            protected void onPostExecute(SpannableString spannableText) {
                textView.setText(spannableText);
                message.setSpannableMessage(spannableText);
                Log.e("sqsong", "Process Spannable Text Cost Time: " + (System.currentTimeMillis() - startTime) + "ms");
            }
        }.execute(message.getMessage());
    }
}
