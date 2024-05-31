package com.example.android_imdb_project.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing the user's profile.
 */
public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1; // Request code for image picker
    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore db;
    private ImageView imv_propic;
    private TextView et_display_name, et_display_email;
    private CardView cardWatchlist, cardFavorites;
    private TextView tvWatchlistCount, tvFavoritesCount;
    private ImageView ivWatchlist1, ivWatchlist2, ivWatchlist3;
    private ImageView ivFavorites1, ivFavorites2, ivFavorites3;

    /**
     * Required empty public constructor.
     */
    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth, Firestore, and Storage
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        imv_propic = view.findViewById(R.id.imv_propic);
        et_display_name = view.findViewById(R.id.et_display_name);
        et_display_email = view.findViewById(R.id.et_display_email);
        cardWatchlist = view.findViewById(R.id.card_watchlist);
        cardFavorites = view.findViewById(R.id.card_favorites);
        tvWatchlistCount = view.findViewById(R.id.tv_watchlist_count);
        tvFavoritesCount = view.findViewById(R.id.tv_favorites_count);
        ivWatchlist1 = view.findViewById(R.id.iv_watchlist_1);
        ivWatchlist2 = view.findViewById(R.id.iv_watchlist_2);
        ivWatchlist3 = view.findViewById(R.id.iv_watchlist_3);
        ivFavorites1 = view.findViewById(R.id.iv_favorites_1);
        ivFavorites2 = view.findViewById(R.id.iv_favorites_2);
        ivFavorites3 = view.findViewById(R.id.iv_favorites_3);

        // Load current user info
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).into(imv_propic);
            } else {
                imv_propic.setImageResource(R.drawable.profile_circle_icon); // Default profile icon
            }

            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                et_display_name.setText(displayName);
            } else {
                et_display_name.setText("No display name");
            }
            et_display_email.setText(currentUser.getEmail());
        }

        // Set click listener to change profile picture
        imv_propic.setOnClickListener(v -> openImagePicker());

        // Fetch watchlist and favorites data
        fetchWatchlistData();
        fetchFavoritesData();

        return view;
    }

    /**
     * Opens the image picker for the user to select a new profile picture.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            }
        }
    }

    /**
     * Uploads the selected image to Firebase Storage and updates the user's profile picture.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageReference = mStorage.getReference()
                .child("profile_images")
                .child(mAuth.getCurrentUser().getUid() + ".jpg");

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            updateProfilePicture(uri);
                            Glide.with(ProfileFragment.this).load(uri).into(imv_propic);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to retrieve download URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the user's profile picture in Firebase Authentication.
     *
     * @param downloadUri The URI of the uploaded profile picture.
     */
    private void updateProfilePicture(Uri downloadUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUri)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Profile picture update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Fetches the watchlist data from Firestore and updates the UI.
     */
    private void fetchWatchlistData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .collection("watchlist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> movieUrls = new ArrayList<>();
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            count++;
                            String photoUrl = document.getString("photoUrl");
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                movieUrls.add(photoUrl);
                            }
                        }
                        Log.d(TAG, "fetchWatchlistData: Movies count = " + count);
                        tvWatchlistCount.setText("Movies: " + count);
                        displayMovieImages(movieUrls, ivWatchlist1, ivWatchlist2, ivWatchlist3, R.drawable.watchlist_default);
                    } else {
                        Log.w(TAG, "Error getting watchlist documents.", task.getException());
                    }
                });
    }

    /**
     * Fetches the favorites data from Firestore and updates the UI.
     */
    private void fetchFavoritesData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> movieUrls = new ArrayList<>();
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            count++;
                            String photoUrl = document.getString("photoUrl");
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                movieUrls.add(photoUrl);
                            }
                        }
                        Log.d(TAG, "fetchFavoritesData: Movies count = " + count);
                        tvFavoritesCount.setText("Movies: " + count);
                        displayMovieImages(movieUrls, ivFavorites1, ivFavorites2, ivFavorites3, R.drawable.favorites_default);
                    } else {
                        Log.w(TAG, "Error getting favorites documents.", task.getException());
                    }
                });
    }

    /**
     * Displays movie images in the given ImageViews. Shows default image if no movies are available.
     *
     * @param movieUrls The list of movie URLs.
     * @param iv1       The first ImageView.
     * @param iv2       The second ImageView.
     * @param iv3       The third ImageView.
     * @param defaultImage The default image resource ID.
     */
    private void displayMovieImages(List<String> movieUrls, ImageView iv1, ImageView iv2, ImageView iv3, int defaultImage) {
        int size = movieUrls.size();
        iv1.setVisibility(View.INVISIBLE);
        iv2.setVisibility(View.INVISIBLE);
        iv3.setVisibility(View.INVISIBLE);

        if (size == 1) {
            iv1.setVisibility(View.VISIBLE);
            iv1.setLayoutParams(createLayoutParams(100)); // Weight of 100
            Glide.with(this).load(movieUrls.get(0)).into(iv1);
        } else if (size == 2) {
            iv1.setVisibility(View.VISIBLE);
            iv2.setVisibility(View.VISIBLE);
            iv1.setLayoutParams(createLayoutParams(50)); // Weight of 50
            iv2.setLayoutParams(createLayoutParams(50)); // Weight of 50
            Glide.with(this).load(movieUrls.get(0)).into(iv1);
            Glide.with(this).load(movieUrls.get(1)).into(iv2);
        } else if (size >= 3) {
            iv1.setVisibility(View.VISIBLE);
            iv2.setVisibility(View.VISIBLE);
            iv3.setVisibility(View.VISIBLE);
            iv1.setLayoutParams(createLayoutParams(33)); // Weight of 33
            iv2.setLayoutParams(createLayoutParams(33)); // Weight of 33
            iv3.setLayoutParams(createLayoutParams(33)); // Weight of 33
            Glide.with(this).load(movieUrls.get(0)).into(iv1);
            Glide.with(this).load(movieUrls.get(1)).into(iv2);
            Glide.with(this).load(movieUrls.get(2)).into(iv3);
        } else {
            iv1.setVisibility(View.VISIBLE);
            iv1.setLayoutParams(createLayoutParams(100)); // Weight of 100
            iv1.setImageResource(defaultImage); // Default image
        }
    }

    /**
     * Creates layout parameters for an ImageView with the specified weight.
     *
     * @param weight The weight for the ImageView.
     * @return The layout parameters.
     */
    private LinearLayout.LayoutParams createLayoutParams(int weight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                weight
        );
        return params;
    }
}
