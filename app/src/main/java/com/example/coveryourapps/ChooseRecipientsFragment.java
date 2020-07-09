package com.example.coveryourapps;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ChooseRecipientsFragment extends Fragment implements View.OnClickListener{
    private EditText usernameSearch;
    private CoverCreatorActvity thisActivity;
    private RecyclerView yourFriendsRecyclerView;
    private ArrayList<User> yourFriends;

    private LinearLayout selectedRecipientsInfo;
    private TextView selectedRecipientsTextView;
    private Button selectedRecipientsClearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_recipients, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();

        yourFriendsRecyclerView = view.findViewById(R.id.yourFriendsRecyclerView);
        yourFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        yourFriends = new ArrayList<>();

        yourFriends.add(thisActivity.getCurrentUser());
        yourFriends.add(thisActivity.getCurrentUser());
        yourFriends.add(thisActivity.getCurrentUser());

        yourFriendsRecyclerView.setAdapter(new ChooseRecipientsFragment.UsersAdapter(yourFriends));


        usernameSearch = view.findViewById(R.id.usernameSearch);
        usernameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    performUsernameSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        selectedRecipientsInfo = view.findViewById(R.id.selectedRecipientsInfo);
        selectedRecipientsTextView = view.findViewById(R.id.selectedRecipientsTextView);
        selectedRecipientsClearButton = view.findViewById(R.id.selectedRecipientsClearButton);
        selectedRecipientsClearButton.setOnClickListener(this);


        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        updateSelectedRecipientsUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selectedRecipientsClearButton:
                thisActivity.clearSelectedRecipients();
                updateSelectedRecipientsUI();
                Log.d("**Choose Recipients Fragment |", "Cleared selected recipients");
                break;
            case R.id.inviteFriendsButton:

                break;
        }
    }


    class UsersAdapter extends RecyclerView.Adapter<ChooseRecipientsFragment.FriendViewHolder> {
        private ArrayList<User> yourFriends;

        public UsersAdapter(ArrayList<User> yourFriends) {
            super();
            this.yourFriends = yourFriends;
        }

        @NonNull
        @Override
        public ChooseRecipientsFragment.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChooseRecipientsFragment.FriendViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ChooseRecipientsFragment.FriendViewHolder holder, int position) {
            holder.bind(this.yourFriends.get(position));
        }

        @Override
        public int getItemCount() {
            return this.yourFriends.size();
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private User friend;
        private ImageView friendProfilePic;
        private TextView friendName, friendDisplayName;
        private ImageButton friendAddButton;

        public FriendViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.choose_recipient_friend_list_item, container, false));
            friendProfilePic = itemView.findViewById(R.id.friendProfilePic);
            friendName = itemView.findViewById(R.id.friendName);
            friendDisplayName = itemView.findViewById(R.id.friendDisplayName);
            friendAddButton = itemView.findViewById(R.id.friendAddButton);
        }

        public void bind(User friend) {
            this.friend = friend;
            //friendProfilePic.setImageDrawable(fads);
            friendName.setText(friend.getName());
            friendDisplayName.setText("@" + friend.getDisplayName());

            friendAddButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.friendAddButton) {

                //add Friend
                if (!thisActivity.getSelectedRecipients().contains(friend.getUid())) {
                    thisActivity.addToSelectedRecipients(friend.getUid());
                    updateSelectedRecipientsUI();
                    Log.d("**Choose Recipients Fragment |", "Added FRIEND UID " + friend.getUid() + " to array. Array is now " + thisActivity.getSelectedRecipients().toString());
                }
            }
        }
    }

    private void performUsernameSearch(String displayName) {
        for (User user : thisActivity.getAllUsers()) {
            if (user.getDisplayName().equals(displayName)) {
                String searchedUserUID = user.getUid();
                if (!thisActivity.getSelectedRecipients().contains(searchedUserUID)) {
                    thisActivity.addToSelectedRecipients(searchedUserUID);
                    updateSelectedRecipientsUI();
                    Log.d("**Choose Recipients Fragment |", "Added USERNAME UID " + user.getUid() + " to array. Array is now " + thisActivity.getSelectedRecipients().toString());
                } else {
                    Log.d("**Choose Recipients Fragment |", "Username UID already added to selected recipients");
                }
            }
        }
        usernameSearch.setText("");
    }

    private void updateSelectedRecipientsUI() {
        if (thisActivity.getSelectedRecipients().size() == 0) {
            selectedRecipientsInfo.setVisibility(View.GONE);
        } else {
            selectedRecipientsInfo.setVisibility(View.VISIBLE);

            ArrayList<String> userNamesToDisplay = new ArrayList();
            for (User user : thisActivity.getAllUsers()) {
                for (String uid : thisActivity.getSelectedRecipients()) {
                    if (user.getUid().contains(uid)) {
                        User userToDisplay = user;
                        userNamesToDisplay.add(userToDisplay.getName());
                    }
                }
            }
            String userNamesToDisplayNoBrackets = userNamesToDisplay.toString().replace("[", "").replace("]", "");
            selectedRecipientsTextView.setText("Selected Recipients: "+userNamesToDisplayNoBrackets);
        }
    }
}