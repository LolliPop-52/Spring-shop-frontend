package com.example.spring_shop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.R;
import com.example.spring_shop.model.CategoryDTO;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<CategoryDTO> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryDTO category);
    }

    public CategoryAdapter(List<CategoryDTO> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryDTO category = categories.get(position);
        holder.categoryName.setText(category.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ViewHolder(View view) {
            super(view);
            categoryName = view.findViewById(R.id.category_name);
        }
    }
}
