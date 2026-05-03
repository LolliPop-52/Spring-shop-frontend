package com.example.spring_shop.api;

import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.ProductDTO;
import com.example.spring_shop.model.UserCredentialsDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MarketplaceApi {

    // Получение списка товаров (публичный эндпоинт)
    @GET("api/v1/product")
    Call<PageResponse<ProductDTO>> getProducts(
            @Query("page") int page,
            @Query("size") int size
    );

    // Авторизация (вход)
    @POST("api/v1/auth/sign-in")
    Call<JwtAuthenticationDTO> signIn(@Body UserCredentialsDTO credentials);

    // Регистрация (если нужна)
    @POST("api/v1/auth/sign-up")
    Call<JwtAuthenticationDTO> signUp(@Body UserCredentialsDTO credentials);
}