package com.example.spring_shop.model;

import java.math.BigDecimal;
import java.util.List;

public class BucketDTO {
    private Long id;
    private String userEmail;
    private List<BucketItemDTO> items;
    private BigDecimal totalItemsAmount;
    private BigDecimal totalPrice;

    public BucketDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public List<BucketItemDTO> getItems() { return items; }
    public void setItems(List<BucketItemDTO> items) { this.items = items; }

    public BigDecimal getTotalItemsAmount() { return totalItemsAmount; }
    public void setTotalItemsAmount(BigDecimal totalItemsAmount) { this.totalItemsAmount = totalItemsAmount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
