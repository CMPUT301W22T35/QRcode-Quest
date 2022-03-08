package com.qrcode_quest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    NavHostFragment navigationHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast toast = Toast.makeText(this, "Hello toast", Toast.LENGTH_SHORT);
        toast.show();

        initFragments();
    }

    private void initFragments() {
        FragmentContainerView container = findViewById(R.id.fragmentContainerView);

        // https://stackoverflow.com/questions/58703451/fragmentcontainerview-as-navhostfragment
        NavController controller = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView))).getNavController();

        NavDirections action = TestFragmentDirections.actionTestFragmentToTestFragment2();
        controller.navigate(action);
        //NavigationUI.setupWithNavController(view, controller);
    }
}