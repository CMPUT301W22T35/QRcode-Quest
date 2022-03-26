package com.qrcode_quest;

import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PhotoStorage;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.HashMap;

/**
 * creates mock instances of Firebase db and storage
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
    public static FirebaseFirestore createSingerPlayerDb(PlayerAccount account,
                                                         String correspondingDeviceID) {
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        new PlayerManager(db).addPlayer(account, result -> { });
        new PlayerManager(db).createPlayerSession(correspondingDeviceID, account.getUsername(), result -> { });
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
}
