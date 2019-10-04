package com.ahmet.barberbookingstaff.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Salon implements Parcelable {

    private String name,email, address, website, phone, openHour, salonType, salonID;

    public Salon(){}

    public Salon(String name, String email, String address, String phone, String openHour, String salonType, String salonID) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.openHour = openHour;
        this.salonType = salonType;
        this.salonID = salonID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSalonID() {
        return salonID;
    }

    public void setSalonID(String salonID) {
        this.salonID = salonID;
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

    public String getOpenHour() {
        return openHour;
    }

    public void setOpenHour(String openHour) {
        this.openHour = openHour;
    }

    protected Salon(Parcel in) {
        name = in.readString();
        address = in.readString();
        salonID = in.readString();
        website = in.readString();
        phone = in.readString();
        salonType = in.readString();
        openHour = in.readString();
    }

    public static final Creator<Salon> CREATOR = new Creator<Salon>() {
        @Override
        public Salon createFromParcel(Parcel in) {
            return new Salon(in);
        }

        @Override
        public Salon[] newArray(int size) {
            return new Salon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(salonID);
        dest.writeString(website);
        dest.writeString(phone);
        dest.writeString(salonType);
        dest.writeString(openHour);
    }
}
