package com.example.coveryourapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ContractTemplateOverviewFragment extends Fragment implements View.OnClickListener {
    private CoverCreatorActvity thisActivity;
    private TextView contractTemplateOverviewTitle, contractTemplateOverviewText;
    private Button continueButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contract_template_overview, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();

        contractTemplateOverviewTitle = view.findViewById(R.id.contractTemplateOverviewTitle);
        contractTemplateOverviewText = view.findViewById(R.id.contractTemplateOverviewText);
        continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);

        displayTemplateText();

        // Inflate the layout for this fragment
        return view;
    }

    public void displayTemplateText() {
        String currentTemplateText = thisActivity.getCurrentContractTemplate().getText().replace("\\n", "\n");//Properly format newlines
        //Replace {example} with ____ after documenting in activity
        boolean replacing = false;
        StringBuilder argumentName = new StringBuilder();
        for (int j = 0; j < currentTemplateText.length(); j++) {
            char currentChar = currentTemplateText.charAt(j);
            if (currentChar == '{') {
                replacing = true;
            } else if (currentChar == '}') {
                thisActivity.addToContractTemplateArguments(argumentName.toString());
                argumentName.setLength(0);//Clear stringbuilder so next replacement is fresh
                replacing = false;
                currentChar = '_';//Catch last }
            }
            if (replacing) {
                if (currentChar != '{') {
                    argumentName.append(currentChar);
                }
                currentChar = '_';
            }

            //Replace character with _
            currentTemplateText = currentTemplateText.substring(0, j)
                    + currentChar
                    + currentTemplateText.substring(j + 1);
        }
        Log.d("Contract Template Overview |", "Argument Array : " + thisActivity.getContractTemplateArguments().toString());

        contractTemplateOverviewTitle.setText(thisActivity.getCurrentContractTemplate().getTitle());
        contractTemplateOverviewText.setText(currentTemplateText);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.continueButton) {
            thisActivity.changeCoverCreatorLayover(thisActivity.getContractTemplateArgumentFragment(), "contractTemplateArgumentFragment");

//            if (thisActivity.getContractTemplateArgumentsIteration() < thisActivity.getContractTemplateArguments().size()) {
//                //Keep asking for arguments
//            }
        }
    }
}