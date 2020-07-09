package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

/*import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;*/
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView profileView;
    private LinearLayout piButtonHolder;
    private TextView nameTextView, displayNameTextView;
    private MenuItem profileHome, profileFriends, profileSettings, profileAbout;


    private ArrayList<Cover> allUserCovers;
    private FirebaseFirestore DB;
    private User currentUser;
    private FirebaseAuth mAuth;
    private FirebaseUser currentFirebaseUser;


    AlertDialog dialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        //Setting up database and current user
        mAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = mAuth.getCurrentUser();

        allUserCovers = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        DB.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("**Main Activity | ", "Users retrieved from usersDB");
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                if (documentSnapshot != null) {
                                    //Log.d("Main Activity | ", documentSnapshot.getId() + " -> " + documentSnapshot.getData());
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user.getUid().equals(currentFirebaseUser.getUid())) {
                                        currentUser = user;
                                        Log.d("**Main Activity | ", "Current user found from usersDB: " + user.toString() + currentUser.toString());
                                        Log.d("**Main Activity | ", "Current user getters " + currentUser.getName() + " " + currentUser.getDisplayName());

                                        nameTextView.setText(currentUser.getName());
                                        displayNameTextView.setText(currentUser.getDisplayName());

                                        //Store cover objects in an array from user CoversID string array
                                        DB.collection("covers")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (DocumentSnapshot coversSnapshot : task.getResult()) {
                                                                if (coversSnapshot != null) {
                                                                    Cover cover = coversSnapshot.toObject(Cover.class);
                                                                    //Temp to add covers while I can't actually add them yet. When deleted covers will be uploaded to database
                                                                    currentUser.addCover(coversSnapshot.getId());

                                                                    if (getCurrentUser().getCovers().contains(coversSnapshot.getId())){
                                                                        Log.d("**Main Activity | ", "User cover array contained same id "+coversSnapshot.getId()+ " "+cover.getStatus());
                                                                        allUserCovers.add(cover);
                                                                    }
                                                                }
                                                            }

                                                            //Fragment displayed on launch, done here so it's only changed when on complete listeners are done
                                                            if (getSavedInstanceState(savedInstanceState) == null) {
                                                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                                        new HomeFragment()).commit();
                                                                profileView.setCheckedItem(R.id.profile_home);
                                                            }
                                                        }
                                                    }
                                                });

                                    }
                                } else {
                                    Log.d("**Main Activity | ", "Document Snapshot returned null");
                                }
                            }
                        } else {
                            Log.w("**Main Activity | ", "Error retrieving from usersDB", task.getException());
                        }
                    }
                });


        //Listener for changing menu item, this allows method to be outside of oncreate
        profileView.setNavigationItemSelectedListener(this);
    }

    //Stupid bc savedInstanceState above needs to be final unless you do this
    public Bundle getSavedInstanceState(Bundle savedInstanceState) {
        return savedInstanceState;
    }

    public void signOutTemp(View view) {
        FirebaseAuth.getInstance().signOut();//Firebase sign out


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
    }


    //This is seperate from onNavigationItemSelected method because it's not a menu item, it's in the nav header
    public void setToProfileInformationFragment(View view) {
        //Change fragment to profile information
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ProfileInformationFragment()).commit();
        //Uncheck all other menus
        for (int i = 0; i < profileView.getMenu().size(); i++) {
            profileView.getMenu().getItem(i).setChecked(false);
        }

        piButtonHolder.setBackgroundColor(getResources().getColor(R.color.menuBackgroundColor));
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {//When something is pressed in profile menu
        switch (item.getItemId()) {
            case R.id.profile_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.profile_friends:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FriendsFragment()).commit();
                break;
            case R.id.profile_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;
            case R.id.profile_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AboutFragment()).commit();
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
            super.onBackPressed();
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


    public FirebaseFirestore getDB() {
        return DB;
    }

    public void setDB(FirebaseFirestore DB) {
        this.DB = DB;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return currentFirebaseUser;
    }

    public void setCurrentFirebaseUser(FirebaseUser currentFirebaseUser) {
        this.currentFirebaseUser = currentFirebaseUser;
    }

    public ArrayList<Cover> getAllUserCovers() {
        return allUserCovers;
    }

    public void setAllUserCovers(ArrayList<Cover> allUserCovers) {
        this.allUserCovers = allUserCovers;
    }
}