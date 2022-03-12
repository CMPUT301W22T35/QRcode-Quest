package com.qrcode_quest.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.R;
import com.qrcode_quest.ui.login.sign_up.SignUpFragment;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity implements SignUpFragment.RegisterHandler {
    /** A tag to be used for logging */
    private static final String CLASS_TAG = "LoginActivity";

    // TODO move this to a global location
    private static final String PREF_PATH = "qrcode_quest";
    private static final String AUTHED_USERNAME_PREF = "authed_username";
    private static final String DEVICE_UID_PREF = "device_UID";

    private PlayerManager playerManager;

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

        playerManager = new PlayerManager();
        SharedPreferences sharedPrefs = this.getApplicationContext()
                .getSharedPreferences(PREF_PATH, MODE_PRIVATE);

        // Try to use a saved device id and username to authenticate
        String deviceUID, authedUsername;
        if (sharedPrefs.contains(DEVICE_UID_PREF) && sharedPrefs.contains(AUTHED_USERNAME_PREF)) {
            // Grab the UID and try to authenticate using it
            deviceUID = sharedPrefs.getString(DEVICE_UID_PREF, "");
            authedUsername = sharedPrefs.getString(AUTHED_USERNAME_PREF, "");

            onRegistered(deviceUID, authedUsername);
        }
        else{
            promptRegistration();
        }
    }

    /**
     * Switches the view to the registration page.
     */
    private void promptRegistration(){
        this.getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerView, SignUpFragment.class, null)
                .commit();
    }

    @Override
    public void onRegistered(String deviceUID, String username) {
        playerManager.validatePlayerSession(deviceUID, username, result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to authenticate players");
                Toast.makeText(this, "Database call failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            Boolean isValid = result.unwrap();
            assert isValid != null;
            Log.e(CLASS_TAG, "Valid? " + isValid + " (" + deviceUID + " " + username + ")");
            if (isValid){
                // Transition to main activity
                Toast.makeText(this, "Auth success!", Toast.LENGTH_SHORT).show();
            }
            else {
                // Register the user
                promptRegistration();
            }
        });
    }


}