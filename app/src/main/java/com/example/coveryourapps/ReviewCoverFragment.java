package com.example.coveryourapps;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReviewCoverFragment extends Fragment implements View.OnClickListener {
    private MainActivity thisActivity;
    private RecyclerView imagesRecyclerView;

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
            contractReviewText.setText(thisActivity.getReviewCover().getContent().replace("\\n", "\n"));//Properly format newlines
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
            view = inflater.inflate(R.layout.fragment_review_lending, container, false);
            TextView lendingReviewTitle = view.findViewById(R.id.lendingReviewTitle);
            TextView lendingReviewText = view.findViewById(R.id.lendingReviewText);

            lendingReviewTitle.setText(thisActivity.getReviewCover().getMemo());
            lendingReviewText.setText(thisActivity.getReviewCover().getContent());

            LinearLayoutManager horizontalLayout = new LinearLayoutManager(thisActivity, LinearLayoutManager.HORIZONTAL, false);
            imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);
            imagesRecyclerView.setLayoutManager(horizontalLayout);
            imagesRecyclerView.setAdapter(new ReviewCoverFragment.ImagesAdapter(thisActivity.getReviewCover().getPictures()));
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


    class ImagesAdapter extends RecyclerView.Adapter<ReviewCoverFragment.ImageViewHolder> {
        private ArrayList<String> urls;

        public ImagesAdapter(ArrayList<String> urls) {
            super();
            this.urls = urls;
        }

        @NonNull
        @Override
        public ReviewCoverFragment.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReviewCoverFragment.ImageViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewCoverFragment.ImageViewHolder holder, int position) {
            holder.bind(this.urls.get(position));
        }

        @Override
        public int getItemCount() {
            if (urls != null) {
                return this.urls.size();
            } else {
                return 0;
            }
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private ImageButton deleteButton;
        private String uri;

        public ImageViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.lend_image_item, container, false));
            image = itemView.findViewById(R.id.image);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(String theUrl) {
            this.uri = theUrl;
            Picasso.get().load(uri).into(this.image);
            deleteButton.setVisibility(View.GONE);
        }
    }
}