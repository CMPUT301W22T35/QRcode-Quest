package com.qrcode_quest.database;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.qrcode_quest.database.ManagerResult.Listener;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interfaces to query and update QRShot objects in the Firestore database
 * @author tianming, jdumouch (barely ;P)
 * @version 1.0
 * @see com.qrcode_quest.entities.QRCode
 * @see com.qrcode_quest.entities.QRShot
 */
public class QRManager extends DatabaseManager {
    FirebaseStorage firebaseStorage;  // for uploading the photos
    public QRManager() {
        super();
        this.firebaseStorage = FirebaseStorage.getInstance();
    }
    public QRManager(FirebaseFirestore db) {
        super(db);
        this.firebaseStorage = FirebaseStorage.getInstance();
    }
    public QRManager(FirebaseFirestore db, FirebaseStorage firebaseStorage) {
        this(db);
        this.firebaseStorage = firebaseStorage;
    }

    /**
     * Get all qr shot rows in the database
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getAllQRShots(Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .get();
        retrieveResultByTask(task, listener, new ManagerResult.QRShotListRetriever());
    }

    /**
     * Get qr shots belong to the qr code corresponding to the specified hash code
     * @param qrHash the hash of the qr code
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getCodeShots(String qrHash, Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_QRHASH, qrHash).get();
        retrieveResultByTask(task, listener, new ManagerResult.QRShotListRetriever());
    }

    /**
     * Get qr shots belong to the specified player
     * @param playerName name of the player
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getPlayerShots(String playerName, Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_PLAYER_NAME, playerName).get();
        retrieveResultByTask(task, listener, new ManagerResult.QRShotListRetriever());
    }

    /**
     * Get all qr codes in the database
     * @param listener handles the returned list of QRCode objects on complete
     */
    public void getAllQRCodes(Listener<ArrayList<QRCode>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .get();
        retrieveResultByTask(task, listener, new ManagerResult.QRCodeListRetriever());
    }

    /**
     * Gets all qr codes in the database as a map, with the key being the hash of the code.
     * @see QRManager#getAllQRCodes(Listener)
     */
    public void getAllQRCodesAsMap(Listener<HashMap<String, QRCode>> listener){
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .get();
        retrieveResultByTask(task, listener, new ManagerResult.QRCodeMapRetriever());
    }

    /**
     * Get qr codes that has the specified hash code
     * @param qrHash the hash of the qr code
     * @param listener handles the returned list of QRCode objects on complete
     */
    public void getQRCode(String qrHash, Listener<QRCode> listener) {
        // use the retrieve list function to get a list of zero or one QRCode object
        // then just return the only object (if it exists)

        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_QRHASH, qrHash).get();
        retrieveResultByTask(task, listener, new ManagerResult.Retriever<QRCode, QuerySnapshot>() {
            @Override
            public Result<QRCode> retrieveResultFrom(QuerySnapshot document) {
                Result<ArrayList<QRCode>> result =
                        new ManagerResult.QRCodeListRetriever().retrieveResultFrom(document);
                if (!result.isSuccess())
                    return new Result<>(result.getError());
                // the result set should either has 0 or 1 element
                int size = result.unwrap().size();
                assert size <= 1;
                if (size == 0) {
                    return new Result<>(result.unwrap().get(0));
                }
                return new Result<>((QRCode) null);
            }
        });
    }

    /**
     * Get qr codes belong to the specified player
     * @param playerName name of the player
     * @param listener handles the returned list of QRCode objects on complete
     */
    public void getPlayerCodes(String playerName, Listener<ArrayList<QRCode>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_PLAYER_NAME, playerName).get();
        retrieveResultByTask(task, listener, new ManagerResult.QRCodeListRetriever());
    }

    /**
     * insert a QRShot row into the database
     * @param shot the object to insert
     * @param onCompleteListener listener on QRShot insert completed
     * @param onImageUploadListener listener on image upload
     */
    public void createQRShot(
            QRShot shot, Listener<Void> onCompleteListener,
            Listener<Void> onImageUploadListener) {
        final CollectionReference collectionRef = getDb().collection(Schema.COLLECTION_QRSHOT);

        Task<Void> task = getDb().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference shotDocRef = collectionRef.document(
                        Schema.getQRShotDocumentName(shot.getCodeHash(), shot.getOwnerName()));
                DocumentSnapshot shotDoc = transaction.get(shotDocRef);
                if (shotDoc.exists())
                    throw new FirebaseFirestoreException("QRShot at position already exists!",
                            FirebaseFirestoreException.Code.ABORTED);

                // insert the QRShot
                HashMap<String, Object> map = new HashMap<>();
                map.put(Schema.QRSHOT_QRHASH, shot.getCodeHash());
                map.put(Schema.QRSHOT_PLAYER_NAME, shot.getOwnerName());
                map.put(Schema.QRSHOT_SCORE, RawQRCode.getScoreFromHash(shot.getCodeHash()));
                Geolocation location = shot.getLocation();
                if (location != null) {
                    map.put(Schema.QRSHOT_LATITUDE, location.getLatitude());
                    map.put(Schema.QRSHOT_LONGITUDE, location.getLongitude());
                }

                // deal with photo attribute
                Bitmap photo = shot.getPhoto();
                if (photo != null) {
                    String path = Schema.getPhotoPathOnCloudStorage(
                            shot.getCodeHash(), shot.getOwnerName());
                    map.put(Schema.QRSHOT_PHOTO_REF, path);
                }
                transaction.set(shotDocRef, map);

                if (photo != null) {
                    // transaction is basically completed, we upload the photo if applicable
                    // TODO: move this to a wrapper on onCompleteListener to guarantee execute upload after transaction complete
                    // see: https://firebase.google.com/docs/storage/android/upload-files
                    StorageReference photoRef = firebaseStorage.getReference();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    UploadTask uploadTask = photoRef.putBytes(baos.toByteArray());
                    retrieveResultByTask(uploadTask, onImageUploadListener, new ManagerResult.TaskSnapshotRetriever());
                }

                return null;
            }
        });
        retrieveResultByTask(task, onCompleteListener, new ManagerResult.VoidResultRetriever());
    }

    public void removeQRCode(String qrHash, Listener<Void> listener) {
        final CollectionReference collectionRef = getDb().collection(Schema.COLLECTION_QRSHOT);

        getCodeShots(qrHash, new Listener<ArrayList<QRShot>>() {
            @Override
            public void onResult(Result<ArrayList<QRShot>> result) {
                if (!result.isSuccess())
                    listener.onResult(new Result<>(result.getError()));
                ArrayList<QRShot> shots = result.unwrap();  // all the shots to delete

                Task<Void> task = getDb().runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        for(QRShot shot: shots) {
                            String docName = Schema.getQRShotDocumentName(shot.getCodeHash(), shot.getOwnerName());
                            DocumentReference docRef = collectionRef.document(docName);
                            transaction.delete(docRef);
                        }
                        return null;
                    }
                });
                retrieveResultByTask(task, listener, new ManagerResult.VoidResultRetriever());
            }
        });
    }
}
