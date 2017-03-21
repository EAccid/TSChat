package com.eaccid.tschat;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ImageViewLoader {
    public static final int EMPTY_ACCOUNT_RES_ID = R.drawable.empty_circle_background_account;
    private final String LOG_TAG = "ImageViewLoader";

    public void loadPictureFromUrl(@NonNull final ImageView imageView, final String url, final @DrawableRes int placeholderId, final @DrawableRes int errorId, boolean isCached) {

        if (url.isEmpty())
            return;
        NetworkPolicy networkPolicy = isCached ? NetworkPolicy.OFFLINE : NetworkPolicy.NO_CACHE;
        load(networkPolicy, imageView, url, placeholderId, errorId);
    }

    private void load(NetworkPolicy networkPolicy, final ImageView imageView, final String url, final @DrawableRes int placeholderId, final @DrawableRes int errorId) {
        Picasso
                .with(imageView.getContext().getApplicationContext())
                .load(url)
                .networkPolicy(networkPolicy)
                .placeholder(placeholderId)
                .error(errorId)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.v(LOG_TAG, "Picasso has fetched the image from "
                                + (networkPolicy == NetworkPolicy.NO_CACHE ? "url online" : "cache")
                        );
                    }

                    @Override
                    public void onError() {
                        if (networkPolicy == NetworkPolicy.OFFLINE) {
                            load(NetworkPolicy.NO_CACHE, imageView, url, placeholderId, errorId);
                        } else {
                            Log.v(LOG_TAG, "Picasso could not fetch the image from cache and url online");
                        }
                    }
                });
    }

}
