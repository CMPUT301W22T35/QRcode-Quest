package com.qrcode_quest.application;

import static com.qrcode_quest.Constants.SHARED_PREF_PATH;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;

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
    private LocationManager locationManager;

    /**
     * creates an app container
     * @param app the app that will hold this container
     */
    public AppContainer(QRCodeQuestApp app) {
        this.app = app;
    }

    /**
     * obtain the Firestore db reference
     * @return a Firestore instance
     */
    public FirebaseFirestore getDb() {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        return db;
    }

    /**
     * initialize Firestore db reference
     * @param db a Firestore instance
     */
    public void setDb(FirebaseFirestore db) {
        assert this.db == null;  // prevent accidentally set twice
        this.db = db;
    }

    /**
     * obtain the PhotoStorage reference
     * @return a PhotoStorage instance
     */
    public PhotoStorage getStorage() {
        if (storage == null)
            storage = new PhotoStorage(
                    FirebaseStorage.getInstance(),
                    new PhotoStorage.PhotoEncoding());
        return storage;
    }

    /**
     * initialize PhotoStorage db reference
     * @param storage a PhotoStorage instance
     */
    public void setStorage(PhotoStorage storage) {
        assert this.storage == null;  // prevent accidentally set twice
        this.storage = storage;
    }

    /**
     * obtain the private device shared preferences
     * @return a SharedPreferences instance
     */
    public SharedPreferences getPrivateDevicePrefs() {
        // can create it each time this method is called as well
        if (privateDevicePrefs == null) {
            privateDevicePrefs = app.getApplicationContext()
                    .getSharedPreferences(SHARED_PREF_PATH, Context.MODE_PRIVATE);
        }
        return privateDevicePrefs;
    }

    /**
     * initialize private device shared preferences
     * @param privateDevicePrefs private device shared preferences
     */
    public void setPrivateDevicePrefs(SharedPreferences privateDevicePrefs) {
        assert this.privateDevicePrefs == null;  // prevent accidentally set twice
        this.privateDevicePrefs = privateDevicePrefs;
    }

    /**
     * obtain the LocationManager instance
     * @return a LocationManager instance
     */
    public LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) app.getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager;
    }

    /**
     * initialize the LocationManager instance
     * @param locationManager a LocationManager instance
     */
    public void setLocationManager(LocationManager locationManager) {
        assert this.locationManager == null;
        this.locationManager = locationManager;
    }
}
