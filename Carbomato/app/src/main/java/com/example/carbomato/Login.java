package com.example.carbomato;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextInputLayout lEmail, lPassword;
    Button btnSignUp, btnLogin, btnChangeLanguage;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        lEmail = findViewById(R.id.tilEmail);
        lPassword = findViewById(R.id.tilPassword);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnLogin = findViewById(R.id.sign_in_btn);
        btnChangeLanguage = findViewById(R.id.change_language);

        // Buttons
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
        });

        if(btnChangeLanguage != null) {
            btnChangeLanguage.setOnClickListener(v -> changeLanguage());
        }

        btnLogin.setOnClickListener(v -> checkLogin());
    }

    private void changeLanguage() {
        final String[] languages = {"English", "Hindi", "Marathi", "Bengali"};
        final String[] codes = {"en", "hi", "mr", "bn"};

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            // Modern language switching
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(codes[which]);
            AppCompatDelegate.setApplicationLocales(appLocale);
            dialog.dismiss();
        });
        mBuilder.show();
    }

    private void checkLogin() {
        String email = (lEmail.getEditText() != null) ? lEmail.getEditText().getText().toString().trim() : "";
        String password = (lPassword.getEditText() != null) ? lPassword.getEditText().getText().toString().trim() : "";

        if(TextUtils.isEmpty(email)) {
            lEmail.setError("Enter Email");
            return;
        } else { lEmail.setError(null); }

        if(TextUtils.isEmpty(password)) {
            lPassword.setError("Enter Password");
            return;
        } else { lPassword.setError(null); }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mFirebase = firebaseAuth.getCurrentUser();
        if(mFirebase != null) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}