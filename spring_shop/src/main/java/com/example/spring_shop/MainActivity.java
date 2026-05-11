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
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        handleIntent(intent, bottomNav);
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