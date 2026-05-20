package com.example.spring_shop.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spring_shop.R;
import com.example.spring_shop.api.MarketplaceApi;
import com.example.spring_shop.api.NetworkService;
import com.example.spring_shop.model.BucketDTO;
import com.example.spring_shop.model.BucketItemDTO;
import com.example.spring_shop.model.ModifyBucketItemDTO;
import com.example.spring_shop.model.SmallProductDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<BucketItemDTO> bucketItems = new ArrayList<>();
    private Set<Long> selectedProductIds = new HashSet<>();
    private final MarketplaceApi api;
    private final String token;
    private final Context context;
    private final OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onCartChanged(BucketDTO newBucket);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean allSelected, BigDecimal selectedTotalPrice);
    }

    private OnSelectionChangeListener selectionListener;

    public void setOnSelectionChangeListener(OnSelectionChangeListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    private void checkSelectionState() {
        if (selectionListener != null) {
            boolean allSelected = !bucketItems.isEmpty() && selectedProductIds.size() == bucketItems.size();
            BigDecimal total = BigDecimal.ZERO;
            for (BucketItemDTO item : bucketItems) {
                if (selectedProductIds.contains(item.getSmallProductDTO().getId())) {
                    total = total.add(item.getTotalPrice());
                }
            }
            selectionListener.onSelectionChanged(allSelected, total);
        }
    }

    public CartAdapter(Context context, String token, OnCartChangedListener listener) {
        this.context = context;
        this.token = token;
        this.listener = listener;
        this.api = NetworkService.getInstance(context).getJSONApi();
    }

    public void setBucketItems(List<BucketItemDTO> bucketItems) {
        this.bucketItems = bucketItems != null ? bucketItems : new ArrayList<>();
        Set<Long> currentIds = new HashSet<>();
        for (BucketItemDTO item : this.bucketItems) {
            Long id = item.getSmallProductDTO().getId();
            currentIds.add(id);
            // Auto check new items
            selectedProductIds.add(id);
        }
        selectedProductIds.retainAll(currentIds);
        notifyDataSetChanged();
        checkSelectionState();
    }

    public List<BucketItemDTO> getSelectedItems() { List<BucketItemDTO> selected = new ArrayList<>(); for (BucketItemDTO item : bucketItems) { if (selectedProductIds.contains(item.getSmallProductDTO().getId())) { selected.add(item); } } return selected; }    public void selectAll(boolean select) {
        selectedProductIds.clear();
        if (select) {
            for (BucketItemDTO item : bucketItems) {
                selectedProductIds.add(item.getSmallProductDTO().getId());
            }
        }
        notifyDataSetChanged();
        checkSelectionState();
    }

    public void deleteSelectedItems() {
        for (Long productId : selectedProductIds) {
            BucketItemDTO itemToDelete = null;
            for (BucketItemDTO item : bucketItems) {
                if (item.getSmallProductDTO().getId().equals(productId)) {
                    itemToDelete = item;
                    break;
                }
            }
            if (itemToDelete != null) {
                deleteCartItem(productId, itemToDelete.getAmount());
            }
        }
        selectedProductIds.clear();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        BucketItemDTO item = bucketItems.get(position);
        SmallProductDTO product = item.getSmallProductDTO();

        holder.title.setText(product.getTitle() != null ? product.getTitle() : "Unknown product");
        holder.price.setText(item.getTotalPrice().intValue() + " ₽");
        holder.amount.setText(String.valueOf(item.getAmount().intValue()));

        if (item.getAmount().intValue() > 1) {
            holder.unitPrice.setVisibility(View.VISIBLE);
            holder.unitPrice.setText(product.getPrice().intValue() + " ₽ / шт.");
        } else {
            holder.unitPrice.setVisibility(View.GONE);
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_gray)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.placeholder_gray);
        }

        View.OnClickListener openDetail = v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.spring_shop.ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            context.startActivity(intent);
        };
        holder.image.setOnClickListener(openDetail);
        holder.title.setOnClickListener(openDetail);

        holder.checkboxItem.setOnCheckedChangeListener(null);
        holder.checkboxItem.setChecked(selectedProductIds.contains(product.getId()));
        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedProductIds.add(product.getId());
            } else {
                selectedProductIds.remove(product.getId());
            }
            checkSelectionState();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getAmount().intValue() <= 1) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Удаление товара")
                        .setMessage("Вы уверены, что хотите удалить товар из корзины?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            deleteCartItem(product.getId(), new BigDecimal("1"));
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            } else {
                deleteCartItem(product.getId(), new BigDecimal("1"));
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            modifyCartItem(product.getId(), new BigDecimal("1"));
        });

        holder.btnRemove.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Удаление товара")
                    .setMessage("Вы уверены, что хотите удалить товар из корзины?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        deleteCartItem(product.getId(), item.getAmount());
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bucketItems.size();
    }

    private void modifyCartItem(Long productId, BigDecimal amountChange) {
        ModifyBucketItemDTO dto = new ModifyBucketItemDTO(productId, amountChange);
        Call<BucketDTO> call = api.addBucketItem("Bearer " + token, dto);
        call.enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (listener != null) listener.onCartChanged(response.body());
                } else {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                Toast.makeText(context, "Error updating item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCartItem(Long productId, BigDecimal amountToRemove) {
        ModifyBucketItemDTO dto = new ModifyBucketItemDTO(productId, amountToRemove);
        Call<BucketDTO> call = api.deleteBucketItem("Bearer " + token, dto);
        call.enqueue(new Callback<BucketDTO>() {
            @Override
            public void onResponse(Call<BucketDTO> call, Response<BucketDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (listener != null) listener.onCartChanged(response.body());
                } else {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BucketDTO> call, Throwable t) {
                Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, price, amount, unitPrice;
        ImageButton btnDecrease, btnIncrease, btnRemove;
        CheckBox checkboxItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_product_image);
            title = itemView.findViewById(R.id.cart_product_title);
            price = itemView.findViewById(R.id.cart_product_price);
            amount = itemView.findViewById(R.id.tv_amount);
            unitPrice = itemView.findViewById(R.id.cart_product_unit_price);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            checkboxItem = itemView.findViewById(R.id.checkbox_item);
        }
    }
}
