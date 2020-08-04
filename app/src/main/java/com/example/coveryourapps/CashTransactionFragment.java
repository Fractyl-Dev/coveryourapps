package com.example.coveryourapps;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class CashTransactionFragment extends Fragment implements View.OnClickListener {
    private CoverCreatorActvity thisActivity;
    private EditText cashAmountEditText, memoEditText;
    private Button continueButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_transaction, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();
        cashAmountEditText = view.findViewById(R.id.cashAmountEditText);
        memoEditText = view.findViewById(R.id.memoEditText);
        continueButton = view.findViewById(R.id.continueButton);

        continueButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        if (!cashAmountEditText.getText().toString().equals("") && !memoEditText.getText().toString().equals("")) {
            if (v.getId() == R.id.continueButton) {
                createAndUploadCover(memoEditText.getText().toString(), cashAmountEditText.getText().toString());
            }
        } else {
            Toast.makeText(thisActivity, "Please fill out both fields", Toast.LENGTH_SHORT).show();
        }
    }


    //Bs because you can't change things that aren't final in DB on success, but you can do it like this
    private static boolean alreadySentBack = false;
    private boolean getAlreadySentBack() {
        return alreadySentBack;
    }
    private void setAlreadySentBack(boolean bool) {
        alreadySentBack = bool;
    }

    public  void createAndUploadCover(String memo, String content) {
        setAlreadySentBack(false);
        for (User recipient : thisActivity.getSelectedRecipients()) {

            // You're supposed to use Map to put data in a Firestore DB
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("content", content);
            updateMap.put("coverType", "cash");
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

                            //Send back to main screen
                            if (!getAlreadySentBack()) {
                                Intent nextIntent = new Intent(thisActivity, MainActivity.class);
                                thisActivity.startActivity(nextIntent);
                                Toast.makeText(thisActivity, "Sent successfully", Toast.LENGTH_SHORT).show();
                                setAlreadySentBack(true);
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
}