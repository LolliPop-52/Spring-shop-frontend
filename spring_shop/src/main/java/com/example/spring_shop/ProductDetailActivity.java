package com.example.spring_shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.BucketDTO;
import com.example.spring_shop.model.CategoryDTO;
import com.example.spring_shop.model.ModifyBucketItemDTO;
import com.example.spring_shop.model.ProductDTO;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvDescription;
    private com.google.android.material.chip.ChipGroup cgCategories;
    private MaterialButton btnAddToCart;
    private MarketplaceApi api;
    private Long productId;
    private ProductDTO currentProduct;
    private boolean isInCart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.text_white));

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ivProduct = findViewById(R.id.iv_detail_image);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvDescription = findViewById(R.id.tv_detail_description);
        cgCategories = findViewById(R.id.cg_detail_categories);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);

        api = NetworkService.getInstance(this).getJSONApi();
        productId = getIntent().getLongExtra("PRODUCT_ID", -1);

        if (productId != -1) {
            loadProductDetails();
            checkCartStatus();
        }

        btnAddToCart.setOnClickListener(v -> {
            if (isInCart) {
                navigateToCart();
            } else {
                addToCart();
            }
        });
    }

    private void checkCartStatus() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);
        if (token != null) {
            api.getBucket("Bearer " + token).enqueue(new Callback<BucketDTO>() {
                @Override
                public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (com.example.spring_shop.model.BucketItemDTO item : response.body().getItems()) {
                            if (item.getSmallProductDTO().getId().equals(productId)) {
                                isInCart = true;
                                updateButtonState();
                                break;
                            }
                        }
                    }
                }
                @Override
                public void onFailure(Call<BucketDTO> call, Throwable t) {}
            });
        }
    }

    private void updateButtonState() {
        if (isInCart) {
            btnAddToCart.setText("В корзине");
            btnAddToCart.setIconResource(R.drawable.ic_cart);
            btnAddToCart.setBackgroundColor(getResources().getColor(R.color.main_sapphire));
        } else {
            btnAddToCart.setText("Добавить в корзину");
            btnAddToCart.setIcon(null);
            btnAddToCart.setBackgroundColor(getResources().getColor(R.color.accent_green));
        }
    }

    private void navigateToCart() {
        // Since there is no easy way to tell MainActivity to switch tabs from here
        // usually we might use a Broadcast or start MainActivity with an Extra
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.putExtra("NAVIGATE_TO", "CART");
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadProductDetails() {
        api.getProductById(productId).enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    displayProduct(currentProduct);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Ошибка загрузки товара", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(ProductDTO product) {
        tvTitle.setText(product.getTitle());
        tvPrice.setText(product.getPrice().intValue() + " ₽");
        
        String desc = product.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            tvDescription.setText("Описание товара скоро появится");
        } else {
            tvDescription.setText(desc);
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_gray)
                    .into(ivProduct);
        }

        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            findViewById(R.id.cv_detail_categories).setVisibility(View.VISIBLE);
            cgCategories.removeAllViews();
            for (CategoryDTO cat : product.getCategories()) {
                com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
                chip.setText(cat.getTitle());
                chip.setChipBackgroundColorResource(R.color.main_sapphire);
                chip.setTextColor(getResources().getColor(R.color.text_white));
                chip.setClickable(false);
                cgCategories.addView(chip);
            }
        } else {
            findViewById(R.id.cv_detail_categories).setVisibility(View.GONE);
        }
    }

    private void addToCart() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);
        boolean isEnabled = prefs.getBoolean("USER_ENABLED", false);

        if (token == null) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEnabled) {
            Toast.makeText(this, "Подтвердите почту для совершения покупок", Toast.LENGTH_SHORT).show();
            return;
        }

        ModifyBucketItemDTO item = new ModifyBucketItemDTO(productId, new BigDecimal("1"));
        api.addBucketItem("Bearer " + token, item).enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (response.isSuccessful()) {
                    isInCart = true;
                    updateButtonState();
                    Toast.makeText(ProductDetailActivity.this, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Removed CategoryTagAdapter class
}
