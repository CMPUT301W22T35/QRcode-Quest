package com.qrcode_quest.ui.leaderboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerListViewModel extends ViewModel {


    /** A tag to be used for logging */
    private static final String CLASS_TAG = "PlayerListViewModel";

    /**
     * Loads the playerlist from the database
     * @return The observable list of ALL players
     */
    public LiveData<ArrayList<PlayerAccount>> getPlayers(){
        if (players == null){
            players = new MutableLiveData<>();
            new PlayerManager().getPlayerList(result -> {
                if (!result.isSuccess()) {
                    Log.e(CLASS_TAG, "Failed to load playerlist from database");
                    return;
                }

                players.setValue(result.unwrap());
            });
        }

        return players;
    }
    private MutableLiveData<ArrayList<PlayerAccount>> players;

    /**
     * Loads the QRShots from the database
     * @return The observable list of ALL QRShots
     */
    public LiveData<ArrayList<QRShot>> getShots(){
        if (shots == null){
            shots = new MutableLiveData<>();
            new QRManager().getAllQRShots(result -> {
                if (!result.isSuccess()) {
                    Log.e(CLASS_TAG, "Failed to load QR Shots from database");
                    return;
                }

                shots.setValue(result.unwrap());
            });
        }

        return shots;
    }
    private MutableLiveData<ArrayList<QRShot>> shots;


    /**
     * Loads the QRCodes from the database as a HashMap
     * @return The observable map of ALL QRCodes
     */
    public LiveData<HashMap<String, QRCode>> getCodes(){
        if (codes == null){
            codes = new MutableLiveData<>();
            new QRManager().getAllQRCodesAsMap(result -> {
                if (!result.isSuccess()) {
                    Log.e(CLASS_TAG, "Failed to load QR codes from database");
                    return;
                }

                codes.setValue(result.unwrap());
            });
        }

        return codes;
    }
    private MutableLiveData<HashMap<String, QRCode>> codes;
}