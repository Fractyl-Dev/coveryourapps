package com.example.coveryourapps;

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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.w3c.dom.Text;

import java.util.Objects;

public class SignInEmailLayoverFragment extends Fragment implements View.OnClickListener {
    public SignInEmailLayoverFragment() {
        // Required empty public constructor
    }

    LoginActivity thisActivity;

    EditText emailEditText;
    TextView emailNotEnteredErrorText, emailNotValidErrorText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_email_layover, container, false);

        thisActivity = (LoginActivity) getActivity();

        Button nextButton = view.findViewById(R.id.emailNextButton);
        nextButton.setOnClickListener(this);

        emailEditText = view.findViewById(R.id.emailEnterText);
        emailNotEnteredErrorText = view.findViewById(R.id.emailNotEnteredErrorText);
        emailNotValidErrorText = view.findViewById(R.id.emailNotValidErrorText);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        // Check email by trying to sign in with an impossible password
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(), "test").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    try {
                        Log.d("** Sign In Email | ", task.getException().getLocalizedMessage() + "");
                        throw Objects.requireNonNull(task.getException());
                    }
                    // if user enters wrong email (Email not in use!)
                    catch (FirebaseAuthInvalidUserException invalidEmail) {
                        Log.d("** Sign In Email | ", "Invalid Email - Email is free to take");
                        normalErrorsCheck();
                    } catch (FirebaseAuthInvalidCredentialsException badlyFormattedEmail) {
                        if (Objects.equals(task.getException().getLocalizedMessage(), "The password is invalid or the user does not have a password.")) {
                            Log.d("** Sign In Email | ", "Wrong Password - Email is taken");
                            normalErrorsCheck();
                        } else {
                            Log.d("** Sign In Email | ", "Email Badly Formatted");
                            displayErrorMessage(emailNotValidErrorText);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void displayErrorMessage(TextView displayedView) {
        emailNotValidErrorText.setVisibility(View.GONE);
        emailNotEnteredErrorText.setVisibility(View.GONE);

        displayedView.setVisibility(View.VISIBLE);
    }

    private void normalErrorsCheck() {
        if (!emailEditText.getText().toString().contains("@") || emailEditText.getText().length() < 3) {
            displayErrorMessage(emailNotValidErrorText);
        } else if (!emailEditText.getText().toString().equals("")) {
            thisActivity.setEmail(emailEditText.getText().toString());
            thisActivity.changeLoginLayover(thisActivity.getSignInPasswordLayoverFragment());
        } else {
            displayErrorMessage(emailNotEnteredErrorText);
        }
    }
}