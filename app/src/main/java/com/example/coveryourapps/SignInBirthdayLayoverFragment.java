package com.example.coveryourapps;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class SignInBirthdayLayoverFragment extends Fragment implements View.OnClickListener {
    private LoginActivity thisActivity;
    private GoogleSignInAccount googleAccount;
    private static Context context;// For create user to call static class onUserCreated when create user is complete

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_in_birthday_layover, container, false);

        googleAccount = GoogleSignIn.getLastSignedInAccount(Objects.requireNonNull(getContext()));
        context = this.getContext();

        thisActivity = (LoginActivity) getActivity();
        Button tempSignOut = view.findViewById(R.id.submitButton);
        tempSignOut.setOnClickListener(this);


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
        }
    }

    private void submit() {
        if (googleAccount != null) {
            // Create user through google creation
            thisActivity.refreshmAuth();
            thisActivity.refreshCurrentUser();
            Log.d("**Sign In Birthday | Submit Selection", "Chose to create account with google");
            new CreateUser.CreateUser3rdParty(getContext(), new User(thisActivity.getCurrentUser().getDisplayName(), thisActivity.getBirthday())).createUser();
        } else {
            // Create user through email and password
            Log.d("**Sign In Birthday | Submit Selection", "Chose to create account with email");
            new CreateUser.CreateUserEmail(getContext(), thisActivity.getEmail(), thisActivity.getPassword(), new User(thisActivity.getName(), thisActivity.getBirthday())).createUser();

            //  Not needed
            thisActivity.refreshmAuth();
            thisActivity.refreshCurrentUser();
        }
    }

    public static void onUserCreated() {
        Log.d("Sign In Birthday | Account Creation", "Authenticated, sent user class to db, now sending to main activity");
        Intent nextIntent = new Intent(context, MainActivity.class);
        context.startActivity(nextIntent);
    }

}