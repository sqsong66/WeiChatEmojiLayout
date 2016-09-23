package com.example.wechatemojilayout.utlis;

import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


/**
 * User: qii
 * Date: 13-10-7
 */
public class ClickableTextViewMentionLinkOnTouchListener implements View.OnTouchListener {

    private boolean find = false;
    private int color;

    public ClickableTextViewMentionLinkOnTouchListener(int color) {
        this.color = color;
    }

    public ClickableTextViewMentionLinkOnTouchListener() {
        this.color = Color.parseColor("#33969696");
    }

    public boolean onTouch(View v, MotionEvent event) {
        Layout layout = ((TextView)v).getLayout();
        if(layout == null) {
            return false;
        } else {
            int x = (int)event.getX();
            int y = (int)event.getY();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, (float)x);
            TextView tv = (TextView)v;
            SpannableString value = SpannableString.valueOf(tv.getText());
            int i$;
            switch(event.getActionMasked()) {
                case 0:
                    MyURLSpan[] urlSpans = (MyURLSpan[])value.getSpans(0, value.length(), MyURLSpan.class);
                    int findStart = 0;
                    int findEnd = 0;
                    MyURLSpan[] lineWidth = urlSpans;
                    int var20 = urlSpans.length;

                    for(int var22 = 0; var22 < var20; ++var22) {
                        MyURLSpan var23 = lineWidth[var22];
                        i$ = value.getSpanStart(var23);
                        int var24 = value.getSpanEnd(var23);
                        if(i$ <= offset && offset <= var24) {
                            this.find = true;
                            findStart = i$;
                            findEnd = var24;
                            break;
                        }
                    }

                    float var19 = layout.getLineWidth(line);
                    this.find &= var19 >= (float)x;
                    if(this.find) {
                        LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                        BackgroundColorSpan var21 = new BackgroundColorSpan(this.color);
                        value.setSpan(var21, findStart, findEnd, 18);
                        tv.setText(value);
                    }

                    return this.find;
                case 1:
                case 3:
                    if(this.find) {
                        LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                        LongClickableLinkMovementMethod.getInstance().removeLongClickCallback();
                    }

                    BackgroundColorSpan[] backgroundColorSpans = (BackgroundColorSpan[])value.getSpans(0, value.length(), BackgroundColorSpan.class);
                    BackgroundColorSpan[] arr$ = backgroundColorSpans;
                    int len$ = backgroundColorSpans.length;

                    for(i$ = 0; i$ < len$; ++i$) {
                        BackgroundColorSpan backgroundColorSpan = arr$[i$];
                        value.removeSpan(backgroundColorSpan);
                    }

                    tv.setText(value);
                    this.find = false;
                    break;
                case 2:
                    if(this.find) {
                        LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
                    }
            }

            return false;
        }
    }
}
