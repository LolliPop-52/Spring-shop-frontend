package com.example.spring_shop;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spring_shop.adapter.ProductAdapter;
import com.example.spring_shop.adapter.SuggestionAdapter; // Твой адаптер
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.CategoryDTO;
import com.example.spring_shop.model.CategoryResponse;
import com.example.spring_shop.model.PageResponse;
import com.example.spring_shop.model.ProductDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView resultsRecycler, suggestionsRecycler;
    private ProductAdapter productAdapter;
    private SuggestionAdapter suggestionAdapter;

    private List<ProductDTO> searchResults = new ArrayList<>();
    private List<CategoryDTO> allCategories = new ArrayList<>();
    private List<String> filteredTitles = new ArrayList<>(); // Список строк для твоего адаптера

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupAdapters();
        loadInitialData();
        setupSearchLogic();

        String catTitle = getIntent().getStringExtra("CATEGORY_TITLE");
        if (catTitle != null) {
            searchInput.setText(catTitle);
            performSearch(catTitle);
        } else {
            showKeyboard();
        }
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        resultsRecycler = findViewById(R.id.search_results_recycler);
        suggestionsRecycler = findViewById(R.id.suggestions_recycler);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void setupAdapters() {
        resultsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(searchResults, this);
        resultsRecycler.setAdapter(productAdapter);

        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        // Используем твой OnItemClickListener, который принимает String
        suggestionAdapter = new SuggestionAdapter(filteredTitles, suggestion -> {
            searchInput.setText(suggestion);
            performSearch(suggestion);
            suggestionsRecycler.setVisibility(View.GONE);
            hideKeyboard();
        });
        suggestionsRecycler.setAdapter(suggestionAdapter);
    }

    private void setupSearchLogic() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    suggestionsRecycler.setVisibility(View.GONE);
                } else {
                    filterCategories(query);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchInput.getText().toString());
                suggestionsRecycler.setVisibility(View.GONE);
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void filterCategories(String query) {
        filteredTitles.clear();
        for (CategoryDTO cat : allCategories) {
            if (cat.getTitle().toLowerCase().contains(query)) {
                filteredTitles.add(cat.getTitle());
            }
        }

        if (!filteredTitles.isEmpty()) {
            suggestionAdapter.notifyDataSetChanged();
            suggestionsRecycler.setVisibility(View.VISIBLE);
        } else {
            suggestionsRecycler.setVisibility(View.GONE);
        }
    }

    private void loadInitialData() {
        NetworkService.getInstance(this).getJSONApi().getAllCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories.clear();
                    allCategories.addAll(response.body().getContent());
                }
            }
            @Override public void onFailure(@NonNull Call<CategoryResponse> call, @NonNull Throwable t) {}
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        NetworkService.getInstance(this).getJSONApi().searchProducts(query, 0, 50).enqueue(new Callback<PageResponse<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Response<PageResponse<ProductDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body().getContent());
                    productAdapter.notifyDataSetChanged();
                    suggestionsRecycler.setVisibility(View.GONE);
                }
            }
            @Override public void onFailure(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Throwable t) {}
        });
    }

    private void showKeyboard() {
        searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }
}