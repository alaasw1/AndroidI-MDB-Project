package com.example.android_imdb_project;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current user

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_watchlist, R.id.navigation_favorite, R.id.navigation_profile)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Add example movies if the collection is empty
        addMoviesOnce();
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
                    movies.add(createMovie("Fight Club", "1999-10-15", "An insomniac office worker and a devil-may-care soap maker form an underground fight club that evolves into much more.", 8.8, "https://image.tmdb.org/t/p/w500/adw6Lq9FiC9zjYEpOqfq03ituwp.jpg"));
                    movies.add(createMovie("Forrest Gump", "1994-07-06", "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal, and other historical events unfold from the perspective of an Alabama man with an IQ of 75.", 8.8, "https://image.tmdb.org/t/p/w500/yE5d3BUhE8hCnkMUJOo1QDoOGNz.jpg"));
                    movies.add(createMovie("Inception", "2010-07-16", "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.", 8.8, "https://image.tmdb.org/t/p/w500/qmDpIHrmpJINaRKAfWQfftjCdyi.jpg"));
                    movies.add(createMovie("The Lord of the Rings: The Two Towers", "2002-12-18", "While Frodo and Sam edge closer to Mordor with the help of the shifty Gollum, the divided fellowship makes a stand against Sauron's new ally, Saruman, and his hordes of Isengard.", 8.8, "https://image.tmdb.org/t/p/w500/5VTN0pR8gcqV3EPUHHfMGnJYN9L.jpg"));
                    movies.add(createMovie("Star Wars: Episode V - The Empire Strikes Back", "1980-05-21", "After the Rebels are overpowered by the Empire on their newly established base, Luke Skywalker begins Jedi training with Yoda, while his friends are pursued across the galaxy by Darth Vader.", 8.7, "https://image.tmdb.org/t/p/w500/2l05cFWJacyIsTpsqSgH0wQXe4V.jpg"));
                    movies.add(createMovie("The Matrix", "1999-03-31", "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.", 8.7, "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg"));
                    movies.add(createMovie("Goodfellas", "1990-09-12", "The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners Jimmy Conway and Tommy DeVito in the Italian-American crime syndicate.", 8.7, "https://image.tmdb.org/t/p/w500/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg"));
                    movies.add(createMovie("One Flew Over the Cuckoo's Nest", "1975-11-19", "A criminal pleads insanity and is admitted to a mental institution, where he rebels against the oppressive nurse and rallies up the scared patients.", 8.7, "https://image.tmdb.org/t/p/w500/3jcbDmRFiQ83drXNOvRDeKHxS0C.jpg"));
                    movies.add(createMovie("Se7en", "1995-09-22", "Two detectives, a rookie and a veteran, hunt a serial killer who uses the seven deadly sins as his motives.", 8.6, "https://image.tmdb.org/t/p/w500/69Sns8WoET6CfaYlIkHbla4l7nC.jpg"));
                    movies.add(createMovie("Seven Samurai", "1954-04-26", "A poor village under attack by bandits recruits seven unemployed samurai to help them defend themselves.", 8.6, "https://image.tmdb.org/t/p/w500/8OKmBV5BUFzMO27KWpDRuZUl5jz.jpg"));

                    // Add movies to Firestore
                    for (Map<String, Object> movie : movies) {
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
                    }
                } else {
                    Log.d(TAG, "Movies already exist in Firestore.");
                }
            } else {
                Log.e(TAG, "Error fetching movies: ", task.getException());
            }
        });
    }

    private Map<String, Object> createMovie(String name, String releaseDate, String description, double rate, String photoUrl) {
        Map<String, Object> movie = new HashMap<>();
        movie.put("name", name);
        movie.put("releaseDate", releaseDate);
        movie.put("description", description);
        movie.put("rate", rate);
        movie.put("photoUrl", photoUrl);
        return movie;
    }
}
