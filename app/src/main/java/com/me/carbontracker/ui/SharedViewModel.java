package com.me.carbontracker.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> totalEm = new MutableLiveData<>();
    private final MutableLiveData<String> totalDistance = new MutableLiveData<>();
    private final MutableLiveData<String> driveLogsText = new MutableLiveData<>();
    private final MutableLiveData<Void> addFakeLogsEvent = new MutableLiveData<>();
    private final MutableLiveData<Void> destroyLogsEvent = new MutableLiveData<>();
    private final MutableLiveData<Void> refreshButtonEvent = new MutableLiveData<>();

    public LiveData<String> getTotalEm() {
        return totalEm;
    }

    public void setTotalEm(String text) {
        totalEm.setValue(text);
    }

    public LiveData<String> getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String text) {
        totalDistance.setValue(text);
    }

    public LiveData<String> getDriveLogsText() {
        return driveLogsText;
    }

    public void setDriveLogsText(String text) {
        driveLogsText.setValue(text);
    }

    // Method to notify button click
    public void onAddFakeLogsClicked() {
        addFakeLogsEvent.setValue(null); // You can use Void type since no data is needed
    }

    // Method for observers to listen to the event
    public LiveData<Void> getAddFakeLogsEvent() {
        return addFakeLogsEvent;
    }

    public void onDestroyLogsClicked() {
        destroyLogsEvent.setValue(null);
    }

    public LiveData<Void> getDestroyLogsEvent() {
        return destroyLogsEvent;
    }

    public LiveData<Void> getRefreshButtonEvent() {
        return refreshButtonEvent;
    }

    public void onRefreshButtonClicked() {
        refreshButtonEvent.setValue(null);
    }
}
