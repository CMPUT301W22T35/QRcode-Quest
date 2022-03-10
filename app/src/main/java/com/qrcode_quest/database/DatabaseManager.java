package com.qrcode_quest.database;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Represents a set of interfaces to access the Firestore database
 * @author jdumouch, tianming
 * @version 1.0
 */
public class DatabaseManager {
    /**
     * Provides a callback interface for requests that can return a result object that extends
     * the class Result; the caller can then handle the returned result in a callback to determine
     * if it is successful, what is the value of the result and etc.
     */
    public interface OnResult<R> {
        void onResult(R result);
    }

    FirebaseFirestore db;

    public DatabaseManager() {
        this.db = FirebaseFirestore.getInstance();
    }


}
