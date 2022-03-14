package com.qrcode_quest.ui.login;

import static com.qrcode_quest.Constants.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.qrcode_quest.MainActivity;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.R;
import com.qrcode_quest.ui.login.sign_up.SignUpFragment;

public class LoginActivity extends AppCompatActivity implements SignUpFragment.RegisterHandler {
    /** A tag to be used for logging */
    private static final String CLASS_TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set initial fragment to the loading spinner
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, LoginLoadingFragment.class, null)
                    .commit();
        }

        SharedPreferences sharedPrefs = this.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PATH, MODE_PRIVATE);

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
        transitionToLoading();

        // Check the database for a device session
        new PlayerManager().validatePlayerSession(deviceUID, username, result ->{
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
        this.getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerView, SignUpFragment.class, null)
                .commit();
    }

    /**
     * Switches to a view of a login spinner
     */
    private void transitionToLoading(){
        this.getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerView, LoginLoadingFragment.class, null)
                .commit();
    }


}