package com.example.carbomato;

import android.content.Intent;
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

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignUp extends AppCompatActivity {

    ImageView reg_image;
    TextView welcome, sign_up_text;
    TextInputLayout regUsername, regEmail, regPassword, regConfirmPassword;
    Button signUp_btn, reg_to_login_btn;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    HelperClass helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();
        helperClass = new HelperClass();


        reg_image = findViewById(R.id.image_view);
        welcome = findViewById(R.id.reg_welcome_text);
        sign_up_text = findViewById(R.id.reg_small_text);
        regUsername = findViewById(R.id.reg_username);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regConfirmPassword = findViewById(R.id.reg_confirm_password);
        signUp_btn = findViewById(R.id.second_last);
        reg_to_login_btn = findViewById(R.id.last);

        reg_to_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {

        String username = regUsername.getEditText().toString().trim();
        String email = regUsername.getEditText().toString().trim();
        String password = regUsername.getEditText().toString().trim();
        String confirmPassword = regConfirmPassword.getEditText().toString().trim();

        if(TextUtils.isEmpty(username)) {

            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.equals(confirmPassword))
        {
            Toast.makeText(this,"Password Not Match",Toast.LENGTH_SHORT).show();
            return;
        }

        helperClass.setUSERNAME(username);
        helperClass.setEMAIL(email);
        helperClass.setPASSWORD(password);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reference.setValue(helperClass);

                Toast.makeText(SignUp.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}