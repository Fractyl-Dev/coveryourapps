package com.example.coveryourapps;

//import android.support.v4.fr

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.res.ResourcesCompat;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private MainActivity thisActivity;
    private RecyclerView pendingCoversRecyclerView, confirmedCoversRecyclerView, expiredCoversRecyclerView;
    private ArrayList<Cover> pendingCovers, confirmedCovers, expiredCovers;
    private TextView pendingNoCoversTextView, confirmedNoCoversTextView, expiredNoCoversTextView;

    private TextView pendingTitleTextView, confirmedTitleTextView, expiredTitleTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        thisActivity = (MainActivity) getActivity();

        pendingCoversRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        confirmedCoversRecyclerView = view.findViewById(R.id.confirmedRecyclerView);
        expiredCoversRecyclerView = view.findViewById(R.id.expiredRecyclerView);

        pendingNoCoversTextView = view.findViewById(R.id.pendingNoCoversTextView);
        confirmedNoCoversTextView = view.findViewById(R.id.confirmedNoCoversTextView);
        expiredNoCoversTextView = view.findViewById(R.id.expiredNoCoversTextView);

        pendingCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        confirmedCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expiredCoversRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        pendingCovers = new ArrayList<>();
        confirmedCovers = new ArrayList<>();
        expiredCovers = new ArrayList<>();

        pendingTitleTextView = view.findViewById(R.id.pendingTitleTextView);
        confirmedTitleTextView = view.findViewById(R.id.confirmedTitleTextView);
        expiredTitleTextView = view.findViewById(R.id.expiredTitleTextView);

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
        pendingCovers.clear();
        confirmedCovers.clear();
        expiredCovers.clear();
        int pendingCount = 0;
        int confirmedCount = 0;
        int expiredCount = 0;

        for (Cover cover : DBHandler.getAllUserCovers()) {
            switch (cover.getStatus()) {
                case "pending":
                    pendingCovers.add(cover);
                    pendingCount ++;
                    break;
                case "confirmed":
                    confirmedCovers.add(cover);
                    confirmedCount ++;
                    break;
                case "expired":
                    expiredCovers.add(cover);
                    expiredCount ++;
                    break;
            }
        }
        //Change count
        pendingTitleTextView.setText("Pending ("+pendingCount+")");
        confirmedTitleTextView.setText("Confirmed ("+confirmedCount+")");
        expiredTitleTextView.setText("Expired ("+expiredCount+")");


        //If it's empty, display no covers text
        if (pendingCovers.size() != 0) {
            pendingNoCoversTextView.setVisibility(View.GONE);
            pendingCoversRecyclerView.setVisibility(View.VISIBLE);
            pendingCoversRecyclerView.setAdapter(new UsersAdapter(pendingCovers));
        } else {
            pendingCoversRecyclerView.setVisibility(View.GONE);
            pendingNoCoversTextView.setVisibility(View.VISIBLE);
        }
        if (confirmedCovers.size() != 0) {
            confirmedNoCoversTextView.setVisibility(View.GONE);
            confirmedCoversRecyclerView.setVisibility(View.VISIBLE);
            confirmedCoversRecyclerView.setAdapter(new UsersAdapter(confirmedCovers));
        } else {
            confirmedCoversRecyclerView.setVisibility(View.GONE);
            confirmedNoCoversTextView.setVisibility(View.VISIBLE);
        }
        if (expiredCovers.size() != 0) {
            expiredNoCoversTextView.setVisibility(View.GONE);
            expiredCoversRecyclerView.setVisibility(View.VISIBLE);
            expiredCoversRecyclerView.setAdapter(new UsersAdapter(expiredCovers));
        } else {
            expiredCoversRecyclerView.setVisibility(View.GONE);
            expiredNoCoversTextView.setVisibility(View.VISIBLE);
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

            if (cover.isDroppedDown()) {
                coverHideableContentLinearLayout.setVisibility(View.VISIBLE);
            } else {
                coverHideableContentLinearLayout.setVisibility(View.GONE);
            }

//            for (Cover iteratedCover : DBHandler.getAllUserCovers()) {
//                if (iteratedCover.equals(cover)) {
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
                Drawable draw = ResourcesCompat.getDrawable(getResources(), R.drawable.check_icon_24, null);
                draw.setTint(ResourcesCompat.getColor(getResources(), R.color.defaultGray, null));
                coverRemindButton.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null);
            }

            //If status is confirmed, get rid of some buttons
            if (cover.getStatus().equals("confirmed")) {
                coverCancelButton.setVisibility(View.GONE);
                coverRemindButton.setVisibility(View.GONE);
                coverCancelSeparator.setVisibility(View.GONE);
                coverRemindSeparator.setVisibility(View.GONE);
            }

            coverTypeTextView.setText(cover.getMemo());
            coverTimeAgoTextView.setText(calculateTimeFromCreation(cover.getCreatedTime()));

            coverCancelButton.setOnClickListener(this);
            coverReviewButton.setOnClickListener(this);
            coverRemindButton.setOnClickListener(this);
            coverDropdownButton.setOnClickListener(this);

            //ResourcesCompat.getDrawable(getResources(), R.drawable.check_icon, null) Is the real way to get resources
            switch (cover.getCoverType()) {
                case "cash":
                    coverLogo.setImageResource(R.drawable.cashx2);
                    // coverLogo.setColorFilter(R.color.colorAccent);
                    //coverLogo.setBackground(getResources().getDrawable(R.drawable.cover_cash_icon));
                    break;
                case "lending":
                    coverLogo.setImageResource(R.drawable.lendingx2);
                    // coverLogo.setColorFilter(R.color.colorAccent);
                    // coverLogo.setBackground(getResources().getDrawable(R.drawable.cover_cash_icon));
                    break;
                case "contract":
                    coverLogo.setImageResource(R.drawable.contract);
                    //  coverLogo.setColorFilter(R.color.colorAccent);
                    //coverLogo.setBackground(getResources().getDrawable(R.drawable.cover_cash_icon));
                    break;
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

                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("remind",cover.getDocID());
                        updateMap.put("time", new Timestamp(System.currentTimeMillis()));


                        DBHandler.getDB().collection("reminders")
                                .add(updateMap);

                        Toast.makeText(thisActivity, "Reminder Sent", Toast.LENGTH_SHORT).show();

                    break;

                case R.id.coverDropdownButton:
                    calculateDropdown();
                    break;
            }
        }

        private void calculateDropdown() {
            if (coverHideableContentLinearLayout.getVisibility() == View.VISIBLE) {
                dropup();
            } else {
                dropdown();
            }
        }

        private void dropdown() {
//            cover.setDroppedDown(true);
            setDBHandlerCoverDropdown(cover, true);
            coverHideableContentLinearLayout.setVisibility(View.VISIBLE);
            coverDropdownButton.setImageResource(R.drawable.cover_dropup_arrow);
        }

        private void dropup() {
//            cover.setDroppedDown(false);
            setDBHandlerCoverDropdown(cover, false);
            coverHideableContentLinearLayout.setVisibility(View.GONE);
            coverDropdownButton.setImageResource(R.drawable.cover_dropdown_arrow);
        }

        private void setDBHandlerCoverDropdown(Cover cover, boolean droppedDown) {
            for (Cover dbCover : DBHandler.getAllUserCovers()) {
                if (cover.equals(dbCover)) {
                    dbCover.setDroppedDown(droppedDown);
                    Log.e("**HomeFragment |", "Cover " + dbCover.getMemo() + " dropped down? " + dbCover.isDroppedDown());

                }
            }
            for (Cover newd : DBHandler.getAllUserCovers()) {
                Log.e("**HomeFragment |     neeeee", "Cover " + newd.getMemo() + " dropped down? " + newd.isDroppedDown());
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
                            thisActivity.refreshCovers();
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
