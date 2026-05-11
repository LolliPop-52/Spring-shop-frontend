package com.example.spring_shop.api;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class NetworkService {
    private static NetworkService mInstance;
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private Retrofit mRetrofit;

    private NetworkService(Context context) {
        // Добавляем логгер, чтобы видеть запросы в Logcat
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Настраиваем клиент
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .authenticator(new TokenAuthenticator(context))
                .connectTimeout(5, TimeUnit.SECONDS) // Уменьшено для быстрого отклика при отсутствии сети
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Привязываем клиент к Retrofit
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized NetworkService getInstance(Context context) {
        if (mInstance == null) {
            // Используем applicationContext для предотвращения утечек памяти
            mInstance = new NetworkService(context.getApplicationContext());
        }
        return mInstance;
    }

    public MarketplaceApi getJSONApi() {
        return mRetrofit.create(MarketplaceApi.class);
    }
}