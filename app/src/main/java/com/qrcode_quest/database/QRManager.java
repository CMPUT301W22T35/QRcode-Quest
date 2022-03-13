package com.qrcode_quest.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Interfaces to query and update QRShot objects in the Firestore database
 * @author tianming, jdumouch (barely ;P)
 * @version 1.0
 * @see com.qrcode_quest.entities.QRCode
 * @see com.qrcode_quest.entities.QRShot
 */
public class QRManager extends DatabaseManager {
    final static long MAX_FILE_SIZE = 16 * 1024;  // 16KB = 128Kb

    public static class PhotoEncoding {
        public byte[] encodeToBytes(Bitmap photo) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }

        public Bitmap decodeFromBytes(byte[] bytes) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return BitmapFactory.decodeStream(bais);
        }
    }

    FirebaseStorage firebaseStorage;  // for uploading the photos
    PhotoEncoding encoding;
    public QRManager() {
        super();
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.encoding = new PhotoEncoding();
    }
    public QRManager(FirebaseFirestore db) {
        super(db);
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.encoding = new PhotoEncoding();
    }
    public QRManager(FirebaseFirestore db, FirebaseStorage firebaseStorage, PhotoEncoding encoding) {
        super(db);
        this.firebaseStorage = firebaseStorage;
        this.encoding = encoding;
    }

    public void retrieveQRShotsWithPhotos(Task<QuerySnapshot> task, Listener<ArrayList<QRShot>> listener) {
        retrieveResultByTask(task, result -> {
            // we have a list of documents but is missing the photos
            // look for ones with photos
            List<DocumentSnapshot> snapshots = result.unwrap();
            final int[] numPhotosRemaining = {0};
            final boolean[] hasExecutedListener = {false};

            ArrayList<QRShot> shots = new ArrayList<>();
            HashMap<String, QRShot> photoPathToShot = new HashMap<>();
            for (DocumentSnapshot snapshot: snapshots) {
                Result<QRShot> shotResult = ManagerResult.getQRShotFromDocumentSnapshot(snapshot);
                if(!shotResult.isSuccess()) {
                    listener.onResult(new Result<>(shotResult.getError()));
                    return;
                }
                QRShot shot = shotResult.unwrap();
                shots.add(shot);
                if(snapshot.getString(Schema.QRSHOT_PHOTO_REF) != null) {
                    numPhotosRemaining[0] += 1;
                    photoPathToShot.put(Schema.getPhotoPathOnCloudStorage(shot.getCodeHash(),
                            shot.getOwnerName()), shot);
                }
            }

            // Return the results without waiting for photos, if possible
            if (numPhotosRemaining[0] == 0){
                listener.onResult(new Result<>(shots));
            }

            // open all download tasks at once
            for (String path: photoPathToShot.keySet()) {
                StorageReference photoRef = firebaseStorage.getReference(path);
                photoRef.getBytes(MAX_FILE_SIZE).addOnCompleteListener(taskLoadPhoto -> {
                    // first we want to make sure this function is executed no more than #photos times
                    // then the listener has not been executed (as the point of loading photos is to
                    // give the loading results to the listener)
                    numPhotosRemaining[0] -= 1;
                    assert numPhotosRemaining[0] >= 0;
                    if (hasExecutedListener[0])
                        return;  // do nothing

                    if (!taskLoadPhoto.isSuccessful()) {
                        Exception e = taskLoadPhoto.getException();
                        assert e != null;
                        listener.onResult(new Result<>(new DbError(
                                "Exception downloading photos: " + e.getLocalizedMessage(), path)));
                        hasExecutedListener[0] = true;
                    }
                    byte[] photoBytes = taskLoadPhoto.getResult();
                    if (photoBytes != null) {
                        Bitmap reconstructedPhoto = encoding.decodeFromBytes(photoBytes);
                        Objects.requireNonNull(photoPathToShot.get(path)).setPhoto(reconstructedPhoto);
                    }

                    if (numPhotosRemaining[0] == 0) {
                        // all photos have been loaded
                        listener.onResult(new Result<>(shots));
                    }
                });
            }
        }, querySnapshot -> {
            assert querySnapshot != null;
            return new Result<>(querySnapshot.getDocuments());
        });
    }

    /**
     * Get all qr shot rows in the database
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getAllQRShots(Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT).get();
        retrieveQRShotsWithPhotos(task, listener);
    }

    /**
     * Get qr shots belong to the qr code corresponding to the specified hash code
     * @param qrHash the hash of the qr code
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getCodeShots(String qrHash, Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_QRHASH, qrHash).get();
        retrieveQRShotsWithPhotos(task, listener);
    }

    /**
     * Get qr shots belong to the specified player
     * @param playerName name of the player
     * @param listener handles the returned list of QRShot objects on complete
     */
    public void getPlayerShots(String playerName, Listener<ArrayList<QRShot>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT)
                .whereEqualTo(Schema.QRSHOT_PLAYER_NAME, playerName).get();
        retrieveQRShotsWithPhotos(task, listener);
    }

    /**
     * Get all qr codes in the database
     * @param listener handles the returned list of QRCode objects on complete
     */
    public void getAllQRCodes(Listener<ArrayList<QRCode>> listener) {
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_QRSHOT).get();
        retrieveResultByTask(task, listener, new ManagerResult.QRCodeListRetriever());
    }

    /**
     * Gets all qr codes in the database as a map, with the key being the hash of the code.
     * @see QRManager#getAllQRCodes(Listener)
     */
    public void getAllQRCodesAsMap(Listener<HashMap<String, QRCode>> listener){
        getAllQRCodes(result -> {
            if (!result.isSuccess()) {
                listener.onResult(new Result<>(result.getError()));
            } else {
                ArrayList<QRCode> codes = result.unwrap();
                HashMap<String, QRCode> map = new HashMap<>();
                for (QRCode code: codes) {
                    map.put(code.getHashCode(), code);
                }
                listener.onResult(new Result<>(map));
            }
        });
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
                if (size != 0) {
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
                String path = Schema.getPhotoPathOnCloudStorage(
                        shot.getCodeHash(), shot.getOwnerName());
                if (photo != null) {
                    map.put(Schema.QRSHOT_PHOTO_REF, path);
                }
                transaction.set(shotDocRef, map);

                if (photo != null) {
                    // transaction is basically completed, we upload the photo if applicable
                    // TODO: move this to a wrapper on onCompleteListener to guarantee execute upload after transaction complete
                    // see: https://firebase.google.com/docs/storage/android/upload-files
                    StorageReference photoRef = firebaseStorage.getReference(path);
                    UploadTask uploadTask = photoRef.putBytes(encoding.encodeToBytes(photo));
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

                Task<Void> task = QRManager.this.getDb().runTransaction(transaction -> {
                    for (QRShot shot : shots) {
                        String docName = Schema.getQRShotDocumentName(shot.getCodeHash(), shot.getOwnerName());
                        DocumentReference docRef = collectionRef.document(docName);
                        transaction.delete(docRef);
                    }

                    // just pray that this is successfully executed so re-adding photos will not cause issues
                    for (QRShot shot : shots) {
                        if (shot.getPhoto() != null) {
                            String photoPath = Schema.getPhotoPathOnCloudStorage(shot.getCodeHash(), shot.getOwnerName());
                            StorageReference ref = firebaseStorage.getReference(photoPath);
                            ref.delete().addOnCompleteListener(task1 -> { });  // addCompleteListener to make unit tests work
                        }
                    }
                    return null;
                });
                QRManager.this.retrieveResultByTask(task, listener, new ManagerResult.VoidResultRetriever());
            }
        });
    }
}
