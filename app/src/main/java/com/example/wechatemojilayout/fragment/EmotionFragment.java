package com.example.wechatemojilayout.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.wechatemojilayout.R;
import com.example.wechatemojilayout.adapter.EmotionGridAdapter;
import com.example.wechatemojilayout.adapter.EmotionGridAdapter.OnEmotionItemClickListener;
import com.example.wechatemojilayout.adapter.EmotionViewPagerAdapter;
import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.utlis.DensityUtils;
import com.example.wechatemojilayout.view.CirclePagerIndicator;

import java.util.ArrayList;
import java.util.List;

public class EmotionFragment extends Fragment implements OnEmotionItemClickListener {
    private static final String EMOTION_LIST = "emotion_list";

    private ArrayList<Emotion> mEmotionLists;
    private ViewPager emotion_viewpager;
    private LinearLayout dot_ll;
    private OnEmotionItemClickListener mListener;

    public static EmotionFragment newInstance(ArrayList<Emotion> emotionLists) {
        EmotionFragment fragment = new EmotionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EMOTION_LIST, emotionLists);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmotionLists = getArguments().getParcelableArrayList(EMOTION_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_emotion, container, false);
        initView(rootView);

        initEmotions();
        return rootView;
    }

    private void initView(View view) {
        emotion_viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        dot_ll = (LinearLayout) view.findViewById(R.id.dot_ll);
    }

    private void initEmotions() {
        List<RecyclerView> emotionRecyclerLists = new ArrayList<>();
        List<Emotion> emotionList = new ArrayList<>();
        for (Emotion emotion: mEmotionLists) {
            emotionList.add(emotion);
            if (emotionList.size() == 20) { //每20个表情创建一页表情
                RecyclerView singlePageRecycler = createSinglePageRecyclerView(emotionList);
                emotionRecyclerLists.add(singlePageRecycler);
                emotionList = new ArrayList<>();
            }
        }

        //最后剩余的表情作为一页
        if (emotionList.size() > 0) {
            RecyclerView emotionRecyclerView = createSinglePageRecyclerView(emotionList);
            emotionRecyclerLists.add(emotionRecyclerView);
        }

        EmotionViewPagerAdapter pagerAdapter = new EmotionViewPagerAdapter(emotionRecyclerLists);
        emotion_viewpager.setAdapter(pagerAdapter);

        //设置导航圆点
        final CirclePagerIndicator indicator = new CirclePagerIndicator(getActivity());
        indicator.setCircleCount(emotionRecyclerLists.size());
        indicator.setNormalColor(getResources().getColor(R.color.color_E4E4E4));
        indicator.setFocusColor(getResources().getColor(R.color.color_999999));

        //将导航圆点添加到圆点容器中
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dot_ll.addView(indicator, lp);

        emotion_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //关联ViewPager
                indicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position) {}

        });
    }

    private RecyclerView createSinglePageRecyclerView(List<Emotion> emotionList) {
        RecyclerView recyclerView = new RecyclerView(getActivity());
        int paddingTB = DensityUtils.dip2px(15);
        recyclerView.setPadding(paddingTB, paddingTB, paddingTB, paddingTB);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        EmotionGridItemDecoration decoration = new EmotionGridItemDecoration(7, DensityUtils.dip2px(10), false);
        recyclerView.addItemDecoration(decoration);
        EmotionGridAdapter adapter = new EmotionGridAdapter(getActivity(), emotionList, this);
        recyclerView.setAdapter(adapter);
        return recyclerView;
    }

    @Override
    public void onEmotionItemClick(Emotion emotion, boolean isDelItem) {
        if (mListener != null) {
            mListener.onEmotionItemClick(emotion, isDelItem);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEmotionItemClickListener) {
            mListener = (OnEmotionItemClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnEmotionItemClickListener");
        }
    }

    public static class EmotionGridItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public EmotionGridItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = (int)(spacing * 2.0f);
                }
            }
        }
    }

}
