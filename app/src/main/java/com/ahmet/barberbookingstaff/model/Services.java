package com.ahmet.barberbookingstaff.model;

public class Services {

    private String id, serviceName;
    private Long servicePrice;

    public Services() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
