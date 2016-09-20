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

    public EmotionGridAdapter(Context context, List<Emotion> emotionList) {
        this.context = context;
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

        if (position == emotionList.size()) {
            holder.emotion_iv.setImageResource(R.drawable.selector_emotion_delete);
        } else {
            Emotion emotion = emotionList.get(position);
            String value = emotion.getValue();
            Bitmap bitmap = decodeHelper.getEmotonAssetBitmap(value, 35);
            holder.emotion_iv.setImageBitmap(bitmap);
        }
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
