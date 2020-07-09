package com.example.coveryourapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContractTemplateArgumentFragment extends Fragment implements View.OnClickListener {
    private CoverCreatorActvity thisActivity;
    private TextView contractTemplateArgumentTitle, argumentTextView;
    private LinearLayout argumentHolder, signatureHolder;
    private EditText argumentResponse, electronicSignature;
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

    public void updateUI() {
        int iteration = thisActivity.getContractTemplateArgumentsIteration();
        argumentResponse.setText("");
        contractTemplateArgumentTitle.setText(thisActivity.getCurrentContractTemplate().getTitle());
        argumentTextView.setText(thisActivity.getContractTemplateArguments().get(iteration));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.continueButton) {
            thisActivity.addToContractTemplateArgumentResponses(argumentResponse.getText().toString());
            thisActivity.setContractTemplateArgumentsIteration(thisActivity.getContractTemplateArgumentsIteration() + 1);
            if (thisActivity.getContractTemplateArgumentsIteration() < thisActivity.getContractTemplateArguments().size()) {
                updateUI();
            } else {
                argumentHolder.setVisibility(View.GONE);
                signatureHolder.setVisibility(View.VISIBLE);
                if (!electronicSignature.getText().toString().equals("")) {
                    Log.d("Contract Template Argument |", "Input data :" +thisActivity.getContractTemplateArguments().toString());
                }
            }
        }
    }
}