package com.example.android_imdb_project.SignIn;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.android_imdb_project.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ImageView imv_propic;
    EditText et_display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        imv_propic = findViewById(R.id.imv_propic);
        et_display_name = findViewById(R.id.et_display_name);

        Glide.with(getApplicationContext()).load(mAuth.getCurrentUser().getPhotoUrl()).into(imv_propic);
        et_display_name.setText(mAuth.getCurrentUser().getDisplayName());
    }
}