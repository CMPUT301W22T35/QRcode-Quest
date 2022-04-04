package com.qrcode_quest;

import android.content.SharedPreferences;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.database.CommentManager;
import com.qrcode_quest.database.ManagerResult;
import com.qrcode_quest.database.PhotoStorage;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.QRManager;
import com.qrcode_quest.database.Result;
import com.qrcode_quest.database.Schema;
import com.qrcode_quest.entities.Comment;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRShot;

import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

/**
 * creates mock instances of Firebase db and storage
 *
 * @author tianming, jdumouch
 * @version 1.1
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
    public static FirebaseFirestore createSinglePlayerDb(PlayerAccount account,
                                                         String correspondingDeviceID) {
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        new PlayerManager(db).addPlayer(account, result -> { });
        new PlayerManager(db).createPlayerSession(correspondingDeviceID, account.getUsername(), result -> { });
        return db;
    }

    /**
     * Populates a mock database using a set of data.
     */
    public static MockStorageBundle createPopulatedDb(List<PlayerAccount> players,
                                                      List<QRShot> shots,
                                                      List<Comment> comments,
                                                      PlayerAccount localPlayer,
                                                      String deviceID) {

        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        FirebaseStorage storage = MockFirebaseStorage.createMockFirebaseStorage(new HashMap<>());
        PhotoStorage photoStorage = MockFirebaseStorage.createMockPhotoStorage(storage);

        // Add players
        PlayerManager pm = new PlayerManager(db);
        for (PlayerAccount player : players) {
            pm.addPlayer(player, r->{});
        }
        pm.addPlayer(localPlayer, r->{});
        pm.createPlayerSession(deviceID, localPlayer.getUsername(), r->{});

        // Add QRShots (and by extension, QRCodes)
        if (shots != null){
            QRManager qm = new QRManager(db, photoStorage);
            for (QRShot shot : shots){
                qm.createQRShot(shot, r->{}, r->{});
            }
        }

        // Add comments
        if (comments != null){
            CommentManager cm = new CommentManager(db);
            for (Comment comment : comments) {
                cm.addComment(comment, r->{});
            }
        }

        // Return the populated storage bundle
        return new MockStorageBundle(db, photoStorage);
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

    /**
     * Create a mock preference of a pre-registered user.
     */
    public static SharedPreferences createRegisteredPreferences(String username,
                                                                String correspondingDeviceID){
        HashMap<String, String> prefMap = new HashMap<>();
        prefMap.put(Constants.AUTHED_USERNAME_PREF, username);
        prefMap.put(Constants.DEVICE_UID_PREF, correspondingDeviceID);

        return MockSharedPref.createMockSharedPref(prefMap);
    }
}
