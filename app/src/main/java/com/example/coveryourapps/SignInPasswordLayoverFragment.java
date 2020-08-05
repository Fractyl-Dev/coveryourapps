package com.example.coveryourapps;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class SignInPasswordLayoverFragment extends Fragment implements View.OnClickListener {
    public SignInPasswordLayoverFragment() {
        // Required empty public constructor
    }

    LoginActivity thisActivity;

    EditText passwordEditText;
    TextView passwordTooShortErrorText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_password_layover, container, false);

        thisActivity = (LoginActivity) getActivity();

        Button nextButton = view.findViewById(R.id.passwordNextButton);
        nextButton.setOnClickListener(this);

        passwordEditText = view.findViewById(R.id.passwordEnterText);
        passwordTooShortErrorText = view.findViewById(R.id.passwordTooShortErrorText);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        if (passwordEditText.length() < 8) {
            passwordTooShortErrorText.setVisibility(View.VISIBLE);
        } else if (!passwordEditText.getText().toString().equals("")) {
            thisActivity.setPassword(passwordEditText.getText().toString());
            if (!thisActivity.isSigningInWithEmail()) {
                thisActivity.changeLoginLayover(thisActivity.getSignInBirthdayLayoverFragment());
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(thisActivity.getEmail(), thisActivity.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("** Sign In Password | ", "Sign in failed");

                        } else {
                            Log.d("**Sign In Password | Email Login", "Email sign in succeeded, now sending to main activity");
                            Intent nextIntent = new Intent(getActivity(), MainActivity.class);
                            startActivity(nextIntent);
                        }
                    }
                });
            }
        }
    }
}