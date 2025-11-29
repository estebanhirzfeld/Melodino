package com.example.melodino;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView outerRing = findViewById(R.id.loading_ring_outer);
        ImageView innerRing = findViewById(R.id.loading_ring_inner);

        Animation spinSlow = AnimationUtils.loadAnimation(this, R.anim.spin_slow);
        Animation spinFast = AnimationUtils.loadAnimation(this, R.anim.spin);

        outerRing.startAnimation(spinSlow);
        innerRing.startAnimation(spinFast);
    }
}
