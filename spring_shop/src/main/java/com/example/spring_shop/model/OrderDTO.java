package com.example.spring_shop.model;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTO {
    private Long id;
    private String address;
    private Long pickupPointId;
    private List<OrderDetailsDTO> details;
    private BigDecimal totalSum;
    private String deliveryStatus;
    private String paymentStatus;
    private String createdTime;
    private String updatedTime;

    // Getters and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Long getPickupPointId() { return pickupPointId; }
    public void setPickupPointId(Long pickupPointId) { this.pickupPointId = pickupPointId; }
    public List<OrderDetailsDTO> getDetails() { return details; }
    public void setDetails(List<OrderDetailsDTO> details) { this.details = details; }
    public BigDecimal getTotalSum() { return totalSum; }
    public void setTotalSum(BigDecimal totalSum) { this.totalSum = totalSum; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }
    public String getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }
}
