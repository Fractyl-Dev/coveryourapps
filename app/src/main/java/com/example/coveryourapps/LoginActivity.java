package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private Fragment signInLayoverFragment, signInNameLayoverFragment, signInEmailLayoverFragment, signInPasswordLayoverFragment, signInBirthdayLayoverFragment;
    private String name, email, password, birthday;
    private boolean signingInWithEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInLayoverFragment = new SignInLayoverFragment();
        signInNameLayoverFragment = new SignInNameLayoverFragment();
        signInEmailLayoverFragment = new SignInEmailLayoverFragment();
        signInPasswordLayoverFragment = new SignInPasswordLayoverFragment();
        signInBirthdayLayoverFragment = new SignInBirthdayLayoverFragment();

        signingInWithEmail = false;

        // Did they sign in with google and then quit app before submitting a birthday? If so, send to birthday to finish account creation.
        if (DBHandler.isGoogleQuitDuringAccountCreation()) {
            changeLoginLayover(signInBirthdayLayoverFragment);
        } else {
            changeLoginLayover(signInLayoverFragment);
        }
    }


    public void changeLoginLayover(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.loginLayover, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    public boolean isSigningInWithEmail() {
        return signingInWithEmail;
    }

    public void setSigningInWithEmail(boolean signingInWithEmail) {
        this.signingInWithEmail = signingInWithEmail;
    }

    public Fragment getSignInLayoverFragment() {
        return signInLayoverFragment;
    }

    public void setSignInLayoverFragment(Fragment signInLayoverFragment) {
        this.signInLayoverFragment = signInLayoverFragment;
    }

    public Fragment getSignInNameLayoverFragment() {
        return signInNameLayoverFragment;
    }

    public void setSignInNameLayoverFragment(Fragment signInNameLayoverFragment) {
        this.signInNameLayoverFragment = signInNameLayoverFragment;
    }

    public Fragment getSignInEmailLayoverFragment() {
        return signInEmailLayoverFragment;
    }

    public void setSignInEmailLayoverFragment(Fragment signInEmailLayoverFragment) {
        this.signInEmailLayoverFragment = signInEmailLayoverFragment;
    }

    public Fragment getSignInPasswordLayoverFragment() {
        return signInPasswordLayoverFragment;
    }

    public void setSignInPasswordLayoverFragment(Fragment signInPasswordLayoverFragment) {
        this.signInPasswordLayoverFragment = signInPasswordLayoverFragment;
    }

    public Fragment getSignInBirthdayLayoverFragment() {
        return signInBirthdayLayoverFragment;
    }

    public void setSignInBirthdayLayoverFragment(Fragment signInBirthdayLayoverFragment) {
        this.signInBirthdayLayoverFragment = signInBirthdayLayoverFragment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}