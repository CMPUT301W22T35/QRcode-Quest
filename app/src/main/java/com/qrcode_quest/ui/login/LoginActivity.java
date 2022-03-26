package com.qrcode_quest.ui.login;

import static com.qrcode_quest.Constants.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.qrcode_quest.MainActivity;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.R;
import com.qrcode_quest.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements SignUpFragment.RegisterHandler {
    /** A tag to be used for logging */
    private static final String CLASS_TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        binding.loginFragmentContainer.setVisibility(View.GONE);
        binding.loginProgress.setVisibility(View.VISIBLE);

        // Set initial fragment to the loading spinner
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.login_fragment_container, SignUpFragment.class, null)
                    .commit();
        }

        AppContainer container = ((QRCodeQuestApp) getApplication()).getContainer();
        SharedPreferences sharedPrefs = container.getPrivateDevicePrefs();

        // Try to use a saved device id and username to authenticate
        String deviceUID, authedUsername;
        if (sharedPrefs.contains(DEVICE_UID_PREF) && sharedPrefs.contains(AUTHED_USERNAME_PREF)) {
            // Grab the UID and try to authenticate using it
            deviceUID = sharedPrefs.getString(DEVICE_UID_PREF, "");
            authedUsername = sharedPrefs.getString(AUTHED_USERNAME_PREF, "");
            onRegistered(deviceUID, authedUsername);
        }
        // On a failed auth attempt, register a new user.
        else{
            transitionToRegistration();
        }

        setContentView(binding.getRoot());
    }

    /**
     * Attempts to authenticate a user with the specified credentials.
     * On success, transitions to main activity.
     * On Failure, opens registration page.
     * @param deviceUID The unique device id to use
     * @param username The username to authenticate with
     */
    @Override
    public void onRegistered(String deviceUID, String username) {
        binding.loginFragmentContainer.setVisibility(View.GONE);
        binding.loginProgress.setVisibility(View.VISIBLE);

        // Check the database for a device session
        AppContainer container = ((QRCodeQuestApp) getApplication()).getContainer();
        new PlayerManager(container.getDb()).validatePlayerSession(deviceUID, username, result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to authenticate players");
                Toast.makeText(this, "Database call failed.", Toast.LENGTH_SHORT).show();
                transitionToRegistration();
                return;
            }

            // Unwrap the result
            Boolean isValid = result.unwrap();
            assert isValid != null;

            if (isValid){
                // Transition to main
                startActivity(new Intent(this, MainActivity.class));
                // End this activity to prevent backing
                finish();
            }
            else {
                // Register the user
                transitionToRegistration();
            }
        });
    }

    /**
     * Switches the view to the registration page.
     */
    private void transitionToRegistration(){
        binding.loginFragmentContainer.setVisibility(View.VISIBLE);
        binding.loginProgress.setVisibility(View.GONE);

        this.getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.login_fragment_container, SignUpFragment.class, null)
                .commit();
    }
}