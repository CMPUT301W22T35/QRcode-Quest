package com.qrcode_quest.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.qrcode_quest.R;
import com.qrcode_quest.database.ManagerResult.*;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.HashMap;

/**
 * Acts as an interface between the app and the database for queries concerning PlayerAccounts.
 * @see com.qrcode_quest.entities.PlayerAccount
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerManager extends DatabaseManager {
    /**
     * Fetches a player's data from the database
     * @param username The username of the player to fetch
     * @param listener The listener to receive the results.
     */
    public void getPlayer(String username, Listener<PlayerAccount> listener){
        Task<DocumentSnapshot> task = db
                .collection(Schema.COLLECTION_PLAYER_ACCOUNT)
                .document(username)
                .get();
        retrieveResultByTask(task, listener, new PlayerAccountRetriever());
    }

    /**
     * Checks the database for username availability
     * @param username The username to check
     * @param listener Calls listener with "true" result if the name is taken, otherwise false.
     */
    public void checkUserExists(String username, OnManagerResult<Boolean> listener) {
        db.collection(Schema.COLLECTION_PLAYER_ACCOUNT)
                .document(username)
                .get()
                .addOnCompleteListener(task->{
                    if (!task.isSuccessful()){
                        listener.onResult(new Result<>(
                                new DbError("Database task failed", task)
                        ));
                        return;
                    }

                    assert task.getResult() != null;
                    listener.onResult(new Result<>(task.getResult().exists()));
                });
    }

    /**
     * Adds a PlayerAccount to the database.
     * The username <b>must</b> be unique or the operation will fail.
     * @param player The PlayerAccount to add
     * @param listener The listener to return the success/failure result
     */
    public void addPlayer(PlayerAccount player, Listener<Void> listener){
        Task<Void> task = db.runTransaction(transaction -> {
            final CollectionReference playersRef = db.collection(Schema.COLLECTION_PLAYER_ACCOUNT);
            final DocumentReference playerRef = playersRef.document(player.getUsername());
            // Prevent overwrites
            if (transaction.get(playerRef).exists()) {
                return null;
            }
            // Insert the player
            transaction.set(playerRef, player.toHashMap());
            return null;
        });
        retrieveResultByTask(task, listener, new VoidResultRetriever());
    }

    /**
     * Sets the deleted status for a player.
     * This can also be used to "undelete" a player, restoring them to view.
     * @param username The username of the player to affect
     * @param deleted The state of deleted to switch to (true being deleted)
     */
    public void setDeletedPlayer(String username, boolean deleted, Listener<Void> listener){
        Task<Void> task = db.runTransaction(transaction -> {
            final CollectionReference playersRef = db.collection(Schema.COLLECTION_PLAYER_ACCOUNT);
            final DocumentReference playerRef = playersRef.document(username);
            if (!transaction.get(playerRef).exists()) {
                return null;
            }
            // Set deleted to true
            transaction.update(playerRef, Schema.PLAYER_IS_DELETED, deleted);
            return null;
        });
        retrieveResultByTask(task, listener, new VoidResultRetriever());
    }

    /**
     * Updates the database copy of a player with the passed PlayerAccount data.
     * The username <b>must</b> exist, or it will result in a no op.
     * @param player The player data to update with
     */
    public void updatePlayer(PlayerAccount player, Listener<Void> listener){
        Task<Void> task = db.runTransaction(transaction -> {
            final CollectionReference playersRef = db.collection(Schema.COLLECTION_PLAYER_ACCOUNT);
            final DocumentReference playerRef = playersRef.document(player.getUsername());
            // Prevent record creation
            if (!transaction.get(playerRef).exists()) {
                return null;
            }
            // Update the player
            transaction.set(playerRef, player.toHashMap());
            return null;
        });
        retrieveResultByTask(task, listener, new VoidResultRetriever());
    }

    /**
     * Tests if a device is authorized to sign into a player's account.
     * @param deviceId The device id to sign with
     * @param username The username of the player to sign into
     * @param listener The listener to return the result to
     */
    public void validatePlayerSession(String deviceId, String username,
                                      OnManagerResult<Boolean> listener) {

        db.collection(Schema.COLLECTION_AUTH)
                .document(Schema.getAuthDocumentName(username, deviceId))
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null){
                        listener.onResult( new Result<>(
                                new DbError("Database task failed", task)
                        ));
                        return;
                    }

                    listener.onResult(new Result<>(task.getResult().exists()));
                });
    }

    /**
     * Authenticates a new device to log in to a player.
     * @param deviceId The device to authenticate
     * @param username The player to authenticate for
     */
    public void createPlayerSession(String deviceId, String username, Listener<Void> listener){
        HashMap<String, Object> sessionMap = new HashMap<>();
        sessionMap.put(Schema.AUTH_DEVICE_ID, deviceId);
        sessionMap.put(Schema.AUTH_PLAYER, username);
        sessionMap.put(Schema.AUTH_IS_PRIMARY_ACCOUNT, true);

        Task<Void> task = db.collection(Schema.COLLECTION_AUTH)
                .document(Schema.getAuthDocumentName(username, deviceId))
                .set(sessionMap);
        retrieveResultByTask(task, listener, new VoidResultRetriever());
    }
}
