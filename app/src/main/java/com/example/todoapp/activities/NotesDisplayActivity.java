package com.example.todoapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.model.NoteBlock;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesDisplayActivity extends AppCompatActivity {

    private TextView displayTime;
    private EditText displayTitle;
    private LinearLayout notesContainer;
    private View confirmButton;
    private NotesViewModel notesViewModel;
    private Notes originalNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_display);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        displayTime = findViewById(R.id.displayTime);
        displayTitle = findViewById(R.id.displayTitle);
        notesContainer = findViewById(R.id.notesContainer);
        confirmButton = findViewById(R.id.confirmButton);

        originalNote = (Notes) getIntent().getSerializableExtra("note");
        if (originalNote != null) {
            displayTitle.setText(originalNote.getText());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
            displayTime.setText(sdf.format(new Date(originalNote.getTimeStamp())));

            if (originalNote.getBlocks() != null && !originalNote.getBlocks().isEmpty()) {
                for (NoteBlock block : originalNote.getBlocks()) {
                    if (block.getType() == NoteBlock.Type.TEXT) {
                        addTextBlock(block.getContent());
                    } else {
                        addImageBlock(block.getContent());
                    }
                }
            } else {
                addTextBlock("");
            }
        }

        confirmButton.setVisibility(View.VISIBLE); // Always show for now to allow simple edits
        confirmButton.setOnClickListener(v -> saveChanges());
    }

    private void addTextBlock(String text) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);
        editText.setText(text);
        editText.setBackground(null);
        editText.setTextSize(18);
        editText.setPadding(0, 20, 0, 20);
        notesContainer.addView(editText);
    }

    private void addImageBlock(String uriString) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 20);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageURI(Uri.parse(uriString));
        imageView.setTag(uriString);
        
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("uri", uriString);
            startActivity(intent);
        });

        notesContainer.addView(imageView);
    }

    private void saveChanges() {
        String title = displayTitle.getText().toString().trim();
        List<NoteBlock> blocks = new ArrayList<>();
        
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View v = notesContainer.getChildAt(i);
            if (v instanceof EditText) {
                String text = ((EditText) v).getText().toString().trim();
                blocks.add(new NoteBlock(NoteBlock.Type.TEXT, text));
            } else if (v instanceof ImageView) {
                String uri = (String) v.getTag();
                blocks.add(new NoteBlock(NoteBlock.Type.IMAGE, uri));
            }
        }

        originalNote.setText(title);
        originalNote.setBlocks(blocks);
        originalNote.setTimeStamp(System.currentTimeMillis());
        notesViewModel.update(originalNote);
        
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
