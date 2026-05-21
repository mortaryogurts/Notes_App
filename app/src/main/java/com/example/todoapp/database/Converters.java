package com.example.todoapp.database;

import androidx.room.TypeConverter;
import com.example.todoapp.model.NoteBlock;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.text.TextUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return TextUtils.join(",", list);
    }

    @TypeConverter
    public static String fromNoteBlocks(List<NoteBlock> blocks) {
        if (blocks == null) return null;
        Gson gson = new Gson();
        return gson.toJson(blocks);
    }

    @TypeConverter
    public static List<NoteBlock> toNoteBlocks(String data) {
        if (data == null) return new ArrayList<>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<NoteBlock>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}
