package com.qrcode_quest.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.manager.PlayerManager;
import com.qrcode_quest.R;
import com.qrcode_quest.ui.login.sign_up.SignUpFragment;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    /** A tag to be used for logging */
    private static final String CLASS_TAG = "LoginActivity";

    private String deviceUID;
    private PlayerAccount authedPlayer;
    private PlayerManager playerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set initial fragment to the loading spinner
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, LoginLoadingFragment.class, null)
                    .commit();
        }

        playerManager = PlayerManager.getInstance();

        // Check SharedPreferences for a stored UID
        SharedPreferences sharedPrefs = this
                .getApplicationContext()
                .getSharedPreferences("qrcode_quest", MODE_PRIVATE);

        // Try to find a saved device ID to lookup which player they are
        String deviceUID;
        if (sharedPrefs.contains("deviceUID")) {
            // Grab the UID and try to authenticate using it
            deviceUID = sharedPrefs.getString("deviceUID", "");
            fetchAuthedPlayer(deviceUID);
        }
        else{
            // Generate a new ID and prompt the user to register

            // TODO check uniqueness of ID on database
            //  (this would catch a _rare_ occurrence of false authentication)

            deviceUID = UUID.randomUUID().toString();
            SharedPreferences.Editor prefEditor = sharedPrefs.edit();
            prefEditor.putString("deviceUID", deviceUID);
            prefEditor.commit();

            promptRegistration();
        }

        this.deviceUID = deviceUID;
        Log.i("Main", "Device UID: " + deviceUID);
    }

    /**
     * Requests the player associated with the specified device UID.
     * If no player (or session) could be found, the view will be switched to the
     * registration view.
     * @param deviceUID The device ID to attempt to authenticate with
     */
    private void fetchAuthedPlayer(String deviceUID) {
        playerManager.getAuthenticatedUser(deviceUID,
                player -> {
                    if (player == null){
                        // If no player could be found, prompt the user to sign up
                        promptRegistration();
                    }
                    else {
                        // Use the player that was retrieved as the logged in user
                        Log.i(CLASS_TAG, "Authenticated player: " + player.getUsername());
                        this.authedPlayer = player;
                        Toast.makeText(LoginActivity.this,
                                "Logged in to " + player.getUsername(),
                                Toast.LENGTH_SHORT).show();
                        // TODO switch to home activity using the fetched player
                    }
                },
                // onError
                ()->{
                    Log.e(CLASS_TAG, "Failed to connect to Firestore");
                });
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
}