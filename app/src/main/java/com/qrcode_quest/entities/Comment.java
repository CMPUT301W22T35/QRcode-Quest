package com.qrcode_quest.entities;

/**
 * Represents one comment made by a user under a qr code.
 * @author tianming
 * @version 1.0
 */
public class Comment {
    private String uid;  // id of the commenter
    private String content;
    private String qrHash;

    /**
     * Create a comment object
     * @param uid id of the player that comments under the qr code
     * @param content content of the comment
     * @param qrHash hash string of the qr code
     */
    public Comment(String uid, String content, String qrHash) {
        this.uid = uid;
        this.content = content;
        this.qrHash = qrHash;
    }

    /**
     * get the id of the commenter
     * @return id of the player commented under the qr code
     */
    public String getUid() {
        return uid;
    }

    /**
     * set the commenter id of the comment
     * @param uid id of the player commented under the qr code
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * get the comment content
     * @return the content string
     */
    public String getContent() {
        return content;
    }

    /**
     * set the comment content
     * @param content the content string
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * get the hash of qr code
     * @return hash string of qr code
     */
    public String getQrHash() {
        return qrHash;
    }

    /**
     * set the comment to be under another qr code
     * @param qrHash hash string of the new qr code
     */
    public void setQrHash(String qrHash) {
        this.qrHash = qrHash;
    }
}
