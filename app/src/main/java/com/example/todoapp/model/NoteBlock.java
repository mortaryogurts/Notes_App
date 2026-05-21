package com.example.todoapp.model;

import java.io.Serializable;

public class NoteBlock implements Serializable {
    public enum Type { TEXT, IMAGE, FILE }

    private Type type;
    private String content; // Text content or Image URI string

    public NoteBlock(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() { return type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
