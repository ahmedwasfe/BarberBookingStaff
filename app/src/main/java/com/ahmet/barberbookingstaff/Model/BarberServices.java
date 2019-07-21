package com.ahmet.barberbookingstaff.Model;

public class BarberServices {

    private String serviceName;
    private Long servicePrice;

    public BarberServices() {}

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(Long servicePrice) {
        this.servicePrice = servicePrice;
    }
}
