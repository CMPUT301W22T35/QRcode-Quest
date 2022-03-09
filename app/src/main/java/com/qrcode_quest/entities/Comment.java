package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

/**
 * Represents one comment made by a user under a qr code.
 * @author tianming
 * @version 1.0
 */
public class Comment {
    @NonNull
    private String uid;  // id of the commenter
    @NonNull
    private String content;
    @NonNull
    private String qrHash;

    /**
     * Create a comment object
     * @param uid id of the player that comments under the qr code
     * @param content content of the comment
     * @param qrHash hash string of the qr code
     */
    public Comment(@NonNull String uid, @NonNull String content, @NonNull String qrHash) {
        this.uid = uid;
        this.content = content;
        this.qrHash = qrHash;
    }

    /**
     * get the id of the commenter
     * @return id of the player commented under the qr code
     */
    public @NonNull String getUid() {
        return uid;
    }

    /**
     * set the commenter id of the comment
     * @param uid id of the player commented under the qr code
     */
    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    /**
     * get the comment content
     * @return the content string
     */
    public @NonNull String getContent() {
        return content;
    }

    /**
     * set the comment content
     * @param content the content string
     */
    public void setContent(@NonNull String content) {
        this.content = content;
    }

    /**
     * get the hash of qr code
     * @return hash string of qr code
     */
    public @NonNull String getQrHash() {
        return qrHash;
    }

    /**
     * set the comment to be under another qr code
     * @param qrHash hash string of the new qr code
     */
    public void setQrHash(@NonNull String qrHash) {
        this.qrHash = qrHash;
    }
}
