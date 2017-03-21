package com.eaccid.tschat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersFragment extends Fragment {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<User, UserViewHolder> mFirebaseUserAdapter;
    public static final String USERS_CHILD = "users";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.users_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new
                DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUserAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.user_item,
                UserViewHolder.class,
                mFirebaseDatabaseReference.child(USERS_CHILD)) {

            @Override
            protected User parseSnapshot(DataSnapshot snapshot) {
                User user = super.parseSnapshot(snapshot);
                if (user != null) {
                    user.setUid(snapshot.getKey());
                }
                return user;
            }

            @Override
            protected void populateViewHolder(final UserViewHolder viewHolder, User user, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (user.getName() != null)
                    viewHolder.name.setText(user.getName());
                if (user.getPhotoUrl() == null) {
                    viewHolder.photo.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID));
                } else {
                    new ImageViewLoader().loadPictureFromUrl(
                            viewHolder.photo,
                            user.getPhotoUrl(),
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            true
                    );
                }
            }
        };
        mRecyclerView.setAdapter(mFirebaseUserAdapter);
    }

}
