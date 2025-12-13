package com.example.carbomato;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirm;
    Button btnSignUp, btnLogin;

    // Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("users");
        helperClass = new HelperClass();

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);

        btnSignUp = findViewById(R.id.btn_sign_up);
        btnLogin = findViewById(R.id.btn_login);

        // Navigate to Login
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Perform Sign Up
        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = (tilUsername.getEditText() != null) ? tilUsername.getEditText().getText().toString().trim() : "";
        String email = (tilEmail.getEditText() != null) ? tilEmail.getEditText().getText().toString().trim() : "";
        String password = (tilPassword.getEditText() != null) ? tilPassword.getEditText().getText().toString().trim() : "";
        String confirmPassword = (tilConfirm.getEditText() != null) ? tilConfirm.getEditText().getText().toString().trim() : "";

        // Validation
        if(TextUtils.isEmpty(username)) {
            tilUsername.setError("Enter Username");
            return;
        } else { tilUsername.setError(null); }

        if(TextUtils.isEmpty(email)) {
            tilEmail.setError("Enter Email");
            return;
        } else { tilEmail.setError(null); }

        if(TextUtils.isEmpty(password)) {
            tilPassword.setError("Enter Password");
            return;
        } else { tilPassword.setError(null); }

        if(!password.equals(confirmPassword)) {
            tilConfirm.setError("Passwords do not match");
            return;
        } else { tilConfirm.setError(null); }

        // Save to Firebase
        helperClass.setUSERNAME(username);
        helperClass.setEMAIL(email);
        helperClass.setPASSWORD(password);

        reference.child(username).setValue(helperClass);

        Toast.makeText(SignUp.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

        // Go to Main Activity
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}