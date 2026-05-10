package com.example.spring_shop.model;

import java.math.BigDecimal;

public class SmallProductDTO {
    private Long id;
    private BigDecimal price;
    private String title;
    private String imageUrl;

    public SmallProductDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
