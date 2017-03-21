package com.eaccid.tschat.messages;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eaccid.tschat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.message_name)
    TextView name;
    @BindView(R.id.message_image)
    ImageView image;
    @BindView(R.id.message_text)
    TextView text;
    @BindView(R.id.photo)
    CircleImageView messengerImageView;

    public MessageViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}
