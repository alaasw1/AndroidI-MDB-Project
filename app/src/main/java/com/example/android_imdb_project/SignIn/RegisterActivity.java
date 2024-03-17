package com.example.android_imdb_project.SignIn;

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

import com.example.android_imdb_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText et_register_uname;
    EditText et_register_email;
    EditText et_register_passwd;
    Button btn_register_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        et_register_uname = findViewById(R.id.et_register_uname);
        et_register_email = findViewById(R.id.et_register_email);
        et_register_passwd = findViewById(R.id.et_register_passwd);
        btn_register_user = findViewById(R.id.btn_register_user);

        btn_register_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_register_uname.getText().toString().isEmpty()){
                    et_register_uname.setError("Please Enter Your Name");
                }else if(et_register_email.getText().toString().isEmpty()){
                    et_register_email.setError("Please Enter Email ID");
                }else if(et_register_passwd.getText().toString().isEmpty()){
                    et_register_passwd.setError("Please Enter Password");
                }else{
                    mAuth.createUserWithEmailAndPassword(et_register_email.getText().toString(),
                            et_register_passwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(et_register_uname.getText().toString()).setPhotoUri(Uri.parse("https://www.shutterstock.com/image-vector/blank-avatar-photo-place-holder-600nw-1095249842.jpg"))
                                        .build();
                                mAuth.getCurrentUser().updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),mAuth.getCurrentUser().getUid(),Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                Toast.makeText(getApplicationContext(),"User Successfully Registered",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(),"Registration Failed: " + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}