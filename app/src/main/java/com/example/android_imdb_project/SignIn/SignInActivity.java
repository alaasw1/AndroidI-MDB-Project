package com.example.android_imdb_project.SignIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.android_imdb_project.HomeActivity;
import com.example.android_imdb_project.MainActivity;
import com.example.android_imdb_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * SignInActivity handles the user sign-in process.
 */
public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText et_sign_in_email;
    EditText et_sign_in_passwd;
    Button btn_sign_in_user;
    TextView tvGoToRegister;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Sign out the current user if they are already signed in
        if(mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }

        // Initialize UI elements
        et_sign_in_email = findViewById(R.id.et_sign_in_email);
        et_sign_in_passwd = findViewById(R.id.et_sign_in_passwd);
        btn_sign_in_user = findViewById(R.id.btn_sign_in_user);
        tvGoToRegister = findViewById(R.id.tv_go_to_register);

        // Set click listener for the sign-in button
        btn_sign_in_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate email and password input
                if(et_sign_in_email.getText().toString().isEmpty()){
                    et_sign_in_email.setError("Please Enter Email ID");
                }else if(et_sign_in_passwd.getText().toString().isEmpty()){
                    et_sign_in_passwd.setError("Please Enter Password");
                }else{
                    // Attempt to sign in the user with Firebase Auth
                    mAuth.signInWithEmailAndPassword(et_sign_in_email.getText().toString(),
                            et_sign_in_passwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // If sign-in is successful, navigate to HomeActivity
                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }else{
                                // Show error message if sign-in fails
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        // Set click listener for the register text view
        tvGoToRegister.setOnClickListener(view -> {
            // Navigate to RegisterActivity
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
        });
    }
}
