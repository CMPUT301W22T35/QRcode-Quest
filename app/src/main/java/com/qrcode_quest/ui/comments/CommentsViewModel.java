package com.qrcode_quest.ui.comments;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.database.CommentManager;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Provides data handling for CommentsFragment.
 *
 * @author jdumouch
 * @version 1.1
 */
public class CommentsViewModel extends AndroidViewModel {
    private static final String CLASS_TAG = "CommentsViewModel";
    private final FirebaseFirestore db;

    public CommentsViewModel(@NonNull Application application, FirebaseFirestore db) {
        super(application);
        this.db = db;
    }

    /**
     * Gets an observable comment list for a specific hash
     * Calls loadComments, if needed.
     * @param codeHash The hash to fetch the comments for.
     */
    public LiveData<ArrayList<Comment>> getComments(String codeHash){
        if (comments == null || !codeHash.equals(commentsHash)){
            comments = new MutableLiveData<>();
            commentsHash = codeHash;
            loadComments();
        }

        return comments;
    }
    private MutableLiveData<ArrayList<Comment>> comments;
    private String commentsHash;

    /**
     * Performs the fetching for comment data
     * Can be used to force a refresh on the comments. (if, for example, one is added)
     *
     * getComments() must be called first, or no hash will be selected to load.
     */
    public void loadComments(){
        if (commentsHash == null) { return; }
        new CommentManager(db).getQRComments(commentsHash, results -> {
            if (!results.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load comments from database.");
                return;
            }

            // Sort with newest at top
            ArrayList<Comment> commentResults = results.unwrap();
            Collections.reverse(commentResults);

            comments.setValue(commentResults);
        });
    }
}
