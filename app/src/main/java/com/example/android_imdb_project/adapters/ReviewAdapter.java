package com.example.android_imdb_project.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.example.android_imdb_project.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter class for displaying a list of reviews in a RecyclerView.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private static final String TAG = "ReviewAdapter";
    private Context context;
    private List<Review> reviews;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String movieId;

    /**
     * Constructor for ReviewAdapter.
     *
     * @param context  The context in which the adapter is used.
     * @param reviews  The list of reviews to display.
     * @param movieId  The ID of the movie to which the reviews belong.
     */
    public ReviewAdapter(Context context, List<Review> reviews, String movieId) {
        this.context = context;
        this.reviews = reviews;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.movieId = movieId;

        Log.d(TAG, "ReviewAdapter initialized with movieId: " + movieId);
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.getUserName());
        holder.tvContent.setText(review.getContent());
        holder.tvLikes.setText(String.valueOf(review.getLikes()));
        holder.tvDislikes.setText(String.valueOf(review.getDislikes()));

        // Load user profile picture using Glide
        Glide.with(context)
                .load(review.getUserProfilePicture())
                .placeholder(R.drawable.ic_profile)  // Placeholder image
                .into(holder.ivUserProfilePicture);

        updateButtonStyles(holder, review);

        // Handle like button click
        holder.ibLike.setOnClickListener(v -> {
            Log.d(TAG, "Like button clicked for reviewId: " + review.getReviewId());
            if (movieId == null || review.getReviewId() == null) {
                Log.e(TAG, "movieId or reviewId is null");
                return;
            }
            handleLikeDislikeAction(review, holder, true);
        });

        // Handle dislike button click
        holder.ibDislike.setOnClickListener(v -> {
            Log.d(TAG, "Dislike button clicked for reviewId: " + review.getReviewId());
            if (movieId == null || review.getReviewId() == null) {
                Log.e(TAG, "movieId or reviewId is null");
                return;
            }
            handleLikeDislikeAction(review, holder, false);
        });

        // Handle delete button visibility and click
        if (review.getUserId().equals(currentUser.getUid())) {
            holder.ibDelete.setVisibility(View.VISIBLE);
            holder.ibDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for reviewId: " + review.getReviewId());
                if (movieId == null || review.getReviewId() == null) {
                    Log.e(TAG, "movieId or reviewId is null");
                    return;
                }
                db.collection("movies")
                        .document(movieId)
                        .collection("reviews")
                        .document(review.getReviewId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            reviews.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, reviews.size());
                        });
            });
        } else {
            holder.ibDelete.setVisibility(View.GONE);
        }
    }

    /**
     * Handles the like or dislike action for a review.
     *
     * @param review   The review to be liked or disliked.
     * @param holder   The ViewHolder containing the views to be updated.
     * @param isLike   True if the action is a like, false if it is a dislike.
     */
    private void handleLikeDislikeAction(Review review, ReviewViewHolder holder, boolean isLike) {
        if (isLike) {
            if (!review.getLikedBy().contains(currentUser.getUid())) {
                if (review.getDislikedBy().contains(currentUser.getUid())) {
                    review.setDislikes(review.getDislikes() - 1);
                    review.getDislikedBy().remove(currentUser.getUid());
                }
                review.setLikes(review.getLikes() + 1);
                review.getLikedBy().add(currentUser.getUid());
            } else {
                review.setLikes(review.getLikes() - 1);
                review.getLikedBy().remove(currentUser.getUid());
            }
        } else {
            if (!review.getDislikedBy().contains(currentUser.getUid())) {
                if (review.getLikedBy().contains(currentUser.getUid())) {
                    review.setLikes(review.getLikes() - 1);
                    review.getLikedBy().remove(currentUser.getUid());
                }
                review.setDislikes(review.getDislikes() + 1);
                review.getDislikedBy().add(currentUser.getUid());
            } else {
                review.setDislikes(review.getDislikes() - 1);
                review.getDislikedBy().remove(currentUser.getUid());
            }
        }

        db.collection("movies")
                .document(movieId)
                .collection("reviews")
                .document(review.getReviewId())
                .update("likes", review.getLikes(), "dislikes", review.getDislikes(),
                        "likedBy", review.getLikedBy(), "dislikedBy", review.getDislikedBy())
                .addOnSuccessListener(aVoid -> {
                    holder.tvLikes.setText(String.valueOf(review.getLikes()));
                    holder.tvDislikes.setText(String.valueOf(review.getDislikes()));
                    updateButtonStyles(holder, review);
                })
                .addOnFailureListener(e -> {
                    if (isLike) {
                        review.setLikes(review.getLikes() - 1);
                        review.getLikedBy().remove(currentUser.getUid());
                    } else {
                        review.setDislikes(review.getDislikes() - 1);
                        review.getDislikedBy().remove(currentUser.getUid());
                    }
                });
    }

    /**
     * Updates the styles of the like and dislike buttons based on user actions.
     *
     * @param holder  The ViewHolder containing the buttons to be updated.
     * @param review  The review object to check for likes and dislikes.
     */
    private void updateButtonStyles(ReviewViewHolder holder, Review review) {
        if (review.getLikedBy().contains(currentUser.getUid())) {
            holder.ibLike.setColorFilter(Color.BLUE);
            holder.ibLike.setAlpha(1.0f);
            holder.ibDislike.setAlpha(0.5f);
        } else if (review.getDislikedBy().contains(currentUser.getUid())) {
            holder.ibDislike.setColorFilter(Color.BLUE);
            holder.ibDislike.setAlpha(1.0f);
            holder.ibLike.setAlpha(0.5f);
        } else {
            holder.ibLike.setColorFilter(null);
            holder.ibDislike.setColorFilter(null);
            holder.ibLike.setAlpha(1.0f);
            holder.ibDislike.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * ViewHolder class for displaying individual review items in the RecyclerView.
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivUserProfilePicture;
        public TextView tvUserName, tvContent, tvLikes, tvDislikes;
        public ImageButton ibLike, ibDislike, ibDelete;

        /**
         * Constructor for ReviewViewHolder.
         *
         * @param itemView The view of the individual review item.
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfilePicture = itemView.findViewById(R.id.iv_user_profile_picture);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvDislikes = itemView.findViewById(R.id.tv_dislikes);
            ibLike = itemView.findViewById(R.id.ib_like);
            ibDislike = itemView.findViewById(R.id.ib_dislike);
            ibDelete = itemView.findViewById(R.id.ib_delete);
        }
    }
}
