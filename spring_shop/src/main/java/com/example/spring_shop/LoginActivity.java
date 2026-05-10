package com.example.spring_shop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.UserDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private android.widget.ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoToRegister = findViewById(R.id.btn_go_to_register);
        btnBack = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите Email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword(password);

        NetworkService.getInstance(this).getJSONApi().signIn(user).enqueue(new Callback<JwtAuthenticationDTO>() {
            @Override
            public void onResponse(@NonNull Call<JwtAuthenticationDTO> call, @NonNull Response<JwtAuthenticationDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveToken(response.body().getToken(), response.body().getRefreshToken());
                    Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                    
                    // Return to MainActivity which will update AccountFragment
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Неверные учетные данные", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JwtAuthenticationDTO> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token, String refreshToken) {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        prefs.edit()
             .putString("JWT_TOKEN", token)
             .putString("REFRESH_TOKEN", refreshToken)
             .apply();
    }
}
