package com.qrcode_quest.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Contains references to storage and an encode algorithm for the photos
 * Used to handle resources needed for direct upload/download of photos from Firebase storage
 *
 * @author tianming
 * @version 1.0
 */
public class PhotoStorage {
    FirebaseStorage storage;
    PhotoEncoding encoding;

    /**
     * @param storage the storage to access
     * @param encoding the encoding algorithm used to encode/decode photos
     */
    public PhotoStorage(FirebaseStorage storage, PhotoEncoding encoding) {
        assert storage != null;
        assert encoding != null;
        this.storage = storage;
        this.encoding = encoding;
    }

    /** @return storage object */
    public FirebaseStorage getStorage() { return storage; }

    /**
     * encode a photo into byte array
     * @param photo the bitmap to encode
     * @return a byte array after applying the encoding algorithm
     */
    public byte[] encodeToBytes(Bitmap photo) { return encoding.encodeToBytes(photo); }

    /**
     * decode a photo from byte array
     * @param bytes a byte array of encoded photo
     * @return a decoded photo Bitmap object
     */
    public Bitmap decodeFromBytes(byte[] bytes) { return encoding.decodeFromBytes(bytes); }

    /**
     * represents an encoding algorithm used for photo upload/download encoding
     */
    public static class PhotoEncoding {
        /**
         * encode a photo into byte array
         * @param photo the bitmap to encode
         * @return a byte array after applying the encoding algorithm
         */
        public byte[] encodeToBytes(Bitmap photo) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }

        /**
         * decode a photo from byte array
         * @param bytes a byte array of encoded photo
         * @return a decoded photo Bitmap object
         */
        public Bitmap decodeFromBytes(byte[] bytes) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return BitmapFactory.decodeStream(bais);
        }
    }
}
