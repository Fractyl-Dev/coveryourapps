package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

public class SplashActivity extends AppCompatActivity {
    private static boolean googleQuitDuringAccountCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DBHandler.refreshUser(true);
//        Log.d("**Splash Activity |", DBHandler.getCurrentFirebaseUser().getUid());

        if (DBHandler.getCurrentFirebaseUser() != null) {
            ensureEverythingReadyDelay();
        } else {
            int SPLASH_LENGTH = 750;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//            Log.d("**Splash Activity |", "User not found, sending to sign in");
                    Intent nextIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(nextIntent);
                }
            }, SPLASH_LENGTH);
        }
    }

    private void ensureEverythingReadyDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**Splash Activity |", "User found, going to main activity");
                    Intent nextIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(nextIntent);
                } else {
                    Log.d("**Splash Activity |", "Not done thinking");
                    if (DBHandler.isGoogleQuitDuringAccountCreation()) {
                        // Send to login because the user quit the app while making an account. This is determined in DBHandler
                        Intent nextIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(nextIntent);
                    } else {
                        ensureEverythingReadyDelay();
                    }
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    public static void setGoogleQuitDuringAccountCreation(boolean value) {
        googleQuitDuringAccountCreation = value;
    }
}