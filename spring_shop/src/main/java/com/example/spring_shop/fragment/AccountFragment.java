package com.example.spring_shop.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spring_shop.R;
import com.example.spring_shop.api.NetworkService; // Твой класс для Retrofit
import com.example.spring_shop.model.UserDTO;       // Твоя модель пользователя
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private LinearLayout profileLayout, authLayout;
    private TextView tvName, tvEmail;
    private MaterialButton btnEditProfile, btnGoToLogin;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация UI
        profileLayout = view.findViewById(R.id.profile_layout);
        authLayout = view.findViewById(R.id.auth_layout);
        tvName = view.findViewById(R.id.user_name_display);
        tvEmail = view.findViewById(R.id.user_email_display);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnGoToLogin = view.findViewById(R.id.btn_go_to_login);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Проверка авторизации при создании экрана
        checkAuthStatus();

        // Слушатели кнопок
        setupClickListeners();
    }

    private void checkAuthStatus() {
        // Извлекаем JWT токен из SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token != null && !token.isEmpty()) {
            loadUserProfile(token);
        } else {
            showGuestState();
        }
    }

    private void loadUserProfile(String token) {
        // Вызов твоего Spring Boot бэкенда для получения данных "О себе"
        // Заголовок должен быть: "Authorization: Bearer " + token
        NetworkService.getInstance(requireContext())
                .getJSONApi()
                .getCurrentUser("Bearer " + token)
                .enqueue(new Callback<UserDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UserDTO> call, @NonNull Response<UserDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            displayUserData(response.body());
                        } else {
                            // Если токен протух или ошибка — переходим в режим гостя
                            showGuestState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserDTO> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showGuestState();
                    }
                });
    }

    private void displayUserData(UserDTO user) {
        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());

        authLayout.setVisibility(View.GONE);
        profileLayout.setVisibility(View.VISIBLE);
    }

    private void showGuestState() {
        profileLayout.setVisibility(View.GONE);
        authLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        // Кнопка входа/регистрации
        btnGoToLogin.setOnClickListener(v -> {
            // Тут открываешь свою Activity для логина (LoginActivity)
            // Intent intent = new Intent(getActivity(), LoginActivity.class);
            // startActivity(intent);
        });

        // Кнопка изменения данных
        btnEditProfile.setOnClickListener(v -> {
            // Логика открытия режима редактирования
            Toast.makeText(getContext(), "Режим редактирования", Toast.LENGTH_SHORT).show();
        });

        // Выход из аккаунта
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
            prefs.edit().remove("JWT_TOKEN").apply(); // Удаляем токен
            showGuestState();
        });
    }
}