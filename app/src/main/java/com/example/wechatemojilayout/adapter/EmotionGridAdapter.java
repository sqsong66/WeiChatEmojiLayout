package com.example.wechatemojilayout.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.wechatemojilayout.R;
import com.example.wechatemojilayout.model.Emotion;
import com.example.wechatemojilayout.utlis.DensityUtils;
import com.example.wechatemojilayout.utlis.EmotionDecodeHelper;

import java.util.List;

/**
 * Created by 青松 on 2016/9/20.
 */
public class EmotionGridAdapter extends RecyclerView.Adapter<EmotionGridAdapter.EmotionItemViewHolder> {

    private Context context;
    private List<Emotion> emotionList;
    private EmotionDecodeHelper decodeHelper;
    private OnEmotionItemClickListener listener;

    public interface OnEmotionItemClickListener {
        /**
         * 表情item点击事件回调
         * @param emotion 表情信息
         * @param isDelItem 是否点击的是删除按钮
         */
        void onEmotionItemClick(Emotion emotion, boolean isDelItem);
    }

    public EmotionGridAdapter(Context context, List<Emotion> emotionList, OnEmotionItemClickListener l) {
        this.context = context;
        this.listener = l;
        this.emotionList = emotionList;
        this.decodeHelper = new EmotionDecodeHelper(context);
    }

    @Override
    public EmotionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emotion_item, parent, false);
        return new EmotionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmotionItemViewHolder holder, int position) {
        int size = DensityUtils.dip2px(35);
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        params.height = size;
        Emotion emotion = null;
        final boolean isDel;
        if (position == emotionList.size()) {
            holder.emotion_iv.setImageResource(R.drawable.selector_emotion_delete);
            isDel = true;
        } else {
            emotion = emotionList.get(position);
            String value = emotion.getValue();
            Bitmap bitmap = decodeHelper.getEmotonAssetBitmap(value, 35);
            holder.emotion_iv.setImageBitmap(bitmap);
            isDel = false;
        }

        final Emotion finalEmotion = emotion;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onEmotionItemClick(finalEmotion, isDel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return emotionList.size() + 1;
    }

    public class EmotionItemViewHolder extends RecyclerView.ViewHolder {

        ImageView emotion_iv;
        View itemView;

        public EmotionItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.emotion_iv = (ImageView) itemView.findViewById(R.id.emotion_iv);
        }
    }

}
