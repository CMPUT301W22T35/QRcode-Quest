package com.qrcode_quest.database;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbErrorTest {
    @Test
    public void testGetters(){
        DbError err = new DbError("msg", this);

        assertEquals(err.getMessage(), "msg");
        assertEquals(err.getSender(), this);
    }
}
