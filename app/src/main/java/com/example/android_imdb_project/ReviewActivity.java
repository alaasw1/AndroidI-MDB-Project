package com.example.android_imdb_project;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_imdb_project.adapters.ReviewAdapter;
import com.example.android_imdb_project.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewActivity handles displaying and submitting reviews for a specific movie.
 */
public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";
    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String movieId;
    private String movieName;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Get movie ID and name from the intent
        movieId = getIntent().getStringExtra("movieId");
        movieName = getIntent().getStringExtra("movieName");
        Log.d(TAG, "ReviewActivity started with movieId: " + movieId);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(movieName); // Set the toolbar title to the movie name
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup RecyclerView for displaying reviews
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(this, reviewList, movieId); // Pass movieId to adapter
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Firestore and get current user
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Fetch existing reviews
        fetchReviews();

        // Setup the submit review button and its click listener
        ImageButton ibSubmitReview = findViewById(R.id.ib_submit_review);
        EditText etReviewContent = findViewById(R.id.et_review_content);

        ibSubmitReview.setOnClickListener(v -> {
            String content = etReviewContent.getText().toString().trim();
            if (!TextUtils.isEmpty(content)) {
                addReview(content);
                etReviewContent.setText("");
            } else {
                Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches existing reviews for the movie from Firestore and updates the adapter.
     */
    private void fetchReviews() {
        db.collection("movies")
                .document(movieId)
                .collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            if (review != null) {
                                review.setReviewId(document.getId()); // Set the reviewId
                                reviewList.add(review);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error getting reviews", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Adds a new review to the Firestore database.
     *
     * @param content The content of the review.
     */
    private void addReview(String content) {
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        String userProfilePicture = (currentUser.getPhotoUrl() != null) ? currentUser.getPhotoUrl().toString() : "https://via.placeholder.com/150"; // Use a default image URL if null
        long timestamp = System.currentTimeMillis();

        Review review = new Review(null, userId, userName, userProfilePicture, content, timestamp, movieId); // Pass movieId

        db.collection("movies")
                .document(movieId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setReviewId(documentReference.getId());
                    db.collection("movies")
                            .document(movieId)
                            .collection("reviews")
                            .document(review.getReviewId())
                            .set(review)
                            .addOnSuccessListener(aVoid -> {
                                reviewList.add(review);
                                adapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error adding review", Toast.LENGTH_SHORT).show());
    }
}
