package com.example.coveryourapps;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SignInNameLayoverFragment extends Fragment implements View.OnClickListener {
    public SignInNameLayoverFragment() {
        // Required empty public constructor
    }

    LoginActivity thisActivity;
    EditText nameEditText;
    TextView nameNotEnteredErrorText;
    TextView nameNotValidErrorText;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in_name_layover, container, false);

        thisActivity = (LoginActivity) getActivity();
        Button nextButton = view.findViewById(R.id.nameNextButton);
        nextButton.setOnClickListener(this);

        nameEditText = view.findViewById(R.id.nameEditText);
        nameNotEnteredErrorText = view.findViewById(R.id.nameNotEnteredErrorText);
        nameNotValidErrorText = view.findViewById(R.id.nameNotValidErrorText);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        if (!nameEditText.getText().toString().contains(" ")){
            nameNotValidErrorText.setVisibility(View.VISIBLE);
            nameNotEnteredErrorText.setVisibility(View.GONE);
        } else if (!nameEditText.getText().toString().equals("")) {
            thisActivity.setName(nameEditText.getText().toString());
            thisActivity.changeLoginLayover(thisActivity.getSignInEmailLayoverFragment());
        } else {
            nameNotEnteredErrorText.setVisibility(View.VISIBLE);
            nameNotValidErrorText.setVisibility(View.GONE);
        }
    }
}