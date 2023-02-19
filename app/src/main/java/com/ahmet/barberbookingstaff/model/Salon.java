package com.ahmet.barberbookingstaff.model;

import java.util.Date;

public class Salon {

    private String salonName, email, address, city, website, phone,
            salonType, salonId;

    private double latitude, longitude;

    private int salonStatus;
    private String create_at;

    public Salon(String salonName) {
        this.salonName = salonName;
    }

    public Salon(String salonName, String email) {
        this.salonName = salonName;
        this.email = email;
    }

    public Salon(){}

//    public Salon(double latitude, double longitude) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//    }


//    public Salon(String salonId, String salonName, String phone, String address, String city, String website, String email, String openHour,
//                 String salonType,
//                 double latitude, double longitude, boolean salonStatus) {
//
//        this.salonId = salonId;
//        this.salonName = salonName;
//        this.phone = phone;
//        this.address = address;
//        this.city = city;
//        this.website = website;
//        this.email = email;
//        this.salonType = salonType;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.salonStatus = salonStatus;
//    }


    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSalonType() {
        return salonType;
    }

    public void setSalonType(String salonType) {
        this.salonType = salonType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

//    public String getOpenHour() {
//        return openHour;
//    }
//
//    public void setOpenHour(String openHour) {
//        this.openHour = openHour;
//    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int isSalonStatus() {
        return salonStatus;
    }

    public void setSalonStatus(int salonStatus) {
        this.salonStatus = salonStatus;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }
}
