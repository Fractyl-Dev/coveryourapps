package com.example.coveryourapps;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ReviewCoverFragment extends Fragment implements View.OnClickListener {
    private MainActivity thisActivity;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisActivity = (MainActivity) getActivity();
        View view;
        //Determine how to review the given cover type
        if (thisActivity.getReviewCover().getCoverType().equals("contract")) {
            view = inflater.inflate(R.layout.fragment_review_contract, container, false);
            TextView contractReviewTitle = view.findViewById(R.id.contractReviewTitle);
            TextView contractReviewText = view.findViewById(R.id.contractReviewText);

            contractReviewTitle.setText(thisActivity.getReviewCover().getMemo());
            contractReviewText.setText(thisActivity.getReviewCover().getContent());
        } else if (thisActivity.getReviewCover().getCoverType().equals("cash")) {
            view = inflater.inflate(R.layout.fragment_review_cash, container, false);
            TextView cashReviewText = view.findViewById(R.id.cashReviewText);
            TextView cashReviewBodyText = view.findViewById(R.id.cashReviewBodyText);

            //Check who sent it to display info
            Cover reviewCover = thisActivity.getReviewCover();
            if (thisActivity.getReviewCover().getSender().getUid().equals(DBHandler.getCurrentFirebaseUser().getUid())) {
                cashReviewText.setText("You gave " + reviewCover.getRecipient().getName() + " $" + reviewCover.getContent());
                cashReviewBodyText.setText("Memo:\n\t" + reviewCover.getMemo());
            } else {
                cashReviewText.setText(reviewCover.getSender().getName() + " gave you $" + reviewCover.getContent());
                cashReviewBodyText.setText("Memo:\n\t" + reviewCover.getMemo());
            }
        } else {
            view = inflater.inflate(R.layout.fragment_review_contract, container, false);
        }

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            thisActivity.changeFragmentLayover(thisActivity.getHomeFragment(), "homeFragment", false);
        }
    }
}