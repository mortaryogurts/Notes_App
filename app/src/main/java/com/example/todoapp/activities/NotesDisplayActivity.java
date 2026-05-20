package com.example.todoapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.todoapp.R;
import com.example.todoapp.adapter.ImageAdapter;
import com.example.todoapp.databinding.ActivityNotesDisplayBinding;
import com.example.todoapp.model.Notes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesDisplayActivity extends AppCompatActivity {

    private ActivityNotesDisplayBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_notes_display);

        // Force light icons for deep background
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Notes note = (Notes) getIntent().getSerializableExtra("note");
        if (note != null) {
            binding.setNote(note);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            String dateString = sdf.format(new Date(note.getTimeStamp()));
            binding.displayTime.setText(dateString);

            if (note.getImageUris() != null && !note.getImageUris().isEmpty()) {
                List<Uri> uris = new ArrayList<>();
                for (String uriString : note.getImageUris()) {
                    uris.add(Uri.parse(uriString));
                }
                
                ImageAdapter adapter = new ImageAdapter(uris, null);
                binding.displayImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                binding.displayImagesRecyclerView.setAdapter(adapter);
                binding.displayImagesRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
