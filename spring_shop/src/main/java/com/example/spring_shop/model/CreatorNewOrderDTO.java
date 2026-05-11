package com.example.spring_shop.model;

import java.util.List;

public class CreatorNewOrderDTO {
    private String userEmail;
    private Long addressId;
    private String paymentType;
    private List<CreatorNewOrderDetailsDTO> orderDetails;

    public CreatorNewOrderDTO() {}

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public List<CreatorNewOrderDetailsDTO> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<CreatorNewOrderDetailsDTO> orderDetails) { this.orderDetails = orderDetails; }
}
