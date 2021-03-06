package com.qrcode_quest.database;

/**
 * Provides a central set of constants that represent the database schema
 * Note the textual description of the schema is at the end of the file, which can be looked up
 * for the data types and reference types of each field
 *
 * It is beneficial to use these constants where possible as changes in database schema will
 * be refactored across the project.
 * Breaking changes here will result in compilation errors, instead of runtime errors.
 * @author jdumouch, tianming
 * @version 1.0
 */
public class Schema {
    public static final String COLLECTION_PLAYER_ACCOUNT = "PlayerAccount";
    /**
     * get the Firestore document name of player of the given player name
     * @param playerName player's name
     * @return document's name
     */
    public static String getPlayerAccountDocumentName(String playerName) {
        return playerName;
    }
    public static final String PLAYER_NAME = "playerName";
    public static final String PLAYER_EMAIL = "email";
    public static final String PLAYER_PHONE = "phone";
    public static final String PLAYER_IS_DELETED = "isDeleted";
    public static final String PLAYER_IS_OWNER = "isOwner";

    public static final String COLLECTION_QRSHOT = "QRShot";
    /**
     * get the Firestore document name of QRShot
     * @param qrHash qr hash of the QRShot object
     * @param playerName player'name
     * @return document's name
     */
    public static String getQRShotDocumentName(String qrHash, String playerName) {
        return qrHash + "_" + playerName;
    }
    public static final String QRSHOT_QRHASH = "qrhash";
    public static final String QRSHOT_PLAYER_NAME = "playerName";
    public static final String QRSHOT_SCORE = "score";
    /**
     * get the photo path on the Firebase storage
     * @param qrHash qr hash of the corresponding qr code
     * @param playerName player that took the shot
     * @return path to the photo
     */
    public static String getPhotoPathOnCloudStorage(String qrHash, String playerName) {
        return "images/" + qrHash + "_" + playerName;
    }
    public static final String QRSHOT_PHOTO_REF = "photoRef";
    public static final String QRSHOT_LONGITUDE = "longitude";
    public static final String QRSHOT_LATITUDE = "latitude";

    public static final String COLLECTION_COMMENT = "Comment";
    /**
     * get the Firestore document name of
     * @param qrHash hash of the qr code that contains the comment
     * @param commentNumber name of the comment
     * @return document's name
     */
    public static String getCommentDocumentName(String qrHash, int commentNumber) {
        return qrHash + "_" + Integer.toString(commentNumber);
    }
    public static final String COMMENT_QRHASH = "qrhash";
    public static final String COMMENT_POSITION = "commentNumber";
    public static final String COMMENT_PLAYER_NAME = "playerName";
    public static final String COMMENT_TEXT = "text";
    // workaround because there is no collection level locking
    /**
     * get the Firestore document name of
     * @param qrHash hash of qr code that corresponding to the meta document
     * @return document's name
     */
    public static String getCommentMetaDocumentName(String qrHash) {
        return "commentMeta_" + qrHash;
    }
    public static final String COMMENT_META_COUNT = "commentNum";

    public static final String COLLECTION_AUTH = "PlayerDevice";
    /**
     * get the Firestore document name of the PlayerDevice relation
     * @param playerName player's name
     * @param deviceUID string id of the device
     * @return document's name
     */
    public static String getAuthDocumentName(String playerName, String deviceUID) {
        return deviceUID + "_" + playerName;
    }
    public static final String AUTH_PLAYER = "playerName";
    public static final String AUTH_DEVICE_ID = "deviceId";
    public static final String AUTH_IS_PRIMARY_ACCOUNT = "isPrimaryAccount";
}

/*
  The fields marked with * are used as or as part of the name of the document, similar to a primary
  key in SQL. The fields marked with - are regular attributes. The fields start with \ are foreign
  key references.

  PlayerAccount
  *playerName: a string, a unique id that is also the displayed name of the player
  -email: a string, email of the player
  -phone: a string, phone number of the player
  -isDeleted: a boolean, true if the account has been deleted by the owner
  -isOwner: a boolean

  QRShot
  *qrhash: a string, identifies the QR code, the hash in hexadecimal format
  \*playerName: a string, the player that takes photo of the QR code
  -score: an integer, the score of the QR code
  -photoRef: a reference object, to the image object which represents the photo taken by the
  player (after compression <64kb)
  -longitude: a floating point number, nullable
  -latitude: a floating point number, nullable

  Comment
  \*qrhash: a string, hash code in hexadecimal format
  *commentNumber: an integer, used to order the comment in the comment section; position of the
  comment in the comment section
  \-playerName: a string, the commentor's name
  -text: a string, the comment body

  PlayerDevice (used for authentication; only registered devices can log in to an account)
  \*playerName: a string
  *deviceId: a string, identifies the device the player account is allowed on
  -isPrimaryAccount: a boolean, whether the account is created on the device
 */