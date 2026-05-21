package com.example.todoapp.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import com.example.todoapp.R;

public class FullImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        ImageView imageView = findViewById(R.id.fullImageView);
        String uriString = getIntent().getStringExtra("uri");
        if (uriString != null) {
            imageView.setImageURI(Uri.parse(uriString));
        }

        findViewById(R.id.closeButton).setOnClickListener(v -> finish());
    }
}
