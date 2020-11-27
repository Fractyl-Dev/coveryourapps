package com.example.coveryourapps;

//import android.support.v4.fr

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FriendsFragment extends Fragment implements View.OnClickListener{
    MainActivity thisActivity;
    RecyclerView friendsRecyclerView;
    EditText usernameSearch;
    ArrayList<User> yourFriends;
    TextView noFriendsTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        thisActivity = (MainActivity) getActivity();

        noFriendsTextView = view.findViewById(R.id.noFriendsTextView);
        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        usernameSearch = view.findViewById(R.id.usernameSearch);
        usernameSearch.setOnClickListener(this);
//        usernameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                Toast.makeText(thisActivity, "EEE", Toast.LENGTH_SHORT).show();
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    performUsernameSearch(v.getText().toString());
//                    return true;
//                }
//                return false;
//            }
//        });
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        yourFriends = new ArrayList<>();
        updateFriendsUI();


        return view;
    }

    public void updateFriendsUI() {
        if (yourFriends != null) {
            yourFriends.clear();
            yourFriends.addAll(DBHandler.getAllUserFriends());

            if (yourFriends.isEmpty()) {
                friendsRecyclerView.setVisibility(View.GONE);
                noFriendsTextView.setVisibility(View.VISIBLE);
            } else {
                noFriendsTextView.setVisibility(View.GONE);
                friendsRecyclerView.setVisibility(View.VISIBLE);
                friendsRecyclerView.setAdapter(new FriendsFragment.UsersAdapter(yourFriends));
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
                                                        DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                                                .update("friends", FieldValue.arrayUnion(user.toObject(User.class).getUid()));
                                                        thisActivity.refreshDB();
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.usernameSearch) {
            thisActivity.changeFragmentLayover(thisActivity.getSearchFragment(), "searchFragment", true);
        }
    }


    class UsersAdapter extends RecyclerView.Adapter<FriendsFragment.FriendViewHolder> {
        private ArrayList<User> yourFriends;

        public UsersAdapter(ArrayList<User> yourFriends) {
            super();
            this.yourFriends = yourFriends;
        }

        @NonNull
        @Override
        public FriendsFragment.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FriendsFragment.FriendViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsFragment.FriendViewHolder holder, int position) {
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
        private ImageButton friendTrashButton, trashCheckButton, trashCancelButton;
        private LinearLayout trashButtonLayout, trashConfirmLayout;

        public FriendViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.friend_list_item, container, false));
            friendProfilePic = itemView.findViewById(R.id.friendProfilePic);
            friendName = itemView.findViewById(R.id.friendName);
            friendDisplayName = itemView.findViewById(R.id.friendDisplayName);
            friendTrashButton = itemView.findViewById(R.id.friendTrashButton);
            trashCheckButton = itemView.findViewById(R.id.trashCheckButton);
            trashCancelButton = itemView.findViewById(R.id.trashCancelButton);
            trashButtonLayout = itemView.findViewById(R.id.trashButtonLayout);
            trashConfirmLayout = itemView.findViewById(R.id.trashConfirmLayout);
        }

        public void bind(User friend) {
            this.friend = friend;
            //friendProfilePic.setImageDrawable(fads);
            friendName.setText(friend.getName());
            friendDisplayName.setText("@" + friend.getDisplayName());

            friendTrashButton.setOnClickListener(this);
            trashCheckButton.setOnClickListener(this);
            trashCancelButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.friendTrashButton:
                    trashButtonLayout.setVisibility(View.GONE);
                    trashConfirmLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(thisActivity, "Are you sure you want to remove " + friend.getName() + " as a friend?", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.trashCheckButton:
                    //Delete friend from DB
                    DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                            .update("friends", FieldValue.arrayRemove(friend.getUid()));
                    thisActivity.refreshDB();
                    break;
                case R.id.trashCancelButton:
                    trashConfirmLayout.setVisibility(View.GONE);
                    trashButtonLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
