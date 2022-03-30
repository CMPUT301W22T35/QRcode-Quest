package com.qrcode_quest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.databinding.ActivityHomeBinding;
import com.qrcode_quest.entities.QRShot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    /** A constant tag used for logging */
    public static final String CLASS_TAG = "MainActivity";

    // Storage Permissions
    private static final int REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    };

    private ActivityHomeBinding binding;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup the NavController, NavBar and ActionBar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_leaderboard, R.id.navigation_home, R.id.navigation_account)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // create the main view model
        AppContainer container = ((QRCodeQuestApp) getApplication()).getContainer();
        ViewModelProvider.Factory mainViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                if (aClass.isAssignableFrom(MainViewModel.class))
                    return Objects.requireNonNull(aClass.cast(new MainViewModel(
                            getApplication(), container.getDb(), container.getStorage())));
                else
                    throw new IllegalArgumentException("Unexpected ViewModelClass type request received by the factory!");
            }
        };

        // Do an initial pull of common data for faster loads in other fragments
        MainViewModel viewModel = new ViewModelProvider(this, mainViewModelFactory).get(MainViewModel.class);
        viewModel.getCurrentPlayer();

        getPermissions(this);

    }

    /**
     * If the app does not have permission,
     * prompt user to grant permissions
     */
    public void getPermissions(Activity activity) {
        if (!hasPermissions() ) {
            Log.d(CLASS_TAG, "Permissions not granted");
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) ==
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) ==
                        PackageManager.PERMISSION_GRANTED;
    }
}
