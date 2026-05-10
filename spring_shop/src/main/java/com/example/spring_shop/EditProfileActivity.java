package com.example.spring_shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spring_shop.R;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.UserUpdateDTO;
import com.example.spring_shop.model.UserDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etNewConfirmPassword;
    private MaterialButton btnSave;
    private ImageButton btnBack;

    private UserDTO currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getWindow().setStatusBarColor(getResources().getColor(R.color.app_background));

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etNewConfirmPassword = findViewById(R.id.et_new_confirm_password);
        btnSave = findViewById(R.id.btn_save_profile);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
        
        tilName.setEndIconOnClickListener(v -> {
            if (currentUser != null) {
                etName.setText(currentUser.getName());
            }
        });

        tilEmail.setEndIconOnClickListener(v -> {
            if (currentUser != null) {
                etEmail.setText(currentUser.getEmail());
            }
        });

        btnSave.setOnClickListener(v -> saveProfile());

        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token != null && !token.isEmpty()) {
            NetworkService.getInstance(this)
                    .getJSONApi()
                    .getCurrentUser("Bearer " + token)
                    .enqueue(new Callback<UserDTO>() {
                        @Override
                        public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                currentUser = response.body();
                                etName.setText(currentUser.getName());
                                etEmail.setText(currentUser.getEmail());
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserDTO> call, Throwable t) {
                            Toast.makeText(EditProfileActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Данные пользователя еще не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String newConfirmPassword = etNewConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите имя");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Неверный формат email");
            return;
        }

        boolean isNameChanged = !name.equals(currentUser.getName());
        boolean isEmailChanged = !email.equals(currentUser.getEmail());
        boolean isPasswordChanged = !TextUtils.isEmpty(newPassword);

        if (!isNameChanged && !isEmailChanged && !isPasswordChanged) {
            Toast.makeText(this, "Нет изменений для сохранения", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Введите текущий пароль для подтверждения изменений");
            return;
        }

        if (isPasswordChanged) {
            if (newPassword.length() < 8) {
                etNewPassword.setError("Пароль должен содержать минимум 8 символов");
                return;
            }
            if (!newPassword.equals(newConfirmPassword)) {
                etNewConfirmPassword.setError("Пароли не совпадают");
                return;
            }
        }

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(currentUser.getEmail()); // Current email to identify the user
        updateDTO.setPassword(currentPassword); // Password for verification
        
        if (isNameChanged) {
            updateDTO.setNewName(name);
        }
        if (isEmailChanged) {
            updateDTO.setNewEmail(email);
        }

        // Если пользователь хочет сменить пароль
        if (isPasswordChanged) {
            updateDTO.setNewPassword(newPassword);
            updateDTO.setNewConfirmPassword(newConfirmPassword);
        }

        SharedPreferences prefs = getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", null);

        if (token != null && !token.isEmpty()) {
            btnSave.setEnabled(false);
            NetworkService.getInstance(this)
                    .getJSONApi()
                    .updateCurrentUser("Bearer " + token, updateDTO)
                    .enqueue(new Callback<JwtAuthenticationDTO>() {
                        @Override
                        public void onResponse(Call<JwtAuthenticationDTO> call, Response<JwtAuthenticationDTO> response) {
                            btnSave.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                // Save new token
                                prefs.edit().putString("JWT_TOKEN", response.body().getToken()).apply();
                                Toast.makeText(EditProfileActivity.this, "Данные успешно сохранены", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Ошибка сохранения: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JwtAuthenticationDTO> call, Throwable t) {
                            btnSave.setEnabled(true);
                            Toast.makeText(EditProfileActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
