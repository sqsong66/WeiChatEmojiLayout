package com.example.wechatemojilayout.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 青松 on 2016/9/2.
 */
public class CirclePagerIndicator extends View {

    private int mRadius;
    private int mIndicatorRadius;
    private int normalColor;
    private int focusColor;
    private int mStrokeWidth;
    private int mCircleSpacing;
    private int mCircleCount;
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private Paint mPaint;

    private List<PointF> mCirclePoints = new ArrayList<>();
    private float mIndicatorX;

    public CirclePagerIndicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRadius = dip2px(3);
        mIndicatorRadius = dip2px(3);
        mCircleSpacing = dip2px(8);
        mStrokeWidth = dip2px(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        prepareCirclePoints();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircles(canvas);
        drawIndicator(canvas);
    }

    private void drawCircles(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(normalColor);
        for (int i = 0, j = mCirclePoints.size(); i < j; i++) {
            PointF pointF = mCirclePoints.get(i);
            canvas.drawCircle(pointF.x, pointF.y, mRadius, mPaint);
        }
    }

    private void drawIndicator(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(focusColor);
        if (mCirclePoints.size() > 0) {
            canvas.drawCircle(mIndicatorX, getHeight() / 2, mIndicatorRadius, mPaint);
        }
    }

    private void prepareCirclePoints() {
        mCirclePoints.clear();
        if (mCircleCount > 0) {
            int y = getHeight() / 2;
            int measureWidth = mCircleCount * mRadius * 2 + (mCircleCount - 1) * mCircleSpacing;
            int centerSpacing = mRadius * 2 + mCircleSpacing;
            int startX = (getWidth() - measureWidth) / 2 + mRadius;
            for (int i = 0; i < mCircleCount; i++) {
                PointF pointF = new PointF(startX, y);
                mCirclePoints.add(pointF);
                startX += centerSpacing;
            }
            mIndicatorX = mCirclePoints.get(0).x;
        }
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mCirclePoints.isEmpty()) {
            return;
        }

        int nextPosition = Math.min(mCirclePoints.size() - 1, position + 1);
        PointF current = mCirclePoints.get(position);
        PointF next = mCirclePoints.get(nextPosition);

        mIndicatorX = current.x + (next.x - current.x) * mStartInterpolator.getInterpolation(positionOffset);
        invalidate();
    }

    public void notifyDataSetChanged() {
        prepareCirclePoints();
        invalidate();
    }

    public void setCircleCount(int count) {
        this.mCircleCount = count;
    }

    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
    }

    public void setFocusColor(int focusColor) {
        this.focusColor = focusColor;
    }

    public int dip2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}
