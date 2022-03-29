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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.application.AppContainer;
import com.qrcode_quest.application.QRCodeQuestApp;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PhotoStorage;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A central view model for all fragments in MainActivity.
 * caches some database data that are commonly shared among the fragments (lists of players, QR
 * codes and so on). The cached lists are initialized as empty so we can assume they are non-null
 *
 * @author jdumouch, tianming
 * @version 1.1
 */
public class MainViewModel extends AndroidViewModel {
    /** A tag used for logging */
    private static final String CLASS_TAG = "MainViewModel";

    private final FirebaseFirestore db;
    private final PhotoStorage photoStorage;

    public MainViewModel(@NonNull Application application,
                         FirebaseFirestore db, PhotoStorage photoStorage) {
        super(application);
        this.db = db;
        this.photoStorage = photoStorage;
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

    /**
     * Gets a list containing all the players.
     */
    public LiveData<ArrayList<PlayerAccount>> getPlayers(){
        if (players == null){
            players = new MutableLiveData<>(new ArrayList<>());
            loadPlayers();
        }

        return players;
    }

    /**
     * Gets a list containing all the QR codes
     */
    public LiveData<ArrayList<QRCode>> getQRCodes(){
        ensureQRCodesAndShotsInitialized();
        return allQRCodes;
    }

    /**
     * Gets a list containing all the QR codes
     */
    public LiveData<ArrayList<QRShot>> getQRShots(){
        ensureQRCodesAndShotsInitialized();
        return allQRShots;
    }
    private MutableLiveData<PlayerAccount> currentPlayer;
    private MutableLiveData<ArrayList<PlayerAccount>> players;
    private MutableLiveData<ArrayList<QRCode>> allQRCodes;
    private MutableLiveData<ArrayList<QRShot>> allQRShots;


    /**
     * Uses the authenticated username preference to load the PlayerAccount from the database.
     */
    private void loadCurrentPlayer(){
        // Grab the username of the authenticated player
        AppContainer container = ((QRCodeQuestApp) getApplication()).getContainer();
        SharedPreferences sharedPrefs = container.getPrivateDevicePrefs();
        if (!sharedPrefs.contains(AUTHED_USERNAME_PREF)) { return; }
        String username = sharedPrefs.getString(AUTHED_USERNAME_PREF, "");

        // Load the players record
        Log.d(CLASS_TAG, "Loaded authed user: " + username + "...");
        new PlayerManager(db).getPlayer(username, result -> {
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
     * Loads the player list into `players` from database
     */
    public void loadPlayers(){
        Log.d("MainViewModel", "Loading players...");
        new PlayerManager(db).getPlayerList(result -> {
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
     * loads the QR codes and shots lists from database
     */
    public void loadQRCodesAndShots(){
        Log.d(CLASS_TAG, "Loading QR codes and shots...");
        new QRManager(db, photoStorage).getAllQRShots(new ManagerResult.Listener<ArrayList<QRShot>>() {
            @Override
            public void onResult(Result<ArrayList<QRShot>> result) {
                if (!result.isSuccess()) {
                    Log.e(CLASS_TAG, "Failed to load qr codes/shots");
                    return;
                }
                ArrayList<QRShot> shots = result.unwrap();
                HashMap<String, QRCode> codes = new HashMap<>();
                for (QRShot shot: shots) {
                    if(!codes.containsKey(shot.getCodeHash())) {
                        String qrHash = shot.getCodeHash();
                        QRCode newCode = new QRCode(qrHash, RawQRCode.getScoreFromHash(qrHash));
                        codes.put(qrHash, newCode);
                    }
                }
                allQRCodes.setValue(new ArrayList<>(codes.values()));
                allQRShots.setValue(shots);
            }
        });
    }

    /**
     * initialize the QRCodes and QRShots lists if they have not already been
     * both lists are always updated at the same time
     */
    private void ensureQRCodesAndShotsInitialized(){
        if (allQRCodes == null || allQRShots == null) {
            allQRCodes = new MutableLiveData<>(new ArrayList<>());
            allQRShots = new MutableLiveData<>(new ArrayList<>());
            loadQRCodesAndShots();
        }
    }
}
