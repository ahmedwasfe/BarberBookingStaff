package com.ahmet.barberbookingstaff.Model.EventBus;

import com.ahmet.barberbookingstaff.Model.Salon;

public class SalonEvent {

    private Salon salon;

    public SalonEvent(Salon salon) {
        this.salon = salon;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }
}
