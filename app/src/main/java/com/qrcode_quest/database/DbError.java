package com.qrcode_quest.database;

import androidx.annotation.NonNull;

/**
 * A database error object. These are created by database managers to forward some form
 * of failure message to a caller.
 *
 * @author jdumouch
 * @version 1.0
 */
 public class DbError {
    /** A message describing the error */
    @NonNull
    private final String message;

    /** The object that experienced the error */
    @NonNull
    private final Object sender;

    /**
     * Builds a new database error message.
     * @param message The message describing the error
     * @param sender The object that built the error
     */
    public DbError(@NonNull String message, @NonNull Object sender){
        this.message = message;
        this.sender = sender;
    }

    /**
     * Returns the message provided by the sender.
     */
    public @NonNull String getMessage(){
        return this.message;
    }

    /**
     * Returns the object that sent the error.
     */
    public @NonNull Object getSender(){
        return this.sender;
    }
}
