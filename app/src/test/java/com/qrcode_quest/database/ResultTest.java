package com.qrcode_quest.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResultTest {
    @Test
    public void testSuccess(){
        Result<String> r1 = new Result<>("test");

        // Test all values are as they should be
        assertTrue(r1.isSuccess());
        assertEquals(r1.getData(), "test");
        assertNull(r1.getError());

        Result<Integer> r2 = new Result<>(10);

        // Test a second generic type
        assertTrue(r2.isSuccess());
        assertEquals(r2.getData().intValue(), 10);
        assertNull(r2.getError());
    }

    @Test
    public void testError(){
        Result<String> r1 = new Result<>(new DbError("failed", this));

        // Test all values are as they should be
        assertFalse(r1.isSuccess());
        assertEquals(r1.getError().getMessage(), "failed");
        assertEquals(r1.getError().getSender(), this);
        assertNull(r1.getData());

        Result<Integer> r2 = new Result<>(new DbError("failed", this));

        // Test all values are as they should be
        assertFalse(r2.isSuccess());
        assertEquals(r2.getError().getMessage(), "failed");
        assertEquals(r2.getError().getSender(), this);
        assertNull(r2.getData());
    }
}
