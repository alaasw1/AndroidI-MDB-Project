package com.example.android_imdb_project.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_imdb_project.R;
import com.example.android_imdb_project.adapters.MovieAdapter;
import com.example.android_imdb_project.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass representing the user's watchlist.
 */
public class WatchlistFragment extends Fragment {
    private static final String TAG = "WatchlistFragment";
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    /**
     * Required empty public constructor.
     */
    public WatchlistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize MovieAdapter with isWatchlistFragment set to true
        adapter = new MovieAdapter(getContext(), movieList, false, true, false);
        recyclerView.setAdapter(adapter);

        // Fetch movies in the user's watchlist
        fetchWatchlistMovies();

        return view;
    }

    /**
     * Fetches movies from the user's watchlist in Firestore and updates the RecyclerView.
     */
    private void fetchWatchlistMovies() {
        if (currentUser != null) {
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("watchlist")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            movieList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Movie movie = document.toObject(Movie.class);
                                movieList.add(movie);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    });
        }
    }
}
