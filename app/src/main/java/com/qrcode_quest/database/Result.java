package com.qrcode_quest.database;

import androidx.annotation.NonNull;

/**
 * A result wrapper for DatabaseManager callbacks.
 * Based loosely off of Rust's std::result type.
 *
 * @param <T> The payload type to wrap with a Result.
 *
 * @author jdumouch
 * @version 1.0
 */
public class Result<T> {
    private DbError error;
    private T result;

    /**
     * Wrap data in a result
     * @param result The data to wrap
     */
    public Result(T result){
        this.result = result;
        this.error = null;
    }

    /**
     * Wrap an error in a result
     * @param error The error to wrap
     */
    public Result(@NonNull DbError error){
        this.error = error;
        this.result = null;
    }

    /**
     * Returns if a Result contains a success response.
     */
    public boolean isSuccess(){
        return (this.error == null);
    }

    /**
     * Returns the data wrapped by the result. <br>
     * (Throws assertion error on unwrapping an error)
     */
    public T unwrap(){
        assert this.error == null;
        return this.result;
    }

    /**
     * Returns the error wrapped by the result <br>
     * (Null on success)
     */
    public DbError getError() {
        return this.error;
    }
}
