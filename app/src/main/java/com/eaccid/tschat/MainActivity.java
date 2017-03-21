package com.eaccid.tschat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            navigateToAuthActivity();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_main, new UsersFragment(), "users")
                .commit();
    }

    @OnClick(R.id.temp_button)
    public void onTempButtonClicked() {
        navigateToAuthActivity();
    }

    private void navigateToAuthActivity() {
        Intent i = new Intent(this, AuthActivity.class);
        startActivity(i);
    }

}
