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

public class MovieListFragment extends Fragment {
    private static final String TAG = "MovieListFragment";
    private static final String TMDB_API_KEY = "9b78a0198d671648f859770a80094412";
    private static final int RETRY_DELAY_MS = 1000;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> filteredMovieList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText editTextFilter;
    private Handler handler = new Handler();

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

        // Add example movies if the collection is empty
        addMoviesOnce();

        // Fetch movies from Firestore
        fetchMovies();

        return view;
    }

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

    private void addMoviesOnce() {
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.isEmpty()) {
                    Log.d(TAG, "Adding example movies to Firestore.");

                    // Example movies data
                    List<Map<String, Object>> movies = new ArrayList<>();

                    movies.add(createMovie("The Shawshank Redemption", "1994-09-23", "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.", 9.3, "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg"));
                    movies.add(createMovie("The Godfather", "1972-03-24", "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.", 9.2, "https://image.tmdb.org/t/p/w500/iVZ3JAcAjmguGPnRNfWFOtLHOuY.jpg"));
                    movies.add(createMovie("The Dark Knight", "2008-07-18", "When the menace known as the Joker emerges from his mysterious past, he wreaks havoc and chaos on the people of Gotham.", 9.0, "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"));
                    movies.add(createMovie("The Godfather: Part II", "1974-12-20", "The early life and career of Vito Corleone in 1920s New York City is portrayed while his son, Michael, expands and tightens his grip on the family crime syndicate.", 9.0, "https://image.tmdb.org/t/p/w500/hek3koDUyRQk7FIhPXsa6mT2Zc3.jpg"));
                    movies.add(createMovie("12 Angry Men", "1957-04-10", "A jury holdout attempts to prevent a miscarriage of justice by forcing his colleagues to reconsider the evidence.", 8.9, "https://image.tmdb.org/t/p/w500/ow3wq89wM8qd5X7hWKxiRfsFf9C.jpg"));
                    movies.add(createMovie("Schindler's List", "1993-12-15", "In German-occupied Poland during World War II, industrialist Oskar Schindler gradually becomes concerned for his Jewish workforce after witnessing their persecution by the Nazis.", 8.9, "https://image.tmdb.org/t/p/w500/c8Ass7acuOe4za6DhSattE359gr.jpg"));
                    movies.add(createMovie("The Lord of the Rings: The Return of the King", "2003-12-17", "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam as they approach Mount Doom with the One Ring.", 8.9, "https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg"));
                    movies.add(createMovie("Pulp Fiction", "1994-10-14", "The lives of two mob hitmen, a boxer, a gangster's wife, and a pair of diner bandits intertwine in four tales of violence and redemption.", 8.9, "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"));
                    movies.add(createMovie("The Good, the Bad and the Ugly", "1966-12-23", "A bounty hunting scam joins two men in an uneasy alliance against a third in a race to find a fortune in gold buried in a remote cemetery.", 8.8, "https://image.tmdb.org/t/p/w500/bX2xnavhMYjWDoZp1VM6VnU1xwe.jpg"));
                    movies.add(createMovie("The Lord of the Rings: The Fellowship of the Ring", "2001-12-19", "A meek Hobbit from the Shire and eight companions set out on a journey to destroy the powerful One Ring and save Middle-earth from the Dark Lord Sauron.", 8.8, "https://image.tmdb.org/t/p/w500/6oom5QYQ2yQTMJIbnvbkBL9cHo6.jpg"));
                    movies.add(createMovie("The Lord of the Rings: The Two Towers", "2002-12-18", "While Frodo and Sam edge closer to Mordor with the help of the shifty Gollum, the divided fellowship makes a stand against Sauron's new ally, Saruman, and his hordes of Isengard.", 8.8, "https://image.tmdb.org/t/p/w500/5VTN0pR8gcqV3EPUHHfMGnJYN9L.jpg"));
                    movies.add(createMovie("Star Wars: Episode V - The Empire Strikes Back", "1980-05-21", "After the Rebels are overpowered by the Empire on their newly established base, Luke Skywalker begins Jedi training with Yoda, while his friends are pursued across the galaxy by Darth Vader.", 8.7, "https://image.tmdb.org/t/p/w500/2l05cFWJacyIsTpsqSgH0wQXe4V.jpg"));
                    movies.add(createMovie("The Matrix", "1999-03-31", "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.", 8.7, "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg"));
                    movies.add(createMovie("Goodfellas", "1990-09-12", "The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners Jimmy Conway and Tommy DeVito in the Italian-American crime syndicate.", 8.7, "https://image.tmdb.org/t/p/w500/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg"));
                    movies.add(createMovie("One Flew Over the Cuckoo's Nest", "1975-11-19", "A criminal pleads insanity and is admitted to a mental institution, where he rebels against the oppressive nurse and rallies up the scared patients.", 8.7, "https://image.tmdb.org/t/p/w500/3jcbDmRFiQ83drXNOvRDeKHxS0C.jpg"));
                    movies.add(createMovie("Se7en", "1995-09-22", "Two detectives, a rookie and a veteran, hunt a serial killer who uses the seven deadly sins as his motives.", 8.6, "https://image.tmdb.org/t/p/w500/69Sns8WoET6CfaYlIkHbla4l7nC.jpg"));
                    movies.add(createMovie("Seven Samurai", "1954-04-26", "A poor village under attack by bandits recruits seven unemployed samurai to help them defend themselves.", 8.6, "https://image.tmdb.org/t/p/w500/8OKmBV5BUFzMO27KWpDRuZUl5jz.jpg"));

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
