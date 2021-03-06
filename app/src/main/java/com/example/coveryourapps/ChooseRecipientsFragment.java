package com.example.coveryourapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Telephony;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChooseRecipientsFragment extends Fragment implements View.OnClickListener {
    private EditText usernameSearch;
    private CoverCreatorActvity thisActivity;
    private RecyclerView yourFriendsRecyclerView;
    private ArrayList<User> yourFriends;

    private LinearLayout selectedRecipientsInfo;
    private TextView selectedRecipientsTextView, noFriendsTextView;
    private Button selectedRecipientsClearButton, inviteFriendsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_recipients, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();

        yourFriendsRecyclerView = view.findViewById(R.id.yourFriendsRecyclerView);
        yourFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usernameSearch = view.findViewById(R.id.usernameSearch);
        usernameSearch.setOnClickListener(this);
//        usernameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    performUsernameSearch(v.getText().toString());
//                    return true;
//                }
//                return false;
//            }
//        });

        selectedRecipientsInfo = view.findViewById(R.id.selectedRecipientsInfo);
        selectedRecipientsTextView = view.findViewById(R.id.selectedRecipientsTextView);
        noFriendsTextView = view.findViewById(R.id.noFriendsTextView);
        inviteFriendsButton = view.findViewById(R.id.inviteFriendsButton);
        selectedRecipientsClearButton = view.findViewById(R.id.selectedRecipientsClearButton);
        selectedRecipientsClearButton.setOnClickListener(this);
        inviteFriendsButton.setOnClickListener(this);


        //Populate friends in the UI
        yourFriends = new ArrayList<>();
        updateFriendsUI();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSelectedRecipientsUI();
    }

    public void updateFriendsUI() {
        ArrayList<User> newFriends = new ArrayList<>(DBHandler.getAllUserFriends());

        //Only update if there is a difference, this allows for updating in the background with nothing happening if nothing is new
        if (!yourFriends.toString().equals(newFriends.toString()) || yourFriends.size() == 0) {
            yourFriends.clear();
            yourFriends.addAll(newFriends);
            if (newFriends.size() != 0) {
                noFriendsTextView.setVisibility(View.GONE);
                yourFriendsRecyclerView.setVisibility(View.VISIBLE);
                yourFriendsRecyclerView.setAdapter(new ChooseRecipientsFragment.UsersAdapter(yourFriends));
            } else {
                yourFriendsRecyclerView.setVisibility(View.GONE);
                noFriendsTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.usernameSearch:
                thisActivity.changeCoverCreatorLayover(thisActivity.getSearchFragment(), "searchFragment");
                break;
            case R.id.selectedRecipientsClearButton:
                thisActivity.clearSelectedRecipients();
                updateSelectedRecipientsUI();
                yourFriendsRecyclerView.setAdapter(new ChooseRecipientsFragment.UsersAdapter(yourFriends));
                break;
            case R.id.inviteFriendsButton:
              sendSMS();
                break;
        }
    }

    private void sendSMS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(thisActivity); // Need to change the build to API 19

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Download CYA Today: Add link ");

            if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
            // any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName);
            }
            startActivity(sendIntent);

        }
        else // For early versions, do what worked for you before.
        {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address","phoneNumber");
            smsIntent.putExtra("sms_body","Download CYA Today: Add link");
            startActivity(smsIntent);
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

            //Change its icon to be a trash if it's included as a recipient
            if (thisActivity.getSelectedRecipients().contains(friend)) {
                friendAddButton.setImageResource(R.drawable.trash_icon);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.friendAddButton) {
                //add Friend
                if (!thisActivity.getSelectedRecipients().contains(friend)) {
                    thisActivity.addToSelectedRecipients(friend);
                    friendAddButton.setImageResource(R.drawable.trash_icon);
                    updateSelectedRecipientsUI();
                } else {
                    thisActivity.getSelectedRecipients().remove(friend);
                    friendAddButton.setImageResource(R.drawable.fab_plus);
                    updateSelectedRecipientsUI();
                }
            }
        }
    }

    private void performUsernameSearch(final String displayName) {
        DBHandler.getDB().collection("displayNames")
                .whereEqualTo("name", displayName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (task.getResult().size() == 0) {
                                Toast.makeText(thisActivity, "User not found", Toast.LENGTH_SHORT).show();
                            }
                            for (DocumentSnapshot displayNameSnapshot : task.getResult()) {
                                DBHandler.getDB().collection("users")
                                        .document(displayNameSnapshot.getId())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful() && task.getResult() != null) {
                                                    DocumentSnapshot user = task.getResult();
                                                    if (user.exists()) {
                                                        thisActivity.addToSelectedRecipients(user.toObject(User.class));
                                                        updateSelectedRecipientsUI();
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(thisActivity, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        usernameSearch.setText("");
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedRecipientsUI() {
        if (thisActivity.getSelectedRecipients().size() == 0) {
            selectedRecipientsInfo.setVisibility(View.GONE);
        } else {
            selectedRecipientsInfo.setVisibility(View.VISIBLE);

            ArrayList<String> userNamesToDisplay = new ArrayList<>();
            for (User user : thisActivity.getSelectedRecipients()) {
                userNamesToDisplay.add(user.getName());
            }
            String userNamesToDisplayNoBrackets = userNamesToDisplay.toString().replace("[", "").replace("]", "");
            selectedRecipientsTextView.setText("Selected Recipients: " + userNamesToDisplayNoBrackets);
        }
    }
}