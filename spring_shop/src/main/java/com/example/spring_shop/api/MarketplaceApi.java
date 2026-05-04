package com.example.spring_shop.api;

import com.example.spring_shop.model.CategoryDTO;
import com.example.spring_shop.model.CategoryResponse;
import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.ProductDTO;
import com.example.spring_shop.model.UserCredentialsDTO;
import com.example.spring_shop.model.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MarketplaceApi {

    @GET("/api/v1/product")
    Call<PageResponse<ProductDTO>> getProducts(
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("/api/v1/auth/sign-in")
    Call<JwtAuthenticationDTO> signIn(@Body UserCredentialsDTO credentials);

    @POST("/api/v1/auth/sign-up")
    Call<JwtAuthenticationDTO> signUp(@Body UserCredentialsDTO credentials);

    @GET("/api/v1/product/search")
    Call<PageResponse<ProductDTO>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/product/category")
    Call<CategoryResponse> getAllCategories();

    @GET("api/v1/users/me") // Проверь, какой именно URL у тебя в UserController на бэкенде
    Call<UserDTO> getCurrentUser(@Header("Authorization") String token);
}