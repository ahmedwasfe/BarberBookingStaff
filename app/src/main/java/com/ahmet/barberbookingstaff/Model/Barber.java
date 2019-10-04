package com.ahmet.barberbookingstaff.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Barber implements Parcelable {

    private String name, username, password, barberType, barberID;
    private long rating;

    public Barber() {
    }

    public Barber(String name, String username, String password, String barberType) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.barberType = barberType;
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

    public String getBarberID() {
        return barberID;
    }

    public void setBarberID(String barberID) {
        this.barberID = barberID;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    protected Barber(Parcel in) {
        name = in.readString();
        username = in.readString();
        password = in.readString();
        barberType = in.readString();
        barberID = in.readString();
        rating = in.readLong();
    }

    public static final Creator<Barber> CREATOR = new Creator<Barber>() {
        @Override
        public Barber createFromParcel(Parcel in) {
            return new Barber(in);
        }

        @Override
        public Barber[] newArray(int size) {
            return new Barber[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(barberType);
        dest.writeString(barberID);
        dest.writeLong(rating);
    }
}
