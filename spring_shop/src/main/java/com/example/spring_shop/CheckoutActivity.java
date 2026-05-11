package com.example.spring_shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.BucketItemDTO;
import com.example.spring_shop.model.CreatorNewOrderDTO;
import com.example.spring_shop.model.CreatorNewOrderDetailsDTO;
import com.example.spring_shop.model.OrderDTO;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.PickupPointDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AutoCompleteTextView spinnerPickupPoint;
    private RadioGroup radioGroupPayment;
    private TextView tvCheckoutTotal;
    private Button btnSubmitOrder;

    private List<BucketItemDTO> selectedItems = new ArrayList<>();
    private List<PickupPointDTO> pickupPoints = new ArrayList<>();
    
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.toolbarCheckout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Оформление заказа");
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

        recyclerView = findViewById(R.id.recyclerViewCheckoutProducts);
        spinnerPickupPoint = findViewById(R.id.spinnerPickupPoint);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        btnSubmitOrder = findViewById(R.id.btnSubmitOrder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        token = prefs.getString("JWT_TOKEN", null);

        String itemsJson = getIntent().getStringExtra("SELECTED_ITEMS");
        if (itemsJson != null) {
            Type listType = new TypeToken<ArrayList<BucketItemDTO>>(){}.getType();
            selectedItems = new Gson().fromJson(itemsJson, listType);
        }

        CheckoutProductsAdapter adapter = new CheckoutProductsAdapter(selectedItems);
        recyclerView.setAdapter(adapter);

        updateTotal();
        loadPickupPoints();

        spinnerPickupPoint.setOnClickListener(v -> {
            spinnerPickupPoint.showDropDown();
        });

        spinnerPickupPoint.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            spinnerPickupPoint.setText(selected, false);
        });

        btnSubmitOrder.setOnClickListener(v -> submitOrder());
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (BucketItemDTO item : selectedItems) {
            if (item.getSmallProductDTO() != null && item.getSmallProductDTO().getPrice() != null && item.getAmount() != null) {
                total = total.add(item.getSmallProductDTO().getPrice().multiply(item.getAmount()));
            }
        }
        tvCheckoutTotal.setText("Итого: " + total.intValue() + " ₽");
    }

    private void loadPickupPoints() {
        MarketplaceApi api = NetworkService.getInstance(this).getJSONApi();
        api.getAllPickupPoints(0, 100).enqueue(new Callback<PageResponse<PickupPointDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<PickupPointDTO>> call, Response<PageResponse<PickupPointDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pickupPoints = response.body().getContent();
                    List<String> addresses = new ArrayList<>();
                    for (PickupPointDTO p : pickupPoints) {
                        String openTime = p.getOpeningTime();
                        String closeTime = p.getClosingTime();
                        String open = (openTime != null && openTime.length() >= 5) ? openTime.substring(0, 5) : (openTime != null ? openTime : "??:??");
                        String close = (closeTime != null && closeTime.length() >= 5) ? closeTime.substring(0, 5) : (closeTime != null ? closeTime : "??:??");
                        addresses.add(p.getAddress() + " (" + open + " - " + close + ")");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this, R.layout.dropdown_item, R.id.text_view_item, addresses);
                    spinnerPickupPoint.setAdapter(adapter);
                } else {
                    Toast.makeText(CheckoutActivity.this, "Ошибка загрузки пунктов выдачи", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<PickupPointDTO>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    
    private int getSelectedIndex(String text) {
        if (text == null || text.isEmpty()) return -1;
        for (int i = 0; i < pickupPoints.size(); i++) {
            com.example.spring_shop.model.PickupPointDTO p = pickupPoints.get(i);
            String openTime = p.getOpeningTime();
            String closeTime = p.getClosingTime();
            String open = (openTime != null && openTime.length() >= 5) ? openTime.substring(0, 5) : (openTime != null ? openTime : "??:??");
            String close = (closeTime != null && closeTime.length() >= 5) ? closeTime.substring(0, 5) : (closeTime != null ? closeTime : "??:??");
            String addressStr = p.getAddress() + " (" + open + " - " + close + ")";
            if (text.equals(addressStr)) return i;
        }
        return -1;
    }

    private void submitOrder() {
        int selectedPosition = getSelectedIndex(spinnerPickupPoint.getText().toString());
        if (pickupPoints == null || pickupPoints.isEmpty() || selectedPosition < 0) {
            Toast.makeText(this, "Выберите пункт выдачи", Toast.LENGTH_SHORT).show();
            return;
        }

        Long pickupId = pickupPoints.get(selectedPosition).getId();
        
        int rId = radioGroupPayment.getCheckedRadioButtonId();
        String paymentType = (rId == R.id.radioOnline) ? "ONLINE" : "CASH_ON_DELIVERY";

        CreatorNewOrderDTO dto = new CreatorNewOrderDTO();
        dto.setAddressId(pickupId);
        dto.setPaymentType(paymentType);

        List<CreatorNewOrderDetailsDTO> details = new ArrayList<>();
        for (BucketItemDTO item : selectedItems) {
            details.add(new CreatorNewOrderDetailsDTO(
                    item.getSmallProductDTO().getId(),
                    item.getSmallProductDTO().getPrice(),
                    item.getAmount()
            ));
        }
        dto.setOrderDetails(details);

        MarketplaceApi api = NetworkService.getInstance(this).getJSONApi();
        api.newOrder("Bearer " + token, dto).enqueue(new Callback<OrderDTO>() {
            @Override
            public void onResponse(Call<OrderDTO> call, Response<OrderDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CheckoutActivity.this, "Заказ успешно оформлен!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 409) {
                    Toast.makeText(CheckoutActivity.this, "Изменение цен на товары. Пересчитываем сумму...", Toast.LENGTH_LONG).show();
                    updatePricesFromConflict();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Ошибка при оформлении заказа", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderDTO> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePricesFromConflict() {
        // Since backend does not return the new prices in 409 directly, we will just fetch the cart again or products
        // Let's reload cart
        MarketplaceApi api = NetworkService.getInstance(this).getJSONApi();
        api.getBucket("Bearer " + token).enqueue(new Callback<com.example.spring_shop.model.BucketDTO>() {
            @Override
            public void onResponse(Call<com.example.spring_shop.model.BucketDTO> call, Response<com.example.spring_shop.model.BucketDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update current prices
                    for (BucketItemDTO loadedItem : response.body().getItems()) {
                        for (BucketItemDTO selectedItem : selectedItems) {
                            if (selectedItem.getSmallProductDTO().getId().equals(loadedItem.getSmallProductDTO().getId())) {
                                selectedItem.getSmallProductDTO().setPrice(loadedItem.getSmallProductDTO().getPrice());
                                break;
                            }
                        }
                    }
                    if (recyclerView.getAdapter() != null) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                    updateTotal();
                }
            }

            @Override
            public void onFailure(Call<com.example.spring_shop.model.BucketDTO> call, Throwable t) {}
        });
    }

    class CheckoutProductsAdapter extends RecyclerView.Adapter<CheckoutProductsAdapter.ViewHolder> {
        List<BucketItemDTO> items;
        public CheckoutProductsAdapter(List<BucketItemDTO> items) { this.items = items; }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_product, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            BucketItemDTO item = items.get(position);
            holder.tvName.setText(item.getSmallProductDTO().getTitle());
            holder.tvAmount.setText("Количество: " + item.getAmount().intValue());
            
            BigDecimal totalPrice = item.getSmallProductDTO().getPrice().multiply(item.getAmount());
            holder.tvPrice.setText(totalPrice.intValue() + " ₽");

            if (item.getSmallProductDTO().getImageUrl() != null && !item.getSmallProductDTO().getImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(item.getSmallProductDTO().getImageUrl())
                        .placeholder(R.drawable.placeholder_gray)
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.drawable.placeholder_gray);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivImage;
            TextView tvName, tvAmount, tvPrice;
            public ViewHolder(android.view.View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivCheckoutProduct);
                tvName = itemView.findViewById(R.id.tvCheckoutProductName);
                tvAmount = itemView.findViewById(R.id.tvCheckoutProductAmount);
                tvPrice = itemView.findViewById(R.id.tvCheckoutProductPrice);
            }
        }
    }
}
