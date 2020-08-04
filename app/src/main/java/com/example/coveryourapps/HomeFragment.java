package com.example.coveryourapps;

//import android.support.v4.fr

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        updateCoversUI();//Immediately displays last info saved, only updates when different than before
//        thisActivity.refreshDB();//Checks for new info and updateCoversUI when finished
        return view;
    }

    @Override
    public void onResume() {
        updateCoversUI();
        super.onResume();
    }

    public void updateCoversUI() {
        ArrayList<Cover> newPendingCovers = new ArrayList<>();
        ArrayList<Cover> newConfirmedCovers = new ArrayList<>();
        ArrayList<Cover> newExpiredCovers = new ArrayList<>();

        for (Cover cover : DBHandler.getAllUserCovers()) {
            if (cover.getStatus().equals("pending")) {
                newPendingCovers.add(cover);
            } else if (cover.getStatus().equals("confirmed")) {
                newConfirmedCovers.add(cover);
            }
        }
        //Only update if there is a difference, this allows for updating in the background with nothing happening if nothing is new
        // || expiredCovers.toString().equals(newExpiredCovers.toString())
        if (!pendingCovers.toString().equals(newPendingCovers.toString()) || !confirmedCovers.toString().equals(newConfirmedCovers.toString())) {
            pendingCovers.clear();
            pendingCovers.addAll(newPendingCovers);
            confirmedCovers.clear();
            confirmedCovers.addAll(newConfirmedCovers);
            expiredCovers.clear();
            expiredCovers.addAll(newExpiredCovers);

//            pendingCovers = newPendingCovers;
//            confirmedCovers = newConfirmedCovers;
//            expiredCovers = newExpiredCovers;
            pendingCoversRecyclerView.setAdapter(new UsersAdapter(pendingCovers));
            confirmedCoversRecyclerView.setAdapter(new UsersAdapter(confirmedCovers));
            expiredCoversRecyclerView.setAdapter(new UsersAdapter(expiredCovers));
        }
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
        private TextView coverTypeTextView, coverTimeAgoTextView, coverSentTitleTextView, coverPeopleTextView;
        private LinearLayout coverHideableContentLinearLayout;
        private Button coverCancelButton, coverReviewButton, coverRemindButton;
        private View coverCancelSeparator, coverRemindSeparator;
        private ImageButton coverDropdownButton;
        private boolean isSender;

//        public Cover getCover() {
//            return this.cover;
//        }

        public CoverViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.cover_list_item, container, false));
            coverLogo = itemView.findViewById(R.id.coverLogo);

            coverTypeTextView = itemView.findViewById(R.id.coverTypeTextView);
            coverTimeAgoTextView = itemView.findViewById(R.id.coverTimeAgoTextView);
            coverSentTitleTextView = itemView.findViewById(R.id.coverSentTitleTextView);
            coverPeopleTextView = itemView.findViewById(R.id.coverPeopleTextView);

            coverHideableContentLinearLayout = itemView.findViewById(R.id.coverHideableContent);

            coverCancelSeparator = itemView.findViewById(R.id.coverCancelSeparator);
            coverRemindSeparator = itemView.findViewById(R.id.coverRemindSeparator);
            coverCancelButton = itemView.findViewById(R.id.coverCancelButton);
            coverReviewButton = itemView.findViewById(R.id.coverReviewButton);
            coverRemindButton = itemView.findViewById(R.id.coverRemindButton);
            coverDropdownButton = itemView.findViewById(R.id.coverDropdownButton);
        }

        public void bind(final Cover cover) {
            this.cover = cover;

            for (Cover iteratedCover : DBHandler.getAllUserCovers()) {
                if (iteratedCover.equals(cover)) {
                    if (cover.getSenderID().equals(DBHandler.getCurrentFirebaseUser().getUid())) {
                        isSender = true;
                        coverSentTitleTextView.setText(R.string.sent_to);
                        coverPeopleTextView.setText(cover.getRecipient().getName());
                    } else {
                        isSender = false;
                        coverSentTitleTextView.setText(R.string.received_from);
                        coverPeopleTextView.setText(cover.getSender().getName());
                        coverCancelButton.setText(R.string.deny);
                        coverRemindButton.setText(R.string.accept);
                    }

                    //If status is confirmed, get rid of some buttons
                    if (cover.getStatus().equals("confirmed")) {
                        coverCancelButton.setVisibility(View.GONE);
                        coverRemindButton.setVisibility(View.GONE);
                        coverCancelSeparator.setVisibility(View.GONE);
                        coverRemindSeparator.setVisibility(View.GONE);
                    }
                }
            }

            coverTypeTextView.setText(cover.getMemo());
            coverTimeAgoTextView.setText(calculateTimeFromCreation(cover.getCreatedTime()));

            coverCancelButton.setOnClickListener(this);
            coverReviewButton.setOnClickListener(this);
            coverRemindButton.setOnClickListener(this);
            coverDropdownButton.setOnClickListener(this);

            if (cover.getCoverType().equals("cash")) {
                coverLogo.setImageResource(R.drawable.cash_icon_medium);
                coverLogo.setColorFilter(R.color.colorAccent);
                coverLogo.setBackground(getResources().getDrawable(R.drawable.cover_cash_icon));
            }
        }

        private String calculateTimeFromCreation(Date creationDate) {
            long diffInMillies = new Timestamp(System.currentTimeMillis()).getTime() - creationDate.getTime();
            String unit;
            long unitAmount;
            if (TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) > 0) {
                if (TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) > 1) {
                    unit = "days";
                } else {
                    unit = "day";
                }
                unitAmount = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            } else if (TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS) > 0) {
                if (TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS) > 1) {
                    unit = "hours";
                } else {
                    unit = "hour";
                }
                unitAmount = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            } else if (TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) > 0) {
                if (TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS) > 1) {
                    unit = "minutes";
                } else {
                    unit = "minute";
                }
                unitAmount = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
            } else {
                if (TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS) == 1) {
                    unit = "second";
                } else {
                    unit = "seconds";
                }
                unitAmount = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            }
            return unitAmount + " " + unit + " ago";
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.coverCancelButton:
                    Log.d("**Home Fragment | ", "Cover Cancel Button pressed");
                    cancel();
                    break;
                case R.id.coverReviewButton:
                    thisActivity.setReviewCover(cover);
                    thisActivity.changeFragmentLayover(thisActivity.getReviewCoverFragment(), "reviewCoverFragment", true);
                    Log.d("**HomeFragment | ", "Cover Review Button pressed");
                    break;
                case R.id.coverRemindButton:
                    Log.d("**HomeFragment | ", "Cover Remind Button pressed");
                    if (!isSender) {
                        DBHandler.getDB().collection("covers")
                                .document(cover.getDocID())
                                .update("status", "confirmed")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("**Home Fragment |", "Cover " + cover.getDocID() + " set to confirmed");
                                        thisActivity.refreshDB();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("**Home Fragment |", "Cover failed to be set to confirmed");
                                        Toast.makeText(thisActivity, "An error occurred, please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    break;
                case R.id.coverDropdownButton:
                    Log.d("**HomeFragment | ", "Cover Dropdown Button pressed");
                    if (coverHideableContentLinearLayout.getVisibility() == View.VISIBLE) {
                        coverHideableContentLinearLayout.setVisibility(View.GONE);
                        coverDropdownButton.setImageResource(R.drawable.cover_dropdown_arrow);
                    } else {
                        coverHideableContentLinearLayout.setVisibility(View.VISIBLE);
                        coverDropdownButton.setImageResource(R.drawable.cover_dropup_arrow);
                    }
                    break;
            }
        }

        public void cancel() {
            DBHandler.getDB().collection("covers")
                    .document(this.cover.getDocID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("**Home Fragment |", "Cancelled cover deleted successfully");
                            thisActivity.refreshDB();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("**Home Fragment |", "Error deleting cancelled cover", e);
                        }
                    });
        }

        private int numberICanReference = 0;

        public int getNumberICanReference() {
            return numberICanReference;
        }

        public void setNumberICanReference(int numberICanReference) {
            this.numberICanReference = numberICanReference;
        }
    }


}
