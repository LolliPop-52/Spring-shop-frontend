package com.example.spring_shop.model;

import java.util.List;

public class ActiveOrdersDTO {
    private List<OrderDTO> orders;

    public List<OrderDTO> getOrders() { return orders; }
    public void setOrders(List<OrderDTO> orders) { this.orders = orders; }
}
