package com.example.spring_shop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
    private List<String> suggestions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String suggestion);
    }

    public SuggestionAdapter(List<String> suggestions, OnItemClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем тот красивый dropdown_item.xml, который мы создали ранее
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String s = suggestions.get(position);
        holder.textView.setText(s);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(s));
    }

    @Override
    public int getItemCount() { return suggestions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_view_item);
        }
    }
}