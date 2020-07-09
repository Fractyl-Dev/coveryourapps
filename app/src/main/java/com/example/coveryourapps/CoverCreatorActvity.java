package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class CoverCreatorActvity extends AppCompatActivity implements View.OnClickListener {

    private Fragment chooseRecipientsFragment, chooseContractFragment, contractTemplateOverviewFragment, contractTemplateArgumentFragment;
    private String displayedFragment;
    private String selectedCover;
    private ArrayList<String> selectedRecipients;

    private ImageButton toolbarBackButton;
    private TextView toolbarTopText;
    private Button toolbarNextButton;

    private int contractTemplateArgumentsIteration;
    private ArrayList<String> contractTemplateArguments;
    private ArrayList<String> contractTemplateArgumentResponses;
    private ArrayList<ContractTemplate> contractTemplates;
    private ContractTemplate currentContractTemplate;

    private FirebaseFirestore DB;
    private User currentUser;
    private ArrayList<User> allUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_creator_actvity);

        selectedCover = getIntent().getStringExtra("Selected Cover");
        Log.d("**Cover Creator Activity |", "Agreement Type: " + selectedCover);
        chooseRecipientsFragment = new ChooseRecipientsFragment();
        chooseContractFragment = new ChooseContractFragment();
        contractTemplateOverviewFragment = new ContractTemplateOverviewFragment();
        contractTemplateArgumentFragment = new ContractTemplateArgumentFragment();

        selectedRecipients = new ArrayList<>();

        //Top Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get rid of title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        toolbarBackButton = findViewById(R.id.toolbarBackButton);
        toolbarNextButton = findViewById(R.id.toolbarNextButton);
        toolbarTopText = findViewById(R.id.toolbarTopText);
        toolbarBackButton.setOnClickListener(this);
        toolbarNextButton.setOnClickListener(this);


        //Setting up database and current user
        mAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = mAuth.getCurrentUser();

        allUsers = new ArrayList<>();
        DB = FirebaseFirestore.getInstance();
        DB.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("**Cover Creator Activity | ", "Users retrieved from usersDB");
                            for (DocumentSnapshot usersSnapshot : task.getResult()) {
                                if (usersSnapshot != null) {
                                    //Log.d("Main Activity | ", documentSnapshot.getId() + " -> " + documentSnapshot.getData());
                                    User user = usersSnapshot.toObject(User.class);
                                    allUsers.add(user);
                                    if (user.getUid().equals(currentFirebaseUser.getUid())) {
                                        currentUser = user;
                                        Log.d("**Cover Creator Activity | ", "Current user found from usersDB: " + user.toString() + currentUser.toString());
                                        Log.d("**Cover Creator Activity | ", "Current user getters " + currentUser.getName() + " " + currentUser.getDisplayName());


                                        //Change fragment to choose recipients fragment; done in here to prevent
                                        //crashing due to function not being complete and returning null
                                        changeCoverCreatorLayover(chooseRecipientsFragment, "chooseRecipientsFragment");
                                    }
                                } else {
                                    Log.w("**Cover Creator Activity | ", "Users Snapshot returned null");
                                }
                            }
                        } else {
                            Log.w("**Cover Creator Activity | ", "Error retrieving from DB", task.getException());
                        }
                    }
                });

        contractTemplates = new ArrayList<>();
        contractTemplateArguments = new ArrayList<>();
        contractTemplateArgumentResponses = new ArrayList<>();
        contractTemplateArgumentsIteration = 0;
        DB.collection("contractTemplates")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("**Cover Creator Activity | ", "Contract templates retrieved from usersDB");
                            for (DocumentSnapshot templatesSnapshot : task.getResult()) {
                                if (templatesSnapshot != null) {
                                    ContractTemplate contractTemplate = templatesSnapshot.toObject(ContractTemplate.class);
                                    contractTemplates.add(contractTemplate);
                                }
                            }
                        } else {
                            Log.w("**Cover Creator Activity | ", "Contract template retrieval task not successful");
                        }
                    }
                });
    }

    //fragmentDescription is an optional string you can use to figure out what fragments are being used
    public void changeCoverCreatorLayover(Fragment fragment, String displayedFragment) {
        this.displayedFragment = displayedFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.coverCreatorLayover, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }


    public Fragment getChooseRecipientsFragment() {
        return chooseRecipientsFragment;
    }

    public void setChooseRecipientsFragment(Fragment chooseRecipientsFragment) {
        this.chooseRecipientsFragment = chooseRecipientsFragment;
    }

    public Fragment getContractTemplateOverviewFragment() {
        return contractTemplateOverviewFragment;
    }

    public void setContractTemplateOverviewFragment(Fragment contractTemplateOverviewFragment) {
        this.contractTemplateOverviewFragment = contractTemplateOverviewFragment;
    }

    public Fragment getContractTemplateArgumentFragment() {
        return contractTemplateArgumentFragment;
    }

    public void setContractTemplateArgumentFragment(Fragment contractTemplateArgumentFragment) {
        this.contractTemplateArgumentFragment = contractTemplateArgumentFragment;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public void refreshmAuth() {
        setmAuth(FirebaseAuth.getInstance());
    }

    public void signOutmAuth() {
        this.mAuth.signOut();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return currentFirebaseUser;
    }

    public void setCurrentFirebaseUser(FirebaseUser currentFirebaseUser) {
        this.currentFirebaseUser = currentFirebaseUser;
    }

    public void refreshCurrentFirebaseUser() {
        setCurrentFirebaseUser(getmAuth().getCurrentUser());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public ArrayList<User> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(ArrayList<User> allUsers) {
        this.allUsers = allUsers;
    }

    public void addToAllUsers(User user) {
        allUsers.add(user);
    }

    public ArrayList<String> getSelectedRecipients() {
        return selectedRecipients;
    }

    public void setSelectedRecipients(ArrayList<String> selectedRecipients) {
        this.selectedRecipients = selectedRecipients;
    }

    public void clearSelectedRecipients() {
        selectedRecipients.clear();
    }

    public void addToSelectedRecipients(String uid) {
        selectedRecipients.add(uid);
    }

    public ArrayList<ContractTemplate> getContractTemplates() {
        return contractTemplates;
    }

    public void setContractTemplates(ArrayList<ContractTemplate> contractTemplates) {
        this.contractTemplates = contractTemplates;
    }

    public ArrayList<String> getContractTemplateArguments() {
        return contractTemplateArguments;
    }

    public void setContractTemplateArguments(ArrayList<String> contractTemplateArguments) {
        this.contractTemplateArguments = contractTemplateArguments;
    }

    public void addToContractTemplateArguments(String argument) {
        contractTemplateArguments.add(argument);
    }

    public int getContractTemplateArgumentsIteration() {
        return contractTemplateArgumentsIteration;
    }

    public void setContractTemplateArgumentsIteration(int contractTemplateArgumentsIteration) {
        this.contractTemplateArgumentsIteration = contractTemplateArgumentsIteration;
    }

    public ArrayList<String> getContractTemplateArgumentResponses() {
        return contractTemplateArgumentResponses;
    }

    public void setContractTemplateArgumentResponses(ArrayList<String> contractTemplateArgumentResponses) {
        this.contractTemplateArgumentResponses = contractTemplateArgumentResponses;
    }

    public void addToContractTemplateArgumentResponses(String argumentResponse) {
        contractTemplateArgumentResponses.add(argumentResponse);
    }

    public ContractTemplate getCurrentContractTemplate() {
        return currentContractTemplate;
    }

    public void setCurrentContractTemplate(ContractTemplate currentContractTemplate) {
        this.currentContractTemplate = currentContractTemplate;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarBackButton:
                goBack();
                break;
            case R.id.toolbarNextButton:
                if (displayedFragment.equals("chooseRecipientsFragment")) {
                    if (selectedCover.equals("New Agreement")) {
                        if (selectedRecipients.size() > 0) {
                            toolbarTopText.setText(getString(R.string.choose_contract));
                            toolbarBackButton.setImageResource(R.drawable.back_arrow);
                            toolbarNextButton.setVisibility(View.GONE);
                            changeCoverCreatorLayover(chooseContractFragment, "chooseContractFragment");
                        } else {
                            Toast.makeText(this, "Please select at least one recipient", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    //Method needed because you can go back in 2 ways (phone back button)
    public void goBack() {
        if (displayedFragment.equals("chooseRecipientsFragment")) {
            Log.d("**Cover Creator Activity | ", "ChooseRecipientsFragment not null");

            Intent nextIntent = new Intent(CoverCreatorActvity.this, MainActivity.class);
            startActivity(nextIntent);
        } else if (displayedFragment.equals("chooseContractFragment")) {
            toolbarTopText.setText(getString(R.string.add_people));
            toolbarBackButton.setImageResource(R.drawable.close_icon);
            toolbarNextButton.setVisibility(View.VISIBLE);
            changeCoverCreatorLayover(chooseRecipientsFragment, "chooseRecipientsFragment");
        } else if (displayedFragment.equals("contractTemplateOverviewFragment")) {
            changeCoverCreatorLayover(chooseContractFragment, "chooseContractFragment");
        }
    }

    //Override phone back button
    @Override
    public void onBackPressed() {
        goBack();
        //moveTaskToBack(true);
    }
}