package com.example.coveryourapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LendItemFragment extends Fragment implements View.OnClickListener{
    CoverCreatorActvity thisActivity;//
    private EditText itemNameEditText, memoEditText;
    private Button uploadImageButton, returnDateButton, continueButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lend_item, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();

        itemNameEditText = view.findViewById(R.id.itemNameEditText);
        memoEditText = view.findViewById(R.id.memoEditText);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        returnDateButton = view.findViewById(R.id.returnDateButton);
        continueButton = view.findViewById(R.id.continueButton);
        uploadImageButton.setOnClickListener(this);
        returnDateButton.setOnClickListener(this);
        continueButton.setOnClickListener(this);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {

    }
}