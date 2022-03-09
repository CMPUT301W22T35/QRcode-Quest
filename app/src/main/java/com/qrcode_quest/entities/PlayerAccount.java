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

    /**
     * Creates a local instance of a player.
     * In most cases, <code>PlayerManager</code> should be used to create new players.
     * <br><br>
     * <i><b>Important</b>: This does <b>not</b> communicate with the database.
     * Username uniqueness is not guaranteed and the addition is <b>not</b> saved automatically.</i>
     * @see PlayerManager
     * @param username
     */
    public PlayerAccount(String username){
        this.username = username;
        this.email = "";
        this.phone = "";
    }

    /**
     * For more detailed documentation:
     * @see PlayerAccount#PlayerAccount(String)
     */
    public PlayerAccount(String username, String email, String phone){
        this.username = username;
        this.email = email;
        this.phone = phone;
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
