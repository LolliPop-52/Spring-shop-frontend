package com.example.spring_shop.model;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {
    private Long id;
    private String title;
    private String description;
    private java.math.BigDecimal price;
    private java.util.List<CategoryDTO> categories;
    private String imageUrl;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String title, String description, BigDecimal price, List<CategoryDTO> categories, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categories = categories;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
