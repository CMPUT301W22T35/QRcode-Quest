package com.qrcode_quest.database;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class MockDb {

    //-------------------------------------Snapshots---------------------------------------------
    /**
     * create a mock document snapshot that uses a hashmap as implementation
     * @param content hashmap representation of the key value pairs in the document
     * @return a mock document snapshot
     */
    static public DocumentSnapshot createMockDocumentSnapshot(
            HashMap<String, Object> content, boolean exists) {
        // make sure the content is independent from the actual mock database
        assert content != null;
        HashMap<String, Object> snapshot = new HashMap<>(content);

        DocumentSnapshot docSnapshot = mock(DocumentSnapshot.class);
        when(docSnapshot.getString(anyString())).thenAnswer((Answer<String>) invocation -> {
            String key = invocation.getArgument(0);
            return (String) snapshot.get(key);
        });
        when(docSnapshot.getLong(anyString())).thenAnswer((Answer<Long>) invocation -> {
            String key = invocation.getArgument(0);
            Object val = snapshot.get(key);
            if (val == null)
                return null;
            if (val.getClass() == Integer.class)
                return new Long((Integer) val);
            else
                return (Long) val;
        });
        when(docSnapshot.getBoolean(anyString())).thenAnswer((Answer<Boolean>) invocation -> {
            String key = invocation.getArgument(0);
            return (Boolean) snapshot.get(key);
        });
        when(docSnapshot.getDouble(anyString())).thenAnswer((Answer<Double>) invocation -> {
            String key = invocation.getArgument(0);
            return (Double) snapshot.get(key);
        });
        when(docSnapshot.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return snapshot.get(key);
        });
        when(docSnapshot.exists()).thenReturn(exists);

        return docSnapshot;
    }

    static public QuerySnapshot createQuerySnapshot(Collection<HashMap<String, Object>> resultDocuments) {
        final ArrayList<HashMap<String, Object>> documents = new ArrayList<>(resultDocuments);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);

        when(querySnapshot.size()).thenReturn(documents.size());
        when(querySnapshot.getDocuments()).thenAnswer(new Answer<ArrayList<DocumentSnapshot>>() {
            @Override
            public ArrayList<DocumentSnapshot> answer(InvocationOnMock invocation) throws Throwable {
                ArrayList<DocumentSnapshot> documentSnapshots = new ArrayList<>();
                for (HashMap<String, Object> map: documents) {
                    documentSnapshots.add(createMockDocumentSnapshot(map, true));
                }
                return new ArrayList<>(documentSnapshots);
            }
        });

        return querySnapshot;
    }

    //----------------------------------------Task-----------------------------------------------

    /**
     * To mock the behavior of a task; this represents a callback that does the same thing that a task
     * does
     * @param <TResult>
     */
    public interface MockTaskAction <TResult> {
        public void onMockTaskExecution(Task<TResult> task) throws FirebaseFirestoreException;
    }

    /**
     * create a task that executes an action when the first listener is attached
     * note by default task will not execute at all if no listeners are attached to it;
     * by default tasks return null on getResult(), meaning they need to be explicitly reset to
     * return correct values inside the action
     * @param action action to perform
     * @param <TResult> one that passed to listeners as Task<TResult> usually DocumentSnapshot or QuerySnapshot
     * @return a mock task
     */
    static public <TResult> Task<TResult> createMockTask(MockTaskAction<TResult> action) {
        final Boolean[] taskIsExecuted = {false};

        Task<TResult> task = mock(Task.class);
        when(task.isSuccessful()).thenAnswer((Answer<Boolean>) invocation -> taskIsExecuted[0]);
        when(task.isComplete()).thenAnswer((Answer<Boolean>) invocation -> taskIsExecuted[0]);
        when(task.getResult()).thenReturn(null);

        when(task.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer((Answer<Task<TResult>>) invocation -> {
            if (!taskIsExecuted[0]) {
                action.onMockTaskExecution(task);
                taskIsExecuted[0] = true;
            }
            ((OnCompleteListener<TResult>) invocation.getArgument(0))
                    .onComplete(task);
            return task;
        });

        when(task.addOnSuccessListener(any(OnSuccessListener.class))).then((Answer<Task<TResult>>) invocation -> {
            if (!taskIsExecuted[0]) {
                action.onMockTaskExecution(task);
                assert task.getResult() != null;
                taskIsExecuted[0] = true;
            }
            if (task.isSuccessful())
                ((OnSuccessListener<TResult>) invocation.getArgument(0))
                        .onSuccess(task.getResult());
            return task;
        });

        when(task.addOnFailureListener(any(OnFailureListener.class))).then((Answer<Task<TResult>>) invocation -> {
            if (!taskIsExecuted[0]) {
                action.onMockTaskExecution(task);
                assert task.getResult() != null;
                taskIsExecuted[0] = true;
            }
            if (!task.isSuccessful())
                ((OnFailureListener) invocation.getArgument(0))
                        .onFailure(Objects.requireNonNull(task.getException()));
            return task;
        });
        return task;
    }

    //------------------------------collection & document references----------------------------

    static private void ensureCollectionExist(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent, String collectionName) {
        if(!dbContent.containsKey(collectionName))
            dbContent.put(collectionName, new HashMap<>());
    }

    static private void ensureDocumentExist(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent, String collectionName,
            String documentName) {
        ensureCollectionExist(dbContent, collectionName);
        HashMap<String, HashMap<String, Object>> map = dbContent.get(collectionName);
        assert map != null;
        if(!map.containsKey(documentName))
            map.put(documentName, new HashMap<String, Object>());
    }

    /**
     * return the document snapshot if collection and document exists, otherwise return null
     * @param dbContent mock database to access
     * @param collectionName name of collection to access
     * @param documentName name of document to retrieve
     * @return the Result object that contains information about hash map content
     */
    static private HashMap<String, Object> safeRetrieveDocumentContent(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent,
            String collectionName, String documentName) {
        HashMap<String, Object> docContent = null;
        HashMap<String, HashMap<String, Object>> colContent = dbContent.get(collectionName);
        if (colContent != null)
            docContent = colContent.get(documentName);
        return docContent;
    }

    static public DocumentReference createMockDocumentReference(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent, CollectionReference colRef,
            String documentName) {
        DocumentReference docRef = mock(DocumentReference.class);
        String collectionName = colRef.getId();

        when(docRef.get()).thenAnswer((Answer<Task<DocumentSnapshot>>) invocation -> createMockTask(task -> {
            HashMap<String, Object> content =
                    safeRetrieveDocumentContent(dbContent, collectionName, documentName);
            DocumentSnapshot snapshot;
            if (content != null) {
                snapshot = createMockDocumentSnapshot(content, true);
            } else {
                // return a null document snapshot for the retriever
                snapshot = createMockDocumentSnapshot(new HashMap<>(), false);
            }
            when(task.getResult()).thenReturn(snapshot);
        }));

        when(docRef.getId()).thenReturn(documentName);
        when(docRef.getParent()).thenReturn(colRef);
        when(docRef.delete()).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                HashMap<String, HashMap<String, Object>> colContent =
                        Objects.requireNonNull(dbContent.get(collectionName));
                Objects.requireNonNull(colContent.get(documentName)).clear();
                colContent.remove(documentName);
                return null;
            }
        });
        when(docRef.set(any(HashMap.class))).thenAnswer(new Answer<Task<Void>>() {
            @Override
            public Task<Void> answer(InvocationOnMock invocation) throws Throwable {
                Task<Void> task1 = createMockTask(task -> {
                    ensureDocumentExist(dbContent, collectionName, documentName);
                    Objects.requireNonNull(dbContent.get(collectionName)).put(documentName,
                            invocation.getArgument(0));
                });
                return task1;
            }
        });

        return docRef;
    }

    static public CollectionReference createMockCollectionReference(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent, String collectionName) {
        CollectionReference colRef = mock(CollectionReference.class);

        when(colRef.document(anyString())).thenAnswer(new Answer<DocumentReference>() {
            @Override
            public DocumentReference answer(InvocationOnMock invocation) throws Throwable {
                return createMockDocumentReference(dbContent, colRef, invocation.getArgument(0));
            }
        });
        when(colRef.get()).thenAnswer(new Answer<Task<QuerySnapshot>>() {
            @Override
            public Task<QuerySnapshot> answer(InvocationOnMock invocation) throws Throwable {
                return createMockTask(task -> when(task.getResult()).thenAnswer((Answer<QuerySnapshot>) invocation1 -> {
                    if(!dbContent.containsKey(collectionName))
                        return createQuerySnapshot(new ArrayList<>()); // empty
                    else
                        return createQuerySnapshot(dbContent.get(collectionName).values());
                }));
            }
        });
        when(colRef.getId()).thenReturn(collectionName);
        when(colRef.whereEqualTo(anyString(), any())).thenAnswer(new Answer<Query>() {
            @Override
            public Query answer(InvocationOnMock invocation) throws Throwable {
                String key = invocation.getArgument(0);
                Object targetValue = invocation.getArgument(1);

                HashMap<String, HashMap<String, Object>> colContent = dbContent.get(collectionName);
                if (colContent == null)
                    colContent = new HashMap<>();
                ArrayList<HashMap<String, Object>> filteredDocuments = new ArrayList<>();
                for (HashMap<String, Object> document: colContent.values()) {
                    if (document.containsKey(key) && Objects.requireNonNull(
                            document.get(key)).equals(targetValue))
                        filteredDocuments.add(document);
                }
                return createMockQuery(filteredDocuments, collectionName);
            }
        });

        return colRef;
    }

    //----------------------------------------Query------------------------------------------------

    static public Query createMockQuery(Collection<HashMap<String, Object>> resultDocuments,
                                 String collectionName) {
        Query query = mock(Query.class);

        when(query.get()).thenAnswer(new Answer<Task<QuerySnapshot>>() {
            @Override
            public Task<QuerySnapshot> answer(InvocationOnMock invocation) throws Throwable {
                return createMockTask(task -> when(task.getResult()).thenAnswer((Answer<QuerySnapshot>) invocation1 -> {
                    return createQuerySnapshot(resultDocuments);
                }));
            }
        });
        when(query.whereEqualTo(anyString(), any())).thenAnswer((Answer<Query>) invocation -> {
            String key = invocation.getArgument(0);
            Object targetValue = invocation.getArgument(2);

            ArrayList<HashMap<String, Object>> filteredDocuments = new ArrayList<>();
            for (HashMap<String, Object> document: resultDocuments) {
                if (document.containsKey(key) &&
                        Objects.requireNonNull(document.get(key)).equals(targetValue))
                    filteredDocuments.add(document);
            }
            return createMockQuery(filteredDocuments, collectionName);
        });
        return query;
    }

    //-------------------------------------Transaction-------------------------------------------

    static public Transaction createMockTransaction(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent
    ) throws FirebaseFirestoreException {
        Transaction transaction = mock(Transaction.class);

        // transaction.get differs from task in that it will not return null document snapshot
        // when the document does not exist; instead it returns an empty snapshot
        when(transaction.get(any(DocumentReference.class))).thenAnswer(new Answer<DocumentSnapshot>() {
            @Override
            public DocumentSnapshot answer(InvocationOnMock invocation) throws Throwable {
                DocumentReference docRef = invocation.getArgument(0);
                CollectionReference colRef = docRef.getParent();
                HashMap<String, Object> content = safeRetrieveDocumentContent(dbContent, colRef.getId(), docRef.getId());
                if (content == null) {
                    content = new HashMap<>();
                    return createMockDocumentSnapshot(content, false);
                } else {
                    return createMockDocumentSnapshot(content, true);
                }
            }
        });
        when(transaction.set(any(DocumentReference.class), any(HashMap.class))).thenAnswer((Answer<Transaction>) invocation -> {
            DocumentReference docRef = invocation.getArgument(0);
            CollectionReference colRef = docRef.getParent();
            HashMap<String, Object> map = invocation.getArgument(1);
            ensureCollectionExist(dbContent, colRef.getId());
            Objects.requireNonNull(dbContent.get(colRef.getId())).put(docRef.getId(), map);

            return transaction;
        });
        when(transaction.update(any(DocumentReference.class), anyString(), any(Object.class))).thenAnswer(new Answer<Transaction>() {
            @Override
            public Transaction answer(InvocationOnMock invocation) throws Throwable {
                DocumentReference docRef = invocation.getArgument(0);
                CollectionReference colRef = docRef.getParent();
                String key = invocation.getArgument(1);
                Object val = invocation.getArgument(2);

                HashMap<String, HashMap<String, Object>> colContent =
                        Objects.requireNonNull(dbContent.get(colRef.getId()));
                Objects.requireNonNull(colContent.get(docRef.getId())).put(key, val);

                return transaction;
            }
        });
        when(transaction.delete(any(DocumentReference.class))).thenAnswer(new Answer<Transaction>() {
            @Override
            public Transaction answer(InvocationOnMock invocation) throws Throwable {
                DocumentReference docRef = invocation.getArgument(0);
                docRef.delete();
                return transaction;
            }
        });

        return transaction;
    }

    //---------------------------------------Database---------------------------------------------

    static public FirebaseFirestore createMockDatabase(
            HashMap<String, HashMap<String, HashMap<String, Object>>> dbContent) {
        FirebaseFirestore db = mock(FirebaseFirestore.class);

        when(db.collection(anyString())).thenAnswer(new Answer<CollectionReference>() {
            @Override
            public CollectionReference answer(InvocationOnMock invocation) throws Throwable {
                String collectionName = invocation.getArgument(0);
                return createMockCollectionReference(dbContent, collectionName);
            }
        });
        when(db.runTransaction(any(Transaction.Function.class))).thenAnswer((Answer<Task<Void>>) invocation -> {
            Transaction.Function<Void> function = invocation.getArgument(0);
            return createMockTask(task -> {
                when(task.getResult()).thenReturn(null);
                function.apply(createMockTransaction(dbContent));
            });
        });

        return db;
    }
}
