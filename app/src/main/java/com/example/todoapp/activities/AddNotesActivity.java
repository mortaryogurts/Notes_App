package com.example.todoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.model.NoteBlock;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddNotesActivity extends AppCompatActivity {
    private EditText enterTitle;
    private LinearLayout notesContainer;
    private NotesViewModel notesViewModel;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        addImageBlock(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_notes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        enterTitle = findViewById(R.id.enterTitle);
        notesContainer = findViewById(R.id.notesContainer);

        findViewById(R.id.imageButton).setOnClickListener(v -> imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        findViewById(R.id.addNoteButton).setOnClickListener(v -> saveNote());

        addTextBlock("");
    }

    private void addTextBlock(String text) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);
        editText.setHint("Start typing...");
        editText.setText(text);
        editText.setBackground(null);
        editText.setTextSize(18);
        editText.setPadding(0, 20, 0, 20);
        notesContainer.addView(editText);
        editText.requestFocus();
    }

    private void addImageBlock(Uri uri) {
        View focusedView = getCurrentFocus();
        int index = -1;
        if (focusedView != null && focusedView.getParent() == notesContainer) {
            index = notesContainer.indexOfChild(focusedView);
        }

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 20);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageURI(uri);
        imageView.setTag(uri.toString());

        if (index != -1) {
            notesContainer.addView(imageView, index + 1);
            addTextBlock("");
        } else {
            notesContainer.addView(imageView);
            addTextBlock("");
        }
    }

    private void saveNote() {
        String title = enterTitle.getText().toString().trim();
        List<NoteBlock> blocks = new ArrayList<>();
        
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View v = notesContainer.getChildAt(i);
            if (v instanceof EditText) {
                String text = ((EditText) v).getText().toString().trim();
                if (!text.isEmpty()) {
                    blocks.add(new NoteBlock(NoteBlock.Type.TEXT, text));
                }
            } else if (v instanceof ImageView) {
                String uri = (String) v.getTag();
                blocks.add(new NoteBlock(NoteBlock.Type.IMAGE, uri));
            }
        }

        if (title.isEmpty() && blocks.isEmpty()) {
            Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show();
            return;
        }

        Notes note = new Notes(title, System.currentTimeMillis());
        note.setBlocks(blocks);
        notesViewModel.insert(note);
        finish();
    }
}
