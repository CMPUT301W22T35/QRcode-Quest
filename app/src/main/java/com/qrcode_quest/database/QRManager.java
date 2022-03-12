package com.qrcode_quest.database;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Contains methods for updating and querying information about the QR codes and QR shots
 * @author tianming
 * @version 1.0
 * @see com.qrcode_quest.entities.QRCode
 * @see com.qrcode_quest.entities.QRShot
 */
public class QRManager extends DatabaseManager {
    public QRManager(FirebaseFirestore db) {
        super(db);
    }


}
