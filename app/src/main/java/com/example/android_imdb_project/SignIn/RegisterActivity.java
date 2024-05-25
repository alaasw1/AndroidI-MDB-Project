package com.example.android_imdb_project.SignIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_imdb_project.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText et_register_uname, et_register_email, et_register_passwd;
    private Button btn_register_user;
    private ImageView profileImageView;
    private Uri imageUri = null;
    private TextView tv_go_to_sign_in;

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        et_register_uname = findViewById(R.id.et_register_uname);
        et_register_email = findViewById(R.id.et_register_email);
        et_register_passwd = findViewById(R.id.et_register_passwd);
        btn_register_user = findViewById(R.id.btn_register_user);
        profileImageView = findViewById(R.id.profileImageView);
        tv_go_to_sign_in = findViewById(R.id.tv_go_to_sign_in);

        profileImageView.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryActivityResultLauncher.launch(galleryIntent);
        });

        btn_register_user.setOnClickListener(view -> {
            String userName = et_register_uname.getText().toString().trim();
            String userEmail = et_register_email.getText().toString().trim();
            String userPasswd = et_register_passwd.getText().toString().trim();
            if (validateInputs(userName, userEmail, userPasswd)) {
                registerUser(userName, userEmail, userPasswd);
            }
        });

        tv_go_to_sign_in.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, SignInActivity.class));
        });

    }

    private boolean validateInputs(String userName, String userEmail, String userPasswd) {
        if (userName.isEmpty() || userEmail.isEmpty() || userPasswd.isEmpty()) {
            if (userName.isEmpty()) {
                et_register_uname.setError("Please Enter Your Name");
            }
            if (userEmail.isEmpty()) {
                et_register_email.setError("Please Enter Email ID");
            }
            if (userPasswd.isEmpty()) {
                et_register_passwd.setError("Please Enter Password");
            }
            return false;
        }
        return true;
    }

    private void registerUser(String userName, String userEmail, String userPasswd) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPasswd).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    if (imageUri != null) {
                        uploadImageToFirebaseStorage(firebaseUser, imageUri, userName);
                    } else {
                        Uri defaultImageUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/android-imdb-project.appspot.com/o/profile_images%2Fprofile_circle_icon.png?alt=media&token=f458abd3-d604-427f-bb3a-0e40c6b384ed");
                        updateUserProfile(firebaseUser, defaultImageUri, userName);
                    }
                } else {
                    Log.e("RegisterActivity", "Registration succeeded, but user is null");
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown Error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebaseStorage(FirebaseUser firebaseUser, Uri imageUri, String userName) {
        Log.d("RegisterActivity", "Attempting to upload image: " + imageUri);
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + firebaseUser.getUid() + ".jpg");

        profileImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Log.d("RegisterActivity", "Image upload successful");
            profileImageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                Log.d("RegisterActivity", "Got download URL: " + downloadUri);
                updateUserProfile(firebaseUser, downloadUri, userName);
            }).addOnFailureListener(e -> {
                Log.e("RegisterActivity", "Error getting download URL", e);
            });
        }).addOnFailureListener(e -> {
            Log.e("RegisterActivity", "Error uploading image", e);
        });
    }

    private void updateUserProfile(FirebaseUser user, Uri photoUri, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("RegisterActivity", "User profile updated.");
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                navigateToSignIn();
            } else {
                Log.e("RegisterActivity", "Error updating user profile", task.getException());
                Toast.makeText(RegisterActivity.this, "User registration successful but failed to update profile photo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(RegisterActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
