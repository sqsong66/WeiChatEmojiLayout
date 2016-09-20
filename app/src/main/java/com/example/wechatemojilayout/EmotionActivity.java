package com.example.wechatemojilayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatemojilayout.fragment.EmotionFragment;
import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.model.EmotionCategoryItem;
import com.example.wechatemojilayout.utlis.DensityUtils;
import com.example.wechatemojilayout.utlis.EmotionAssetDbHelper;
import com.example.wechatemojilayout.utlis.EmotionDecodeHelper;

import java.util.ArrayList;
import java.util.List;

public class EmotionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView send_tv;
    private ImageView plus_iv;
    private EditText input_edit;
    private LinearLayout emotion_ll;
    private ImageView emotion_face_iv;
    private ViewPager emotion_viewpager;
    private LinearLayout emotion_tab_ll;
    private RecyclerView talk_recyclerView;

    private InputMethodManager inputMethodManager;
    private EmotionDecodeHelper mEmotionDecodeHelper;
    private List<Fragment> mCategoryFragments = new ArrayList<>();
    private EmotionCategoryPagerAdapter mEmotionCategoryPagerAdapter;

    private int curSelectTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji);

        initView();
        initEvents();
        initEmotions();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("微信");
        setSupportActionBar(toolbar);

        talk_recyclerView = (RecyclerView) findViewById(R.id.talk_recyclerView);
        input_edit = (EditText) findViewById(R.id.input_edit);
        emotion_face_iv = (ImageView) findViewById(R.id.emotion_face_iv);
        plus_iv = (ImageView) findViewById(R.id.plus_iv);
        send_tv = (TextView) findViewById(R.id.send_tv);
        emotion_ll = (LinearLayout) findViewById(R.id.emotion_ll);
        emotion_viewpager = (ViewPager) findViewById(R.id.emotion_viewpager);
        emotion_tab_ll = (LinearLayout) findViewById(R.id.emotion_tab_ll);
    }

    private void initEvents() {
        emotion_face_iv.setOnClickListener(this);
        input_edit.addTextChangedListener(textWatcher);
        input_edit.post(new Runnable() {
            @Override
            public void run() {
                input_edit.requestFocus();
            }
        });

        emotion_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageSelected(int position) {
                curSelectTab = position;
                int childCount = emotion_tab_ll.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = emotion_tab_ll.getChildAt(i);
                    if (i == position) {
                        child.setBackgroundColor(getResources().getColor(R.color.color_tab_checked));
                    } else {
                        child.setBackgroundColor(getResources().getColor(R.color.color_tab_normal));
                    }
                }
            }

        });

        mEmotionCategoryPagerAdapter = new EmotionCategoryPagerAdapter(getSupportFragmentManager(), mCategoryFragments);
        emotion_viewpager.setAdapter(mEmotionCategoryPagerAdapter);
    }

    private void initEmotions() {
        new AsyncTask<Void, Void, List<EmotionCategoryItem>>(){

            @Override
            protected List<EmotionCategoryItem> doInBackground(Void... voids) {
                EmotionAssetDbHelper assetDbHelper = new EmotionAssetDbHelper(getApplicationContext());
                return assetDbHelper.queryEmotionList(EmotionAssetDbHelper.DB_EMOTION);
            }

            @Override
            protected void onPostExecute(List<EmotionCategoryItem> emotionCategoryItems) {
                addEmotionFragments(emotionCategoryItems);
                addEmotionCategoryTab(emotionCategoryItems);
            }
        }.execute();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String inputStr = input_edit.getText().toString();
            float alpha = send_tv.getAlpha();
            if (inputStr.length() > 0 && alpha == 0) { //设置添加按钮和发送按钮显示和消失动画
                ObjectAnimator alpha1 = ObjectAnimator.ofFloat(send_tv, "alpha", 0, 1);
                ObjectAnimator alpha2 = ObjectAnimator.ofFloat(plus_iv, "alpha", 1, 0);
                AnimatorSet set = new AnimatorSet();
                set.setDuration(300);
                set.playTogether(alpha1, alpha2);
                set.start();
                send_tv.setOnClickListener(EmotionActivity.this);
            } else if (inputStr.length() == 0 && alpha == 1){
                ObjectAnimator alpha1 = ObjectAnimator.ofFloat(send_tv, "alpha", 1, 0);
                ObjectAnimator alpha2 = ObjectAnimator.ofFloat(plus_iv, "alpha", 0, 1);
                AnimatorSet set = new AnimatorSet();
                set.setDuration(300);
                set.playTogether(alpha1, alpha2);
                set.start();
                send_tv.setOnClickListener(null);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emotion_face_iv:
                toggleEmotionVisibility();
                break;
            case R.id.send_tv:
                Toast.makeText(EmotionActivity.this, "Send", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 触发表情布局是否显示
     */
    private void toggleEmotionVisibility() {
        if (emotion_ll.isShown()) {
            lockContentHeight();
            hideEmotionLayout();
            unLockContentHeight();
            emotion_face_iv.setImageResource(R.drawable.selector_publish_face);
        } else {
            lockContentHeight();
            showEmotionLayout();
            unLockContentHeight();
            emotion_face_iv.setImageResource(R.drawable.selector_publish_keyboard);
        }
    }

    /**
     * 显示Emoji表情布局
     */
    private void showEmotionLayout() {
        int inputKeyBoardHeight = DensityUtils.getSupportSoftInputHeight(this);
        if (inputKeyBoardHeight == 0) {
            inputKeyBoardHeight = DensityUtils.dip2px(250);
        }
        hideInputKeyBoard(true);
        emotion_ll.getLayoutParams().height = inputKeyBoardHeight;
        emotion_ll.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏Emoji表情布局
     */
    private void hideEmotionLayout() {
        emotion_ll.setVisibility(View.GONE);
        hideInputKeyBoard(false);
    }

    /**
     * 锁定表情布局之上的内容
     */
    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) talk_recyclerView.getLayoutParams();
        params.height = talk_recyclerView.getHeight();
        params.weight = 0;
    }

    /**
     * 解锁表情布局之上的内容
     */
    private void unLockContentHeight() {
        emotion_ll.postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) talk_recyclerView.getLayoutParams();
                params.weight = 1;
            }
        }, 200);
    }

    /**
     * 是否隐藏软键盘
     *
     * @param hide true：隐藏软键盘  false:显示软键盘
     */
    private void hideInputKeyBoard(boolean hide) {
        if (inputMethodManager == null) {
            inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (hide) {
            inputMethodManager.hideSoftInputFromWindow(input_edit.getWindowToken(), 0);
        } else {
            input_edit.requestFocus();
            inputMethodManager.showSoftInput(input_edit, InputMethodManager.SHOW_FORCED);
        }
    }

    private void addEmotionFragments(List<EmotionCategoryItem> categoryItems) {
        for (EmotionCategoryItem categoryItem : categoryItems) {
            mCategoryFragments.add(EmotionFragment.newInstance((ArrayList<Emotion>) categoryItem.getEmotionList()));
        }
        mEmotionCategoryPagerAdapter.notifyDataSetChanged();
    }

    private void addEmotionCategoryTab(List<EmotionCategoryItem> categoryItems) {
        for (int i=0; i<categoryItems.size(); i++) {
            EmotionCategoryItem categoryItem = categoryItems.get(i);
            Emotion emotion = categoryItem.getEmotionList().get(0);
            String value = emotion.getValue();
            View categoryView = generateCatView(value);
            if (i == 0) {
                categoryView.setBackgroundColor(getResources().getColor(R.color.color_tab_checked));
            } else {
                categoryView.setBackgroundColor(getResources().getColor(R.color.color_tab_normal));
            }

            categoryView.setTag(i);
            categoryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tab = (int) view.getTag();
                    if (curSelectTab != tab) {
                        emotion_viewpager.setCurrentItem(tab, true);
                    }
                }
            });
            emotion_tab_ll.addView(categoryView);
        }
    }

    private View generateCatView(String value) {
        RelativeLayout layout = new RelativeLayout(getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DensityUtils.dip2px(60), ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        if (mEmotionDecodeHelper == null) {
            mEmotionDecodeHelper = new EmotionDecodeHelper(getApplicationContext());
        }
        Bitmap emotionAssetBitmap = mEmotionDecodeHelper.getEmotonAssetBitmap(value, 28);
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setImageBitmap(emotionAssetBitmap);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imageView.setLayoutParams(params);
        layout.addView(imageView);
        return layout;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideEmotionLayout();
    }

    public static class EmotionCategoryPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public EmotionCategoryPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
