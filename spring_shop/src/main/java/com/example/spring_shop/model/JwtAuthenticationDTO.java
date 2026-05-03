package com.example.spring_shop.model;

import com.google.gson.annotations.SerializedName;

public class JwtAuthenticationDTO {
    // Используем SerializedName, если в JSON поле называется "accessToken"
    @SerializedName("token")
    private String token;

    public JwtAuthenticationDTO() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}