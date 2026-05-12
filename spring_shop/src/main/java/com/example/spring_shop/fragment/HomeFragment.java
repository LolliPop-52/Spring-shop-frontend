package com.example.spring_shop.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.R;
import com.example.spring_shop.SearchActivity; // Убедись, что создал это Activity
import com.example.spring_shop.adapter.ProductAdapter;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.CategoryDTO;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.ProductDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<ProductDTO> productList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        LinearLayout searchContainer = view.findViewById(R.id.search_view_container);
        EditText searchInput = view.findViewById(R.id.search_view);

        if (searchInput != null) {
            searchInput.setFocusable(false);
            searchInput.setClickable(true);
        }

        View.OnClickListener openSearchListener = v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };

        if (searchContainer != null) searchContainer.setOnClickListener(openSearchListener);
        if (searchInput != null) searchInput.setOnClickListener(openSearchListener);


        recyclerView = view.findViewById(R.id.products_recycler);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            loadProductsFromServer();
        } else {
            Log.e("ERROR", "RecyclerView не найден в макете!");
        }
    }

    private void loadProductsFromServer() {
        NetworkService.getInstance(requireContext())
                .getJSONApi()
                .getProducts(0, 20)
                .enqueue(new Callback<PageResponse<ProductDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Response<PageResponse<ProductDTO>> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            // Spring возвращает Page, где данные лежат в поле content
                            productList = response.body().getContent();

                            if (productList != null && !productList.isEmpty()) {
                                adapter = new ProductAdapter(productList, getContext());
                                recyclerView.setAdapter(adapter);
                                Log.d("NETWORK", "Загружено товаров: " + productList.size());
                            }
                        } else {
                            Log.e("NETWORK", "Ошибка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Log.e("NETWORK", "Ошибка сети (проверь, запущен ли Spring Boot): " + t.getMessage());
                        android.widget.Toast.makeText(getContext(), "Ошибка сети: нет подключения к серверу", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }
}