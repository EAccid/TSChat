package com.eaccid.tschat.messages;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eaccid.tschat.R;
import com.eaccid.tschat.entity.Message;
import com.eaccid.tschat.semantics.ImageViewLoader;
import com.eaccid.tschat.semantics.TSChatPreferences;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

//by example of friendly chat -> firebase.google.com

public class ConversationScreen extends AppCompatActivity {
    private static final String TAG = "ConversationScreen";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private String mUsername;
    private String mPhotoUrl;
    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseMessageAdapter;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAnalytics mFirebaseAnalytics;
    private EditText mMessageEditText;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_screen);
        //todo bind
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        ImageView mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = mFirebaseUser.getDisplayName();
        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        String uidToConnectWith = getIntent().getStringExtra("chat_id");
        Query messagesQuery = mFirebaseDatabaseReference
                .child(MESSAGES_CHILD)
                .child(mFirebaseUser.getUid())
                .child(uidToConnectWith);
        mFirebaseMessageAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                messagesQuery) {

            @Override
            protected Message parseSnapshot(DataSnapshot snapshot) {
                Message message = super.parseSnapshot(snapshot);
                if (message != null) {
                    message.setId(snapshot.getKey());
                }
                return message;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              Message message, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (message.getText() != null) {
                    viewHolder.text.setText(message.getText());
                    viewHolder.text.setVisibility(TextView.VISIBLE);
                    viewHolder.photo.setVisibility(ImageView.GONE);
                } else {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                task -> {
                                    if (task.isSuccessful()) {
                                        String downloadUrl = task.getResult().toString();
                                        new ImageViewLoader().loadPictureFromUrl(
                                                viewHolder.image,
                                                downloadUrl,
                                                ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                                                ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                                                true
                                        );
                                    } else {
                                        Log.w(TAG, "Getting download url was not successful.",
                                                task.getException());
                                    }
                                });
                    } else {
                        new ImageViewLoader().loadPictureFromUrl(
                                viewHolder.image,
                                message.getImageUrl(),
                                ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                                ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                                true
                        );
                    }
                    viewHolder.image.setVisibility(ImageView.VISIBLE);
                    viewHolder.text.setVisibility(TextView.GONE);
                }

                viewHolder.name.setText(message.getName());
                viewHolder.photo.setVisibility(ImageView.VISIBLE);
                if (message.getPhotoUrl() == null) {
                    viewHolder.photo.setImageDrawable(ContextCompat.getDrawable(ConversationScreen.this,
                            R.drawable.empty_circle_background_account));
                } else {
                    new ImageViewLoader().loadPictureFromUrl(
                            viewHolder.photo,
                            message.getPhotoUrl(),
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            true
                    );
                }
            }

        };

        mFirebaseMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mFirebaseMessageAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseMessageAdapter);

        //config
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();
        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(TSChatPreferences.MSG_LENGTH, 10L);
        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(TSChatPreferences.MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mAddMessageImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(view -> {
            Message message = new Message(mMessageEditText.getText().toString(), mFirebaseUser.getUid(), mUsername, mPhotoUrl, null);
            mFirebaseDatabaseReference
                    .child(MESSAGES_CHILD)
                    .child(mFirebaseUser.getUid())
                    .child(uidToConnectWith)
                    .push()
                    .setValue(message);
            mMessageEditText.setText("");
            mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
        });
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(aVoid -> {
                    // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                    mFirebaseRemoteConfig.activateFetched();
                    applyRetrievedLengthLimit();
                })
                .addOnFailureListener(e -> {
                    // There has been an error fetching the config
                    Log.w(TAG, "Error fetching config", e);
                    applyRetrievedLengthLimit();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());
                    Message tempMessage = new Message(null, mFirebaseUser.getUid(), mUsername, mPhotoUrl, LOADING_IMAGE_URL);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                            .setValue(tempMessage, (databaseError, databaseReference) -> {
                                if (databaseError == null) {
                                    String key = databaseReference.getKey();
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance()
                                                    .getReference(mFirebaseUser.getUid())
                                                    .child(key)
                                                    .child(uri.getLastPathSegment());

                                    putImageInStorage(storageReference, uri, key);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            });
                }
            }
        } else if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");
                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);
                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(ConversationScreen.this, task -> {
            if (task.isSuccessful()) {
                Message message =
                        new Message(
                                null, mFirebaseUser.getUid(), mUsername, mPhotoUrl, task.getResult().getMetadata().getDownloadUrl().toString()
                        );
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                        .setValue(message);
            } else {
                Log.w(TAG, "Image upload task was not successful.",
                        task.getException());
            }
        });
    }

    /**
     * Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from
     * cached values.
     */
    private void applyRetrievedLengthLimit() {
        Long msg_length = mFirebaseRemoteConfig.getLong(TSChatPreferences.MSG_LENGTH);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(msg_length.intValue())});
        Log.d(TAG, "FML is: " + msg_length);
    }
}
