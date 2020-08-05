package com.example.coveryourapps;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class WriteAContractFragment extends Fragment implements View.OnClickListener {
    private CoverCreatorActvity thisActivity;
    private Button continueButton;
    private EditText titleText, contractText, electronicSignature;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_a_contract, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();
        titleText = view.findViewById(R.id.titleText);
        contractText = view.findViewById(R.id.contractText);
        electronicSignature = view.findViewById(R.id.electronicSignature);
        continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        titleText.setText("");
        contractText.setText("");
        electronicSignature.setText("");
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.continueButton) {
            if (!titleText.getText().toString().equals("") || !contractText.getText().toString().equals("")) {
                if (!electronicSignature.getText().toString().equals("")) {
                    createAndUploadCover(titleText.getText().toString(), contractText.getText().toString());
                } else {
                    Toast.makeText(thisActivity, "Please write your electronic signature", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(thisActivity, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //Bs because you can't change things that aren't final in DB on success, but you can do it like this
    int recipientIteration;
    public void createAndUploadCover(String memo, String content) {
        recipientIteration = 0;
        for (final User recipient : thisActivity.getSelectedRecipients()) {
            // You're supposed to use Map to put data in a Firestore DB
            final Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("content", content);
            updateMap.put("coverType", "contract");
            updateMap.put("createdTime", new Timestamp(System.currentTimeMillis()));
            updateMap.put("id", "Useless ID for android");
            updateMap.put("memo", memo);
            updateMap.put("recipientID", recipient.getUid());
            updateMap.put("senderID", DBHandler.getCurrentFirebaseUser().getUid());
            updateMap.put("status", "pending");

            DBHandler.getDB().collection("covers")
                    .add(updateMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("**Write A Contract |", "Cover added to DB");
                            //Add Recipient to friends list
                            if (!DBHandler.getAllUserFriends().contains(recipient)) {
                                DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                        .update("friends", FieldValue.arrayUnion(recipient.getUid()));
                            }
                            recipientIteration ++;
                            if (recipientIteration == thisActivity.getSelectedRecipients().size()) {
                                refresh();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("**Contract Template Argument |", "Cover failed to be added to DB");
                            Toast.makeText(thisActivity, "Failed to send", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void refresh() {
        DBHandler.refreshUser(true);
        onRefreshFinished();
    }
    private void onRefreshFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DBHandler.checkIfDoneThinking()) {
                    Log.d("**WriteAContractFragment |", "Uploaded cover to database and DBHandler has updated, sending to home fragment");
                    Intent nextIntent = new Intent(thisActivity, MainActivity.class);
                    thisActivity.startActivity(nextIntent);
                    Toast.makeText(thisActivity, "Sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }
}