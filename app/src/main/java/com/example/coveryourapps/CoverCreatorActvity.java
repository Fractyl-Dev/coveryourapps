package com.example.coveryourapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
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

    private Fragment cashTransactionFragment, contractTemplateOverviewFragment, contractTemplateArgumentFragment, writeAContractFragment;
    private ChooseRecipientsFragment chooseRecipientsFragment;
    private ChooseContractFragment chooseContractFragment;
    private String displayedFragment;
    private String selectedCover;

    private ImageButton toolbarBackButton;
    private TextView toolbarTopText;
    private Button toolbarNextButton;
    private SwipeRefreshLayout swipeRefreshLayout;


    private ArrayList<User> selectedRecipients = new ArrayList<>();

    //Template variables
    private int contractTemplateArgumentsIteration;
    private ArrayList<String> contractTemplateArguments;
    private ArrayList<String> contractTemplateArgumentResponses;
    private ContractTemplate currentContractTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_creator_actvity);

        selectedCover = getIntent().getStringExtra("Selected Cover");
        Log.d("**Cover Creator Activity |", "Agreement Type: " + selectedCover);
        chooseRecipientsFragment = new ChooseRecipientsFragment();
        cashTransactionFragment = new CashTransactionFragment();
        chooseContractFragment = new ChooseContractFragment();
        contractTemplateOverviewFragment = new ContractTemplateOverviewFragment();
        contractTemplateArgumentFragment = new ContractTemplateArgumentFragment();
        writeAContractFragment = new WriteAContractFragment();

        selectedRecipients = new ArrayList<>();

        //Top Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Swipe Refresh Layout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //Get rid of title
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        toolbarBackButton = findViewById(R.id.toolbarBackButton);
        toolbarNextButton = findViewById(R.id.toolbarNextButton);
        toolbarTopText = findViewById(R.id.toolbarTopText);
        toolbarBackButton.setOnClickListener(this);
        toolbarNextButton.setOnClickListener(this);


        contractTemplateArguments = new ArrayList<>();
        contractTemplateArgumentResponses = new ArrayList<>();
        contractTemplateArgumentsIteration = 0;
        changeCoverCreatorLayover(chooseRecipientsFragment, "chooseRecipientsFragment");
    }
    private void refresh() {
        //Needs to call refresh user to get a refreshed list of friends
        DBHandler.refreshUser(false);
        DBHandler.refreshContractTemplates();
        swipeRefreshLayout.setRefreshing(true);
        onRefreshFinished();
    }
    private void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**Cover Creator Activity |", "Handler done thinking, updating friends list");
                    chooseRecipientsFragment.updateFriendsUI();
                    chooseContractFragment.updateContractTemplatesUI();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
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

    public void setChooseRecipientsFragment(ChooseRecipientsFragment chooseRecipientsFragment) {
        this.chooseRecipientsFragment = chooseRecipientsFragment;
    }

    public Fragment getContractTemplateOverviewFragment() {
        return contractTemplateOverviewFragment;
    }

    public void setContractTemplateOverviewFragment(Fragment contractTemplateOverviewFragment) {
        this.contractTemplateOverviewFragment = contractTemplateOverviewFragment;
    }

    public Fragment getContractTemplateArgumentFragment() {
        //Doesn't matter if you save anything so just make it new
        return new ContractTemplateArgumentFragment();
    }

    public void setContractTemplateArgumentFragment(Fragment contractTemplateArgumentFragment) {
        this.contractTemplateArgumentFragment = contractTemplateArgumentFragment;
    }

    public Fragment getWriteAContractFragment() {
        return writeAContractFragment;
    }

    public void setWriteAContractFragment(Fragment writeAContractFragment) {
        this.writeAContractFragment = writeAContractFragment;
    }

    public ArrayList<User> getSelectedRecipients() {
        return selectedRecipients;
    }

    public void clearSelectedRecipients() {
        selectedRecipients.clear();
    }

    public void addToSelectedRecipients(User user) {
        selectedRecipients.add(user);
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

    public void setToolbarTopText(String toolbarTopText) {
        this.toolbarTopText.setText(toolbarTopText);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarBackButton:
                goBack();
                break;
            case R.id.toolbarNextButton:
                if (displayedFragment.equals("chooseRecipientsFragment")) {
                    if (selectedRecipients.size() > 0) {
                        if (selectedCover.equals("New Agreement")) {
                            toolbarTopText.setText(getString(R.string.choose_contract));
                            toolbarBackButton.setImageResource(R.drawable.back_arrow);
                            toolbarNextButton.setVisibility(View.GONE);
                            changeCoverCreatorLayover(chooseContractFragment, "chooseContractFragment");
                        } else if (selectedCover.equals("Cash Transaction")) {
                            toolbarTopText.setText(R.string.cash_transaction);
                            toolbarBackButton.setImageResource(R.drawable.back_arrow);
                            toolbarNextButton.setVisibility(View.GONE);
                            changeCoverCreatorLayover(cashTransactionFragment, "cashTransactionFragment");
                        }
                    } else {
                        Toast.makeText(this, "Please select at least one recipient", Toast.LENGTH_SHORT).show();
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
        } else if (displayedFragment.equals("chooseContractFragment") || displayedFragment.equals("cashTransactionFragment")) {
            toolbarTopText.setText(getString(R.string.add_people));
            toolbarBackButton.setImageResource(R.drawable.close_icon);
            toolbarNextButton.setVisibility(View.VISIBLE);
            changeCoverCreatorLayover(chooseRecipientsFragment, "chooseRecipientsFragment");
        } else if (displayedFragment.equals("contractTemplateOverviewFragment")) {
            contractTemplateArgumentResponses.clear();
            contractTemplateArguments.clear();
            changeCoverCreatorLayover(chooseContractFragment, "chooseContractFragment");
        } else if (displayedFragment.equals("contractTemplateArgumentFragment")) {
            if (contractTemplateArgumentsIteration != 0) {
                contractTemplateArgumentsIteration--;
                ContractTemplateArgumentFragment.updateUI();//Written this way because updateUI is static
                //Response isn't removed here, it's removed in updateUI so it can populate it with what you said
//                if (ContractTemplateArgumentFragment.getSignatureHolder().getVisibility() != View.VISIBLE) {
//
//                } else {
//                    Log.d("**Cover Creator |", "sig visible");
//                }
            } else {
                changeCoverCreatorLayover(contractTemplateOverviewFragment, "contractTemplateOverviewFragment");
            }
            Log.d("**Cover Creator |", "Update :" + getContractTemplateArgumentResponses().toString());
            Log.d("**Cover Creator |", "Update :" + getContractTemplateArguments().toString());
        } else if (displayedFragment.equals("writeAContractFragment")) {
            changeCoverCreatorLayover(chooseContractFragment, "chooseContractFragment");
            toolbarTopText.setText(R.string.choose_contract);
        }
    }

    public void removeLastFromResponse() {
        contractTemplateArgumentResponses.remove(contractTemplateArgumentResponses.size() - 1);
    }

    //Override phone back button
    @Override
    public void onBackPressed() {
        goBack();
        //moveTaskToBack(true);
    }
}