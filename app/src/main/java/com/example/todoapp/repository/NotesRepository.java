package com.example.todoapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.todoapp.database.AppDatabase;
import com.example.todoapp.database.NotesDao;
import com.example.todoapp.model.Notes;

import java.util.List;

public class NotesRepository {
    private NotesDao notesDao;
    private LiveData<List<Notes>> allNotes;

    public NotesRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        notesDao = database.notesDao();
        allNotes = notesDao.getAllNotes();
    }

    public void insert(Notes note) {
        // In a real app, use background thread. Room with allowMainThreadQueries is currently used.
        notesDao.insert(note);
    }

    public void update(Notes note) {
        notesDao.update(note);
    }

    public void delete(Notes note) {
        notesDao.delete(note);
    }

    public LiveData<List<Notes>> getAllNotes() {
        return allNotes;
    }
}
