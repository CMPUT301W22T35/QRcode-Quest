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

    /**
     * Create a database manager that fetches data from the provided database instance
     * @param db a FirebaseFirestore instance
     */
    public DatabaseManager(FirebaseFirestore db) { this.db = db; }

    /**
     * get the Firestore database instance in the manager
     * @return the Firestore instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }

    protected <T, DocumentType> void retrieveResultByTask(
            Task<DocumentType> task,
            ManagerResult.Listener<T> listener,
            ManagerResult.Retriever<T, DocumentType> retriever) {
        // execute the task
        task.addOnCompleteListener(new OnCompleteListener<DocumentType>() {
            @Override
            public void onComplete(@NonNull Task<DocumentType> task) {
                if (!task.isSuccessful()) {
                    Result<T> result = new Result<>(new DbError(
                            "DatabaseManager.retrieveResultByTask received failure!" +
                                    task.getException(),
                            SENDER_NAME));
                    listener.onResult(result);
                } else {
                    // successful task, process the result
                    DocumentType doc = task.getResult();
                    if (doc == null) {
                        Result<T> result = new Result<T>((T) null);
                        listener.onResult(result);
                    } else {
                        Result<T> result = retriever.retrieveResultFrom(doc);
                        listener.onResult(result);
                    }
                }
            }
        });
    }

    /**
     * retrieve object of given type from a Firestore
     * @param collectionName name of the collection where the document is located
     * @param documentName name of the document to retrieve
     * @param listener handler that handles the object after retrieval completes
     * @param retriever handler that retrieves the object from the document snapshot of the document
     * @param <T> The class type of retrieved object
     */
    protected <T> void retrieveObjectFromDocument(
            String collectionName,
            String documentName,
            ManagerResult.Listener<T> listener,
            ManagerResult.Retriever<T, DocumentSnapshot> retriever) {
        // get a task that retrieves the document
        Task<DocumentSnapshot> task =
                db.collection(collectionName)
                        .document(documentName)
                        .get();
        // execute the task
        retrieveResultByTask(task, listener, retriever);
    }
}
