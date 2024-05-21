package com.example.android_imdb_project.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_imdb_project.R;
import com.example.android_imdb_project.adapters.MovieAdapter;
import com.example.android_imdb_project.models.Movie;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieListFragment extends Fragment {
    private static final String TAG = "MovieListFragment";
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> filteredMovieList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText editTextFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set isFavoritesFragment to false
        adapter = new MovieAdapter(getContext(), filteredMovieList, false, false);
        recyclerView.setAdapter(adapter);

        editTextFilter = view.findViewById(R.id.edit_text_filter);
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch movies from Firestore
        fetchMovies();

        return view;
    }

    private void fetchMovies() {
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            Log.d(TAG, "Movie URL: " + movie.getPhotoUrl());
                            movieList.add(movie);
                        }
                        filteredMovieList.clear();
                        filteredMovieList.addAll(movieList);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void filterMovies(String query) {
        filteredMovieList.clear();
        if (query.isEmpty()) {
            filteredMovieList.addAll(movieList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredMovieList.addAll(movieList.stream()
                    .filter(movie -> movie.getName().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList()));
        }
        adapter.notifyDataSetChanged();
    }
}
