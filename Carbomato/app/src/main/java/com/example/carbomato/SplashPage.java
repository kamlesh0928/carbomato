package com.example.carbomato;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashPage extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    ImageView splashImage;
    TextView splashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation_splash_page);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation_splash_page);

        splashImage = findViewById(R.id.splash_page_imageView);
        splashText = findViewById(R.id.splash_page_textView);

        if (splashImage != null) splashImage.setAnimation(topAnim);
        if (splashText != null) splashText.setAnimation(bottomAnim);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashPage.this, Login.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}