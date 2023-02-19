package com.ahmet.barberbookingstaff.model;

public class Barber {

    private String error_msg;
    private String name, username, password, image, barberType, barberId;
    private String phone;
    private int gender;
    private String salon;
    private long rate;
    private int available;

    public Barber() {
    }

    public Barber(String name, String username, String password, String barberType) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.barberType = barberType;
    }

    public Barber(String name, String username, String password, String barberType, int available) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.barberType = barberType;
        this.available = available;
    }

    public Barber(String barberId, String name, String username, String password, String image, String barberType, int available) {
        this.barberId = barberId;
        this.name = name;
        this.username = username;
        this.password = password;
        this.image = image;
        this.barberType = barberType;
        this.available = available;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBarberType() {
        return barberType;
    }

    public void setBarberType(String barberType) {
        this.barberType = barberType;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getRate() {
        return rate;
    }

    public void setRate(long rate) {
        this.rate = rate;
    }

    public int isAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getSalon() {
        return salon;
    }

    public void setSalon(String salon) {
        this.salon = salon;
    }

    public int getAvailable() {
        return available;
    }
}
