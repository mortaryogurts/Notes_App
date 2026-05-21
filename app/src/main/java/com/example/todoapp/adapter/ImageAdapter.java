package com.example.todoapp.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todoapp.R;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Uri> images;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onImageRemove(int position);
        void onImageClick(Uri uri);
    }

    public ImageAdapter(List<Uri> images, OnImageActionListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = images.get(position);
        holder.imageView.setImageURI(uri);
        
        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(uri);
            }
        });

        holder.removeButton.setVisibility(listener != null ? View.VISIBLE : View.GONE);
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton removeButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
