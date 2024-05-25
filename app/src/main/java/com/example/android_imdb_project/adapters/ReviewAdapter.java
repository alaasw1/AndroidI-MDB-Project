package com.example.android_imdb_project.adapters;

import android.content.Context;
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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviews;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
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

        Glide.with(context).load(review.getUserProfilePicture()).into(holder.ivUserProfilePicture);

        holder.ibLike.setOnClickListener(v -> {
            review.setLikes(review.getLikes() + 1);
            db.collection("movies")
                    .document(review.getMovieId())
                    .collection("reviews")
                    .document(review.getReviewId())
                    .update("likes", review.getLikes())
                    .addOnSuccessListener(aVoid -> holder.tvLikes.setText(String.valueOf(review.getLikes())))
                    .addOnFailureListener(e -> review.setLikes(review.getLikes() - 1));
        });

        holder.ibDislike.setOnClickListener(v -> {
            review.setDislikes(review.getDislikes() + 1);
            db.collection("movies")
                    .document(review.getMovieId())
                    .collection("reviews")
                    .document(review.getReviewId())
                    .update("dislikes", review.getDislikes())
                    .addOnSuccessListener(aVoid -> holder.tvDislikes.setText(String.valueOf(review.getDislikes())))
                    .addOnFailureListener(e -> review.setDislikes(review.getDislikes() - 1));
        });

        if (review.getUserId().equals(currentUser.getUid())) {
            holder.ibDelete.setVisibility(View.VISIBLE);
            holder.ibDelete.setOnClickListener(v -> {
                db.collection("movies")
                        .document(review.getMovieId())
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

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivUserProfilePicture;
        public TextView tvUserName, tvContent, tvLikes, tvDislikes;
        public ImageButton ibLike, ibDislike, ibDelete;

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
