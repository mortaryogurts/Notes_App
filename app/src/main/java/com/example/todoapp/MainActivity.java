package com.example.todoapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.todoapp.activities.AddNotesActivity;
import com.example.todoapp.adapter.NotesAdapter;
import com.example.todoapp.databinding.ActivityMainBinding;
import com.example.todoapp.model.Notes;
import com.example.todoapp.viewmodel.NotesViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnSelectionChangeListener {

    private ActivityMainBinding binding;
    private NotesViewModel notesViewModel;
    private NotesAdapter adapter;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        
        setSupportActionBar(binding.toolbar);

        // Force light icons for deep background
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        binding.notesRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.notesRecyclerview.setHasFixedSize(true);

        notesViewModel.getAllNotes().observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                binding.setHasNotes(false);
            } else {
                binding.setHasNotes(true);
                adapter = new NotesAdapter(this, notes, this);
                binding.notesRecyclerview.setAdapter(adapter);
            }
            binding.executePendingBindings();
        });

        binding.fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddNotesActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.optionsMenu = menu;
        onSelectionChanged(false); // Initial state
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (adapter != null) {
                List<Notes> selectedNotes = adapter.getSelectedNotes();
                notesViewModel.deleteNotes(selectedNotes);
                onSelectionChanged(false); // Reset menu state after deletion
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionChanged(boolean hasSelection) {
        if (optionsMenu != null) {
            MenuItem deleteItem = optionsMenu.findItem(R.id.action_delete);
            if (deleteItem != null) {
                deleteItem.setEnabled(hasSelection);
                
                // Manually set a faded color for the disabled state
                SpannableString s = new SpannableString("Delete");
                int color = hasSelection ? getResources().getColor(R.color.primary_brand, getTheme()) : getResources().getColor(R.color.text_hint, getTheme());
                s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
                deleteItem.setTitle(s);
            }
        }
    }
}
