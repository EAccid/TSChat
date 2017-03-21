package com.eaccid.tschat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class AuthActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_UP = 123;
    private final int RC_SIGN_IN = 9001;
    private final String LOG_TAG = "AuthActivity";
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Class mClassToBack;
    @BindView(R.id.sign_in_button)
    SignInButton mSignInButton;
    @BindView(R.id.sign_up_button)
    Button mSignUpButton;
    @BindView(R.id.sign_out_button)
    Button mSignOutButton;
    @BindView(R.id.delete_account_button)
    Button mDeleteAccountButton;
    @BindView(R.id.signed_in)
    View signed_in_root_view;
    @BindView(R.id.signed_out)
    View signed_out_root_view;
    @BindView(R.id.user_profile_picture)
    CircleImageView mUserProfilePicture;
    @BindView(R.id.user_email)
    TextView mUserEmail;
    @BindView(R.id.user_display_name)
    TextView mUserDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);
        ButterKnife.bind(this);
        mClassToBack = MainActivity.class;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(LOG_TAG, "onAuthStateChanged: signed_in -> user id: " + user.getUid());
            } else {
                Log.d(LOG_TAG, "onAuthStateChanged: signed_out.");
            }
        };
        setVisibleRootView();
        loadUserDataIfExists();
        loadGoogleOptions();
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @OnClick(R.id.sign_in_button)
    public void onSignInButtonClicked() {
        signIn();
    }

    @OnClick(R.id.sign_up_button)
    public void onSignUpButtonClicked() {
        signUp();
    }

    @OnClick(R.id.sign_out_button)
    public void onSignOutButtonClicked() {
        signOut();
    }

    @OnClick(R.id.delete_account_button)
    public void onDeleteAccountButtonClicked() {
        deleteAccount();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
            handleGoogleSignInResponse(data, resultCode);
        if (requestCode == RC_SIGN_UP)
            handleEmailSignUpResponse(data, resultCode);
    }

    private void handleGoogleSignInResponse(Intent data, int resultCode) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            onGoogleSignInError(result);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startSucceedAuthAction();
                });
    }

    private void onGoogleSignInError(GoogleSignInResult result) {
        Status status = result.getStatus();
        Log.e(LOG_TAG, "Google Sign In failed: " + result.getStatus());
        if (status.getStatusCode() == CommonStatusCodes.INVALID_ACCOUNT) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
            return;
        }
        if (status.getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Google Sign In: Something went wrong", Toast.LENGTH_SHORT).show();
    }

    private void handleEmailSignUpResponse(Intent data, int resultCode) {
        if (data == null) {
            // User pressed back button
            Toast.makeText(this, "Sign up is cancelled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        startSucceedAuthAction();
    }

    private void signIn() {
        Intent authorizeIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(authorizeIntent, RC_SIGN_IN);
    }

    private void signUp() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppThemeNoActionBar)
                        .build(),
                RC_SIGN_UP);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> Log.i(LOG_TAG, "signOut result status: " + status.getStatus()));
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> startSucceedAuthAction());
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Deletion succeeded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AuthActivity.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                    }
                    startSucceedAuthAction();
                });
    }

    private void setVisibleRootView() {
        if (mFirebaseAuth.getCurrentUser() == null) {
            signed_out_root_view.setVisibility(View.GONE);
            signed_in_root_view.setVisibility(View.VISIBLE);
            return;
        }
        signed_in_root_view.setVisibility(View.GONE);
        signed_out_root_view.setVisibility(View.VISIBLE);
    }

    private void loadUserDataIfExists() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user == null) return;
        if (user.getPhotoUrl() != null) {
            new ImageViewLoader()
                    .loadPictureFromUrl(mUserProfilePicture,
                            user.getPhotoUrl().toString(),
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            ImageViewLoader.EMPTY_ACCOUNT_RES_ID,
                            true
                    );
        }
        mUserEmail.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());
        mUserDisplayName.setText(
                TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName());
    }

    private void loadGoogleOptions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
    }

    private void startSucceedAuthAction() {
        Log.i(LOG_TAG, "Back to main activity");
        startActivity(new Intent(AuthActivity.this, mClassToBack));
        finish();
    }

}
