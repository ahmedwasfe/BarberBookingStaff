package com.ahmet.barberbookingstaff.model;

public class City {

    private String city, country;

    public City() {
    }

    public City(String city, String country) {
        this.city = city;
        this.country = country;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
