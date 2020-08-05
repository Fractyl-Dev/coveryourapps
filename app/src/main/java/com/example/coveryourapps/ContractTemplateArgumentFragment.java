package com.example.coveryourapps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContractTemplateArgumentFragment extends Fragment implements View.OnClickListener {
    private static CoverCreatorActvity thisActivity;
    private static TextView contractTemplateArgumentTitle, argumentTextView;
    private static LinearLayout argumentHolder, signatureHolder;
    private static EditText argumentResponse, electronicSignature;
    private Button continueButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisActivity = (CoverCreatorActvity) getActivity();
        View view = inflater.inflate(R.layout.fragment_contract_template_argument, container, false);
        argumentResponse = view.findViewById(R.id.argumentResponse);
        contractTemplateArgumentTitle = view.findViewById(R.id.contractTemplateArgumentTitle);
        argumentTextView = view.findViewById(R.id.argumentTextView);
        argumentHolder = view.findViewById(R.id.argumentHolder);
        electronicSignature = view.findViewById(R.id.electronicSignature);
        signatureHolder = view.findViewById(R.id.signatureHolder);
        continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);

        updateUI();


        // Inflate the layout for this fragment
        return view;
    }

    public static void updateUI() {
        int iteration = thisActivity.getContractTemplateArgumentsIteration();
        //Fill their response if they went back
        if (thisActivity.getContractTemplateArgumentResponses().size() == iteration + 1) {
            argumentResponse.setText(thisActivity.getContractTemplateArgumentResponses().get(iteration));
            thisActivity.removeLastFromResponse();
        } else {
            argumentResponse.setText("");
        }
        if (iteration != thisActivity.getContractTemplateArguments().size()) {
            argumentHolder.setVisibility(View.VISIBLE);
            signatureHolder.setVisibility(View.GONE);
            contractTemplateArgumentTitle.setText(thisActivity.getCurrentContractTemplate().getTitle());
            argumentTextView.setText(thisActivity.getContractTemplateArguments().get(iteration));
        } else {
            //closeKeyboard();
            argumentHolder.setVisibility(View.GONE);
            signatureHolder.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.continueButton) {
            if (thisActivity.getContractTemplateArgumentsIteration() < thisActivity.getContractTemplateArguments().size()) {
                thisActivity.addToContractTemplateArgumentResponses(argumentResponse.getText().toString());
                thisActivity.setContractTemplateArgumentsIteration(thisActivity.getContractTemplateArgumentsIteration() + 1);
                updateUI();
            } else {
                if (!electronicSignature.getText().toString().equals("")) {
                    createAndUploadCover(thisActivity.getCurrentContractTemplate().getTitle(), mergeTemplateAndResponses());
                } else {
                    Toast.makeText(getContext(), "Please write your electronic signature", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static String mergeTemplateAndResponses() {
        String currentTemplateText = thisActivity.getCurrentContractTemplate().getText();
        int responsesAppended = 0;

        boolean replacing = false;
        StringBuilder argument = new StringBuilder(currentTemplateText);
        StringBuilder mergedText = new StringBuilder();
        for (int j = 0; j < currentTemplateText.length(); j++) {
            char currentChar = currentTemplateText.charAt(j);
            if (currentChar == '{') {
                replacing = true;
            } else if (currentChar == '}') {
                //Add their response
                mergedText.append(thisActivity.getContractTemplateArgumentResponses().get(responsesAppended));
                responsesAppended++;
                replacing = false;
            } else if (!replacing) {
                mergedText.append(currentChar);
            }
        }
        //Log.d("**Contract Template Argument |", "Uploaded Content " + newText.toString());

        return mergedText.toString();
    }

    //Bs because you can't change things that aren't final in DB on success, but you can do it like this
    int recipientIteration;

    public void createAndUploadCover(String memo, String content) {
        recipientIteration = 0;
        for (final User recipient : thisActivity.getSelectedRecipients()) {
            // You're supposed to use Map to put data in a Firestore DB
            Map<String, Object> updateMap = new HashMap<>();
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
                            Log.d("**Contract Template Argument |", "Cover added to DB");
                            //Add Recipient to friends list
                            if (!DBHandler.getAllUserFriends().contains(recipient)) {
                                DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                        .update("friends", FieldValue.arrayUnion(recipient.getUid()));
                            }
                            recipientIteration++;
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
                    Log.d("**Contract Template Argument Fragment |", "Uploaded cover to database and DBHandler has updated, sending to home fragment");
                    Intent nextIntent = new Intent(thisActivity, MainActivity.class);
                    thisActivity.startActivity(nextIntent);
                    Toast.makeText(thisActivity, "Sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    onRefreshFinished();
                }
            }
        }, DBHandler.getRefreshDelay());
    }

    public static LinearLayout getSignatureHolder() {
        return signatureHolder;
    }

    public static void setSignatureHolder(LinearLayout signatureHolder) {
        ContractTemplateArgumentFragment.signatureHolder = signatureHolder;
    }

    public static void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) thisActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(thisActivity.getCurrentFocus()).getWindowToken(), 0);
    }
}
