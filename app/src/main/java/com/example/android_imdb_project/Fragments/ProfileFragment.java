package com.example.android_imdb_project.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private FirebaseAuth mAuth;
    private ImageView imv_propic;
    private TextView et_display_name, et_display_email;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and views
        mAuth = FirebaseAuth.getInstance();
        imv_propic = view.findViewById(R.id.imv_propic);
        et_display_name = view.findViewById(R.id.et_display_name);
        et_display_email = view.findViewById(R.id.et_display_email);

        // Load current user info
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this).load(currentUser.getPhotoUrl()).into(imv_propic);
            } else {
                imv_propic.setImageResource(R.drawable.profile_circle_icon);
            }
            et_display_name.setText(currentUser.getDisplayName());
            et_display_email.setText(currentUser.getEmail());
        }

        // Set click listener to change profile picture
        imv_propic.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Glide.with(this).load(imageUri).into(imv_propic);
                // Optionally, you can upload this imageUri to Firebase Storage and update the user's profile picture URL
            }
        }
    }
}
