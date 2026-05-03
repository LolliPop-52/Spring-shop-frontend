package com.example.spring_shop.api;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {
    private static NetworkService mInstance;
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // Твой адрес Spring Boot
    private Retrofit mRetrofit;

    private NetworkService(Context context) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NetworkService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkService(context);
        }
        return mInstance;
    }

    public MarketplaceApi getJSONApi() {
        return mRetrofit.create(MarketplaceApi.class);
    }
}