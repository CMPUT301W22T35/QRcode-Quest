package com.qrcode_quest;

import static org.junit.Assert.assertEquals;

import com.qrcode_quest.entities.RawQRCode;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class RawQRCodeTest {
    /**
     * test function getQRHash()
     */
    @Test
    public void testQRHash() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        RawQRCode rawCode = new RawQRCode("qr code message");
        byte[] hash = rawCode.getQRHash();

        // used an online SHA-256 converter to generate an example
        assertEquals("882e9a09286d24c67453fdbe29b2c91786b841bd6c52d1328d61cf1905550528",
                new String(RawQRCode.translateHashToReadable(hash), StandardCharsets.US_ASCII));

        // should always generate a 64 length hex string
        String[] examples = {"", "hello qr code", "*-a&%$!)$ <<>", "0",
                "this is a longer message than the last one, and contains more than 64 characters in this sentence"};
        for (int i = 0; i < 5; i++) {
            RawQRCode code = new RawQRCode(examples[i]);
            assertEquals(64, code.getQRHash().length);
        }
    }

    /**
     * test converting a hex hash array to integer score
     */
    @Test
    public void testHashToScore() {
        // The example on eclass doesn't seem to count 0, I added 4 to the total score because
        // there are 4 zeros
        int score = RawQRCode.getScoreFromHash(RawQRCode.translateReadableToHash(
                "696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6"
                        .getBytes(StandardCharsets.US_ASCII)));
        assertEquals(115, score);

        // edge case: 0 and 15
        score = RawQRCode.getScoreFromHash(RawQRCode.translateReadableToHash(
                "0abcd00abcd000abcdfabcdffabcdfffabcdabcdabcdabcdabcdabcdabcdabcd"
                        .getBytes(StandardCharsets.US_ASCII)));
        assertEquals(1 + 20 + 400 + 15 + 225, score);

        // zero score
        score = RawQRCode.getScoreFromHash(RawQRCode.translateReadableToHash(
                "abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"
                        .getBytes(StandardCharsets.US_ASCII)));
        assertEquals(0, score);

        // overflow behavior, and correct handling of 1s
        score = RawQRCode.getScoreFromHash(RawQRCode.translateReadableToHash(
                "0000001100000000000000000000000000000000000000000000000000000000"
                        .getBytes(StandardCharsets.US_ASCII)));
        assertEquals(3200000 + 1, score);
    }

    @Test
    public void testGetScore() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        RawQRCode rawCode = new RawQRCode("qr code message");
        assertEquals(36, rawCode.getScore());
    }
}
