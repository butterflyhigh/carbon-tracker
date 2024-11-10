package com.me.carbontracker.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.me.carbontracker.Car;
import com.me.carbontracker.CarData;
import com.me.carbontracker.databinding.FragmentSettingsBinding;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private static final String TAG = "SettingsFragment";

    private AutoCompleteTextView autoCompleteCarModel;
    private CarData carData;
    private List<Car> carList;
    private List<String> makeModelList;
    private TextView currentSelectedCar;
    private TextView currentSelctedCo2;

    private SettingsViewModel settingsViewModel;

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_SELECTED_CAR = "selected_car";
    private static final String KEY_CO2 = "co2"; // g/km

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        currentSelectedCar = binding.currentSelectedCar;
        currentSelctedCo2 = binding.selectedCo2;
        Context context = getContext();

        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        carData = CarData.getInstance(context);
        carList = carData.getCarList();

        makeModelList = new ArrayList<>();
        for (Car car : carList) {
            makeModelList.add(car.getMake() + " " + car.getModel());
        }

        autoCompleteCarModel = binding.autoCompleteCarModel;
        assert context != null;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, makeModelList);
        autoCompleteCarModel.setAdapter(adapter);

        autoCompleteCarModel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedMakeModel = (String) parent.getItemAtPosition(position);
                Car selectedCar = carData.findCarByMakeModel(selectedMakeModel);
                Log.d(TAG, "Selected " + selectedCar);

                settingsViewModel.setCarType(selectedCar);

                saveSelectedCar(selectedMakeModel, context);
                saveCo2(selectedCar.getEmissions(), context);

                currentSelctedCo2.setText(String.valueOf(selectedCar.getEmissions()));

                Log.d(TAG, "Saved CO2: " + selectedCar.getEmissions());
            }
        });

        settingsViewModel.getCarType().observe(getViewLifecycleOwner(), carType -> {
            currentSelectedCar.setText(carType.getMake() + " " + carType.getModel());
            try {
                currentSelctedCo2.setText(String.valueOf(carType.getEmissions()));
            } catch(Exception e) {
                currentSelctedCo2.setText("Unset");
            }

            autoCompleteCarModel.setText(carType.getMake() + " " + carType.getModel(), false);
        });

        loadSavedCar(context);
        View root = binding.getRoot();
        return root;
    }

    /**
     * Saves the selected car's make and model to SharedPreferences.
     *
     * @param makeModel The make and model of the selected car.
     * @param context   The context to access SharedPreferences.
     */
    private void saveSelectedCar(String makeModel, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SELECTED_CAR, makeModel);
        editor.apply();
        Log.d(TAG, "Saved selected car: " + makeModel);
    }

    private void saveCo2(long co2, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_CO2, co2);
        editor.apply();
        Log.d(TAG, "Saved CO2 emissions: " + co2);
    }

    private void loadSavedCar(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedMakeModel = sharedPreferences.getString(KEY_SELECTED_CAR, null);
        if (savedMakeModel != null) {
            Car savedCar = carData.findCarByMakeModel(savedMakeModel);
            if (savedCar != null) {
                settingsViewModel.setCarType(savedCar);
                Log.d(TAG, "Loaded saved car: " + savedMakeModel);
            }
        } else {
            if (!carList.isEmpty()) {
                Car defaultCar = carList.get(0);
                settingsViewModel.setCarType(defaultCar);
                Log.d(TAG, "No saved car, set default: " + defaultCar.getMake() + " " + defaultCar.getModel());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
