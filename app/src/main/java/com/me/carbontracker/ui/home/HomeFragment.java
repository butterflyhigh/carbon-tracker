package com.me.carbontracker.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.me.carbontracker.databinding.FragmentHomeBinding;
import com.me.carbontracker.ui.SharedViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView driveLogsTextView;
    private Button addFakeLogsButton;
    private Button destroyLogsButton;
    private Button refreshButton;
    private SharedViewModel sharedViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Obtain the ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        driveLogsTextView = binding.driveLogs;

        sharedViewModel.getDriveLogsText().observe(getViewLifecycleOwner(), newText -> {
            driveLogsTextView.setText(newText);
        });

        addFakeLogsButton = binding.addFakeLogsButton;
        destroyLogsButton = binding.destroyLogsButton;
        refreshButton = binding.refreshButton;

        addFakeLogsButton.setOnClickListener(v -> {
            sharedViewModel.onAddFakeLogsClicked();
        });
        destroyLogsButton.setOnClickListener(v -> {
            sharedViewModel.onDestroyLogsClicked();
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
}