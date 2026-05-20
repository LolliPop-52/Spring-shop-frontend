package com.example.spring_shop;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.spring_shop.fragment.AccountFragment;
import com.example.spring_shop.fragment.ActiveSearchFragment;
import com.example.spring_shop.fragment.CartFragment;
import com.example.spring_shop.fragment.HomeFragment;
import com.example.spring_shop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.UserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        getWindow().setStatusBarColor(getResources().getColor(R.color.app_background));

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
// Отключаем внутренний механизм отрисовки индикатора
        navView.setItemActiveIndicatorEnabled(false);

        checkUserStatus();

        if (savedInstanceState == null) {
            handleIntent(getIntent(), bottomNav);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) selected = new HomeFragment();
            else if (id == R.id.nav_search) selected = new ActiveSearchFragment();
            else if (id == R.id.nav_cart) selected = new CartFragment();
            else if (id == R.id.nav_account) selected = new AccountFragment();

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected).commit();
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        handleIntent(intent, bottomNav);
    }

    private void checkUserStatus() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token != null && !token.isEmpty()) {
            NetworkService.getInstance(this).getJSONApi()
                    .getCurrentUser("Bearer " + token)
                    .enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                boolean enabled = response.body().isEnabled();
                                prefs.edit().putBoolean("USER_ENABLED", enabled).apply();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {}
                    });
        }
    }

    public void updateCartBadge() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (token == null || token.isEmpty()) {
            bottomNav.removeBadge(R.id.nav_cart);
            return;
        }

        NetworkService.getInstance(this).getJSONApi()
                .getBucketAmount("Bearer " + token)
                .enqueue(new Callback<java.math.BigDecimal>() {
                    @Override
                    public void onResponse(Call<java.math.BigDecimal> call, Response<java.math.BigDecimal> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int count = response.body().intValue();
                            
                            if (count > 0) {
                                com.google.android.material.badge.BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
                                badge.setBackgroundColor(getResources().getColor(R.color.accent_green));
                                badge.setBadgeTextColor(getResources().getColor(R.color.black));
                                badge.setMaxCharacterCount(3);
                                badge.setNumber(count);
                                badge.setVisible(true);
                            } else {
                                bottomNav.removeBadge(R.id.nav_cart);
                            }
                        } else {
                            bottomNav.removeBadge(R.id.nav_cart);
                        }
                    }

                    @Override
                    public void onFailure(Call<java.math.BigDecimal> call, Throwable t) {
                        bottomNav.removeBadge(R.id.nav_cart);
                    }
                });
    }

    private void handleIntent(android.content.Intent intent, BottomNavigationView bottomNav) {
        if (intent == null) return;

        if (intent.getBooleanExtra("OPEN_ACCOUNT_FRAGMENT", false)) {
            bottomNav.setSelectedItemId(R.id.nav_account);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AccountFragment()).commit();
        } else if ("CART".equals(intent.getStringExtra("NAVIGATE_TO"))) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CartFragment()).commit();
        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }
}