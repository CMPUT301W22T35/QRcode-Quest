package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommentTest {
    @Test
    public void testSetGets() {
        Comment dogComment = new Comment("dog", "I bark", "awionvoi");
        assertEquals("dog", dogComment.getUid());
        assertEquals("I bark", dogComment.getContent());
        assertEquals("awionvoi", dogComment.getQrHash());
    }
}
