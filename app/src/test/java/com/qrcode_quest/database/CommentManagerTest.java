package com.qrcode_quest.database;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.MockDb;
import com.qrcode_quest.entities.Comment;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentManagerTest {

    /**
     * create a list of comments with a fixed pattern of messages, qr hash and id
     * @param size number of comments
     * @param qrHash gives the QR code that the comments are positioned under
     * @return an array list of comments
     */
    public ArrayList<Comment> createMockComments(int size, String qrHash) {
        ArrayList<Comment> comments = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            Comment comment = mock(Comment.class);
            String positionStr = Integer.toString(i + 1);
            String content = "content" + positionStr;
            when(comment.getUid()).thenReturn(positionStr);
            when(comment.getContent()).thenReturn(content);
            when(comment.getQrHash()).thenReturn(qrHash);
            comments.add(comment);
        }
        return comments;
    }

    @Test
    public void testAddComment() {
        FirebaseFirestore db = MockDb.createMockDatabase(new HashMap<>());
        CommentManager manager = new CommentManager(db);

        // a list of comments to add one by one
        ArrayList<Comment> comments = createMockComments(3, "hash1");

        // initially no comments should be present
        manager.getQRComments("hash1", result -> assertEquals(0, result.unwrap().size()));

        // insert one comment
        manager.addComment(comments.get(0), result -> assertTrue(result.isSuccess()));
        manager.getQRComments("hash1", result -> {
            assertEquals(1, result.unwrap().size());
        });

        // insert more comments at different positions
        manager.addComment(comments.get(1), result -> assertTrue(result.isSuccess()));
        manager.addComment(comments.get(2), result -> assertTrue(result.isSuccess()));
        ArrayList<Comment> otherComments = createMockComments(2, "hash2");
        manager.addComment(otherComments.get(1), result -> assertTrue(result.isSuccess()));
        manager.addComment(otherComments.get(0), result -> assertTrue(result.isSuccess()));
        manager.getQRComments("hash1", result -> {
            ArrayList<Comment> ret = result.unwrap();
            assertEquals(3, ret.size());
            assertEquals("1", ret.get(0).getUid());
            assertEquals("hash1", ret.get(1).getQrHash());
            assertEquals("content3", ret.get(2).getContent());
        });
        // notice the order should be reversed
        manager.getQRComments("hash2", result -> {
            ArrayList<Comment> ret = result.unwrap();
            assertEquals(2, ret.size());
            assertEquals("1", ret.get(1).getUid());
            assertEquals("2", ret.get(0).getUid());
        });
        manager.getQRComments("hash3", result -> assertEquals(0, result.unwrap().size()));
    }
}
