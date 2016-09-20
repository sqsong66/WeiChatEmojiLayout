package com.example.wechatemojilayout.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by 青松 on 2016/9/20.
 */
public class EmotionViewPagerAdapter extends PagerAdapter {

    private List<RecyclerView> emotionRecyclerView;

    public EmotionViewPagerAdapter(List<RecyclerView> emotionRecyclerView) {
        this.emotionRecyclerView = emotionRecyclerView;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RecyclerView recyclerView = emotionRecyclerView.get(position);
        container.addView(recyclerView);
        return recyclerView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RecyclerView) object);
    }

    @Override
    public int getCount() {
        return emotionRecyclerView.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}

