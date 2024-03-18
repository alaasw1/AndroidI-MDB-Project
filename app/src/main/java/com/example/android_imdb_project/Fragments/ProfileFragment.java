package com.example.android_imdb_project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private ImageView imv_propic;
    private TextView et_display_name, et_display_email;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            Glide.with(this).load(currentUser.getPhotoUrl()).into(imv_propic);
            et_display_name.setText(currentUser.getDisplayName());
            et_display_email.setText(currentUser.getEmail());
        }

        return view;
    }
}
