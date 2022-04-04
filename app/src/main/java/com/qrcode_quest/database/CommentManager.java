package com.qrcode_quest.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.qrcode_quest.database.ManagerResult.CommentListRetriever;
import com.qrcode_quest.database.ManagerResult.Listener;
import com.qrcode_quest.database.ManagerResult.VoidResultRetriever;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains functions for query and update comments in the comment section
 * @author tianming
 * @version 1.0
 * @see Comment
 */
public class CommentManager extends DatabaseManager {
    /**
     * Create a Comment manager
     * @param db Firestore database instance to query and update
     */
    public CommentManager(FirebaseFirestore db) {
        super(db);
    }

    /**
     * add a comment under a QR code
     * @param comment the comment to be added
     * @param listener the callback to be executed to decide what to do when update finishes
     */
    public void addComment(Comment comment, Listener<Void> listener) {

        final String qrHash = comment.getQrHash();
        final CollectionReference commentCollectionRef = getDb().collection(Schema.COLLECTION_COMMENT);
        final DocumentReference metaCommentDocRef = commentCollectionRef.document(
                Schema.getCommentMetaDocumentName(qrHash));

        Task<Void> task = getDb().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                // get the end position for new comment to be inserted at
                Long commentNum = transaction.get(metaCommentDocRef).getLong(Schema.COMMENT_META_COUNT);
                int intCommentNum = 1;  // this is first comment if document not found
                if (commentNum != null) {
                    // not the first comment, set position to one after the end
                    intCommentNum = commentNum.intValue() + 1;
                }
                // insert a new comment document and make sure it does not already exist
                DocumentReference commentDocRef = commentCollectionRef.document(
                        Schema.getCommentDocumentName(qrHash, intCommentNum));
                DocumentSnapshot commentDoc = transaction.get(commentDocRef);
                if (commentDoc.exists())
                    throw new FirebaseFirestoreException("Comment at position already exists!",
                            FirebaseFirestoreException.Code.ABORTED);

                // update meta info
                if (commentNum != null) {
                    transaction.update(metaCommentDocRef, Schema.COMMENT_META_COUNT, intCommentNum);
                } else {
                    // the first comment, create mata info document
                    Map<String, Object> map = new HashMap<>();
                    map.put(Schema.COMMENT_META_COUNT, intCommentNum);
                    transaction.set(metaCommentDocRef, map);
                }

                // then insert the new comment at the end position
                // prepare insert data
                Map<String, Object> map = new HashMap<>();
                map.put(Schema.COMMENT_PLAYER_NAME, comment.getUid());
                map.put(Schema.COMMENT_POSITION, intCommentNum);
                map.put(Schema.COMMENT_QRHASH, qrHash);
                map.put(Schema.COMMENT_TEXT, comment.getContent());
                transaction.set(commentDocRef, map);

                return null;
            }
        });
        retrieveResultByTask(task, listener, new VoidResultRetriever());
    }

    /**
     * retrieve comments under the specified QR code
     * @param qrHash identifies the QR code that holds the comments
     * @param onResult specifies how the result should be handled
     */
    public void getQRComments(String qrHash, Listener<ArrayList<Comment>> onResult) {
        // create a task to retrieve all Comment documents that has the given qrHash
        Task<QuerySnapshot> task = getDb().collection(Schema.COLLECTION_COMMENT)
                .whereEqualTo(Schema.COMMENT_QRHASH, qrHash)
                .get();
        retrieveResultByTask(task, onResult, new CommentListRetriever());
    }
}
