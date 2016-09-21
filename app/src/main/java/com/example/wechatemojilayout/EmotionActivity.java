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
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatemojilayout.adapter.EmotionGridAdapter;
import com.example.wechatemojilayout.fragment.EmotionFragment;
import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.model.EmotionCategoryItem;
import com.example.wechatemojilayout.utlis.DensityUtils;
import com.example.wechatemojilayout.utlis.EmotionAssetDbHelper;
import com.example.wechatemojilayout.utlis.EmotionDecodeHelper;
import com.example.wechatemojilayout.utlis.ImageBitmapCache;

import java.util.ArrayList;
import java.util.List;

public class EmotionActivity extends AppCompatActivity implements View.OnClickListener, EmotionGridAdapter.OnEmotionItemClickListener {

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
    private int inputKeyBoardHeight;
    private EmotionAssetDbHelper mEmotionAssetDbHelper;

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
        mEmotionAssetDbHelper = new EmotionAssetDbHelper(getApplicationContext());
        mEmotionDecodeHelper = new EmotionDecodeHelper(getApplicationContext());

        emotion_face_iv.setOnClickListener(this);
        input_edit.addTextChangedListener(textWatcher);
        input_edit.setFilters(new InputFilter[]{editFilter});
        input_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (emotion_ll.isShown()) {
                    lockContentHeight();
                    hideEmotionLayout();
                    unLockContentHeight();
                    return true;
                }
                return false;
            }
        });
        input_edit.post(new Runnable() {
            @Override
            public void run() {
                input_edit.requestFocus();
            }
        });
        emotion_ll.postDelayed(new Runnable() {
            @Override
            public void run() {
                inputKeyBoardHeight = DensityUtils.getSupportSoftInputHeight(EmotionActivity.this);
            }
        }, 200);

        emotion_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

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
        new AsyncTask<Void, Void, List<EmotionCategoryItem>>() {

            @Override
            protected List<EmotionCategoryItem> doInBackground(Void... voids) {
                return mEmotionAssetDbHelper.queryEmotionList(EmotionAssetDbHelper.DB_EMOTION);
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
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

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
            } else if (inputStr.length() == 0 && alpha == 1) {
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

    private InputFilter editFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int i, int i1, Spanned spanned, int i2, int i3) {
            if ("".equals(source)) {
                return source;
            }
            CharSequence result = source;
            if (result.toString().startsWith("[")) {
                String emojiPicName = mEmotionAssetDbHelper.queryEmotionValue(EmotionAssetDbHelper.DB_EMOTION, source.toString());
                Bitmap bitmap = mEmotionDecodeHelper.getEmotonAssetBitmap(emojiPicName, 23);
                SpannableString emotionSpannable = new SpannableString(result);
                ImageSpan imageSpan = new ImageSpan(getApplicationContext(), bitmap);
                emotionSpannable.setSpan(imageSpan, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                result = emotionSpannable;
            }
            return result;
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

    @Override
    public void onEmotionItemClick(Emotion emotion, boolean isDelItem) {
        if (emotion != null && !isDelItem) {
            String name = emotion.getName();
            Editable editable = input_edit.getEditableText();
            int start = input_edit.getSelectionStart();
            editable.insert(start, name);
        } else {
            input_edit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
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
        } else {
            // 如果键盘是显示的，则需要锁定布局切换emoji表情布局防止出现闪跳；
            // 否则不需要锁定布局，如果键盘未显示锁定了布局，就会导致emoji布局无法弹出
            if (isSoftInputShown()) {
                lockContentHeight();
                showEmotionLayout();
                unLockContentHeight();
            } else {
                showEmotionLayout();
            }
        }
    }

    /**
     * 判断键盘当前是否有显示
     *
     * @return true: 键盘显示  false: 键盘未显示
     */
    private boolean isSoftInputShown() {
        return DensityUtils.getSupportSoftInputHeight(this) != 0;
    }

    /**
     * 显示Emoji表情布局
     */
    private void showEmotionLayout() {
        if (inputKeyBoardHeight == 0) {
            inputKeyBoardHeight = DensityUtils.getSupportSoftInputHeight(this);
        }
        hideInputKeyBoard(true);
        emotion_ll.getLayoutParams().height = inputKeyBoardHeight;
        emotion_ll.setVisibility(View.VISIBLE);
        emotion_face_iv.setImageResource(R.drawable.selector_publish_keyboard);
    }

    /**
     * 隐藏Emoji表情布局
     */
    private void hideEmotionLayout() {
        emotion_ll.setVisibility(View.GONE);
        hideInputKeyBoard(false);
        emotion_face_iv.setImageResource(R.drawable.selector_publish_face);
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

    /**
     * 添加底部的分类tab栏
     * @param categoryItems emoji表情分类集合
     */
    private void addEmotionCategoryTab(List<EmotionCategoryItem> categoryItems) {
        for (int i = 0; i < categoryItems.size(); i++) {
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

    /**
     * 创建底部的emoji分类tab栏，将分类中的第一个表情作为图标
     * @param value 表情图片名称
     * @return 分类tab布局
     */
    private View generateCatView(String value) {
        RelativeLayout layout = new RelativeLayout(getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DensityUtils.dip2px(60), ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageBitmapCache.getInstance().clearMemCache();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (emotion_ll.isShown()) {
                hideEmotionLayout();
                hideInputKeyBoard(true);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
