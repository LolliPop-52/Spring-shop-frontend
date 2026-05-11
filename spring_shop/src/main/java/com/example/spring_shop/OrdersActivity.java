package com.example.spring_shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.ActiveOrdersDTO;
import com.example.spring_shop.model.OrderDTO;
import com.example.spring_shop.model.OrderDetailsDTO;
import com.example.spring_shop.model.ProductDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Toolbar toolbar = findViewById(R.id.toolbarOrders);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Мои заказы");
        }
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.text_white));
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_white));
        toolbar.setNavigationOnClickListener(v -> finish());

        // Fix status bar color
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_sapphire));
        androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        MarketplaceApi api = NetworkService.getInstance(this).getJSONApi();
        api.getMyOrders("Bearer " + token).enqueue(new Callback<ActiveOrdersDTO>() {
            @Override
            public void onResponse(Call<ActiveOrdersDTO> call, Response<ActiveOrdersDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getOrders() != null) {
                        List<OrderDetailsDTO> allDetails = new ArrayList<>();
                        for (OrderDTO order : response.body().getOrders()) {
                            if (order.getDetails() != null) {
                                allDetails.addAll(order.getDetails());
                            }
                        }
                        adapter.setDetails(allDetails);
                    }
                } else {
                    Toast.makeText(OrdersActivity.this, "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ActiveOrdersDTO> call, Throwable t) {
                Toast.makeText(OrdersActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        List<OrderDetailsDTO> details;
        Map<Long, String> productImages = new HashMap<>();
        Set<Long> loadingProducts = new HashSet<>();

        public OrdersAdapter(List<OrderDetailsDTO> details) { this.details = details; }

        public void setDetails(List<OrderDetailsDTO> details) {
            this.details = details;
            notifyDataSetChanged();
        }

        private String translateDeliveryStatus(String status) {
            if (status == null) return "В ожидании";
            switch (status) {
                case "PROCESSING":
                case "IN_PROCESS": return "В обработке";
                case "SHIPPED": return "Отправлен";
                case "DELIVERED": return "Доставлен";
                case "CANCELLED": return "Отменен";
                default: return "В ожидании";
            }
        }

        private String translatePaymentStatus(String status) {
            if (status == null) return "В ожидании";
            switch (status) {
                case "PAID": return "Оплачен";
                case "UNPAID": return "Не оплачен";
                case "CANCELLED": return "Отменен";
                case "REFUNDED": return "Возврат";
                default: return "В ожидании";
            }
        }

        private String formatEstimatedDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "Дата уточняется";
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                java.util.Date date = sdf.parse(dateStr.substring(0, 10));
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("d MMMM, EEEE", new java.util.Locale("ru", "RU"));
                return "Приедет: " + outputFormat.format(date);
            } catch (Exception e) {
                return "Приедет: " + dateStr;
            }
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            OrderDetailsDTO detail = details.get(position);
            
            holder.tvDetailTotal.setText("Сумма: " + (detail.getTotalPrice() != null ? detail.getTotalPrice().intValue() : 0) + " ₽");
            holder.tvDetailAmount.setText("Кол-во: " + (detail.getAmount() != null ? detail.getAmount().intValue() : 1));
            
            holder.tvDetailDeliveryStatus.setText(translateDeliveryStatus(detail.getDeliveryStatus()));
            holder.tvDetailPaymentStatus.setText(translatePaymentStatus(detail.getPaymentStatus()));
            holder.tvDetailEstimatedDate.setText(formatEstimatedDate(detail.getEstimatedDeliveryDate()));

            Long productId = detail.getProductId();
            if (productImages.containsKey(productId)) {
                String imageUrl = productImages.get(productId);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_gray)
                            .into(holder.ivProductImage);
                } else {
                    holder.ivProductImage.setImageResource(R.drawable.placeholder_gray);
                }
            } else {
                holder.ivProductImage.setImageResource(R.drawable.placeholder_gray);
                if (!loadingProducts.contains(productId)) {
                    loadingProducts.add(productId);
                    NetworkService.getInstance(holder.itemView.getContext())
                            .getJSONApi().getProductById(productId)
                            .enqueue(new Callback<ProductDTO>() {
                                @Override
                                public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        productImages.put(productId, response.body().getImageUrl());
                                        notifyItemChanged(holder.getAdapterPosition());
                                    }
                                }
                                @Override
                                public void onFailure(Call<ProductDTO> call, Throwable t) {
                                    // Ignore
                                }
                            });
                }
            }
        }

        @Override
        public int getItemCount() { return details.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivProductImage;
            TextView tvDetailTotal, tvDetailAmount, tvDetailDeliveryStatus, tvDetailPaymentStatus, tvDetailEstimatedDate;
            public ViewHolder(android.view.View itemView) {
                super(itemView);
                ivProductImage = itemView.findViewById(R.id.ivProductImage);
                tvDetailTotal = itemView.findViewById(R.id.tvDetailTotal);
                tvDetailAmount = itemView.findViewById(R.id.tvDetailAmount);
                tvDetailDeliveryStatus = itemView.findViewById(R.id.tvDetailDeliveryStatus);
                tvDetailPaymentStatus = itemView.findViewById(R.id.tvDetailPaymentStatus);
                tvDetailEstimatedDate = itemView.findViewById(R.id.tvDetailEstimatedDate);
            }
        }
    }
}
