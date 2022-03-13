package com.qrcode_quest.entities;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.qrcode_quest.database.PlayerManager;
import com.qrcode_quest.database.Schema;

import java.util.HashMap;

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
public class PlayerAccount implements Parcelable {

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

    /**
     * Builds a PlayerAccount from a DocumentSnapshot.
     */
    public static PlayerAccount fromDocument(DocumentSnapshot document){
        String username = document.getString(Schema.PLAYER_NAME);
        String email = document.getString(Schema.PLAYER_EMAIL);
        String phone = document.getString(Schema.PLAYER_PHONE);
        Boolean isOwner = document.getBoolean(Schema.PLAYER_IS_OWNER);
        Boolean isDeleted = document.getBoolean(Schema.PLAYER_IS_DELETED);

        assert username != null && email != null && phone != null
                && isOwner != null && isDeleted != null;

        return new PlayerAccount( username, email, phone, isDeleted, isOwner );
    }

    /**
     * Builds a HashMap out of a PlayerAccount
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> playerMap = new HashMap<>();
        playerMap.put(Schema.PLAYER_NAME, this.getUsername());
        playerMap.put(Schema.PLAYER_EMAIL, this.getEmail());
        playerMap.put(Schema.PLAYER_PHONE, this.getPhoneNumber());
        playerMap.put(Schema.PLAYER_IS_OWNER, this.isOwner());
        playerMap.put(Schema.PLAYER_IS_DELETED, this.isDeleted());
        // TODO implement QRCodes
        playerMap.put(Schema.PLAYER_LOGIN_QRCODE, "");
        playerMap.put(Schema.PLAYER_PROFILE_QRCODE, "");
        return playerMap;
    }

    /**
     * Implement a creator to build PlayerAccounts from Parcels
     */
    public static final Parcelable.Creator<PlayerAccount> CREATOR =
            new Parcelable.Creator<PlayerAccount>(){

        @Override
        public PlayerAccount createFromParcel(Parcel parcel) {
            return new PlayerAccount(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readInt() > 0,
                parcel.readInt() > 0
            );
        }

        @Override
        public PlayerAccount[] newArray(int i) {
            return new PlayerAccount[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.username);
        parcel.writeString(this.email);
        parcel.writeString(this.phone);
        parcel.writeInt(this.isDeleted ? 1 : 0);
        parcel.writeInt(this.isOwner ? 1 : 0);
    }
}
