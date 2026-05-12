package com.example.spring_shop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.R;
import com.example.spring_shop.SearchActivity;
import com.example.spring_shop.adapter.CategoryAdapter;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.CategoryResponse;
import com.example.spring_shop.model.CategoryDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveSearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<CategoryDTO> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View searchTrigger = view.findViewById(R.id.search_bar_trigger);
        if (searchTrigger != null) {
            searchTrigger.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        recyclerView = view.findViewById(R.id.categories_recycler);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

            adapter = new CategoryAdapter(categoryList, category -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("CATEGORY_TITLE", category.getTitle());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);

            loadCategories();
        }
    }

    private void loadCategories() {
        NetworkService.getInstance(requireContext())
                .getJSONApi()
                .getAllCategories()
                .enqueue(new Callback<CategoryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
                        if (!isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            categoryList.clear();
                            categoryList.addAll(response.body().getContent());
                            adapter.notifyDataSetChanged();
                            Log.d("DEBUG_TAG", "Categories loaded: " + categoryList.size());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CategoryResponse> call, @NonNull Throwable t) {
                        if (!isAdded() || getContext() == null) return;
                        Log.e("DEBUG_TAG", "Load failed: " + t.getMessage());
                        android.widget.Toast.makeText(getContext(), "Ошибка сети: нет подключения к серверу", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }
}