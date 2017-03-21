package com.eaccid.tschat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.eaccid.tschat.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.last_message)
    TextView lastMessage;
    @BindView(R.id.photo)
    CircleImageView photo;

    public UserViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}
