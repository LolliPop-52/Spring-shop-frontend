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
import com.example.spring_shop.model.ModifyBucketItemDTO;
import com.example.spring_shop.model.BucketDTO;
import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDTO> products;
    private Context context;
    private MarketplaceApi api;
    private java.util.Set<Long> cartProductIds = new java.util.HashSet<>();

    public ProductAdapter(List<ProductDTO> products, Context context) {
        this.products = products;
        this.context = context;
        this.api = NetworkService.getInstance(context).getJSONApi();
        loadCartState();
    }

    private void loadCartState() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);
        if (token != null) {
            api.getBucket("Bearer " + token).enqueue(new Callback<BucketDTO>() {
                @Override
                public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        cartProductIds.clear();
                        for (com.example.spring_shop.model.BucketItemDTO item : response.body().getItems()) {
                            cartProductIds.add(item.getSmallProductDTO().getId());
                        }
                        notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(Call<BucketDTO> call, Throwable t) {}
            });
        }
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
        holder.price.setText(product.getPrice().intValue() + " ₽");

        // ИСПОЛЬЗУЕМ СТРОКУ imageUrl напрямую
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl()) // Больше никаких .get(0)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_gray)
                    .error(R.drawable.placeholder_gray) // Поможет понять, если ссылка битая
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder_gray);
        }
        
        boolean isInCart = cartProductIds.contains(product.getId());
        holder.addToCartBtn.setText("");
        if (isInCart) {
            holder.addToCartBtn.setIconResource(R.drawable.ic_cross);
            holder.addToCartBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(android.R.color.darker_gray)));
        } else {
            holder.addToCartBtn.setIconResource(R.drawable.ic_plus);
            holder.addToCartBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.accent_green)));
        }
        
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.spring_shop.ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            context.startActivity(intent);
        });
        
        holder.addToCartBtn.setOnClickListener(v -> {
            SharedPreferences prefs = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
            String token = prefs.getString("JWT_TOKEN", null);

            if (token == null || token.isEmpty()) {
                Toast.makeText(context, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cartProductIds.contains(product.getId())) {
                removeFromCart(token, product.getId(), position, holder);
            } else {
                addToCart(token, product.getId(), position, holder);
            }
        });
    }

    private void addToCart(String token, Long productId, int position, ProductViewHolder holder) {
        ModifyBucketItemDTO item = new ModifyBucketItemDTO(productId, new BigDecimal("1"));
        api.addBucketItem("Bearer " + token, item).enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (response.isSuccessful()) {
                    cartProductIds.add(productId);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromCart(String token, Long productId, int position, ProductViewHolder holder) {
        // Find existing amount to remove all
        api.getBucket("Bearer " + token).enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BigDecimal amount = BigDecimal.ZERO;
                    for (com.example.spring_shop.model.BucketItemDTO item : response.body().getItems()) {
                        if (item.getSmallProductDTO().getId().equals(productId)) {
                            amount = item.getAmount();
                            break;
                        }
                    }
                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        api.deleteBucketItem("Bearer " + token, new ModifyBucketItemDTO(productId, amount)).enqueue(new Callback<BucketDTO>() {
                            @Override
                            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                                if (response.isSuccessful()) {
                                    cartProductIds.remove(productId);
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Удалено из корзины", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<BucketDTO> call, Throwable t) {}
                        });
                    }
                }
            }
            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {}
        });
    }


    @Override
    public int getItemCount() {
        return products.size();
    }

    // Класс-держатель элементов интерфейса
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price;
        com.google.android.material.button.MaterialButton addToCartBtn;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.product_image);
            title = itemView.findViewById(R.id.product_title);
            price = itemView.findViewById(R.id.product_price);
            addToCartBtn = itemView.findViewById(R.id.add_to_cart_btn);
        }
    }
}