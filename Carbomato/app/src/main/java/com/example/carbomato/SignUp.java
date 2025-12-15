package com.example.carbomato;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirm;
    Button btnSignUp, btnLogin;

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = (tilUsername.getEditText() != null) ? tilUsername.getEditText().getText().toString().trim() : "";
        String email = (tilEmail.getEditText() != null) ? tilEmail.getEditText().getText().toString().trim() : "";
        String password = (tilPassword.getEditText() != null) ? tilPassword.getEditText().getText().toString().trim() : "";
        String confirmPassword = (tilConfirm.getEditText() != null) ? tilConfirm.getEditText().getText().toString().trim() : "";

        // --- Validations ---
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Enter Username");
            return;
        } else {
            tilUsername.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Enter Email");
            return;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Enter Password");
            return;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirm.setError("Passwords do not match");
            return;
        } else {
            tilConfirm.setError(null);
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);

                        db.collection("users").document(username).set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignUp.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUp.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        if (task.getException() != null) {
                            Toast.makeText(SignUp.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}