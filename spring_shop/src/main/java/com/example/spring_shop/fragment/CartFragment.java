package com.example.spring_shop.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.LoginActivity;
import com.example.spring_shop.R;
import com.example.spring_shop.adapter.CartAdapter;
import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.BucketDTO;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private RelativeLayout cartLayout;
    private LinearLayout authLayout, unverifiedLayout;
    private MaterialButton btnGoToLogin, btnGoToAccount;
    
    private RecyclerView cartRecycler;
    private CartAdapter cartAdapter;
    private MaterialButton orderButton;
    private TextView selectedTotalPriceView;
    private android.widget.CheckBox checkboxSelectAll;
    private Button btnDeleteAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartLayout = view.findViewById(R.id.cart_layout);
        authLayout = view.findViewById(R.id.auth_layout);
        unverifiedLayout = view.findViewById(R.id.unverified_layout);
        btnGoToLogin = view.findViewById(R.id.btn_go_to_login);
        btnGoToAccount = view.findViewById(R.id.btn_go_to_account);
        
        cartRecycler = view.findViewById(R.id.cart_recycler);
        cartRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        orderButton = view.findViewById(R.id.order_button);
        selectedTotalPriceView = view.findViewById(R.id.selected_total_price);
        checkboxSelectAll = view.findViewById(R.id.checkbox_select_all);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all);

        checkAuthStatus();

        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btnGoToAccount.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_account);
            }
        });

        checkboxSelectAll.setOnClickListener(v -> {
            boolean isChecked = checkboxSelectAll.isChecked();
            if (cartAdapter != null) {
                cartAdapter.selectAll(isChecked);
            }
        });

        btnDeleteAll.setOnClickListener(v -> {
            if (cartAdapter != null) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Очистить корзину")
                    .setMessage("Вы уверены, что хотите удалить все товары из корзины?")
                    .setPositiveButton("Очистить", (dialog, which) -> {
                        clearCart();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            }
        });
    }

    private void clearCart() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);
        if (token == null) return;

        MarketplaceApi api = NetworkService.getInstance(getContext()).getJSONApi();
        api.clearBucket("Bearer " + token).enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body());
                    checkboxSelectAll.setChecked(false);
                } else {
                    Toast.makeText(getContext(), "Ошибка при очистке корзины", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAuthStatus();
    }

    private void checkAuthStatus() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token != null && !token.isEmpty()) {
            checkUserEnabled(token);
        } else {
            showGuestState();
        }
    }

    private void checkUserEnabled(String token) {
        NetworkService.getInstance(requireContext())
                .getJSONApi()
                .getCurrentUser("Bearer " + token)
                .enqueue(new Callback<com.example.spring_shop.model.UserDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.spring_shop.model.UserDTO> call, @NonNull Response<com.example.spring_shop.model.UserDTO> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.spring_shop.model.UserDTO user = response.body();
                            if (!user.isEnabled()) {
                                showUnverifiedState();
                            } else {
                                showCartState(token);
                            }
                        } else {
                            showGuestState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.spring_shop.model.UserDTO> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showGuestState() {
        cartLayout.setVisibility(View.GONE);
        unverifiedLayout.setVisibility(View.GONE);
        authLayout.setVisibility(View.VISIBLE);
    }

    private void showUnverifiedState() {
        cartLayout.setVisibility(View.GONE);
        authLayout.setVisibility(View.GONE);
        unverifiedLayout.setVisibility(View.VISIBLE);
    }

    private void showCartState(String token) {
        cartLayout.setVisibility(View.VISIBLE);
        authLayout.setVisibility(View.GONE);
        unverifiedLayout.setVisibility(View.GONE);
        loadCartData(token);
    }
    
    private void loadCartData(String token) {
        if (cartAdapter == null) {
            cartAdapter = new CartAdapter(getContext(), token, this::updateCartUI);
            cartAdapter.setOnSelectionChangeListener((allSelected, selectedTotalPrice) -> {
                checkboxSelectAll.setChecked(allSelected);
                selectedTotalPriceView.setText(selectedTotalPrice.intValue() + " ₽");
            });
            cartRecycler.setAdapter(cartAdapter);
        }
        
        MarketplaceApi api = NetworkService.getInstance(getContext()).getJSONApi();
        api.getBucket("Bearer " + token).enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to load cart", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Log.e("CartFragment", "Error loading cart", t);
                Toast.makeText(getContext(), "Error loading cart", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCartUI(BucketDTO bucket) {
        if (getActivity() instanceof com.example.spring_shop.MainActivity) {
            ((com.example.spring_shop.MainActivity) getActivity()).updateCartBadge();
        }
        if (bucket.getItems() == null || bucket.getItems().isEmpty()) {
            // Cart is empty
            orderButton.setText("Корзина пуста");
            orderButton.setEnabled(false);
            cartAdapter.setBucketItems(new java.util.ArrayList<>());
            selectedTotalPriceView.setText("0 ₽");
        } else {
            orderButton.setEnabled(true); orderButton.setOnClickListener(v -> { java.util.List<com.example.spring_shop.model.BucketItemDTO> items = cartAdapter.getSelectedItems(); if (items.isEmpty()) { Toast.makeText(getContext(), "Выберите товары", Toast.LENGTH_SHORT).show(); return; } String json = new com.google.gson.Gson().toJson(items); Intent i = new Intent(getActivity(), com.example.spring_shop.CheckoutActivity.class); i.putExtra("SELECTED_ITEMS", json); startActivity(i); });
            orderButton.setText("К оформлению");
            cartAdapter.setBucketItems(bucket.getItems());
        }
    }
}
