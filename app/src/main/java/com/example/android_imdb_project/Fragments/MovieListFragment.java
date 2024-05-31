package com.example.android_imdb_project.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A fragment representing the list of movies.
 */
public class MovieListFragment extends Fragment {
    private static final String TAG = "MovieListFragment";
    private static final String TMDB_API_KEY = "9b78a0198d671648f859770a80094412"; // Your TMDB API Key
    private static final int RETRY_DELAY_MS = 1000; // Retry delay in milliseconds
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> filteredMovieList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText editTextFilter;
    private Handler handler = new Handler();

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     *                 any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     *                  UI should be attached to. The fragment should not add the view itself,
     *                  but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set isFavoritesFragment and isWatchlistFragment to false, and showReviewButton to true
        adapter = new MovieAdapter(getContext(), filteredMovieList, false, false, true);
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

        // Add example movies if the collection is empty
        addMoviesOnce();

        // Fetch movies from Firestore
        fetchMovies();

        return view;
    }

    /**
     * Fetches the list of movies from Firestore.
     */
    private void fetchMovies() {
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            movieList.clear();
                            for (QueryDocumentSnapshot document : result) {
                                Movie movie = document.toObject(Movie.class);
                                movie.setId(document.getId()); // Set the movie ID
                                Log.d(TAG, "Movie URL: " + movie.getPhotoUrl());
                                movieList.add(movie);
                            }
                            filteredMovieList.clear();
                            filteredMovieList.addAll(movieList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No movies found, retrying in 1 second.");
                            handler.postDelayed(this::fetchMovies, RETRY_DELAY_MS);
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    /**
     * Filters the list of movies based on the query.
     *
     * @param query The search query entered by the user.
     */
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

    /**
     * Shows a dialog to add a new movie.
     */
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

    /**
     * Adds a movie by its name using The Movie Database (TMDB) API.
     *
     * @param movieName The name of the movie to add.
     */
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

                    Movie movie = new Movie("", name, releaseDate, description, rate, photoUrl);
                    checkIfMovieExistsAndAdd(movie);
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Movie not found", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error adding movie", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Checks if a movie already exists in Firestore and adds it if it doesn't.
     *
     * @param movie The movie to check and add.
     */
    private void checkIfMovieExistsAndAdd(Movie movie) {
        String lowerCaseMovieName = movie.getName().toLowerCase();
        db.collection("movies")
                .whereEqualTo("nameLowerCase", lowerCaseMovieName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && result.isEmpty()) {
                            Map<String, Object> movieMap = createMovieMap(movie);
                            movieMap.put("nameLowerCase", lowerCaseMovieName);
                            db.collection("movies").add(movieMap).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentReference documentReference = task1.getResult();
                                    movie.setId(documentReference.getId()); // Set the movie ID
                                    getActivity().runOnUiThread(() -> {
                                        movieList.add(movie);
                                        filterMovies(editTextFilter.getText().toString());
                                        Toast.makeText(getContext(), "Movie added successfully", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Movie already exists in the database", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error checking for movie existence", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    /**
     * Adds example movies to Firestore if the collection is empty.
     */
    private void addMoviesOnce() {
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.isEmpty()) {
                    Log.d(TAG, "Adding example movies to Firestore.");

                    // Example movies data
                    List<Map<String, Object>> movies = new ArrayList<>();

                    movies.add(createMovie("Avatar", "2009-12-15", "In the 22nd century, a paraplegic Marine is dispatched to the moon Pandora on a unique mission, but becomes torn between following orders and protecting an alien civilization.", 7.581, "https://image.tmdb.org/t/p/w500/kyeqWdyUXW608qlYkRqosgbbJyK.jpg"));
                    movies.add(createMovie("Avengers: Endgame", "2019-04-24", "After the devastating events of Avengers: Infinity War, the universe is in ruins due to the efforts of the Mad Titan, Thanos. With the help of remaining allies, the Avengers must assemble once more in order to undo Thanos' actions and restore order to the universe once and for all, no matter what consequences may be in store.", 8.254, "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg"));
                    movies.add(createMovie("Furious 7", "2015-04-01", "Deckard Shaw seeks revenge against Dominic Toretto and his family for his comatose brother.", 7.235, "https://image.tmdb.org/t/p/w500/ktofZ9Htrjiy0P6LEowsDaxd3Ri.jpg"));
                    // Add movies to Firestore
                    for (Map<String, Object> movie : movies) {
                        addMovieIfNotExists(movie);
                    }

                    // Fetch movies after adding example movies
                    fetchMovies();
                } else {
                    Log.d(TAG, "Movies already exist in Firestore.");
                }
            } else {
                Log.e(TAG, "Error fetching movies: ", task.getException());
            }
        });
    }

    /**
     * Adds a movie to Firestore if it doesn't already exist.
     *
     * @param movie The movie to add.
     */
    private void addMovieIfNotExists(Map<String, Object> movie) {
        String lowerCaseMovieName = movie.get("name").toString().toLowerCase();
        db.collection("movies")
                .whereEqualTo("nameLowerCase", lowerCaseMovieName)
                .whereEqualTo("releaseDate", movie.get("releaseDate"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            db.collection("movies")
                                    .add(movie)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Movie already exists in Firestore: " + movie.get("name"));
                        }
                    } else {
                        Log.w(TAG, "Error checking for movie existence: ", task.getException());
                    }
                });
    }

    /**
     * Creates a map representing a movie.
     *
     * @param name The name of the movie.
     * @param releaseDate The release date of the movie.
     * @param description The description of the movie.
     * @param rate The rating of the movie.
     * @param photoUrl The photo URL of the movie.
     * @return A map representing the movie.
     */
    private Map<String, Object> createMovie(String name, String releaseDate, String description, double rate, String photoUrl) {
        Map<String, Object> movie = new HashMap<>();
        movie.put("name", name);
        movie.put("nameLowerCase", name.toLowerCase());
        movie.put("releaseDate", releaseDate);
        movie.put("description", description);
        movie.put("rate", rate);
        movie.put("photoUrl", photoUrl);
        return movie;
    }

    /**
     * Creates a map representing a movie.
     *
     * @param movie The movie object.
     * @return A map representing the movie.
     */
    private Map<String, Object> createMovieMap(Movie movie) {
        Map<String, Object> movieMap = new HashMap<>();
        movieMap.put("name", movie.getName());
        movieMap.put("releaseDate", movie.getReleaseDate());
        movieMap.put("description", movie.getDescription());
        movieMap.put("rate", movie.getRate());
        movieMap.put("photoUrl", movie.getPhotoUrl());
        movieMap.put("nameLowerCase", movie.getName().toLowerCase());
        return movieMap;
    }
}
