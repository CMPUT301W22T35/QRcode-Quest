package com.qrcode_quest.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Represents a set of interfaces to access the Firestore database;
 * implemented as a singleton (initialize/get by getInstance())
 * @author tianming
 * @version 1.0
 */
public class DatabaseManager {

    static private final String SENDER_NAME = "DatabaseManager";

    protected FirebaseFirestore db;
    public DatabaseManager() { this.db = FirebaseFirestore.getInstance(); }
    public DatabaseManager(FirebaseFirestore db) { this.db = db; }

    /**
     * A simple callback interface for Managers to return data to their callers.
     */
    public interface OnManagerResult<T> {
        /**
         * Passes data from a Manager to a caller.
         * @param result The data wrapped in a Result
         */
        void onResult(Result<T> result);
    }

    /**
     * get the Firestore database instance in the manager
     * @return the Firestore instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    protected <T, DocumentType> void retrieveResultByTask(
            Task<DocumentType> task,
            ManagerResult.OnRetrieveResult<Result<T>, DocumentType> onRetrieve) {
        // execute the task
        task.addOnCompleteListener(new OnCompleteListener<DocumentType>() {
            @Override
            public void onComplete(@NonNull Task<DocumentType> task) {
                if (!task.isSuccessful()) {
                    Result<T> result = new Result<>(new DbError(
                            "DatabaseManager.retrieveResultByTask received failure!" +
                            task.getException(),
                            SENDER_NAME));
                    onRetrieve.onResult(result);
                } else {
                    // successful task, process the result
                    DocumentType doc = task.getResult();
                    Result<T> result = onRetrieve.retrieveResultFrom(doc);;
                    onRetrieve.onResult(result);
                }
            }
        });
    }

    protected <T> void retrieveObjectFromDocument(
            String collectionName,
            String documentName,
            ManagerResult.OnRetrieveResult<Result<T>, DocumentSnapshot> onRetrieve) {
        // get a task that retrieves the document
        Task<DocumentSnapshot> task =
                db.collection(collectionName)
                .document(documentName)
                .get();
        // execute the task
        retrieveResultByTask(task, onRetrieve);
    }
}
