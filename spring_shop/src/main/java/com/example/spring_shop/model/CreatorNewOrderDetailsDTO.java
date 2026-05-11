package com.example.spring_shop.model;

import java.math.BigDecimal;

public class CreatorNewOrderDetailsDTO {
    private Long productId;
    private BigDecimal priceOnOrder;
    private BigDecimal amount;

    public CreatorNewOrderDetailsDTO(Long productId, BigDecimal priceOnOrder, BigDecimal amount) {
        this.productId = productId;
        this.priceOnOrder = priceOnOrder;
        this.amount = amount;
    }

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public BigDecimal getPriceOnOrder() { return priceOnOrder; }
    public void setPriceOnOrder(BigDecimal priceOnOrder) { this.priceOnOrder = priceOnOrder; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
