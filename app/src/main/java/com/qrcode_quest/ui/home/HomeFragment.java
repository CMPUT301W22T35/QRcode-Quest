package com.qrcode_quest.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.qrcode_quest.MainViewModel;
import com.qrcode_quest.R;
import com.qrcode_quest.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    /** A constant tag used for logging */
    public static final String CLASS_TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    // Permissions
    private static final int REQUEST_CODE = 1;
    private static final String[] MAP_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final String[] CAM_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Handle Map button click to go to Map
        binding.homeMapButton.setOnClickListener(view -> {
            // Get permissions before going to map
            promptMapPermissions();
            if (hasMapPermissions()){
                Navigation.findNavController(root).navigate(R.id.action_navigation_home_to_mapFragment);
            } else {
                Toast.makeText(getContext(),
                        "Failed to get location permissions",
                        Toast.LENGTH_LONG).show();
                Log.d(CLASS_TAG, "Must grant permissions");
            }
        });

        // If the main player reloads, update the buttons
        mainViewModel.getCurrentPlayer().observe(getViewLifecycleOwner(), player->{
            NavController navController = NavHostFragment.findNavController(this);

            // Hook up the camera button
            binding.homeCaptureButton.setOnClickListener(view -> {
                if (hasCamPermissions()) {
                    // Navigate to the capture fragment
                    HomeFragmentDirections.ActionHomeToCapture action =
                            HomeFragmentDirections.actionHomeToCapture(player);
                    navController.navigate(action);
                }
                else{
                    promptCamPermissions();
                }
            });

            // Hook up the 'My Captures' button
            binding.homeQrsButton.setOnClickListener(view -> {
                    // Navigate to the QRView of the clicked shot
                    HomeFragmentDirections.ActionHomeToPlayerQrlist action =
                            HomeFragmentDirections.actionHomeToPlayerQrlist(player);
                    navController.navigate(action);
                });
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * If the app does not have permission to use maps,
     * prompt user to grant permissions
     */
    public void promptMapPermissions() {
        if (!hasMapPermissions() ) {
            // Permissions not granted, prompt the user
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    MAP_PERMISSIONS,
                    REQUEST_CODE
            );
        }
    }

    /**
     * Checks if the app has necessary permissions to open the map
     */
    private boolean hasMapPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the app has necessary permissions to open the camera
     */
    private boolean hasCamPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
    }

    /**
     * If the app does not have permission to use camera,
     * prompt user to grant permissions
     */
    public void promptCamPermissions() {
        if (!hasCamPermissions() ) {
            // Permissions not granted, prompt the user
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    CAM_PERMISSIONS,
                    REQUEST_CODE
            );
        }
    }

}