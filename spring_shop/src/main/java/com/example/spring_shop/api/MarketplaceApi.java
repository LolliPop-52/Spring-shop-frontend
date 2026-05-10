package com.example.spring_shop.api;

import com.example.spring_shop.model.CategoryDTO;
import com.example.spring_shop.model.CategoryResponse;
import com.example.spring_shop.model.JwtAuthenticationDTO;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.ProductDTO;
import com.example.spring_shop.model.UserUpdateDTO;
import com.example.spring_shop.model.UserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.DELETE;
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
    Call<JwtAuthenticationDTO> signIn(@Body UserDTO credentials);

    @POST("/api/v1/auth/sign-up")
    Call<JwtAuthenticationDTO> signUp(@Body UserDTO credentials);

    @POST("/api/v1/auth/refresh")
    Call<JwtAuthenticationDTO> refresh(@Body com.example.spring_shop.model.RefreshTokenDTO refreshTokenDTO);

    @GET("/api/v1/product/search")
    Call<PageResponse<ProductDTO>> searchProducts(
            @Query("query") String query,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/product/category")
    Call<CategoryResponse> getAllCategories();

    @GET("api/v1/user/me")
    Call<UserDTO> getCurrentUser(@Header("Authorization") String token);

    @POST("api/v1/user/update")
    Call<JwtAuthenticationDTO> updateCurrentUser(@Header("Authorization") String token, @Body UserUpdateDTO user);

    @GET("/api/v1/bucket")
    Call<com.example.spring_shop.model.BucketDTO> getBucket(@Header("Authorization") String token);

    @POST("/api/v1/bucket")
    Call<com.example.spring_shop.model.BucketDTO> addBucketItem(@Header("Authorization") String token, @Body com.example.spring_shop.model.ModifyBucketItemDTO modifyBucketItemDTO);

    @HTTP(method = "DELETE", path = "/api/v1/bucket", hasBody = true)
    Call<com.example.spring_shop.model.BucketDTO> deleteBucketItem(@Header("Authorization") String token, @Body com.example.spring_shop.model.ModifyBucketItemDTO deleteBucketItemDTO);

    @DELETE("/api/v1/bucket/clear")
    Call<com.example.spring_shop.model.BucketDTO> clearBucket(@Header("Authorization") String token);
}