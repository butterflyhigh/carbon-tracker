package com.me.carbontracker;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CarData {
    private final List<Car> carList;
    private static CarData instance;

    private static final String TAG = "CarData";

    CarData(Context context) {
        List<String[]> dataList = readCsv(context);
        carList = new ArrayList<>();
        for (String[] data : dataList) {
            carList.add(new Car(data));
        }
    }

    public static synchronized CarData getInstance(Context context) {
        if (instance == null) {
            instance = new CarData(context);
        }
        return instance;
    }

    public List<Car> getCarList() {
        return carList;
    }

    private List<String[]> readCsv(Context context) {
        List<String[]> dataList = new ArrayList<>();
        AssetManager assetManager = context.getAssets();
        try (InputStream is = assetManager.open("data.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            reader.readLine(); // Skip the header line
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",", -1); // Use -1 to include trailing empty strings
                dataList.add(tokens);
            }
        } catch (IOException e) {
            Log.e(TAG, String.valueOf(e));
        }

        return dataList;
    }

    public Car findCarByMakeModel(String makeModel) {
        for (Car car : carList) {
            if ((car.getMake() + " " + car.getModel()).equals(makeModel)) {
                return car;
            }
        }
        return null;
    }
}

