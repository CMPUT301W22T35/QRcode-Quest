package com.qrcode_quest.database;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of interfaces that allow database managers to asynchronously return data to a listener after
 * the query was requested.
 *
 * @author jdumouch, tianming
 * @see DatabaseManager
 */
public class ManagerResult {
    /**
     * Since queries to the database are asynchronous, we should provide callbacks to handle the
     * return values when the queries are completed. Below provides an OnResult interface that
     * will be called when the query completes execution.
     */
    public interface OnRetrieveResult<ResultType, DocumentType> {
        /**
         * after the database retrieved the data, the data will not be in the format of objects
         * we want, but rather in a document format that has several fields that can be read;
         * we need to convert it to the target class by providing a method to do so
         * @param document retrieved database document reference
         * @return result data object after being converted into the desired class
         */
        ResultType retrieveResultFrom(DocumentType document);

        /**
         * A callback interface for a db request that returns a result object; the result object is
         * of the class Result; the caller can then handle the returned result in this callback to
         * determine if it is successful, what is the value of the result and other information if
         * applicable
         * @param result the result of query on completion, contains query result and error information
         */
        void onResult(ResultType result);
    }

    public abstract static class OnInsertDocumentResult implements
            OnRetrieveResult<Result<Void>, Void> {
        @Override
        public Result<Void> retrieveResultFrom(Void document) {
            return new Result<>((Void) null);  // if this callback is called then task is already successful
        }
    }

    public abstract static class OnPlayerAccountResult implements
            OnRetrieveResult<Result<PlayerAccount>, DocumentSnapshot> {

        public Result<PlayerAccount> retrieveResultFrom(DocumentSnapshot document) {
            assert document != null;
            // not an error when result does not exist
            if (!document.exists())
                return new Result<PlayerAccount>((PlayerAccount) null);
            Boolean isDeleted = document.getBoolean(Schema.PLAYER_IS_DELETED);
            if (isDeleted == null || isDeleted)
                return new Result<PlayerAccount>((PlayerAccount) null);

            // re-construct PlayerAccount object by reading the document
            String name = document.getString(Schema.PLAYER_NAME);
            String email = document.getString(Schema.PLAYER_EMAIL);
            String phone = document.getString(Schema.PLAYER_PHONE);
            if (name == null || email == null || phone == null) {
                DbError error = new DbError("Player's account contains null field on attributes " +
                        "playerName, email or phone in the database!", document.getId());
                return new Result<>(error);
            }
            PlayerAccount account = new PlayerAccount(name, email, phone);
            return new Result<>(account);
        }
    }

    public abstract static class OnCommentListResult implements
            OnRetrieveResult<Result<ArrayList<Comment>>, QuerySnapshot> {
        /**
         * turn the query result into a list of Comment objects
         * @param querySnapshot database query result snapshot
         * @return the comment list in ascending order of position
         */
        @Override
        public Result<ArrayList<Comment>> retrieveResultFrom(QuerySnapshot querySnapshot) {
            assert querySnapshot != null;
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
            ArrayList<Comment> comments = new ArrayList<>();
            for(int i = 0; i < documents.size(); i++)
                comments.add(null);
            for (DocumentSnapshot document: documents) {
                String name = document.getString(Schema.COMMENT_PLAYER_NAME);
                String content = document.getString(Schema.COMMENT_TEXT);
                Long position = document.getLong(Schema.COMMENT_POSITION);
                String qrHash = document.getString(Schema.COMMENT_QRHASH);
                if (name == null || content == null || position == null || qrHash == null) {
                    DbError error = new DbError("Comment relation contains null attributes " +
                            "in the database!", document.getId());
                    return new Result<>(error);
                } else if (position <= 0 || position > documents.size()) {
                    DbError error = new DbError("Position attribute contains illegal value " +
                            position + "in the database!", document.getId());
                    return new Result<>(error);
                }
                int intIndex = position.intValue() - 1;
                Comment comment = new Comment(name, content, qrHash);
                if (comments.get(intIndex) != null) {
                    DbError error = new DbError("Repeated comment position index detected in " +
                            "the Comment relation of the database!", document.getId());
                    return new Result<>(error);
                }
                comments.set(intIndex, comment);
            }
            return new Result<>(comments);
        }
    }
}
