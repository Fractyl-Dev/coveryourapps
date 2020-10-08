package com.example.coveryourapps;

//import android.support.v4.fr

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    MainActivity thisActivity;
    Button signOutButton;
    CheckBox autoAddFriendCheck;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        thisActivity = (MainActivity) getActivity();
        signOutButton = v.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        autoAddFriendCheck = v.findViewById(R.id.autoAddFriendCheck);
        autoAddFriendCheck.setChecked(Settings.isAutoAddFriends());
        autoAddFriendCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
                prefs.edit().putBoolean("autoAddFriends", isChecked).apply();
                Settings.setAutoAddFriends(thisActivity);
                Log.d("**Settings Fragment", "Auto Add Friends set to " + Settings.isAutoAddFriends());
            }
        });


        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signOutButton:
                signOut();
                break;
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();//Firebase sign out

        //Get rid of notification token
        if (DBHandler.getCurrentUser().getNotificationTokens().contains(FirebaseInstanceId.getInstance().getToken())) {
            DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                    .update("notificationTokens", FieldValue.arrayRemove(FirebaseInstanceId.getInstance().getToken()));
        }

        // Google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(thisActivity, gso);

        //Kick out to main activity
        mGoogleSignInClient.signOut().addOnCompleteListener(thisActivity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent nextIntent = new Intent(thisActivity, LoginActivity.class);
                        startActivity(nextIntent);
                    }
                })
                .addOnFailureListener(thisActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("**Settings Fragment", "Failed to logout", e);
                        Toast.makeText(thisActivity, "Logout failed, please restart the app and try again.", Toast.LENGTH_SHORT).show();
                    }
                });


        // Facebook
        /*
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();
        AccessToken.setCurrentAccessToken(null);*/


    }

}
