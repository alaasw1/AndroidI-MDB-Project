package com.example.android_imdb_project.SignIn;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText et_sign_in_email;
    EditText et_sign_in_passwd;
    Button btn_sign_in_user;
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

        if(mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }

        et_sign_in_email = findViewById(R.id.et_sign_in_email);
        et_sign_in_passwd = findViewById(R.id.et_sign_in_passwd);
        btn_sign_in_user = findViewById(R.id.btn_sign_in_user);

        btn_sign_in_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_sign_in_email.getText().toString().isEmpty()){
                    et_sign_in_email.setError("Please Enter Email ID");
                }else if(et_sign_in_passwd.getText().toString().isEmpty()){
                    et_sign_in_passwd.setError("Please Enter Password");
                }else{
                    mAuth.signInWithEmailAndPassword(et_sign_in_email.getText().toString(),
                            et_sign_in_passwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(getApplicationContext(),task.getException().getMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}