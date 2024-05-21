package com.example.android_imdb_project.adapters;

import android.content.Context;
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
import com.example.android_imdb_project.models.Movie;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private static final String TAG = "MovieAdapter";
    private List<Movie> movies;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private boolean isFavoritesFragment;

    public MovieAdapter(Context context, List<Movie> movies, boolean isFavoritesFragment) {
        this.context = context;
        this.movies = movies;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.isFavoritesFragment = isFavoritesFragment;
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

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder) // Placeholder image
                .error(R.drawable.error); // Error image

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(movie.getPhotoUrl())
                .into(holder.moviePhoto);

        if (isFavoritesFragment) {
            holder.buttonFavorite.setVisibility(View.GONE);
            holder.buttonWatchlist.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.buttonDelete.setOnClickListener(v -> removeFromFavorites(movie));
        } else {
            holder.buttonFavorite.setVisibility(View.VISIBLE);
            holder.buttonWatchlist.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.GONE);
            holder.buttonFavorite.setOnClickListener(v -> addToCollection("favorites", movie));
            holder.buttonWatchlist.setOnClickListener(v -> addToCollection("watchlist", movie));
        }
    }

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

    private void removeFromFavorites(Movie movie) {
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("favorites")
                    .whereEqualTo("name", movie.getName())
                    .whereEqualTo("releaseDate", movie.getReleaseDate())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentReference documentReference = task.getResult().getDocuments().get(0).getReference();
                            documentReference.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Movie removed from favorites");
                                        movies.remove(movie);
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "Error removing movie from favorites", e));
                        } else {
                            Log.w(TAG, "Movie not found in favorites");
                        }
                    });
        } else {
            Log.w(TAG, "User not authenticated. Cannot remove movie from favorites");
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        public TextView movieName, releaseDate, movieDescription, movieRate;
        public ImageView moviePhoto;
        public Button buttonFavorite, buttonWatchlist, buttonDelete;

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
        }
    }
}
