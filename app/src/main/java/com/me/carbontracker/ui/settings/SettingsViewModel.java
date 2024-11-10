package com.me.carbontracker.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.me.carbontracker.Car;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Car> carType;
    private final MutableLiveData<Car> carSelectEvent = new MutableLiveData<>();

    public SettingsViewModel() {
        carType = new MutableLiveData<>();
    }

    public LiveData<Car> getCarType() {
        return carType;
    }

    public void setCarType(Car car) {
        carType.setValue(car);
    }
}