package com.example.todoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.adapter.ImageAdapter;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddNotesActivity extends AppCompatActivity {
    EditText enterNote;
    Button addNote;
    MaterialButton bulletButton, imageButton;
    RecyclerView imagesRecyclerView;
    ImageAdapter imageAdapter;
    private NotesViewModel notesViewModel;
    private boolean isBulletMode = false;
    private List<Uri> selectedImageUris = new ArrayList<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    selectedImageUris.addAll(uris);
                    updateImagesVisibility();
                    imageAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_notes);

        // Force light icons for deep background
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        enterNote = findViewById(R.id.enterNote);
        addNote = findViewById(R.id.addNoteButton);
        bulletButton = findViewById(R.id.bulletButton);
        imageButton = findViewById(R.id.imageButton);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);

        imageAdapter = new ImageAdapter(selectedImageUris, position -> {
            selectedImageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImageUris.size());
            updateImagesVisibility();
        });
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imageAdapter);

        bulletButton.setOnClickListener(v -> toggleBulletMode());
        imageButton.setOnClickListener(v -> imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        enterNote.addTextChangedListener(new TextWatcher() {
            private boolean isNewLine = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 && start < s.length() && s.charAt(start) == '\n') {
                    isNewLine = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isNewLine) {
                    isNewLine = false;
                    int pos = enterNote.getSelectionStart();
                    if (pos > 0) {
                        String text = s.toString();
                        int lastNewLine = text.lastIndexOf('\n', pos - 2);
                        int lineStart = (lastNewLine == -1) ? 0 : lastNewLine + 1;

                        if (text.startsWith("• ", lineStart) || isBulletMode) {
                            s.insert(pos, "• ");
                        }
                    }
                }
            }
        });

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = enterNote.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(AddNotesActivity.this, "Please Enter a Note", Toast.LENGTH_SHORT).show();
                } else {
                    Notes note = new Notes(text, System.currentTimeMillis());
                    if (!selectedImageUris.isEmpty()) {
                        List<String> uris = new ArrayList<>();
                        for (Uri uri : selectedImageUris) {
                            uris.add(uri.toString());
                        }
                        note.setImageUris(uris);
                    }
                    notesViewModel.insert(note);
                    Toast.makeText(AddNotesActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void updateImagesVisibility() {
        if (selectedImageUris.isEmpty()) {
            imagesRecyclerView.setVisibility(View.GONE);
        } else {
            imagesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleBulletMode() {
        isBulletMode = !isBulletMode;
        if (isBulletMode) {
            bulletButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_brand, getTheme())));
            bulletButton.setTextColor(getResources().getColor(R.color.white, getTheme()));
            bulletButton.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_brand, getTheme())));
            
            int start = enterNote.getSelectionStart();
            String text = enterNote.getText().toString();
            int lineStart = text.lastIndexOf('\n', start - 1) + 1;
            if (!text.startsWith("• ", lineStart)) {
                enterNote.getText().insert(start, "• ");
            }
        } else {
            bulletButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.card_bg, getTheme())));
            bulletButton.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
            bulletButton.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary, getTheme())));
        }
    }
}
