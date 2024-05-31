package com.example.android_imdb_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android_imdb_project.R;
import com.example.android_imdb_project.ReviewActivity;
import com.example.android_imdb_project.models.Movie;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Adapter class for displaying a list of movies in a RecyclerView.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private static final String TAG = "MovieAdapter";
    private List<Movie> movies;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private boolean isFavoritesFragment;
    private boolean isWatchlistFragment;
    private boolean showReviewButton;

    /**
     * Constructor for MovieAdapter.
     *
     * @param context             The context in which the adapter is used.
     * @param movies              The list of movies to display.
     * @param isFavoritesFragment Indicates if the adapter is used in the favorites fragment.
     * @param isWatchlistFragment Indicates if the adapter is used in the watchlist fragment.
     * @param showReviewButton    Indicates if the review button should be shown.
     */
    public MovieAdapter(Context context, List<Movie> movies, boolean isFavoritesFragment, boolean isWatchlistFragment, boolean showReviewButton) {
        this.context = context;
        this.movies = movies;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.isFavoritesFragment = isFavoritesFragment;
        this.isWatchlistFragment = isWatchlistFragment;
        this.showReviewButton = showReviewButton;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.movieName.setText(movie.getName());
        holder.releaseDate.setText(movie.getReleaseDate());
        holder.movieDescription.setText(movie.getDescription());
        holder.movieRate.setText(String.valueOf(movie.getRate()));

        // Setting placeholder and error images using Glide
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder) // Placeholder image
                .error(R.drawable.error); // Error image

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(movie.getPhotoUrl())
                .into(holder.moviePhoto);

        if (isFavoritesFragment || isWatchlistFragment) {
            holder.buttonFavorite.setVisibility(View.GONE);
            holder.buttonWatchlist.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonReview.setVisibility(showReviewButton ? View.VISIBLE : View.GONE);
            holder.buttonDelete.setOnClickListener(v -> {
                if (isFavoritesFragment) {
                    removeFromCollection("favorites", movie);
                } else {
                    removeFromCollection("watchlist", movie);
                }
            });
        } else {
            holder.buttonFavorite.setVisibility(View.VISIBLE);
            holder.buttonWatchlist.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.GONE);
            holder.buttonReview.setVisibility(View.VISIBLE);
            holder.buttonFavorite.setOnClickListener(v -> addToCollection("favorites", movie));
            holder.buttonWatchlist.setOnClickListener(v -> addToCollection("watchlist", movie));
        }

        holder.buttonReview.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewActivity.class);
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("movieName", movie.getName());
            context.startActivity(intent);
        });
    }

    /**
     * Adds a movie to the specified Firestore collection.
     *
     * @param collectionName The name of the collection (favorites or watchlist).
     * @param movie          The movie to add.
     */
    private void addToCollection(String collectionName, Movie movie) {
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection(collectionName)
                    .whereEqualTo("name", movie.getName())
                    .whereEqualTo("releaseDate", movie.getReleaseDate())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                db.collection("users")
                                        .document(currentUser.getUid())
                                        .collection(collectionName)
                                        .add(movie)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "Movie added to " + collectionName + " with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding movie to " + collectionName, e);
                                            }
                                        });
                            } else {
                                Log.d(TAG, "Movie already exists in " + collectionName);
                            }
                        } else {
                            Log.w(TAG, "Error checking movie in " + collectionName, task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "User not authenticated. Cannot add movie to " + collectionName);
        }
    }

    /**
     * Removes a movie from the specified Firestore collection.
     *
     * @param collectionName The name of the collection (favorites or watchlist).
     * @param movie          The movie to remove.
     */
    private void removeFromCollection(String collectionName, Movie movie) {
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection(collectionName)
                    .whereEqualTo("name", movie.getName())
                    .whereEqualTo("releaseDate", movie.getReleaseDate())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentReference documentReference = task.getResult().getDocuments().get(0).getReference();
                            documentReference.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Movie removed from " + collectionName);
                                        movies.remove(movie);
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Error removing movie from " + collectionName, e));
                        } else {
                            Log.w(TAG, "Movie not found in " + collectionName);
                        }
                    });
        } else {
            Log.w(TAG, "User not authenticated. Cannot remove movie from " + collectionName);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * ViewHolder class for displaying individual movie items in the RecyclerView.
     */
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        public TextView movieName, releaseDate, movieDescription, movieRate;
        public ImageView moviePhoto;
        public Button buttonFavorite, buttonWatchlist, buttonDelete, buttonReview;

        /**
         * Constructor for MovieViewHolder.
         *
         * @param itemView The view of the individual movie item.
         */
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieName = itemView.findViewById(R.id.movie_name);
            releaseDate = itemView.findViewById(R.id.release_date);
            movieDescription = itemView.findViewById(R.id.movie_description);
            movieRate = itemView.findViewById(R.id.movie_rate);
            moviePhoto = itemView.findViewById(R.id.movie_photo);
            buttonFavorite = itemView.findViewById(R.id.button_favorite);
            buttonWatchlist = itemView.findViewById(R.id.button_watchlist);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            buttonReview = itemView.findViewById(R.id.button_review);
        }
    }
}
