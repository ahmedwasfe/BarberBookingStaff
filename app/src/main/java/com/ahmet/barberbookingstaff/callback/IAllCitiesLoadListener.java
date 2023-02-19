package com.ahmet.barberbookingstaff.callback;

import com.ahmet.barberbookingstaff.model.City;

import java.util.List;

public interface IAllCitiesLoadListener {

    void onAllCitiesLoadSuccess(List<City> mListCity);
    void onAllCitiesLoadFailed(String error);
}
