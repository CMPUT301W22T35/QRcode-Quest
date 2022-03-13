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
     * @param documentName document id
     * @param content hashmap representation of the key value pairs in the document
     * @return a mock document snapshot
     */
    static public DocumentSnapshot createMockDocumentSnapshot(
            String documentName, HashMap<String, Object> content) {
        // make sure the content is independent from the actual mock database
        assert content != null;
        HashMap<String, Object> snapshot = new HashMap<>(content);

        DocumentSnapshot docRef = mock(DocumentSnapshot.class);
        when(docRef.getString(anyString())).thenAnswer((Answer<String>) invocation -> {
            String key = invocation.getArgument(0);
            return (String) snapshot.get(key);
        });
        when(docRef.getLong(anyString())).thenAnswer((Answer<Long>) invocation -> {
            String key = invocation.getArgument(0);
            Object val = snapshot.get(key);
            if (val == null)
                return null;
            if (val.getClass() == Integer.class)
                return new Long((Integer) val);
            else
                return (Long) val;
        });
        when(docRef.getBoolean(anyString())).thenAnswer((Answer<Boolean>) invocation -> {
            String key = invocation.getArgument(0);
            return (Boolean) snapshot.get(key);
        });
        when(docRef.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return snapshot.get(key);
        });
        when(docRef.getId()).thenReturn(documentName);

        return docRef;
    }

    static public QuerySnapshot createQuerySnapshot(Collection<HashMap<String, Object>> resultDocuments) {
        final ArrayList<HashMap<String, Object>> documents = new ArrayList<>(resultDocuments);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);

        when(querySnapshot.size()).thenReturn(documents.size());
        when(querySnapshot.getDocuments()).thenAnswer(new Answer<ArrayList<HashMap<String,Object>>>() {
            @Override
            public ArrayList<HashMap<String, Object>> answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<>(documents);
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
     * note by default task will not execute at all if no listeners are attached to it
     * @param action action to perform
     * @param <TResult> one that passed to listeners as Task<TResult> usually DocumentSnapshot or QuerySnapshot
     * @return a mock task
     */
    static public <TResult> Task<TResult> createMockTask(MockTaskAction<TResult> action) {
        final Boolean[] taskIsExecuted = {false};

        Task<TResult> task = mock(Task.class);
        when(task.isSuccessful()).thenReturn(true);
        when(task.isComplete()).thenReturn(taskIsExecuted[0]);
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

        when(docRef.get()).thenAnswer(new Answer<Task<DocumentSnapshot>>() {
            @Override
            public Task<DocumentSnapshot> answer(InvocationOnMock invocation) throws Throwable {
                return createMockTask(task -> {
                    HashMap<String, Object> content =
                            safeRetrieveDocumentContent(dbContent, collectionName, documentName);
                    if (content != null) {
                        DocumentSnapshot snapshot = createMockDocumentSnapshot(
                                documentName,
                                content);
                        when(task.getResult()).thenReturn(snapshot);
                    } else {
                        // return a null document snapshot for the retriever
                        when(task.getResult()).thenReturn(null);
                    }
                });
            }
        });

        when(docRef.getId()).thenReturn(documentName);
        when(docRef.getParent()).thenReturn(colRef);
        when(docRef.delete()).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Objects.requireNonNull(dbContent.get(collectionName)).remove(documentName);
                return null;
            }
        });
        when(docRef.set(any(HashMap.class))).thenAnswer(new Answer<Task<DocumentSnapshot>>() {
            @Override
            public Task<DocumentSnapshot> answer(InvocationOnMock invocation) throws Throwable {
                return createMockTask(task -> {
                    ensureDocumentExist(dbContent, collectionName, documentName);
                    Objects.requireNonNull(dbContent.get(collectionName)).put(documentName,
                            invocation.getArgument(0));
                });
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
                Object targetValue = invocation.getArgument(2);

                ArrayList<HashMap<String, Object>> filteredDocuments = new ArrayList<>();
                for (HashMap<String, Object> document: Objects.requireNonNull(dbContent.get(collectionName)).values()) {
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

        when(transaction.get(any(DocumentReference.class))).thenAnswer(new Answer<DocumentSnapshot>() {
            @Override
            public DocumentSnapshot answer(InvocationOnMock invocation) throws Throwable {
                DocumentReference docRef = invocation.getArgument(0);
                CollectionReference colRef = docRef.getParent();
                HashMap<String, Object> content = safeRetrieveDocumentContent(dbContent, colRef.getId(), docRef.getId());
                if (content == null)
                    return null;
                else
                    return createMockDocumentSnapshot(docRef.getId(),
                        safeRetrieveDocumentContent(dbContent, colRef.getId(), docRef.getId()));
            }
        });
        when(transaction.set(any(DocumentReference.class), HashMap.class)).thenAnswer(new Answer<Transaction>() {
            @Override
            public Transaction answer(InvocationOnMock invocation) throws Throwable {
                DocumentReference docRef = invocation.getArgument(0);
                CollectionReference colRef = docRef.getParent();
                HashMap<String, Object> map = invocation.getArgument(1);
                Objects.requireNonNull(dbContent.get(colRef.getId())).put(docRef.getId(), map);

                return transaction;
            }
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
            createMockTask(task -> {
                when(task.getResult()).thenReturn(null);
                function.apply(createMockTransaction(dbContent));
            });
            return null;
        });

        return db;
    }
}
