package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private HomeFragment homeFragment;
    private FriendsFragment friendsFragment;
    private Fragment settingsFragment, aboutFragment, profileInformationFragment, reviewCoverFragment;
    private String displayedFragment;
    private DrawerLayout drawerLayout;
    private NavigationView profileView;
    private LinearLayout piButtonHolder;
    private FloatingActionButton floatingActionButton;
    private TextView nameTextView, displayNameTextView;
    private MenuItem profileHome, profileFriends, profileSettings, profileAbout;
    private SwipeRefreshLayout refreshLayout;

    //Review variables
    private Cover reviewCover;
    AlertDialog dialog;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.d("Token", ""+ FirebaseInstanceId.getInstance().getToken());
        homeFragment = new HomeFragment();
        friendsFragment = new FriendsFragment();
        settingsFragment = new SettingsFragment();
        aboutFragment = new AboutFragment();
        profileInformationFragment = new ProfileInformationFragment();
        reviewCoverFragment = new ReviewCoverFragment();

        floatingActionButton = findViewById(R.id.agreementPopupMenu);


        //Top Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get rid of title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");


        //Navigation view setup to change values
        //The profile view container needs to be in normal content view as it's in activity_main.xml
        drawerLayout = findViewById(R.id.drawer_layout);
        profileView = (NavigationView) findViewById(R.id.profile_view);

        View profileHeaderView = profileView.getHeaderView(0);
        nameTextView = (TextView) profileHeaderView.findViewById(R.id.nameTextView);
        displayNameTextView = (TextView) profileHeaderView.findViewById(R.id.displayNameTextView);
        piButtonHolder = (LinearLayout) profileHeaderView.findViewById(R.id.piButtonLineHolder);
        piButtonHolder.setBackgroundColor(Color.TRANSPARENT);

        Menu profileMenu = profileView.getMenu();
        profileHome = profileMenu.findItem(R.id.profile_home);
        profileFriends = profileMenu.findItem(R.id.profile_friends);
        profileSettings = profileMenu.findItem(R.id.profile_settings);
        profileAbout = profileMenu.findItem(R.id.profile_about);

        nameTextView.setText(DBHandler.getCurrentUser().getName());
        displayNameTextView.setText(DBHandler.getCurrentUser().getDisplayName());

        //Refresh Layout
        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDB();
            }
        });
        //Display first fragment
        changeFragmentLayover(homeFragment, "homeFragment");
        profileView.setCheckedItem(R.id.profile_home);
