package com.qrcode_quest.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.MockDb;

import org.junit.Test;

import java.util.HashMap;
import java.util.Objects;

public class DatabaseManagerTest {
    @Test
    public void testRetrieveResultByTask() {
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        DatabaseManager dbManager = new DatabaseManager(db);

        HashMap<String, Object> map = new HashMap<>();
        map.put("testNull", null);
        map.put("testString", new String("hello world"));
        map.put("testLong", 3);  // int, but will be converted to Long on database and back to int on retrieval
        Task<Void> setTask =
                db.collection("testCol")
                        .document("testDoc").set(map);
        assertNotNull(setTask);
        final Boolean[] testFlags = {false, false, false};
        dbManager.retrieveResultByTask(setTask, new ManagerResult.Listener<Void>() {
            @Override
            public void onResult(Result<Void> result) {
                testFlags[0] = true;
                if (result.isSuccess())
                    testFlags[1] = true;
            }
        }, new ManagerResult.VoidResultRetriever());

        // mock class should have method executed immediately after listener is attached
        assertTrue(testFlags[0]);
        assertTrue(testFlags[1]);

        // make sure the document is correctly saved
        Task<DocumentSnapshot> getTask =
                db.collection("testCol").document("testDoc").get();
        dbManager.retrieveResultByTask(getTask, new ManagerResult.Listener<Void>() {
            @Override
            public void onResult(Result<Void> result) {
                // directly assert because we don't worry about callback not executed now
                assertTrue(result.isSuccess());
            }
        }, new ManagerResult.Retriever<Void, DocumentSnapshot>() {
            @Override
            public Result<Void> retrieveResultFrom(DocumentSnapshot document) {
                testFlags[2] = (document.get("testNull") == null);
                assertEquals("hello world", document.get("testString"));
                assertEquals(3,
                        Objects.requireNonNull(document.getLong("testLong")).intValue());
                assertNull(document.get("thisKeyDoesNotExist"));
                return new Result<>((Void) null);
            }
        });
        assertTrue(testFlags[2]);
    }

    @Test
    public void testRetrieveObjectFromDocument() {
        // setup mock data
        HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent = new HashMap<>();
        HashMap<String, HashMap<String, Object>> colContent = new HashMap<>();
        HashMap<String, Object> docContent = new HashMap<>();
        dbContent.put("col", colContent);
        colContent.put("doc", docContent);
        docContent.put("testValue", 10);

        FirebaseFirestore db = MockDb.createMockDatabase(dbContent);
        DatabaseManager dbManager = new DatabaseManager(db);
        dbManager.retrieveObjectFromDocument("col", "doc",
                result -> assertEquals(10, result.unwrap().intValue()),
                document -> new Result<>(document.getLong("testValue")));
        // test with a document that does not exist
        dbManager.retrieveObjectFromDocument("col", "abcd",
                result -> {
                    assertFalse(result.isSuccess());  // should fail
                    assertEquals("document does not exist",
                            result.getError().getMessage());  // should return error message
                },
                new ManagerResult.Retriever<Object, DocumentSnapshot>() {
                    @Override
                    public Result<Object> retrieveResultFrom(DocumentSnapshot document) {
                        assertTrue(document == null || !document.exists());
                        return new Result<>(new DbError("document does not exist", "test"));
                    }
                });
    }
}
