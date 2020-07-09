package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private FirebaseFirestore usersDB = FirebaseFirestore.getInstance();
    //private User user =
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        usersDB.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                //Log.d("User Query", documentSnapshot.getId() + " -> " + documentSnapshot.getData());

                                int SPLASH_LENGTH = 750;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent nextIntent;
                                        if (user != null) {
                                            Log.d("**Splash Activity | Sign in Check", "User found, going to main activity");
                                            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
                                        } else {
                                            Log.d("**Splash Activity | Sign in Check", "User not found, sending to sign in");
                                            nextIntent = new Intent(SplashActivity.this, LoginActivity.class);
                                        }
                                        startActivity(nextIntent);

                                        finish();
                                    }
                                }, SPLASH_LENGTH);
                            }
                        } else {
                            Log.w("**User Query", "Error getting the documents.", task.getException());
                        }
                    }
                });
    }
}