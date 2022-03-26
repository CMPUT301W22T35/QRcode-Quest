package com.qrcode_quest.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.qrcode_quest.MockDb;
import com.qrcode_quest.entities.Geolocation;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.MockFirebaseStorage;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class QRManagerTest {
    static final String QR_HASH1 = "af";
    static final String QR_HASH2 = "ad";

    private HashMap<String, byte[]> pathToPhotos;
    private FirebaseFirestore db;
    private PhotoStorage storage;
    private QRManager manager;

    public ArrayList<QRShot> createMockQRShots(int size, String qrHash) {
        ArrayList<QRShot> shots = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            QRShot shot = mock(QRShot.class);
            String indexStr = Integer.toString(i);
            String ownerName = indexStr;
            String bitmapMessage = "bitmap" + indexStr;
            Bitmap bitmap = MockFirebaseStorage.createMockBitmap(bitmapMessage.getBytes(StandardCharsets.UTF_8));
            when(shot.getName()).thenReturn("placeholder");
            when(shot.getCodeHash()).thenReturn(qrHash);
            when(shot.getLocation()).thenReturn(new Geolocation(i, 1 + i));
            when(shot.getPhoto()).thenReturn(bitmap);
            when(shot.getOwnerName()).thenReturn(ownerName);
            shots.add(shot);
        }
        return shots;
    }

    @Before
    public void initManager() {
        // Note: the hashmap and the storage shares same data
        // modifying one will lead to changes in the other
        pathToPhotos = new HashMap<>();
        FirebaseStorage firebaseStorage = MockFirebaseStorage.createMockFirebaseStorage(pathToPhotos);

        db = MockDb.createMockDatabase(new HashMap<>());
        storage = MockFirebaseStorage.createMockPhotoStorage(firebaseStorage);  // avoid creating real bitmaps
        manager = new QRManager(db, storage);
    }

    @Test
    public void testAddQRShot() {
        ArrayList<QRShot> shots = createMockQRShots(4, "13af");

        final boolean[] testFlags = {false, false, false, false};
        manager.createQRShot(shots.get(0),
                result -> testFlags[0] = result.isSuccess(),
                result -> testFlags[1] = result.isSuccess());
        assertTrue(testFlags[0]);
        assertTrue(testFlags[1]);
        manager.getAllQRShots(result -> {
            testFlags[2] = true;
            ArrayList<QRShot> shots1 = result.unwrap();
            assertEquals(1, shots1.size());
            QRShot shot = shots1.get(0);
            assertEquals("13af", shot.getCodeHash());
            assertEquals("0", shot.getOwnerName());
            assertEquals("bitmap0",
                    new String(storage.encodeToBytes(shot.getPhoto()), StandardCharsets.UTF_8));
            Geolocation location = shot.getLocation();
            assertNotNull(location);
            assertEquals(0, location.getLatitude(), 0);
            assertEquals(1, location.getLongitude(), 0);
        });
        assertTrue(testFlags[2]);

        // add a few more and retrieve together
        for(int i = 1; i < 4; i++) {
            manager.createQRShot(shots.get(i),
                    result -> assertTrue(result.isSuccess()),
                    result -> assertTrue(result.isSuccess()));
        }
        manager.getAllQRShots(result -> {
            testFlags[3] = true;
            ArrayList<QRShot> shots1 = result.unwrap();
            assertEquals(4, shots1.size());

            HashMap<String, QRShot> shotMap = new HashMap<>();
            for (QRShot shot: shots1) {
                shotMap.put(shot.getOwnerName(), shot);
            }
            for (int i = 0; i < 4; i++) {
                String indexStr = Integer.toString(i);
                QRShot shot = shotMap.get(indexStr);
                assertNotNull(shot);
                assertEquals("13af", shot.getCodeHash());
                assertEquals(indexStr, shot.getOwnerName());
                assertEquals("bitmap" + indexStr,
                        new String(storage.encodeToBytes(shot.getPhoto()), StandardCharsets.UTF_8));
                Geolocation location = shot.getLocation();
                assertNotNull(location);
                assertEquals(i, location.getLatitude(), 0.01);
                assertEquals(1 + i, location.getLongitude(), 0.01);
            }
        });
        assertTrue(testFlags[3]);
    }

    @Test
    public void testGetters() {
        // get when empty returns an empty list
        manager.getCodeShots(QR_HASH1, result -> assertTrue(result.unwrap().isEmpty()));
        manager.getAllQRShots(result -> assertTrue(result.unwrap().isEmpty()));
        manager.getPlayerShots("0", result -> assertTrue(result.unwrap().isEmpty()));
        manager.getAllQRCodes(result -> assertTrue(result.unwrap().isEmpty()));
        manager.getQRCode(QR_HASH1, result -> assertNull(result.unwrap()));
        manager.getPlayerCodes("0", result -> assertTrue(result.unwrap().isEmpty()));
        manager.getAllQRCodesAsMap(result -> assertTrue(result.unwrap().isEmpty()));

        // insert 3 shots at two QR codes
        ArrayList<QRShot> shots1 = createMockQRShots(1, QR_HASH1);
        ArrayList<QRShot> shots2 = createMockQRShots(2, QR_HASH2);
        manager.createQRShot(shots1.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(1),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));

        // test all methods again on expected sizes
        manager.getCodeShots(QR_HASH1, result -> assertEquals(1, result.unwrap().size()));
        manager.getAllQRShots(result -> assertEquals(3, result.unwrap().size()));
        manager.getPlayerShots("0", result -> assertEquals(2, result.unwrap().size()));
        manager.getAllQRCodes(result -> assertEquals(2, result.unwrap().size()));
        manager.getQRCode(QR_HASH1, result -> assertNotNull(result.unwrap()));
        manager.getPlayerCodes("0", result -> assertEquals(2, result.unwrap().size()));
        manager.getAllQRCodesAsMap(result -> assertEquals(2, result.unwrap().size()));
    }

    @Test
    public void testDeleteCode() {
        // insert 3 shots at two QR codes
        ArrayList<QRShot> shots1 = createMockQRShots(1, QR_HASH1);
        ArrayList<QRShot> shots2 = createMockQRShots(2, QR_HASH2);
        manager.createQRShot(shots1.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(1),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));

        // test remove the first code
        manager.removeQRCode(QR_HASH1, result -> assertTrue(result.isSuccess()));
        manager.getCodeShots(QR_HASH1, result -> assertEquals(0, result.unwrap().size()));
        manager.getCodeShots(QR_HASH2, result -> assertEquals(2, result.unwrap().size()));

        // remove the second code so collection is empty again
        manager.removeQRCode(QR_HASH2, result -> assertTrue(result.isSuccess()));
        manager.getCodeShots(QR_HASH1, result -> assertEquals(0, result.unwrap().size()));
        manager.getCodeShots(QR_HASH2, result -> assertEquals(0, result.unwrap().size()));

        // add them back
        manager.createQRShot(shots1.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(0),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.createQRShot(shots2.get(1),
                result -> assertTrue(result.isSuccess()),
                result -> assertTrue(result.isSuccess()));
        manager.getCodeShots(QR_HASH1, result -> assertEquals(1, result.unwrap().size()));
        manager.getCodeShots(QR_HASH2, result -> assertEquals(2, result.unwrap().size()));
    }
}
