package com.example.todoapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.activities.NotesDisplayActivity;
import com.example.todoapp.databinding.NotesCardBinding;
import com.example.todoapp.model.Notes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{

    private Context context;
    private List<Notes> notesList;
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public NotesAdapter(Context context, List<Notes> notesList, OnSelectionChangeListener listener) {
        this.context = context;
        this.notesList = notesList;
        this.selectionChangeListener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        NotesCardBinding binding = NotesCardBinding.inflate(layoutInflater, parent, false);
        return new NotesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        Notes note = notesList.get(position);
        holder.binding.setNote(note);
        holder.binding.setSelected(note.isSelected());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(note.getTimeStamp()));
        
        holder.binding.noteTime.setText(dateString); 
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public List<Notes> getSelectedNotes() {
        List<Notes> selected = new ArrayList<>();
        for (Notes note : notesList) {
            if (note.isSelected()) {
                selected.add(note);
            }
        }
        return selected;
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder{
        private NotesCardBinding binding;

        public NotesViewHolder(NotesCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Notes clickedNote = notesList.get(position);
                        if (clickedNote.isSelected()) {
                            clickedNote.setSelected(false);
                            binding.setSelected(false);
                            checkSelection();
                        } else {
                            Intent intent = new Intent(context, NotesDisplayActivity.class);
                            intent.putExtra("note", clickedNote);
                            context.startActivity(intent);
                        }
                    }
                }
            });

            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Notes note = notesList.get(position);
                        note.setSelected(true);
                        binding.setSelected(true);
                        checkSelection();
                    }
                    return true;
                }
            });
        }

        private void checkSelection() {
            boolean hasSelection = false;
            for (Notes n : notesList) {
                if (n.isSelected()) {
                    hasSelection = true;
                    break;
                }
            }
            if (selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(hasSelection);
            }
        }
    }
}
