package com.example.spring_shop.model;

import com.google.gson.annotations.SerializedName;

public class JwtAuthenticationDTO {
    // Используем SerializedName, если в JSON поле называется "accessToken"
    @SerializedName("token")
    private String token;

    @SerializedName("refreshToken")
    private String refreshToken;

    public JwtAuthenticationDTO() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}