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

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
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

        holder.buttonFavorite.setOnClickListener(v -> addToCollection("favorites", movie));
        holder.buttonWatchlist.setOnClickListener(v -> addToCollection("watchlist", movie));
    }

    private void addToCollection(String collectionName, Movie movie) {
        if (currentUser != null) {
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
            Log.w(TAG, "User not authenticated. Cannot add movie to " + collectionName);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        public TextView movieName, releaseDate, movieDescription, movieRate;
        public ImageView moviePhoto;
        public Button buttonFavorite, buttonWatchlist;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            movieName = itemView.findViewById(R.id.movie_name);
            releaseDate = itemView.findViewById(R.id.release_date);
            movieDescription = itemView.findViewById(R.id.movie_description);
            movieRate = itemView.findViewById(R.id.movie_rate);
            moviePhoto = itemView.findViewById(R.id.movie_photo);
            buttonFavorite = itemView.findViewById(R.id.button_favorite);
            buttonWatchlist = itemView.findViewById(R.id.button_watchlist);
        }
    }
}
