package com.qrcode_quest.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PhotoStorage {
    FirebaseStorage storage;
    PhotoEncoding encoding;

    public PhotoStorage(FirebaseStorage storage, PhotoEncoding encoding) {
        assert storage != null;
        assert encoding != null;
        this.storage = storage;
        this.encoding = encoding;
    }

    public FirebaseStorage getStorage() { return storage; }
    public byte[] encodeToBytes(Bitmap photo) { return encoding.encodeToBytes(photo); }
    public Bitmap decodeFromBytes(byte[] bytes) { return encoding.decodeFromBytes(bytes); }

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
}
