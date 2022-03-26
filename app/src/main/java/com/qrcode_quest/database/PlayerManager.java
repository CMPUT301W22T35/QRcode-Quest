package com.qrcode_quest.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.qrcode_quest.database.ManagerResult.*;
import com.qrcode_quest.entities.PlayerAccount;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Acts as an interface between the app and the database for queries concerning PlayerAccounts.
 * All results are returned to the caller through the ManagerResult.Listener interface, wrapped
 * in a Result.
 * @see com.qrcode_quest.entities.PlayerAccount
 * @see ManagerResult.Listener
 * @see Result
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerManager extends DatabaseManager {
    public PlayerManager(FirebaseFirestore db) {
        super(db);
    }

    /**
     * Fetches a player's data from the database
     * @param username The username of the player to fetch
     */
    public void getPlayer(String username, Listener<PlayerAccount> listener){
        Task<DocumentSnapshot> task = db
                .collection(Schema.COLLECTION_PLAYER_ACCOUNT)
                .document(Schema.getPlayerAccountDocumentName(username))
                .get();
        retrieveResultByTask(task, listener, new PlayerAccountRetriever());
    }

    /**
     * Checks the database for username availability
     * @param username The username to check
     */
    public void checkUserExists(String username, Listener<Boolean> listener) {
        db.collection(Schema.COLLECTION_PLAYER_ACCOUNT)
                .document(Schema.getPlayerAccountDocumentName(username))
                .get()
                .addOnCompleteListener(task->{
                    if (!task.isSuccessful()){
                        listener.onResult(new Result<>(
                                new DbError("Database task failed", task)
                        ));
                        return;
                    }
                    if (task.getResult() == null) {  // document does not exist
                        listener.onResult(new Result<>(Boolean.FALSE));
                    } else {
                        listener.onResult(new Result<>(task.getResult().exists()));
                    }
                });
    }

    /**
     * Adds a PlayerAccount to the database.
     * The username <b>must</b> be unique or the operation will fail.
     * @param player The PlayerAccount to add
     */
    public void addPlayer(PlayerAccount player, Listener<Void> listener){
        Task<Void> task = db.runTransaction(transaction -> {
            final CollectionReference playersRef = db.collection(Schema.COLLECTION_PLAYER_ACCOUNT);
            final DocumentReference playerRef = playersRef.document(
                    Schema.getPlayerAccountDocumentName(player.getUsername()));
            // Prevent overwrites
            if (transaction.get(playerRef).exists()) {
                throw new FirebaseFirestoreException(
                        "Player with same name already exists!", FirebaseFirestoreException.Code.ABORTED);
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
            final DocumentReference playerRef = playersRef.document(
                    Schema.getPlayerAccountDocumentName(username));
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
            final DocumentReference playerRef = playersRef.document(
                    Schema.getPlayerAccountDocumentName(player.getUsername()));

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
     * Fetches all the undeleted players from the database
     */
    public void getPlayerList(Listener<ArrayList<PlayerAccount>> listener){
        Task<QuerySnapshot> task = db.collection(Schema.COLLECTION_PLAYER_ACCOUNT)
                .whereEqualTo(Schema.PLAYER_IS_DELETED, false)
                .get();
        retrieveResultByTask(task, listener, new PlayerListRetriever());
    }

    /**
     * Tests if a device is authorized to sign into a player's account.
     * @param deviceId The device id to sign with
     * @param username The username of the player to sign into
     */
    public void validatePlayerSession(String deviceId, String username,
                                      Listener<Boolean> listener) {

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
