package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

/**
 * Represents a player's account data.
 * This provides a local copy of the player's data and changes will not automatically be
 * reflected on the database.
 *
 * Database interaction concerning a PlayerAccount should be handled through PlayerManager.
 * @see PlayerManager
 *
 * @author jdumouch
 * @version 1.0
 */
public class PlayerAccount {

    /**
     * A unique username associated with the account.<br>
     * This can only be changed in PlayerManager.
     * @see PlayerManager
     */
    @NonNull
    private final String username;

    /** (Optional) A user's email address */
    @NonNull
    private String email;

    /** (Optional) A user's phone number */
    @NonNull
    private String phone;

    /** A flag determining if a user has privileged access */
    private boolean isOwner;

    /** A flag determining of a user has been deleted and should not be displayed */
    private boolean isDeleted;

    /**
     * Creates a local instance of a player.
     * In most cases, <code>PlayerManager</code> should be used to create new players.
     * <br><br>
     * <i><b>Important</b>: This does <b>not</b> communicate with the database.
     * Username uniqueness is not guaranteed and the addition is <b>not</b> saved automatically.</i>
     *
     * @param username The unique username of the new PlayerAccount
     * @see PlayerManager
     */
    public PlayerAccount(@NonNull String username){
        this.username = username;
        this.email = "";
        this.phone = "";
        this.isDeleted = false;
        this.isOwner = false;
    }

    /**
     * For more detailed documentation:
     * @see PlayerAccount#PlayerAccount(String)
     */
    public PlayerAccount(@NonNull String username, @NonNull String email, @NonNull String phone){

        this.username = username;
        this.email = email;
        this.phone = phone;
        this.isDeleted = false;
        this.isOwner = false;
    }

    /**
     * For more detailed documentation:
     * @see PlayerAccount#PlayerAccount(String)
     */
    public PlayerAccount(@NonNull String username, @NonNull String email, @NonNull String phone,
                         boolean isDeleted, boolean isOwner){

        this.username = username;
        this.email = email;
        this.phone = phone;
        this.isDeleted = isDeleted;
        this.isOwner = isOwner;
    }


    // isDeleted getter/setter
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public boolean isDeleted() {
        return this.isDeleted;
    }

    // isOwner getter/setter
    public void setOwner(boolean isOwner){
        this.isOwner = isOwner;
    }
    public boolean isOwner() {
        return this.isOwner;
    }

    public @NonNull String getUsername() {
        return username;
    }

    // Email getter/setter
    public void setEmail(@NonNull String email) {
        this.email = email;
    }
    public @NonNull String getEmail() {
        return email;
    }

    // Phone getter/setter
    public void setPhoneNumber(@NonNull String phoneNumber){
        this.phone = phoneNumber;
    }
    public @NonNull String getPhoneNumber() {
        return phone;
    }

}
