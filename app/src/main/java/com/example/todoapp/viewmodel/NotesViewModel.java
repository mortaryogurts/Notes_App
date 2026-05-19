package com.example.todoapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todoapp.model.Notes;
import com.example.todoapp.repository.NotesRepository;

import java.util.List;

public class NotesViewModel extends AndroidViewModel {
    private NotesRepository repository;
    private LiveData<List<Notes>> allNotes;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        repository = new NotesRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Notes note) {
        repository.insert(note);
    }

    public void update(Notes note) {
        repository.update(note);
    }

    public void delete(Notes note) {
        repository.delete(note);
    }

    public void deleteNotes(List<Notes> notes) {
        for (Notes note : notes) {
            repository.delete(note);
        }
    }

    public LiveData<List<Notes>> getAllNotes() {
        return allNotes;
    }
}
