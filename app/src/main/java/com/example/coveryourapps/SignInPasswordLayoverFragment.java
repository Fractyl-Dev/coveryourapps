package com.example.coveryourapps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class SignInPasswordLayoverFragment extends Fragment implements View.OnClickListener {
    public SignInPasswordLayoverFragment() {
        // Required empty public constructor
    }

    LoginActivity thisActivity;

    EditText passwordEditText;
    TextView passwordTooShortErrorText, incorrectPasswordErrorText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_password_layover, container, false);

        thisActivity = (LoginActivity) getActivity();

        Button nextButton = view.findViewById(R.id.passwordNextButton);
        nextButton.setOnClickListener(this);

        passwordEditText = view.findViewById(R.id.passwordEnterText);
        passwordTooShortErrorText = view.findViewById(R.id.passwordTooShortErrorText);
        incorrectPasswordErrorText = view.findViewById(R.id.incorrectPasswordErrorText);


        // Inflate the layout for this fragment
        return view;
    }

    private void refreshDB() {
        DBHandler.refreshUser(true);
        onRefreshFinished();
    }

    private void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**SignInPasswordLayoverFragment |", "DB refreshed, sending to main activity");
                    Intent nextIntent = new Intent(getActivity(), MainActivity.class);
                    startActivity(nextIntent);
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    @Override
    public void onClick(View v) {
        if (passwordEditText.length() < 8) {
            displayErrorMessage(passwordTooShortErrorText);
        } else if (!passwordEditText.getText().toString().equals("")) {
            thisActivity.setPassword(passwordEditText.getText().toString());
            FirebaseAuth.getInstance().signInWithEmailAndPassword(thisActivity.getEmail(), thisActivity.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Log.e("** Sign In Password | ", "Sign in failed");
                        try {
                            Log.d("** Sign In Email | ", task.getException().getLocalizedMessage() + "");
                            throw Objects.requireNonNull(task.getException());
                        }
                        // if user enters wrong email (Email not in use!)
                        catch (FirebaseAuthInvalidUserException invalidEmail) {
                            Log.d("** Sign In Email | ", "Invalid Email - Email is free to take");
                            thisActivity.changeLoginLayover(thisActivity.getSignInNameLayoverFragment());
                        } catch (FirebaseAuthInvalidCredentialsException badlyFormattedEmail) {
                            if (Objects.equals(task.getException().getLocalizedMessage(), "The password is invalid or the user does not have a password.")) {
                                Log.d("** Sign In Email | ", "Wrong Password");
                                displayErrorMessage(incorrectPasswordErrorText);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        signInSuccess();
                    }
                }
            });
//            }
        }
    }

    private void displayErrorMessage(TextView displayedView) {
        passwordTooShortErrorText.setVisibility(View.GONE);
        incorrectPasswordErrorText.setVisibility(View.GONE);

        displayedView.setVisibility(View.VISIBLE);
    }

    private void signInSuccess() {
        Log.d("**Sign In Password | Email Login", "Email sign in succeeded, now sending to main activity");
        refreshDB();
    }
}