package com.example.carbomato;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class Login extends AppCompatActivity {

    ImageView image;
    TextView welcome_text, sign_in_text;
    TextInputLayout l_email, l_password;
    Button btn_signUp, btn_login, btn_change_language;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        loadLocale();

        firebaseAuth = FirebaseAuth.getInstance();

        image = findViewById(R.id.login_image);
        welcome_text = findViewById(R.id.login_welcome_text);
        sign_in_text = findViewById(R.id.login_small_text);
        l_password = findViewById(R.id.password);
        btn_signUp = findViewById(R.id.btn_sign_up);
        btn_login = findViewById(R.id.sign_in_btn);
        btn_change_language = findViewById(R.id.change_language);
        l_email = findViewById(R.id.l_email);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        btn_change_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
    }

    private void changeLanguage() {

        final String languages[] = {"English", "Hindi", "Marathi", "Bengali"};

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0) {
                    setLocale("");
                    recreate();
                }
                else if(which == 1) {
                    setLocale("hi");
                    recreate();
                }
                else if(which == 2) {
                    setLocale("mr");
                    recreate();
                }
                else if(which == 3) {
                    setLocale("bn");
                    recreate();
                }
            }
        });

        mBuilder.create();
        mBuilder.show();
    }

    private void setLocale(String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;

        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("app_lang", language);
        editor.apply();
    }

    protected void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("app_lang", "");

        setLocale(language);
    }

    private void checkLogin() {

        String email = l_email.getEditText().getText().toString();
        String password = l_password.getEditText().getText().toString();

        if(TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(!task.isSuccessful()) {
                    Toast.makeText(Login.this, "Sign In", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
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
        }
    }
}