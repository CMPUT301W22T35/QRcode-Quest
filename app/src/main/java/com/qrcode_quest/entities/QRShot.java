package com.qrcode_quest.entities;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Represents a user's capture of a QRCode.
 * Multiple QRShots can be associated with the same underlying QRCode.
 * However, each user can only have one shot of one code.
 * The photo and geolocation of the shot are optionally stored at the users discretion.
 *
 * @author jdumouch
 * @version 1.0
 */
public class QRShot {
    /** The name of the QRShot */
    @NonNull
    private final String name;
    /** The username of the player who recorded the shot */
    @NonNull
    private final String ownerName;
    /** The hash of the QRCode this shot refers to */
    @NonNull
    private final String codeHash;

    /** (Optional) The users captured photo of the QRCode */
    private Bitmap photo;
    /** (Optional) The location the QRCode was captured at */
    private Geolocation location;

    /**
     * Creates a new QRShot.
     * In most cases, <code>QRManager</code> should be used to create new players.
     * <br><br>
     * <i><b>Important</b>: This does <b>not</b> communicate with the database.
     * Addition is <b>not</b> saved automatically.</i>
     * @see QRManager
     * @param owner The player that captured the shot
     * @param codeHash The hash of the code this shot is referring to
     */
    public QRShot(@NonNull String owner, @NonNull String codeHash){
        this.name = generateName();
        this.ownerName = owner;
        this.codeHash = codeHash;
        this.photo = null;
        this.location = null;
    }

    /**
     * Creates a new QRShot
     * For more details:
     * @see QRShot#QRShot(String, String)
     */
    public QRShot(@NonNull String owner, @NonNull String codeHash, Bitmap photo, Geolocation location){
        this.name = generateName();
        this.ownerName = owner;
        this.codeHash = codeHash;
        this.photo = photo;
        this.location = location;
    }

    /**
     * Generates a pseudo-random name using the details of the QRShot
     * as seed data.
     * @return A pseudo-random name
     */
    private String generateName(){

        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        String name;

        for (int i = 0; i < this.codeHash.length(); i++)
        {
            int randInt = rand.nextInt(this.codeHash.length());
            char randChar = codeHash.charAt(randInt);
            sb.append(randChar);
        }

        name = sb.toString();

        return name;

    }

    /**
     * Gets the immutable name of the QRShot's owner.
     */
    public String getOwnerName(){
        return this.ownerName;
    }

    /**
     * Gets the immutable hash of the QRShot's referent QRCode.
     */
    public String getCodeHash(){
        return this.codeHash;
    }

    /**
     * Gets the immutable name of the QRShot.
     */
    public String getName(){
        return this.name;
    }

    // Photo getter/setters
    public void setPhoto(Bitmap photo){
        this.photo = photo;
    }
    public Bitmap getPhoto(){
        return this.photo;
    }

    //Location getter/setters
    public void setLocation(Geolocation location){
        this.location = location;
    }
    public Geolocation getLocation(){
        return this.location;
    }


}
