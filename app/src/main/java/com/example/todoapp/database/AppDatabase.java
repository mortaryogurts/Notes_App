package com.example.todoapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.todoapp.model.Notes;

@Database(entities = {Notes.class}, version = 6)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract NotesDao notesDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "notes_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Only for simplicity in this example; use background threads in production
                    .build();
        }
        return instance;
    }
}
