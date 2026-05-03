package com.example.spring_shop.fragment;

import android.annotation.SuppressLint;
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
        // 1. Создаем View, "раздувая" макет фрагмента
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. Инициализируем RecyclerView, связывая его с ID из XML
        // ВАЖНО: поиск (findViewById) должен идти через созданный 'view'!
        recyclerView = view.findViewById(R.id.products_recycler);

        // 3. Теперь вызываем методы (здесь была ошибка, потому что recyclerView был null)
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            Log.e("ERROR", "RecyclerView не найден в макете! Проверь ID в XML.");
        }

        showMockData();

        return view;
    }

//    private void loadProductsFromServer() {
//        // Используем requireContext(), чтобы гарантированно получить контекст фрагмента
//        NetworkService.getInstance(requireContext())
//                .getJSONApi()
//                .getProducts(0, 20)
//                .enqueue(new Callback<PageResponse<ProductDTO>>() {
//                    @Override
//                    public void onResponse(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Response<PageResponse<ProductDTO>> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            // Если getContent() все еще красный, проверь имя метода в классе PageResponse
//                            productList = response.body().getContent();
//
//                            if (productList != null) {
//                                adapter = new ProductAdapter(productList, requireContext());
//                                recyclerView.setAdapter(adapter);
//                                Log.d("NETWORK", "Загружено товаров: " + productList.size());
//                            }
//                        } else {
//                            Log.e("NETWORK", "Ошибка сервера: " + response.code());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<PageResponse<ProductDTO>> call, @NonNull Throwable t) {
//                        Log.e("NETWORK", "Ошибка сети: " + t.getMessage());
//                    }
//                });
//
//
//    }

    // Вместо loadProductsFromServer() напиши временный метод:
    private void showMockData() {
        productList = new ArrayList<>();
        // Добавляем 10 фейковых товаров
        for (int i = 1; i <= 10; i++) {
            ProductDTO newProductDTO = new ProductDTO();
            newProductDTO.setId((long) i);
            newProductDTO.setTitle("Шорты широкие Madison Hill Оверсайз " + i);
            newProductDTO.setDescription("Очень крутой товар");
            newProductDTO.setPrice(BigDecimal.valueOf(1000));
            List<CategoryDTO> categoryDTOS = new ArrayList<>();
            categoryDTOS.add(new CategoryDTO("норм товар", 1L));
            newProductDTO.setCategories(categoryDTOS);
            newProductDTO.setImageUrls(List.of("https://via.placeholder.com/150", "https://via.placeholder.com/151"));
            productList.add(newProductDTO);
        }

        // Инициализируем адаптер этими данными
        adapter = new ProductAdapter(productList, requireContext());
        recyclerView.setAdapter(adapter);
    }
}