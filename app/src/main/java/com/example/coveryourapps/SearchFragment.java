package com.example.coveryourapps;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashSet;

public class SearchFragment extends Fragment implements View.OnClickListener {
    MainActivity thisMainActivity;
    CoverCreatorActvity thisCCActivity;
    RecyclerView searchResultsRecyclerView;
    EditText searchBar;
    ImageButton backButton;
    TextView noResultsTextView;
    InputMethodManager imm;
    HashSet<User> searchResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        if (getContext() instanceof MainActivity)
            thisMainActivity = (MainActivity) getActivity();
        else
            thisCCActivity = (CoverCreatorActvity) getActivity();

        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Overriding gets rid of the weird animations within recyclerviews when you scroll
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        noResultsTextView = view.findViewById(R.id.noResultsTextView);
        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        searchBar = view.findViewById(R.id.searchBar);
        //Pull up keyboard
        searchBar.requestFocus();//Apparently can be done in xml with <request focus />
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                performUserSearch(s.toString());
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    void performUserSearch(String s) {
        HashSet<User> allUsersCopy = new HashSet<>(DBHandler.getAllUsers());
        searchResult = new HashSet<>(allUsersCopy);

        for (User u : allUsersCopy) {
            String typedName;
            String typedDisplayName;

            //Remove all users who have smaller names than typed
            if (s.length() > u.getName().length() || s.length() > u.getDisplayName().length()) {
                searchResult.remove(u);
                continue;
            } else {
                typedName = u.getName().toLowerCase().substring(0, s.length());
                typedDisplayName = u.getDisplayName().toLowerCase().substring(0, s.length());
            }

            if (!s.equals(typedName) && !s.equals(typedDisplayName)) {
                searchResult.remove(u);
//                continue;
            }

        }
        //Print
//        for (User u : searchResult) {
//            Log.d("**Search Fragment | ", "User " + u.getName() + " | " + u.getDisplayName() + " is included in search.");
//        }
//        Log.d("**Search Fragment | ", "-----No more users included in the search.");

        //Now, searchResult is the current accurate list of searched people
        if (searchResult.isEmpty() || s.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setVisibility(View.GONE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            searchResultsRecyclerView.setAdapter(new SearchFragment.UsersAdapter(searchResult));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (getContext() instanceof MainActivity)
                thisMainActivity.goBack();
            else
                thisCCActivity.goBack();
            searchBar.setText("");
        }
    }


    class UsersAdapter extends RecyclerView.Adapter<SearchFragment.SearchViewHolder> {
        private ArrayList<User> yourFriends;

        public UsersAdapter(HashSet<User> yourFriends) {
            super();
            //Convert hashset to array to bind position for recycler view, don't need it to be a hashset anymore
            this.yourFriends = new ArrayList<>(yourFriends);
        }

        @NonNull
        @Override
        public SearchFragment.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SearchFragment.SearchViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchFragment.SearchViewHolder holder, int position) {
            holder.bind(this.yourFriends.get(position));
        }

        @Override
        public int getItemCount() {
            return this.yourFriends.size();
        }
    }

    class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private User search;
        private ImageView friendProfilePic;
        private TextView friendName, friendDisplayName;
        private ImageButton friendTrashButton, trashCheckButton, trashCancelButton;
        private LinearLayout trashButtonLayout, trashConfirmLayout;

        public SearchViewHolder(ViewGroup container) {
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

        public void bind(User search) {
            this.search = search;
            //friendProfilePic.setImageDrawable(fads);
            friendName.setText(search.getName());
            friendDisplayName.setText("@" + search.getDisplayName());

            friendTrashButton.setOnClickListener(this);
            trashCheckButton.setOnClickListener(this);
            trashCancelButton.setOnClickListener(this);

            if (getContext() instanceof MainActivity) {
                //Are they already a friend?
                if (!DBHandler.getAllUserFriends().contains(search)) {
                    friendTrashButton.setImageResource(R.drawable.fab_plus);
                } else {
                    friendTrashButton.setImageResource(R.drawable.trash_icon);
                }

            } else {
                if (thisCCActivity.getSelectedRecipients().contains(search)) {
                    friendTrashButton.setImageResource(R.drawable.trash_icon);
                } else {
                    friendTrashButton.setImageResource(R.drawable.fab_plus);
                }
            }
        }


        @Override
        public void onClick(View v) {
            if (getContext() instanceof MainActivity) {
                switch (v.getId()) {
                    case R.id.friendTrashButton:
                        // If they are already a friend
                        if (DBHandler.getAllUserFriends().contains(search)) {
                            trashButtonLayout.setVisibility(View.GONE);
                            trashConfirmLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(thisMainActivity, "Are you sure you want to remove " + search.getName() + " as a friend?", Toast.LENGTH_SHORT).show();
                        } else {
                            //Add search to friends
                            DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                    .update("friends", FieldValue.arrayUnion(search.getUid()));
                            thisMainActivity.refreshDB();
                            friendTrashButton.setImageResource(R.drawable.trash_icon);
                        }
                        break;
                    case R.id.trashCheckButton:
                        //Delete friend from DB
                        DBHandler.getDB().collection("users").document(DBHandler.getCurrentFirebaseUser().getUid())
                                .update("friends", FieldValue.arrayRemove(search.getUid()));
                        thisMainActivity.refreshDB();
                        trashConfirmLayout.setVisibility(View.GONE);
                        trashButtonLayout.setVisibility(View.VISIBLE);
                        friendTrashButton.setImageResource(R.drawable.fab_plus);
                        break;
                    case R.id.trashCancelButton:
                        trashConfirmLayout.setVisibility(View.GONE);
                        trashButtonLayout.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                switch (v.getId()) {
                    case R.id.friendTrashButton:
                        // If they are already selected
                        if (thisCCActivity.getSelectedRecipients().contains(search)) {
                            thisCCActivity.getSelectedRecipients().remove(search);
                            friendTrashButton.setImageResource(R.drawable.fab_plus);
//                            trashButtonLayout.setVisibility(View.GONE);
//                            trashConfirmLayout.setVisibility(View.VISIBLE);
//                            Toast.makeText(thisMainActivity, "Are you sure you want to remove " + search.getName() + " as a friend?", Toast.LENGTH_SHORT).show();
                        } else {
                            //Add search to selected
                            thisCCActivity.getSelectedRecipients().add(search);
                            friendTrashButton.setImageResource(R.drawable.trash_icon);
                        }
                        break;
                }
            }
        }
    }
}