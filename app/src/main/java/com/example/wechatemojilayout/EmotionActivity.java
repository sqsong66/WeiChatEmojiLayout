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
import android.support.v7.widget.LinearLayoutManager;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.wechatemojilayout.adapter.EmotionGridAdapter;
import com.example.wechatemojilayout.adapter.MessageAdapter;
import com.example.wechatemojilayout.fragment.EmotionFragment;
import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.model.EmotionCategoryItem;
import com.example.wechatemojilayout.model.Message;
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
    private LinearLayout root_ll;
    private LinearLayout emotion_ll;
    private ImageView emotion_face_iv;
    private ViewPager emotion_viewpager;
    private LinearLayout emotion_tab_ll;
    private RecyclerView message_recyclerView;

    /** 消息适配器*/
    private MessageAdapter mMessageAdapter;
    /** 输入法管理器*/
    private InputMethodManager inputMethodManager;
    /** 根据表情图片名称从assets目录获取标签bitmap帮助类*/
    private EmotionDecodeHelper mEmotionDecodeHelper;
    /** 数据库表情信息查询帮助类*/
    private EmotionAssetDbHelper mEmotionAssetDbHelper;
    /** 消息集合*/
    private List<Message> mMessageList = new ArrayList<>();
    /** 分类标签fragment集合*/
    private List<Fragment> mCategoryFragments = new ArrayList<>();
    /** 外层表情分类ViewPager适配器*/
    private EmotionCategoryPagerAdapter mEmotionCategoryPagerAdapter;

    /** 当前选中的表情tab*/
    private int curSelectTab = 0;
    /** 键盘高度*/
    private int inputKeyBoardHeight;

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

        message_recyclerView = (RecyclerView) findViewById(R.id.message_recyclerView);
        input_edit = (EditText) findViewById(R.id.input_edit);
        emotion_face_iv = (ImageView) findViewById(R.id.emotion_face_iv);
        plus_iv = (ImageView) findViewById(R.id.plus_iv);
        send_tv = (TextView) findViewById(R.id.send_tv);
        emotion_ll = (LinearLayout) findViewById(R.id.emotion_ll);
        emotion_viewpager = (ViewPager) findViewById(R.id.emotion_viewpager);
        emotion_tab_ll = (LinearLayout) findViewById(R.id.emotion_tab_ll);
        root_ll = (LinearLayout) findViewById(R.id.root_ll);
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
                // 进入activity获取键盘高度，防止进入界面后，点击返回键隐藏键盘，再点击表情图标时，由于键盘未弹出测量到的键盘
                // 高度为0导致emoji表情无法弹出来
                inputKeyBoardHeight = DensityUtils.getSupportSoftInputHeight(EmotionActivity.this);
            }
        }, 200);

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

        message_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageAdapter = new MessageAdapter(this, mMessageList);
        message_recyclerView.setAdapter(mMessageAdapter);

        root_ll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = root_ll.getRootView().getHeight() - root_ll.getHeight();
                if (heightDiff > 500) { // 当键盘或者表情布局弹出来时，让消息列表滚动到最低部
                    message_recyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount());
                }
            }
        });

        message_recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // 点击键盘或者表情以外部分时，让键盘或者表情布局隐藏
                // 防止滑动recyclerview时，touch事件拦截recyclerview的滚动事件
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (emotion_ll.isShown()) {
                        hideEmotionLayout();
                    }
                    hideInputKeyBoard(true);
                    return true;
                }
                return false;
            }
        });
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
            if (inputStr.length() >= 0 && alpha == 0) { //设置添加按钮和发送按钮显示和消失动画
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

    /** 识别输入的表情*/
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
                refreshMessageList();
                break;
        }
    }

    /**
     * 刷新消息列表
     */
    private void refreshMessageList() {
        String text = input_edit.getText().toString();
        Message message = new Message(text, "");
        mMessageList.add(message);
        mMessageAdapter.notifyItemInserted(mMessageList.size());
        input_edit.setText("");
        //消息列表滚动到最底部
        message_recyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount());
    }

    @Override
    public void onEmotionItemClick(Emotion emotion, boolean isDelItem) {
        if (emotion != null && !isDelItem) { //点击表情时，在输入框中插入标签
            String name = emotion.getName();
            Editable editable = input_edit.getEditableText();
            int start = input_edit.getSelectionStart();
            editable.insert(start, name);
        } else { //当点击的是删除按钮时，删除文字或者表情
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
        //表情布局显示时，消息列表滚动到最底部
        message_recyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount());
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
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message_recyclerView.getLayoutParams();
        params.height = message_recyclerView.getHeight();
        params.weight = 0;
    }

    /**
     * 解锁表情布局之上的内容
     */
    private void unLockContentHeight() {
        emotion_ll.postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) message_recyclerView.getLayoutParams();
                params.weight = 1;
            }
        }, 200);
    }

    /**
     * 是否隐藏软键盘
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

    /** 添加分类表情fragment*/
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
            // 点击返回按钮时，如果表情布局或者键盘正在显示则先隐藏
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
