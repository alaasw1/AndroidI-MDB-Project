package com.example.android_imdb_project.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieListFragment extends Fragment {
    private static final String TAG = "MovieListFragment";
    private static final String TMDB_API_KEY = "9b78a0198d671648f859770a80094412";
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
        // Set isFavoritesFragment and isWatchlistFragment to false
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

        Button addButton = view.findViewById(R.id.button_add_movie);
        addButton.setOnClickListener(v -> showAddMovieDialog());

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

    private void showAddMovieDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Movie");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_movie, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.input_movie_name);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String movieName = input.getText().toString();
                addMovieByName(movieName);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addMovieByName(String movieName) {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.themoviedb.org/3/search/movie?api_key=" + TMDB_API_KEY + "&query=" + movieName;
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");
                if (results.length() > 0) {
                    JSONObject movieJson = results.getJSONObject(0);
                    String name = movieJson.getString("title");
                    String releaseDate = movieJson.getString("release_date");
                    String description = movieJson.getString("overview");
                    double rate = movieJson.getDouble("vote_average");
                    String photoUrl = "https://image.tmdb.org/t/p/w500" + movieJson.getString("poster_path");

                    Movie movie = new Movie(name, releaseDate, description, rate, photoUrl);
                    db.collection("movies").add(movie).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            getActivity().runOnUiThread(() -> {
                                movieList.add(movie);
                                filterMovies(editTextFilter.getText().toString());
                                Toast.makeText(getContext(), "Movie added successfully", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Movie not found", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error adding movie", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }
}
