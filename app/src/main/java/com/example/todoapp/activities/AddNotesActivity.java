package com.example.todoapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.model.NoteBlock;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNotesActivity extends AppCompatActivity {
    private EditText enterTitle;
    private LinearLayout notesContainer;
    private NotesViewModel notesViewModel;
    private Notes existingNote;
    private boolean isBulletMode = false;
    private Uri cameraImageUri;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.todoapp.fileprovider";

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

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    addImageBlock(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                        addFileBlock(uri);
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

        findViewById(R.id.cameraButton).setOnClickListener(v -> openCamera());
        findViewById(R.id.fileButton).setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        MaterialButton bulletBtn = findViewById(R.id.bulletButton);
        bulletBtn.setOnClickListener(v -> {
            isBulletMode = !isBulletMode;
            if (isBulletMode) {
                bulletBtn.setStrokeWidth(4);
                bulletBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary_brand, getTheme())));
            } else {
                bulletBtn.setStrokeWidth(1);
                bulletBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.text_secondary, getTheme())));
            }
        });

        findViewById(R.id.addNoteButton).setOnClickListener(v -> saveNote());

        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString("cameraImageUri");
            if (uriString != null) {
                cameraImageUri = Uri.parse(uriString);
            }
        }

        existingNote = (Notes) getIntent().getSerializableExtra("note");
        if (existingNote != null) {
            ((MaterialButton)findViewById(R.id.addNoteButton)).setText("Update Note");
            enterTitle.setText(existingNote.getText());
            if (existingNote.getBlocks() != null && !existingNote.getBlocks().isEmpty()) {
                for (NoteBlock block : existingNote.getBlocks()) {
                    switch (block.getType()) {
                        case TEXT:
                            addTextBlock(block.getContent());
                            break;
                        case IMAGE:
                            addImageBlock(Uri.parse(block.getContent()));
                            break;
                        case FILE:
                            addFileBlock(Uri.parse(block.getContent()));
                            break;
                    }
                }
            } else {
                addTextBlock("");
            }
        } else if (savedInstanceState == null) {
            addTextBlock("");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cameraImageUri != null) {
            outState.putString("cameraImageUri", cameraImageUri.toString());
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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
        
        editText.addTextChangedListener(new TextWatcher() {
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
                if (isNewLine && isBulletMode) {
                    isNewLine = false;
                    int pos = editText.getSelectionStart();
                    s.insert(pos, "• ");
                }
            }
        });

        notesContainer.addView(editText);
        editText.requestFocus();
    }

    private void addImageBlock(Uri uri) {
        View focusedView = getCurrentFocus();
        int index = -1;
        if (focusedView != null && focusedView.getParent() == notesContainer) {
            index = notesContainer.indexOfChild(focusedView);
        }

        View imageLayout = LayoutInflater.from(this).inflate(R.layout.item_image, notesContainer, false);
        ImageView imageView = imageLayout.findViewById(R.id.imageView);
        ImageButton removeButton = imageLayout.findViewById(R.id.removeButton);
        
        imageView.setImageURI(uri);
        imageLayout.setTag(uri.toString());

        removeButton.setOnClickListener(v -> notesContainer.removeView(imageLayout));
        
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullImageActivity.class);
            intent.putExtra("uri", uri.toString());
            startActivity(intent);
        });

        if (index != -1) {
            notesContainer.addView(imageLayout, index + 1);
            addTextBlock("");
        } else {
            notesContainer.addView(imageLayout);
            addTextBlock("");
        }
    }

    private void addFileBlock(Uri uri) {
        View focusedView = getCurrentFocus();
        int index = -1;
        if (focusedView != null && focusedView.getParent() == notesContainer) {
            index = notesContainer.indexOfChild(focusedView);
        }

        View fileLayout = LayoutInflater.from(this).inflate(R.layout.item_file, notesContainer, false);
        TextView fileNameText = fileLayout.findViewById(R.id.fileName);
        ImageButton removeButton = fileLayout.findViewById(R.id.removeFileButton);
        
        fileNameText.setText(getFileName(uri));
        fileLayout.setTag(uri.toString());

        removeButton.setOnClickListener(v -> notesContainer.removeView(fileLayout));
        
        fileLayout.setOnClickListener(v -> openFile(uri));

        if (index != -1) {
            notesContainer.addView(fileLayout, index + 1);
            addTextBlock("");
        } else {
            notesContainer.addView(fileLayout);
            addTextBlock("");
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private void openFile(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getContentResolver().getType(uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();
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
            } else if (v.getTag() != null) {
                String uri = (String) v.getTag();
                // Check if it's an image or a file
                if (v.findViewById(R.id.imageView) != null) {
                    blocks.add(new NoteBlock(NoteBlock.Type.IMAGE, uri));
                } else if (v.findViewById(R.id.fileName) != null) {
                    blocks.add(new NoteBlock(NoteBlock.Type.FILE, uri));
                }
            }
        }

        if (title.isEmpty() && blocks.isEmpty()) {
            Toast.makeText(this, "Empty note", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingNote == null) {
            Notes note = new Notes(title, System.currentTimeMillis());
            note.setBlocks(blocks);
            notesViewModel.insert(note);
        } else {
            existingNote.setText(title);
            existingNote.setBlocks(blocks);
            existingNote.setTimeStamp(System.currentTimeMillis());
            notesViewModel.update(existingNote);
        }
        finish();
    }
}
