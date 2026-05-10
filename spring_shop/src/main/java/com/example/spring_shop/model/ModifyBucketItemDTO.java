package com.example.spring_shop.model;

import java.math.BigDecimal;

public class ModifyBucketItemDTO {
    private String userEmail;
    private Long productId;
    private BigDecimal amount;

    public ModifyBucketItemDTO() {}

    public ModifyBucketItemDTO(Long productId, BigDecimal amount) {
        this.productId = productId;
        this.amount = amount;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
