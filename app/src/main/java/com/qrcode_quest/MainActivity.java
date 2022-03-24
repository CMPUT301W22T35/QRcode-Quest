package com.qrcode_quest;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.ActivityHomeBinding;
import com.qrcode_quest.entities.QRShot;

public class MainActivity extends AppCompatActivity {
    /** A constant tag used for logging */
    public static final String CLASS_TAG = "MainActivity";

    private ActivityHomeBinding binding;

    private NavController navController;
    private MainViewModelFactory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (viewModelFactory == null) {
            viewModelFactory = new MainViewModelFactory(getApplication());
        }

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

        // Do an initial pull of common data for faster loads in other fragments
        MainViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        viewModel.getCurrentPlayer();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

    public void setViewModelFactory(MainViewModelFactory viewModelFactory) {
        // enforce the method to never be called after onCreate()
        assert this.viewModelFactory == null;
        this.viewModelFactory = viewModelFactory;
    }
}