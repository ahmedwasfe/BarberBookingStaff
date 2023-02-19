package com.ahmet.barberbookingstaff.model.EventBus;

import com.ahmet.barberbookingstaff.model.Barber;

public class BarberEvent {

    private Barber barber;

    public BarberEvent(Barber barber) {
        this.barber = barber;
    }

    public Barber getBarber() {
        return barber;
    }

    public void setBarber(Barber barber) {
        this.barber = barber;
    }
}
