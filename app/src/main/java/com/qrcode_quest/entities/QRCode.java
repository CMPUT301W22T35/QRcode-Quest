package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a QR code and manages its associated comments made by players
 * @author tianming
 * @version 1.0
 */
public class QRCode {
    @NonNull private final String hashCode;
    private final int score;

    /**
     * create a QRCode object from a RawQRCode; no comments will be attached to the
     * qr code initially (with an empty ArrayList object)
     * @param rawCode raw QR code that the object is initialized from
     */
    public QRCode(@NonNull RawQRCode rawCode)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.hashCode = rawCode.getQRHash();
        this.score = rawCode.getScore();
    }

    /**
     * create a QRCode object
     * @param hashCode hash string of the actual qr code represented by this object
     * @param score score of the qr code
     */
    public QRCode(@NonNull String hashCode, int score) {
        this.hashCode = hashCode;
        this.score = score;
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
}
