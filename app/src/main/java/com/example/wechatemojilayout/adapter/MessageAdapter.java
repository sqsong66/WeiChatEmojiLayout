package com.example.wechatemojilayout.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wechatemojilayout.R;
import com.example.wechatemojilayout.model.Message;
import com.example.wechatemojilayout.utlis.ClickableTextViewMentionLinkOnTouchListener;
import com.example.wechatemojilayout.utlis.TextFormatUtils;
import com.example.wechatemojilayout.view.CircleImageView;

import java.util.List;

/**
 * Created by 青松 on 2016/9/23.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context mContext;
    private List<Message> mMessageList;
    private LayoutInflater mInflater;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        ClickableTextViewMentionLinkOnTouchListener listener = new ClickableTextViewMentionLinkOnTouchListener();
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return listener.onTouch(view, motionEvent);
        }
    };

    public MessageAdapter(Context context, List<Message> messageList) {
        this.mContext = context;
        this.mMessageList = messageList;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_message_right, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        SpannableString spannableMessage = message.getSpannableMessage();
        if (spannableMessage != null) {
            holder.message_tv.setText(spannableMessage);
        } else {
            TextFormatUtils.setSpannableText(holder.message_tv, message);
        }
        holder.message_tv.setOnTouchListener(touchListener);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView message_tv;
        CircleImageView avatar_civ;

        public MessageViewHolder(View itemView) {
            super(itemView);
            message_tv = (TextView) itemView.findViewById(R.id.message_tv);
            avatar_civ = (CircleImageView) itemView.findViewById(R.id.avatar_civ);
        }
    }

}
