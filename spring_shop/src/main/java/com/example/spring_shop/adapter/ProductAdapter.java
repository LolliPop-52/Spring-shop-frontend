package com.example.spring_shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spring_shop.R;
import com.example.spring_shop.model.ProductDTO;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDTO> products;
    private Context context;

    public ProductAdapter(List<ProductDTO> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Указываем XML-файл разметки одной плитки товара
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = products.get(position);

        // Наполняем текстовые поля данными из DTO
        holder.title.setText(product.getTitle());
        holder.price.setText(product.getPrice().toString() + " ₽");

        // Загружаем картинку (первую из списка) с помощью Glide
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0)) // берем первую ссылку
                    .placeholder(R.drawable.placeholder_gray) // картинка-заглушка
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Класс-держатель элементов интерфейса
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            title = itemView.findViewById(R.id.product_title);
            price = itemView.findViewById(R.id.product_price);
        }
    }
}