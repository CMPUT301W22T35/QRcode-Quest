package com.qrcode_quest.ui.comments;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.database.CommentManager;
import com.qrcode_quest.entities.Comment;

import java.util.ArrayList;

/**
 * Provides data handling for CommentsFragment.
 *
 * @author jdumouch
 * @version 1.0
 */
public class CommentsViewModel extends ViewModel {
    private static final String CLASS_TAG = "CommentsViewModel";


    /**
     * Gets an observable comment list for a specific hash
     * @param codeHash The hash to fetch the comments for.
     */
    public LiveData<ArrayList<Comment>> getComments(String codeHash){
        if (comments == null || !codeHash.equals(commentsHash)){
            comments = new MutableLiveData<>();
            commentsHash = codeHash;
            loadComments(commentsHash);
        }

        return comments;
    }
    private MutableLiveData<ArrayList<Comment>> comments;
    private String commentsHash;

    /** Performs the fetching for comment data */
    private void loadComments(String codeHash){
        new CommentManager(FirebaseFirestore.getInstance()).getQRComments(codeHash, results -> {
            if (!results.isSuccess()){
                Log.e(CLASS_TAG, "Failed to load comments from database.");
                return;
            }

            comments.setValue(results.unwrap());
        });
    }
}
