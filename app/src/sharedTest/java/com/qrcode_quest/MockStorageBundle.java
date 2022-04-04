package com.qrcode_quest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.database.PhotoStorage;

/**
 * A data structure to pass the database bundle around.
 */
public class MockStorageBundle {
    public FirebaseFirestore db;
    public PhotoStorage photoStorage;

    public MockStorageBundle(FirebaseFirestore db, PhotoStorage photoStorage){
        this.db = db;
        this.photoStorage = photoStorage;
    }
}
