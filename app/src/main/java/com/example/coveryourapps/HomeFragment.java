package com.example.coveryourapps;

//import android.support.v4.fr

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
private MainActivity thisActivity;
    private RecyclerView pendingCoversRecyclerView, confirmedCoversRecyclerView, expiredCoversRecyclerView;
    private ArrayList<Cover> pendingCovers, confirmedCovers, expiredCovers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        pendingCoversRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        confirmedCoversRecyclerView = view.findViewById(R.id.confirmedRecyclerView);
        expiredCoversRecyclerView = view.findViewById(R.id.expiredRecyclerView);

        pendingCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        confirmedCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expiredCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        thisActivity = (MainActivity) getActivity();

        pendingCovers = new ArrayList<>();
        confirmedCovers = new ArrayList<>();
        expiredCovers = new ArrayList<>();

        for (Cover cover : thisActivity.getAllUserCovers()){
            if (cover.getStatus().equals("pending")) {
                Log.d("**Home Fragment | ", "Cover status pending");
                pendingCovers.add(cover);
            }else {
                Log.d("**Home Fragment | ", "wh");
            }
        }

        pendingCoversRecyclerView.setAdapter(new UsersAdapter(pendingCovers));
        confirmedCoversRecyclerView.setAdapter(new UsersAdapter(confirmedCovers));
        expiredCoversRecyclerView.setAdapter(new UsersAdapter(expiredCovers));


//        ArrayList<String> tempUsersSentTo = new ArrayList<>();
//        tempUsersSentTo.add("Jake");
//        tempUsersSentTo.add("Hank");
//
//        Cover coverCash = new Cover(80.4, "Cash", "Memo For your birthday", "Note I don't like you", "Sent 4 hours ago", tempUsersSentTo);
//        //Cover coverWaiver = new Cover("e3rd", "Cash", "Memo For your birthday", "Note I don't like you", "Sent 4 hours ago", tempUsersSentTo);
//        Cover coverWaiver = new Cover("erre3", "This is the contract, give my machine back", "Waiver", "Memo For your birthday", "Note I don't like you", "Sent 4 hours ago", tempUsersSentTo);
//        pendingCovers.add(coverCash);
//        pendingCovers.add(coverWaiver);
//
//        confirmedCovers = new ArrayList<>();
//        confirmedCovers.add(coverWaiver);
//        confirmedCovers.add(coverWaiver);
//        confirmedCovers.add(coverCash);
//
//        expiredCovers = new ArrayList<>();
//        expiredCovers.add(coverCash);
//        expiredCovers.add(coverCash);
//        expiredCovers.add(coverWaiver);





        return view;
    }

    class UsersAdapter extends RecyclerView.Adapter<CoverViewHolder> {
        private ArrayList<Cover> pendingCovers;

        public UsersAdapter(ArrayList<Cover> covers) {
            super();
            this.pendingCovers = covers;
        }

        @NonNull
        @Override
        public CoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CoverViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CoverViewHolder holder, int position) {
            holder.bind(this.pendingCovers.get(position));
        }

        @Override
        public int getItemCount() {
            return this.pendingCovers.size();
        }
    }

    class CoverViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Cover cover;
        private ImageView coverLogo;
        private TextView coverTypeTextView, coverStatusTextView, coverSentTitleTextView, coverPeopleTextView;
        private LinearLayout coverHideableContentLinearLayout;
        private Button coverCancelButton, coverReviewButton, coverRemindButton;
        private ImageButton coverDropdownButton;

        public CoverViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.cover_list_item, container, false));
            coverLogo = itemView.findViewById(R.id.coverLogo);

            coverTypeTextView = itemView.findViewById(R.id.coverTypeTextView);
            coverStatusTextView = itemView.findViewById(R.id.coverStatusTextView);
            coverSentTitleTextView = itemView.findViewById(R.id.coverSentTitleTextView);
            coverPeopleTextView = itemView.findViewById(R.id.coverPeopleTextView);

            coverHideableContentLinearLayout = itemView.findViewById(R.id.coverHideableContent);

            coverCancelButton = itemView.findViewById(R.id.coverCancelButton);
            coverReviewButton = itemView.findViewById(R.id.coverReviewButton);
            coverRemindButton = itemView.findViewById(R.id.coverRemindButton);
            coverDropdownButton = itemView.findViewById(R.id.coverDropdownButton);
        }

        public void bind(Cover cover) {
            this.cover = cover;
            if (cover.getType().equals("contract")) {
                coverTypeTextView.setText(R.string.cover_type_contract);
            }
            coverStatusTextView.setText(cover.getStatus());
            coverSentTitleTextView.setText("Sent to:");
            //coverPeopleTextView.setText(cover.getUsersInvolved().toString().replace("[", "").replace("]", ""));

            coverCancelButton.setOnClickListener(this);
            coverReviewButton.setOnClickListener(this);
            coverRemindButton.setOnClickListener(this);
            coverDropdownButton.setOnClickListener(this);

            if (cover.getType().equals("Cash")) {
                coverLogo.setImageResource(R.drawable.cash_icon_medium);
                coverLogo.setColorFilter(R.color.colorAccent);
                coverLogo.setBackground(getResources().getDrawable(R.drawable.cover_cash_icon));
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.coverCancelButton:
                    Log.d("**Home Fragment | ", "Cover Cancel Button pressed");
                    break;
                case R.id.coverReviewButton:
                    Log.d("**HomeFragment | ", "Cover Review Button pressed");
                    break;
                case R.id.coverRemindButton:
                    Log.d("**HomeFragment | ", "Cover Remind Button pressed");
                    break;
                case R.id.coverDropdownButton:
                    Log.d("**HomeFragment | ", "Cover Dropdown Button pressed");
                    if (coverHideableContentLinearLayout.getVisibility() == View.VISIBLE){
                        coverHideableContentLinearLayout.setVisibility(View.GONE);
                        coverDropdownButton.setImageResource(R.drawable.cover_dropdown_arrow);
                    } else {
                        coverHideableContentLinearLayout.setVisibility(View.VISIBLE);
                        coverDropdownButton.setImageResource(R.drawable.cover_dropup_arrow);
                    }
                    break;
            }
        }
    }


}
