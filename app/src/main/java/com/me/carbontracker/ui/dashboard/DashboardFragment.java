package com.me.carbontracker.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.me.carbontracker.databinding.FragmentDashboardBinding;
import com.me.carbontracker.ui.SharedViewModel;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private TextView totalEmView;
    private TextView totalDistanceView;
    private Button refreshButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        // Obtain the ViewModel
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        totalEmView = binding.totalEm;
        refreshButton = binding.button;
        totalDistanceView = binding.totalDistance;

        // Observe the LiveData
        sharedViewModel.getTotalEm().observe(getViewLifecycleOwner(), newText -> {
            totalEmView.setText("Total Emissions (KG): " + newText);
        });
        sharedViewModel.getTotalDistance().observe(getViewLifecycleOwner(), newText -> {
            totalDistanceView.setText("Total Distance Driven (KM): " + newText);
        });

        refreshButton.setOnClickListener(v -> {
            sharedViewModel.onRefreshButtonClicked();
        });

        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public TextView getTotalEmView() {
        return binding.totalEm;
    }

    public void setTotalEmView(String text) {
        totalEmView = binding.totalEm;
        totalEmView.setText(text);
    }
}