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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
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
        mStorage = FirebaseStorage.getInstance();
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
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            }
        }
    }

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
}