//        automaticAppRefresh();//If the user wants their messages and stuff to update regularly

        //Listener for changing menu item, this allows method to be outside of oncreate
        profileView.setNavigationItemSelectedListener(this);
    }

    public void refreshDB() {
        DBHandler.refreshUser(true);
        refreshLayout.setRefreshing(true);
        onRefreshFinished();
    }
    public void refreshCovers() {
        DBHandler.refreshCovers();
        refreshLayout.setRefreshing(true);
        onRefreshFinished();
    }

    private void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**Main Activity |", "Handler done refreshing, updating UI");
                    homeFragment.updateCoversUI();
                    friendsFragment.updateFriendsUI();
                    refreshLayout.setRefreshing(false);
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    /*public void automaticAppRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshCoverArray();
                automaticAppRefresh();
            }
        }, 5000);
    }*/

    public void signOutTemp(View view) {
        FirebaseAuth.getInstance().signOut();//Firebase sign out

        //Get rid of notification token
        if (DBHandler.getCurrentUser().getNotificationTokens().contains(FirebaseInstanceId.getInstance().getToken())) {
            DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                    .update("notificationTokens", FieldValue.arrayRemove(FirebaseInstanceId.getInstance().getToken()));
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent nextIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(nextIntent);
                    }
                });


        // Facebook
        /*
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();
        AccessToken.setCurrentAccessToken(null);*/


    }
    public void openProfileMenu(View view) {
        drawerLayout.openDrawer(GravityCompat.END);
    }

    //Click new agreement floating button
    public void showAgreementPopupMenu(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        view = getLayoutInflater().inflate(R.layout.agreement_popup_menu_layout, null);

       // floatingActionButton.addOnHideAnimationListener();

        //Dialog builder
        builder.setView(view);
        dialog = builder.create();// Creating the dialog object that pops up
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Gravity of dialog to bottom
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = Objects.requireNonNull(window).getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        dialog.show();
    }

    public void closeAgreementPopupMenu(View view) {
        dialog.dismiss();
        floatingActionButton.show();
    }


    //This is seperate from onNavigationItemSelected method because it's not a menu item, it's in the nav header
   // public void setToProfileInformationFragment(View view) {
        //Change fragment to profile information
    //    changeFragmentLayover(profileInformationFragment, "profileInformationFragment");
        //Uncheck all other menus
    //    for (int i = 0; i < profileView.getMenu().size(); i++) {
    //        profileView.getMenu().getItem(i).setChecked(false);
     //   }

       // piButtonHolder.setBackgroundColor(getResources().getColor(R.color.menuBackgroundColor));
        //drawerLayout.closeDrawer(GravityCompat.END);
   // }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {//When something is pressed in profile menu
        switch (item.getItemId()) {
            case R.id.profile_home:
                changeFragmentLayover(homeFragment, "homeFragment", false);
                break;
            case R.id.profile_friends:
                changeFragmentLayover(friendsFragment, "friendsFragment", true);
                break;
            case R.id.profile_settings:
                changeFragmentLayover(settingsFragment, "settingsFragment", true);
                break;
            case R.id.profile_about:
                changeFragmentLayover(aboutFragment, "aboutFragment", true);
                break;
        }
        item.setChecked(true);
        piButtonHolder.setBackgroundColor(Color.TRANSPARENT);
        drawerLayout.closeDrawer(GravityCompat.END);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            //super.onBackPressed();

            goBack();
        }
    }


    public void goBack() {
        if (displayedFragment.equals("reviewCoverFragment")
             //   || displayedFragment.equals("profileInformationFragment")
                || displayedFragment.equals("friendsFragment")
                || displayedFragment.equals("settingsFragment")
                || displayedFragment.equals("aboutFragment")) {
            changeFragmentLayover(getHomeFragment(), "homeFragment", false);
            profileView.setCheckedItem(R.id.profile_home);
        }
    }


    public void agreementLendAnItemOnClick(View view) {
        Log.d("**Main Activity |", "Lend an Item On Click, going to Cover Creator");
        Intent nextIntent = new Intent(MainActivity.this, CoverCreatorActvity.class);
        nextIntent.putExtra("Selected Cover", "Lend an Item");
        startActivity(nextIntent);
        dialog.dismiss();
    }

    public void agreementCashTransactionOnClick(View view) {
        Log.d("**Main Activity |", "Cash Transaction On Click, going to Cover Creator");
        Intent nextIntent = new Intent(MainActivity.this, CoverCreatorActvity.class);
        nextIntent.putExtra("Selected Cover", "Cash Transaction");
        startActivity(nextIntent);
        dialog.dismiss();
    }

    public void agreementNewAgreementOnClick(View view) {
        Log.d("**Main Activity |", "New Agreement On Click, going to Cover Creator");
        Intent nextIntent = new Intent(MainActivity.this, CoverCreatorActvity.class);
        nextIntent.putExtra("Selected Cover", "New Agreement");
        startActivity(nextIntent);
        dialog.dismiss();
    }

    public void changeFragmentLayover(Fragment fragment, String displayedFragment) {
        this.displayedFragment = displayedFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void changeFragmentLayover(Fragment fragment, String displayedFragment, boolean hideFAB) {
        changeFragmentLayover(fragment, displayedFragment);
        if (hideFAB) {
            floatingActionButton.setVisibility(View.GONE);
        } else {
            floatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    public Cover getReviewCover() {
        return reviewCover;
    }

    public void setReviewCover(Cover reviewCover) {
        this.reviewCover = reviewCover;
    }

    public Fragment getHomeFragment() {
        return homeFragment;
    }

    public void setHomeFragment(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    public Fragment getFriendsFragment() {
        return friendsFragment;
    }

    public void setFriendsFragment(FriendsFragment friendsFragment) {
        this.friendsFragment = friendsFragment;
    }

    public Fragment getSettingsFragment() {
        return settingsFragment;
    }

    public void setSettingsFragment(Fragment settingsFragment) {
        this.settingsFragment = settingsFragment;
    }

    public Fragment getAboutFragment() {
        return aboutFragment;
    }

    public void setAboutFragment(Fragment aboutFragment) {
        this.aboutFragment = aboutFragment;
    }

    //public Fragment getProfileInformationFragment() {
       // return profileInformationFragment;
    //}

    //public void setProfileInformationFragment(Fragment profileInformationFragment) {
    //    this.profileInformationFragment = profileInformationFragment;
    //}

    public Fragment getReviewCoverFragment() {
        return reviewCoverFragment;
    }

    public void setReviewCoverFragment(Fragment reviewCoverFragment) {
        this.reviewCoverFragment = reviewCoverFragment;
    }
}