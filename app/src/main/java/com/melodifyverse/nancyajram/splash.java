package com.melodifyverse.nancyajram;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashImageView = findViewById(R.id.splashIv);
        Glide.with(this)
                .asGif()
                .load(R.drawable.splashv)
                .into(splashImageView);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(splash.this, Menu.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }
        }, 1900);
    }

    // onDestroy removed — it was re-launching Menu on every finish(), causing double navigation.
}