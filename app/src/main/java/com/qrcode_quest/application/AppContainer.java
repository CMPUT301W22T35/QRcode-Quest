package com.qrcode_quest.application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.database.PhotoStorage;

/**
 * for manual dependency injection
 * this class lazily initializes all variables whenever possible
 */
public class AppContainer {
    private FirebaseFirestore db;
    private PhotoStorage storage;

    public FirebaseFirestore getDb() {
        if (db == null)
            db = FirebaseFirestore.getInstance();
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        assert this.db == null;
        this.db = db;
    }

    public PhotoStorage getStorage() {
        if (storage == null)
            storage = new PhotoStorage();
        return storage;
    }

    public void setStorage(PhotoStorage storage) {
        assert this.storage == null;
        this.storage = storage;
    }
}
