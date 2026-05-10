package com.example.spring_shop.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.spring_shop.LoginActivity;
import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.RefreshTokenDTO;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        String path = response.request().url().encodedPath();
        if (path.contains("/refresh") || path.contains("/sign-in") || path.contains("/sign-up")) {
            return null; // Don't intercept these routes
        }

        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        String refreshToken = prefs.getString("REFRESH_TOKEN", null);

        if (refreshToken == null) {
            return null;
        }

        MarketplaceApi api = NetworkService.getInstance(context).getJSONApi();
        Call<JwtAuthenticationDTO> refreshCall = api.refresh(new RefreshTokenDTO(refreshToken));
        retrofit2.Response<JwtAuthenticationDTO> refreshResponse = refreshCall.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            String newToken = refreshResponse.body().getToken();
            String newRefreshToken = refreshResponse.body().getRefreshToken();

            prefs.edit()
                    .putString("JWT_TOKEN", newToken)
                    .putString("REFRESH_TOKEN", newRefreshToken)
                    .apply();

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newToken)
                    .build();
        } else {
            logout();
            return null;
        }
    }

    private void logout() {
        SharedPreferences prefs = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(context, com.example.spring_shop.MainActivity.class);
        intent.putExtra("OPEN_ACCOUNT_FRAGMENT", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
