package com.example.spring_shop.model;

import java.math.BigDecimal;

public class BucketItemDTO {
    private Long id;
    private Long bucketId;
    private SmallProductDTO smallProductDTO;
    private BigDecimal amount;
    private BigDecimal totalPrice;

    public BucketItemDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBucketId() { return bucketId; }
    public void setBucketId(Long bucketId) { this.bucketId = bucketId; }

    public SmallProductDTO getSmallProductDTO() { return smallProductDTO; }
    public void setSmallProductDTO(SmallProductDTO smallProductDTO) { this.smallProductDTO = smallProductDTO; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
