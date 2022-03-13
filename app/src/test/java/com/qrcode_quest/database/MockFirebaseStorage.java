package com.qrcode_quest.database;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class MockFirebaseStorage {

    static public QRManager.PhotoEncoding createMockEncoding() {
        QRManager.PhotoEncoding encoding = mock(QRManager.PhotoEncoding.class);
        when(encoding.encodeToBytes(any())).thenCallRealMethod();
        when(encoding.decodeFromBytes(any(byte[].class))).thenAnswer(
                (Answer<Bitmap>) invocation -> createMockBitmap(invocation.getArgument(0)));
        return encoding;
    }

    static public Bitmap createMockBitmap(byte[] content) {
        Bitmap bitmap = mock(Bitmap.class);

        when(bitmap.compress(any(Bitmap.CompressFormat.class), anyInt(), any(ByteArrayOutputStream.class)))
                .thenAnswer((Answer<Boolean>) invocation -> {
                    ByteArrayOutputStream baos = invocation.getArgument(2);
                    baos.write(content);
                    return true;
                });

        return bitmap;
    }

    /**
     * create an upload task
     * @return a mock task
     */
    static public UploadTask createMockUploadTask(HashMap<String, byte[]> photos, String refPath, byte[] photoBytes) {
        UploadTask task = mock(UploadTask.class);
        // for implementation below
        Task<UploadTask.TaskSnapshot> innerTask = MockDb.createMockTask(new MockDb.MockTaskAction<UploadTask.TaskSnapshot>() {
            @Override
            public void onMockTaskExecution(Task<UploadTask.TaskSnapshot> task) throws FirebaseFirestoreException {
                if(photos.containsKey(refPath)) {
                    // photo already exists
                    when(task.isSuccessful()).thenReturn(false);
                } else {
                    photos.put(refPath, photoBytes);
                }
            }
        });

        when(task.isSuccessful()).thenAnswer((Answer<Boolean>) invocation -> innerTask.isSuccessful());
        when(task.isComplete()).thenAnswer((Answer<Boolean>) invocation -> innerTask.isComplete());
        when(task.getResult()).thenAnswer((Answer<UploadTask.TaskSnapshot>) invocation -> innerTask.getResult());

        when(task.addOnCompleteListener(any(OnCompleteListener.class))).thenAnswer((Answer<UploadTask>) invocation -> {
            innerTask.addOnCompleteListener((OnCompleteListener<UploadTask.TaskSnapshot>) invocation.getArgument(0));
            return task;
        });
        when(task.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer((Answer<UploadTask>) invocation -> {
            innerTask.addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) invocation.getArgument(0));
            return task;
        });
        when(task.addOnFailureListener(any(OnFailureListener.class))).thenAnswer((Answer<UploadTask>) invocation -> {
            innerTask.addOnFailureListener((OnFailureListener) invocation.getArgument(0));
            return task;
        });
        return task;
    }

    static public StorageReference createMockStorageReference(HashMap<String, byte[]> photos, String refPath) {
        StorageReference ref = mock(StorageReference.class);

        when(ref.getBytes(any(Long.class)))
                .thenAnswer(new Answer<Task<byte[]>>() {
            @Override
            public Task<byte[]> answer(InvocationOnMock invocation) throws Throwable {
                Task<byte[]> task = MockDb.createMockTask(task1 -> { });
                if (!photos.containsKey(refPath))
                    when(task.isSuccessful()).thenReturn(false);
                byte[] photoBytes = photos.get(refPath);
                assert Objects.requireNonNull(photoBytes).length <=
                        ((Long) invocation.getArgument(0)).intValue();
                when(task.getResult()).thenReturn(photoBytes);
                return task;
            }
        });
        when(ref.putBytes(any(byte[].class))).thenAnswer(new Answer<UploadTask>() {
            @Override
            public UploadTask answer(InvocationOnMock invocation) throws Throwable {
                byte[] bytes = invocation.getArgument(0);
                return createMockUploadTask(photos, refPath, bytes);
            }
        });

        return ref;
    }

    static public FirebaseStorage createMockFirebaseStorage(HashMap<String, byte[]> photos) {
        FirebaseStorage storage = mock(FirebaseStorage.class);

        when(storage.getReference(anyString())).thenAnswer(
                (Answer<StorageReference>) invocation ->
                        createMockStorageReference(photos, invocation.getArgument(0)));

        return storage;
    }
}
