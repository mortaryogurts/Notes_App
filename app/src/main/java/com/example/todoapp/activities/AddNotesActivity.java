package com.example.todoapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;

public class AddNotesActivity extends AppCompatActivity {
    EditText enterNote;
    Button addNote;
    private NotesViewModel notesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_notes);

        // Force dark icons (light status bar)
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        enterNote = findViewById(R.id.enterNote);
        addNote = findViewById(R.id.addNoteButton);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = enterNote.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(AddNotesActivity.this, "Please Enter a Note", Toast.LENGTH_SHORT).show();
                } else {
                    Notes note = new Notes(text, System.currentTimeMillis());
                    notesViewModel.insert(note);
                    Toast.makeText(AddNotesActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
