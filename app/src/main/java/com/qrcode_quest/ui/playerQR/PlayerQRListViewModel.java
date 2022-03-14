package com.qrcode_quest.ui.playerQR;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A ViewModel for PlayerQRListFragment
 *
 * @author jdumouch
 */
public class PlayerQRListViewModel extends ViewModel {
    /** A tag to be used for logging */
    private static final String CLASS_TAG = "PlayerQRListViewModel";

    /** A player name to track which player's shots are loaded */
    private String loadedPlayerName;

    /** A player's entire QRShot list */
    private MutableLiveData<ArrayList<QRShot>> playerShots;
    /**
     * Requests a specified player's QRShot records.
     * @param playerName The username of the player to load QRShots of
     */
    public LiveData<ArrayList<QRShot>> getPlayerShots(String playerName){
        // Reload every time in case we popped back from deleting a code
        playerShots = new MutableLiveData<>();
        loadPlayerShots(playerName);
        return playerShots;
    }

    /**
     * Handles fetching a player's QRShots from the database
     */
    private void loadPlayerShots(String playerName){
        Log.d(CLASS_TAG, String.format("Loading %s's QRShots...", playerName));
        new QRManager().getPlayerShots(playerName, result -> {
            if (!result.isSuccess()) {
                Log.e(CLASS_TAG, "Failed to get QRShots.");
                return;
            }

            Log.d(CLASS_TAG, String.format("Loading %s's QRShots...done", playerName));
            playerShots.setValue(result.unwrap());
            loadedPlayerName = playerName;
        });
    }


    /**
     * Gets a list containing all of the QRCodes
     */
    public LiveData<HashMap<String, QRCode>> getCodes(){
        if (qrCodes == null){
            qrCodes = new MutableLiveData<>();
            loadQRCodes();
        }

        return qrCodes;
    }
    private MutableLiveData<HashMap<String, QRCode>> qrCodes;

    /**
     * Forces a refresh of the QRCodes
     */
    private void loadQRCodes(){
        Log.d(CLASS_TAG, "Loading QRCodes...");
        new QRManager().getAllQRCodesAsMap(result ->{
            if (!result.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load QR codes");
                return;
            }

            Log.d(CLASS_TAG, "Loading QRCodes...done.");
            qrCodes.setValue(result.unwrap());
        });
    }
}