package com.qrcode_quest.application;

import static com.qrcode_quest.Constants.SHARED_PREF_PATH;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.database.PhotoStorage;

/**
 * for manual dependency injection
 * this class lazily initializes all variables whenever possible
 */
public class AppContainer {
    private QRCodeQuestApp app;
    private FirebaseFirestore db;
    private PhotoStorage storage;
    private SharedPreferences privateDevicePrefs;

    public AppContainer(QRCodeQuestApp app) {
        this.app = app;
    }

    public FirebaseFirestore getDb() {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        assert this.db == null;  // prevent accidentally set twice
        this.db = db;
    }

    public PhotoStorage getStorage() {
        if (storage == null)
            storage = new PhotoStorage(
                    FirebaseStorage.getInstance(),
                    new PhotoStorage.PhotoEncoding());
        return storage;
    }

    public void setStorage(PhotoStorage storage) {
        assert this.storage == null;  // prevent accidentally set twice
        this.storage = storage;
    }

    public SharedPreferences getPrivateDevicePrefs() {
        // can create it each time this method is called as well
        if (privateDevicePrefs == null) {
            privateDevicePrefs = app.getApplicationContext()
                    .getSharedPreferences(SHARED_PREF_PATH, Context.MODE_PRIVATE);
        }
        return privateDevicePrefs;
    }

    public void setPrivateDevicePrefs(SharedPreferences privateDevicePrefs) {
        assert this.privateDevicePrefs == null;  // prevent accidentally set twice
        this.privateDevicePrefs = privateDevicePrefs;
    }
}
