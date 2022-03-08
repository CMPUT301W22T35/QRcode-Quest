package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CommentTest {
    @Test
    public void testSetGets() {
        Comment emptyComment = new Comment(null, null, null);
        assertNull(emptyComment.getUid());
        assertNull(emptyComment.getContent());
        assertNull(emptyComment.getQrHash());
        Comment dogComment = new Comment("dog", "I bark", "awionvoi");
        assertEquals("dog", dogComment.getUid());
        assertEquals("I bark", dogComment.getContent());
        assertEquals("awionvoi", dogComment.getQrHash());
    }
}
