package com.example.coveryourapps;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.Executor;

public class SignInLayoverFragment extends Fragment implements View.OnClickListener {

    LoginActivity thisActivity;
    private FirebaseAuth mAuth;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private static int RC_GOOGLE_SIGN_IN = 0;//Can be anything

    // Facebook Sign In
    CallbackManager fbCallbackManager = CallbackManager.Factory.create();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_layover, container, false);

        thisActivity = (LoginActivity) getActivity();

        // Declare layout objects
        Button tempSignOut = view.findViewById(R.id.tempLogoutButton);
        tempSignOut.setOnClickListener(this);
        Button createAccountButton = view.findViewById(R.id.createAccount);
        createAccountButton.setOnClickListener(this);
        Button emailSignInButton = view.findViewById(R.id.emailSignInButton);
        emailSignInButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // Google Sign In

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = thisActivity.getmAuth();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

        Objects.requireNonNull(getView()).findViewById(R.id.googleSignInButton).setOnClickListener(this);


//        // FB Sign In
//        LoginButton loginButton = getView().findViewById(R.id.fb_login_button);
//        loginButton.setReadPermissions("email");
//
//
//        // Callback registration
//        loginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d("Sign In Facebook", "facebook:onSuccess:" + loginResult);
//                handleFacebookAccessToken(loginResult.getAccessToken());
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d("Sign In Facebook", "facebook:onCancel");
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                Log.d("Sign In Facebook", "facebook:onError " );
//            }
//        });
//
//
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "{com.example.coveryourapps}",                  //Insert your own package name.
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.d("KeyHash:", "notFound");
//
//        } catch (NoSuchAlgorithmException e) {
//            Log.d("KeyHash:", "no algorithm");
//        }
//
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("**Sign In Layover | Sign In Google", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("**Sign In Layover | Sign In Google", "Google sign in failed", e);

            }
        } else {
            // Pass the activity result back to the Facebook SDK
            //fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("**Sign In Layover | Firebase Google Auth", "signInWithCredential:success");

                            if (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser()) {
                                Log.d("**Sign In Layover | Determine if new user", "Is a new user");
                                thisActivity.changeLoginLayover(thisActivity.getSignInBirthdayLayoverFragment());
                            } else {
                                Log.d("**Sign In Layover | Determine if new user", "Not a new user");
                                Intent nextIntent = new Intent(getActivity(), MainActivity.class);
                                startActivity(nextIntent);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("**Sign In Layover | Firebase Auth", "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("**Sign In Facebook", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("Sign In Facebook", "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
//                            startActivity(mainIntent);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("Sign In Facebook", "signInWithCredential:failure", task.getException());
//                        }
//
//                        // ...
//                    }
//                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googleSignInButton:
                googleSignIn();
                break;
            case R.id.tempLogoutButton:
                signOutTemp(v);
                break;
            case R.id.emailSignInButton:
                emailSignIn();
                break;
            case R.id.createAccount:
                createAccount();
                break;
        }
    }

    private void emailSignIn() {
        thisActivity.setSigningInWithEmail(true);
        thisActivity.changeLoginLayover(thisActivity.getSignInEmailLayoverFragment());
    }

    private void createAccount() {
        // Sign out of everything  because they're going in with email
        thisActivity.signOutmAuth();//Firebase sign out

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);
        // Google sign out
        mGoogleSignInClient.signOut();


        // Change fragment
        thisActivity.changeLoginLayover(thisActivity.getSignInNameLayoverFragment());
    }


    public void signOutTemp(View view) {
        thisActivity.signOutmAuth();//Firebase sign out

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gso);

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        thisActivity.changeLoginLayover(thisActivity.getSignInLayoverFragment());
                    }
                });


        // Facebook

//        FacebookSdk.sdkInitialize(getApplicationContext());
//        LoginManager.getInstance().logOut();
//        AccessToken.setCurrentAccessToken(null);

    }
}