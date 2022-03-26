package com.qrcode_quest;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.databinding.ActivityHomeBinding;
import com.qrcode_quest.entities.QRShot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    /** A constant tag used for logging */
    public static final String CLASS_TAG = "MainActivity";

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}