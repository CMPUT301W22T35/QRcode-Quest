package com.qrcode_quest;

import static com.qrcode_quest.Constants.*;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A central view model for all fragments in MainActivity.
 *
 * @author jdumouch
 * @version 1.0
 */
public class MainViewModel extends AndroidViewModel {
    /** A tag used for logging */
    private static final String CLASS_TAG = "MainViewModel";

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Gets the PlayerAccount of the active user
     */
    public LiveData<PlayerAccount> getCurrentPlayer(){
        if (currentPlayer == null){
            currentPlayer = new MutableLiveData<>();
            loadCurrentPlayer();
        }

        return currentPlayer;
    }
    private MutableLiveData<PlayerAccount> currentPlayer;


    /**
     * Uses the authenticated username preference to load the PlayerAccount from the database.
     */
    private void loadCurrentPlayer(){
        // Grab the username of the authenticated player
        SharedPreferences sharedPrefs = getApplication().getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PATH, Context.MODE_PRIVATE);
        if (!sharedPrefs.contains(AUTHED_USERNAME_PREF)) { return; }
        String username = sharedPrefs.getString(AUTHED_USERNAME_PREF, "");

        // Load the players record
        Log.d(CLASS_TAG, "Loaded authed user: " + username + "...");
        new PlayerManager().getPlayer(username, result -> {
            // Catch errors/failure
            if (!result.isSuccess() || result.unwrap() == null) {
                Log.e(CLASS_TAG, "Failed to load current player.");
                return;
            }

            Log.d(CLASS_TAG, "Loading authed user: " + username + "... done.");
            // Store the user
            currentPlayer.setValue(result.unwrap());
        });
    }
}
