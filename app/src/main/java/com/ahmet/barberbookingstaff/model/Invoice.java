package com.ahmet.barberbookingstaff.model;

import java.util.List;

public class Invoice {

    private String salonID, salonName, salonAddress;
    private String barberID, barberName;
    private String customerName, customerPhone;
    private String imageUrl;
    private double finalPrice;
    private List<CartItem> mListShopping;
    private List<Services> mListServices;

    public Invoice() {}

    public String getSalonID() {
        return salonID;
    }

    public void setSalonID(String salonID) {
        this.salonID = salonID;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getSalonAddress() {
        return salonAddress;
    }

    public void setSalonAddress(String salonAddress) {
        this.salonAddress = salonAddress;
    }

    public String getBarberID() {
        return barberID;
    }

    public void setBarberID(String barberID) {
        this.barberID = barberID;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<CartItem> getmListShopping() {
        return mListShopping;
    }

    public void setmListShopping(List<CartItem> mListShopping) {
        this.mListShopping = mListShopping;
    }

    public List<Services> getmListServices() {
        return mListServices;
    }

    public void setmListServices(List<Services> mListServices) {
        this.mListServices = mListServices;
    }
}
