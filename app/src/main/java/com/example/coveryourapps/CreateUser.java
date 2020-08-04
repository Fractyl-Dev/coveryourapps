package com.example.coveryourapps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class CreateUser {
    private static Context context;
    private static User user;

    private static FirebaseAuth mAuth;
    private static FirebaseUser currentUser;
    private static FirebaseFirestore usersDB, displayNamesDB;


    public static class CreateUserEmail {
        private String email, password;

        public CreateUserEmail(Context givenContext, String email, String password, User givenUser) {
            context = givenContext;
            this.email = email;
            this.password = password;
            user = givenUser;

            declareFirebaseVariables();
        }

        public void createUser() {
            // Create password authenticated user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("**Create User | User Creation Status", "createUserWithEmail:success");
                                currentUser = mAuth.getCurrentUser();
                                user.setUid(currentUser != null ? currentUser.getUid() : null);// Fancy if/else

                                doesUserNeedMoreTime();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("**Create User | User Creation Status", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(context, "Failed to create a user.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public static class CreateUser3rdParty {


        public CreateUser3rdParty(Context givenContext, User givenUser) {
            context = givenContext;
            user = givenUser;

            declareFirebaseVariables();
        }

        public void createUser() {
            // Signing in was already a success through google, or else this wouldn't be called - safe to assume signed in
            currentUser = mAuth.getCurrentUser();
            user.setUid(currentUser != null ? currentUser.getUid() : null);

            doesUserNeedMoreTime();// If User class is added to database too fast, then the display name may be null - fixed by this
        }
    }

    private static void addUserToDB() {
        //  Add user traits to firestore database, listener ensures it works

        // You're supposed to use Map to put data in a Firestore DB
        Map<String, Object> updateMap = new HashMap();
        updateMap.put("birthdate", user.getBirthdate());
        updateMap.put("displayName", user.getDisplayName());
        updateMap.put("name", user.getName());
        updateMap.put("notificationTokens", user.getNotificationTokens());
        updateMap.put("uid", user.getUid());

        usersDB.collection("users")
                .document(user.getUid())
                .set(updateMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("**Create User | Database Upload UserStatus", "User was added to database");

                            Map<String, Object> updateMap = new HashMap();
                            updateMap.put("name", user.getDisplayName());

                            //Populate display name
                            displayNamesDB.collection("displayNames")
                                    .document(user.getUid())
                                    .set(updateMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("**Create User | Database Upload Display Name Status", "Display name was added to database");
                                                SignInBirthdayLayoverFragment.onUserCreated();
                                            } else {
                                                Log.d("**Create User | Database Upload Display Name Status", "Adding display name to database failed", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.d("**Create User | Database Upload User Status", "Adding user to database failed", task.getException());
                        }
                    }
                });

    }

    private static void doesUserNeedMoreTime() {

        int DELAY_LENGTH = 50;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user.getDisplayName() == null) {
                    Log.d("**Create User | User Population Check", "User needed more time");
                    doesUserNeedMoreTime();
                } else {
                    addUserToDB();
                }
            }
        }, DELAY_LENGTH);
    }

    private static void declareFirebaseVariables() {
        mAuth = FirebaseAuth.getInstance();
        usersDB = FirebaseFirestore.getInstance();
        displayNamesDB = FirebaseFirestore.getInstance();
    }
}
