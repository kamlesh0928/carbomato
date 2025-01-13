package com.example.carbomato;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashPage extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    ImageView splashImage;
    TextView splashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation_splash_page);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation_splash_page);

        splashImage = findViewById(R.id.splash_page_imageView);
        splashText = findViewById(R.id.splash_page_textView);

        splashImage.setAnimation(topAnim);
        splashText.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(SplashPage.this, Login.class);

                Pair[] pairs = new Pair[1];

                View image = splashImage;
                pairs[0] = new Pair<View, String>(image, "splash_image");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashPage.this, pairs);
                startActivity(intent, options.toBundle());
            }
        }, 3000);
    }
}