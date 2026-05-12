package com.example.spring_shop.model;

import java.util.List;

public class CategoryResponse {
    private List<CategoryDTO> content;

    public List<CategoryDTO> getContent() {
        return content;
    }

    public void setContent(List<CategoryDTO> content) {
        this.content = content;
    }
}