package com.qrcode_quest;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PhotoStorage;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.database.Schema;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * creates mock instances of Firebase db and storage
 *
 * @author tianming, jdumouch
 * @version 1.1
 */
public class MockInstances {
    /**
     * creates a completely empty database
     * @return an empty FirebaseFirestore db
     */
    public static FirebaseFirestore createEmptyDb() {
        return MockDb.createMockDatabase(new HashMap<>());
    }

    /**
     * creates a database with only one player registered
     * @return a FirebaseFirestore db
     */
    public static FirebaseFirestore createSinglePlayerDb(PlayerAccount account,
                                                         String correspondingDeviceID) {
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        new PlayerManager(db).addPlayer(account, result -> { });
        new PlayerManager(db).createPlayerSession(correspondingDeviceID, account.getUsername(), result -> { });
        return db;
    }

    /**
     * Creates a database with a player and QR Shots
     * @return a FirebaseFirestore db
     */
    public static FirebaseFirestore createPlayersQRShotsDb(ArrayList<QRShot> shots,
                                                           PlayerAccount user,
                                                           String correspondingDeviceID){
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        PlayerManager playerManager = new PlayerManager(db);
        playerManager.addPlayer(user, result -> {});
        playerManager.createPlayerSession(correspondingDeviceID, user.getUsername(), result -> {});

        QRManager qrManager = new QRManager(db, createEmptyPhotoStorage());
        for (QRShot qrShot: shots){
            qrManager.createQRShot(qrShot, result -> {}, result -> {});
        }

        return db;
    }

    /**
     * creates an empty PhotoStorage mock object
     */
    public static PhotoStorage createEmptyPhotoStorage() {
        FirebaseStorage storage = MockFirebaseStorage.createMockFirebaseStorage(new HashMap<>());
        return MockFirebaseStorage.createMockPhotoStorage(storage);
    }

    public static SharedPreferences createEmptySharedPreferences() {
        return MockSharedPref.createMockSharedPref(new HashMap<>());
    }

    /**
     * Create a mock preference of a pre-registered user.
     * @param username The username of the player
     * @param deviceID The device ID of the player.
     */
    public static SharedPreferences createRegisteredPreferences(String username, String deviceID){
        HashMap<String, String> prefMap = new HashMap<>();
        prefMap.put(Constants.AUTHED_USERNAME_PREF, username);
        prefMap.put(Constants.DEVICE_UID_PREF, deviceID);

        return MockSharedPref.createMockSharedPref(prefMap);
    }
}
