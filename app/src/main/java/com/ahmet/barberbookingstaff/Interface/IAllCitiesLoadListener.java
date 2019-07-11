package com.ahmet.barberbookingstaff.Interface;

import com.ahmet.barberbookingstaff.Model.City;

import java.util.List;

public interface IAllCitiesLoadListener {

    void onAllCitiesLoadSuccess(List<City> mListCity);
    void onAllCitiesLoadFailed(String error);
}
