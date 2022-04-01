package com.qrcode_quest.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.qrcode_quest.R;
import com.qrcode_quest.databinding.FragmentHomeBinding;
import com.qrcode_quest.ui.map.MapFragment;

public class HomeFragment extends Fragment {
    /** A constant tag used for logging */
    public static final String CLASS_TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    // Permissions
    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        // Handle Map button click to go to Map
        binding.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get permissions before going to map
                getPermissions(getActivity());
                if (hasPermissions()){
                    Navigation.findNavController(view).navigate(R.id.action_navigation_home_to_mapFragment);
                } else {
                    Log.d(CLASS_TAG, "Must grant permissions");
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * If the app does not have permission,
     * prompt user to grant permissions
     */
    public void getPermissions(Activity activity) {
        if (!hasPermissions() ) {
            // Log.d(CLASS_TAG, "Permissions not granted");
            // Permissions not granted, prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_CODE
            );
        }
    }

    /**
     * Checks if the app has necessary permissions
     * @return  a boolean value
     */
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
    }

}