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
        return new QRCode(MockRawQRCode());
    }

    @Test
    public void testSetGets() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        RawQRCode rawCode = MockRawQRCode();
        QRCode code = MockQRCode();

        // test the getters of mock QR code
        assertEquals(rawCode.getQRHash(), code.getHashCode());
        assertEquals(rawCode.getScore(), code.getScore());

        // test the other constructor
        QRCode other = new QRCode("hash67", 44);
        assertEquals("hash67", other.getHashCode());
        assertEquals(44, other.getScore());
    }
}
