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
import com.qrcode_quest.entities.QRShot;

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



    /**
     * Gets a list containing all the players.
     */
    public LiveData<ArrayList<PlayerAccount>> getPlayers(){
        if (players == null){
            players = new MutableLiveData<>();
            loadPlayers();
        }

        return players;
    }
    private MutableLiveData<ArrayList<PlayerAccount>> players;
    /**
     * Loads the player list into `players`
     */
    public void loadPlayers(){
        Log.d(CLASS_TAG, "Loading players...");
        new PlayerManager().getPlayerList(result -> {
            // Catch errors
            if (!result.isSuccess()) {
                Log.e(CLASS_TAG, "Failed to load player list");
                return;
            }
            // Store the list
            players.setValue(result.unwrap());
        });
    }



    /**
     * Gets a list containing all the players.
     */
    public LiveData<HashMap<String, QRCode>> getCodes(){
        if (codes == null){
            codes = new MutableLiveData<>();
            loadCodes();
        }

        return codes;
    }
    private MutableLiveData<HashMap<String,QRCode>> codes;

    /**
     * Loads the QR code list into `codes`
     */
    public void loadCodes(){
        Log.d(CLASS_TAG, "Loading QR codes...");
        new QRManager().getAllQRCodesAsMap(result -> {
            // Catch errors
            if (!result.isSuccess()) {
                Log.e(CLASS_TAG, "Failed to load QR Code list");
                return;
            }
            // Store the list
            codes.setValue(result.unwrap());
        });
    }




    /**
     * Gets a list containing all the players.
     */
    public LiveData<ArrayList<QRShot>> getShots(){
        if (shots == null){
            shots = new MutableLiveData<>();
            loadShots();
        }

        return shots;
    }
    private MutableLiveData<ArrayList<QRShot>> shots;

    /**
     * Loads the QR shot list into `shots`
     */
    public void loadShots(){
        Log.d(CLASS_TAG, "Loading QR shot...");
        new QRManager().getAllQRShots(result -> {
            // Catch errors
            if (!result.isSuccess()) {
                Log.e(CLASS_TAG, "Failed to load QR Shot list");
                return;
            }
            // Store the list
            shots.setValue(result.unwrap());
        });
    }
}
