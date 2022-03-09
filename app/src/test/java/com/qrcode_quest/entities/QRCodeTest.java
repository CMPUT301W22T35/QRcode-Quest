package com.qrcode_quest.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class QRCodeTest {

    private RawQRCode MockRawQRCode() {
        return new RawQRCode("some message");
    }

    /**
     * return a mock qr code with the mock raw qr code and a comment made by user "dog", with
     * content "I bark"
     * @return a mock QRCode object
     */
    private QRCode MockQRCode() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        QRCode code = new QRCode(MockRawQRCode());
        String hash = code.getHashCode();
        ArrayList<Comment> comments = code.getComments();
        comments.add(new Comment("dog", "I bark", hash));
        return code;
    }

    @Test
    public void testSetGets() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        RawQRCode rawCode = MockRawQRCode();
        QRCode code = MockQRCode();

        // test the getters of mock QR code
        assertEquals(rawCode.getQRHash(), code.getHashCode());
        assertEquals(rawCode.getScore(), code.getScore());
        assertEquals(1, code.getComments().size());

        // change to a new comment list
        ArrayList<Comment> newComments = new ArrayList<>();
        newComments.add(new Comment("cat", "I purr", "hash12"));
        newComments.add(new Comment("elephant", "I trumpet", "hash23"));
        code.setComments(newComments);
        assertEquals(2, code.getComments().size());
        assertEquals("I trumpet", code.getComments().get(1).getContent());

        // array list should be modified after modifying the array list get by getComments() interface
        ArrayList<Comment> comments = code.getComments();
        comments.add(new Comment("turtle", "I crawl", "hash34"));
        comments.get(1).setQrHash("hash45");
        assertEquals(3, code.getComments().size());
        assertEquals("hash45", code.getComments().get(1).getQrHash());

        // test the other constructor
        QRCode other = new QRCode("hash67", 44, new ArrayList<>());
        assertEquals("hash67", other.getHashCode());
        assertEquals(44, other.getScore());
        assertEquals(0, other.getComments().size());
    }
}
