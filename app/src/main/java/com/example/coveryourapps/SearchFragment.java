package com.example.coveryourapps;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class SearchFragment extends Fragment implements View.OnClickListener {
    MainActivity thisActivity;
    EditText searchBar;
    ImageButton backButton;
    TextView noResultsTextView;
    InputMethodManager imm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        thisActivity = (MainActivity) getActivity();

        noResultsTextView = view.findViewById(R.id.noResultsTextView);
        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        searchBar = view.findViewById(R.id.searchBar);
        //Pull up keyboard
        searchBar.requestFocus();//Apparently can be done in xml with <request focus />
        imm = (InputMethodManager) thisActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        searchBar.addTextChangedListener(new TextWatcher() {
            //These functions are abstract so they need to be declared, but they're not used for this purpose
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            //When they change what text is inputted, search through all users
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d("**Search Fragment | ", "Key pressed " + s);

                //performUserSearch(s.toString());
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    void performUserSearch(String s) {
        HashSet<User> allUsersCopy = DBHandler.getAllUsers();
        HashSet<User> searchResult = allUsersCopy;

        for (User u : allUsersCopy) {
            String typedName;
            String typedDisplayName;

            //Remove all users who have smaller names than typed
            if (s.length() > u.getName().length() || s.length() > u.getDisplayName().length()) {
                allUsersCopy.remove(u);
                continue;
            } else {
                typedName = u.getName().toLowerCase().substring(0, s.length());
                typedDisplayName = u.getDisplayName().toLowerCase().substring(0, s.length());
            }

            if (!s.equals(typedName)) {
                allUsersCopy.remove(u);
                continue;
            }

        }
        for (User u : allUsersCopy) {
            Log.d("**Search Fragment | ", "User " + u.getName() + " is included.");
        }

//        for (User u : allUsersCopy) {
//            String typedName = u.getName().toLowerCase().substring(0, s.length());
//            String typedDisplayName = u.getDisplayName().toLowerCase().substring(0, s.length());
////            Log.d("**Search Fragment | ", "Typed Name: " + typedName);
////            Log.d("**Search Fragment | ", "Typed Display Name: " + typedDisplayName);
//
//
//            if (!s.equals(typedName) && !s.equals(typedDisplayName)) {
////                searchResult.remove(u);
////                continue;
//            }
////            Log.d("**Search Fragment | ", "User " + u.getName() + " is included.");
//        }


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            thisActivity.goBack();
        }
    }
}