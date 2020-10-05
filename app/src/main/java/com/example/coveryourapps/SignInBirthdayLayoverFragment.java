package com.example.coveryourapps;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;

public class SignInBirthdayLayoverFragment extends Fragment implements View.OnClickListener {
    private static LoginActivity thisActivity;
    private GoogleSignInAccount googleAccount;

    private TextView signUpQuitEarlyTextView;
    private Button submitButton, signUpQuitEarlyButton;
    private static Context context;// For create user to call static class onUserCreated when create user is complete

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in_birthday_layover, container, false);

        googleAccount = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getContext()));
        context = this.getContext();

        thisActivity = (LoginActivity) getActivity();

        signUpQuitEarlyTextView = view.findViewById(R.id.signUpQuitEarlyTextView);
        submitButton = view.findViewById(R.id.submitButton);
        signUpQuitEarlyButton = view.findViewById(R.id.signUpQuitEarlyButton);
        submitButton.setOnClickListener(this);
        signUpQuitEarlyButton.setOnClickListener(this);

        if (DBHandler.isGoogleQuitDuringAccountCreation()) {
            signUpQuitEarlyTextView.setVisibility(View.VISIBLE);
            signUpQuitEarlyButton.setVisibility(View.VISIBLE);
        }


        // Calendar
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Calendar c = Calendar.getInstance();

        c.add(Calendar.YEAR, -13);
        datePicker.setMaxDate(c.getTimeInMillis());

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String todate = dateFormat.format(currentdate());


        thisActivity.setBirthday(todate.toString()); //here you get current date

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                monthOfYear++;
                thisActivity.setBirthday("" + lessThan10Fix(monthOfYear) + lessThan10Fix(dayOfMonth) + year);
            }
        });


        return view;
    }

    private static void refreshDB() {
        DBHandler.refreshUser(true);
        onRefreshFinished();
    }

    private static void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**SignInBirthdayLayoverFragment |", "Birthday updated, sending to main activity");
                    Intent nextIntent = new Intent(thisActivity, MainActivity.class);
                    thisActivity.startActivity(nextIntent);
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    private String lessThan10Fix(int input) {
        String output;
        if (input < 10) {
            output = "0" + input;
        } else {
            output = "" + input;
        }
        return output;
    }

    private Date currentdate() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        return cal.getTime();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitButton:
                submit();
                break;
            case R.id.signUpQuitEarlyButton:
                signOut();
                break;
        }
    }

    private void signOut() {
        DBHandler.setGoogleQuitDuringAccountCreation(false);

        FirebaseAuth.getInstance().signOut();//Firebase sign out

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(thisActivity, gso);

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(thisActivity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        thisActivity.changeLoginLayover(thisActivity.getSignInLayoverFragment());
                    }
                });


        // Facebook
        /*
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();
        AccessToken.setCurrentAccessToken(null);*/

    }

    private void submit() {
        if (googleAccount != null) {
            // Create user through google creation
            Log.d("**Sign In Birthday | Submit Selection", "Chose to create account with google");
            new CreateUser.CreateUser3rdParty(getContext(), new User(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), thisActivity.getBirthday())).createUser();
        } else {
            // Create user through email and password
            Log.d("**Sign In Birthday | Submit Selection", "Chose to create account with email");
            new CreateUser.CreateUserEmail(getContext(), thisActivity.getEmail(), thisActivity.getPassword(), new User(thisActivity.getName(), thisActivity.getBirthday())).createUser();
        }
    }

    public static void onUserCreated() {
        //Called from Create User onComplete()
        Log.d("Sign In Birthday | Account Creation", "Authenticated, sent user class to db, now sending to main activity");
//        Intent nextIntent = new Intent(context, MainActivity.class);
//        context.startActivity(nextIntent);
        refreshDB();
    }

}