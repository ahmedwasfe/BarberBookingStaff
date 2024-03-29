package com.ahmet.barberbookingstaff.model.EventBus;

import com.ahmet.barberbookingstaff.model.Barber;
import com.ahmet.barberbookingstaff.model.Salon;

public class EnableNextButton {

    private int step;
    private Salon salon;
    private Barber barber;


    public EnableNextButton(int step, Salon salon) {
        this.step = step;
        this.salon = salon;
    }

    public EnableNextButton(int step, Barber barber) {
        this.step = step;
        this.barber = barber;
    }


    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public Barber getBarber() {
        return barber;
    }

    public void setBarber(Barber barber) {
        this.barber = barber;
    }
}
