package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Represents a QR code and manages its associated comments made by players
 * @author tianming
 * @version 1.0
 */
public class QRCode {
    @NonNull private final String hashCode;
    private final int score;
    @NonNull private ArrayList<Comment> comments;

    /**
     * create a QRCode object from a RawQRCode; no comments will be attached to the
     * qr code initially (with an empty ArrayList object)
     * @param rawCode raw QR code that the object is initialized from
     */
    public QRCode(@NonNull RawQRCode rawCode)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.hashCode = rawCode.getQRHash();
        this.score = rawCode.getScore();
        this.comments = new ArrayList<>();
    }

    /**
     * create a QRCode object
     * @param hashCode hash string of the actual qr code represented by this object
     * @param score score of the qr code
     * @param comments comments made under the qr code by players
     */
    public QRCode(@NonNull String hashCode, int score, @NonNull ArrayList<Comment> comments) {
        this.hashCode = hashCode;
        this.score = score;
        this.comments = comments;
    }

    /**
     * get hash string used to uniquely identify the qr code
     * @return the hash string
     */
    public @NonNull String getHashCode() {
        return hashCode;
    }

    /**
     * get score of the qr code
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * get a reference to the object's inner comment list
     * @return the comment list under the qr code
     */
    public @NonNull ArrayList<Comment> getComments() {
        return comments;
    }

    /**
     * set the object's inner comment list as provided
     * @param comments the comments to set under the qr code
     */
    public void setComments(@NonNull ArrayList<Comment> comments) {
        this.comments = comments;
    }
}
