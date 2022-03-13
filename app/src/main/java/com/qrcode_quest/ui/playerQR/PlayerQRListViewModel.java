package com.qrcode_quest.ui.playerQR;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;

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
        if (playerShots == null || !playerName.equals(loadedPlayerName)){
            playerShots = new MutableLiveData<>();
            loadPlayerShots(playerName);
        }

        return playerShots;
    }

    /**
     * Handles fetching a player's QRShots from the database
     */
    private void loadPlayerShots(String playerName){
        new QRManager().getPlayerShots(playerName, result -> {
            if (!result.isSuccess()) {
                Log.e(CLASS_TAG, "Failed to get QRShots.");
                return;
            }
            playerShots.setValue(result.unwrap());
            loadedPlayerName = playerName;
        });
    }
}