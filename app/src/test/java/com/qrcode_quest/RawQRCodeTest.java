package com.qrcode_quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

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

        // used an online SHA-256 converter to generate an example
        assertEquals("882e9a09286d24c67453fdbe29b2c91786b841bd6c52d1328d61cf1905550528",
                new String(rawCode.getQRHash(), StandardCharsets.US_ASCII));

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
        int score = RawQRCode.getScoreFromHash(
                "696ce4dbd7bb57cbfe58b64f530f428b74999cb37e2ee60980490cd9552de3a6"
                        .getBytes(StandardCharsets.US_ASCII));
        assertEquals(115, score);

        // edge case: 0 and 15
        score = RawQRCode.getScoreFromHash(
                "0abcd00abcd000abcdfabcdffabcdfffabcdabcdabcdabcdabcdabcdabcdabcd"
                        .getBytes(StandardCharsets.US_ASCII));
        assertEquals(1 + 20 + 400 + 15 + 225, score);

        // zero score
        score = RawQRCode.getScoreFromHash(
                "abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"
                        .getBytes(StandardCharsets.US_ASCII));
        assertEquals(0, score);

        // overflow behavior, and correct handling of 1s
        score = RawQRCode.getScoreFromHash(
                "0000001100000000000000000000000000000000000000000000000000000000"
                        .getBytes(StandardCharsets.US_ASCII));
        assertEquals(3200000 + 1, score);
    }

    @Test
    public void testGetScore() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        RawQRCode rawCode = new RawQRCode("qr code message");
        assertEquals(36, rawCode.getScore());
    }

    @Test
    public void testIsSameQRCode() {
        RawQRCode code1 = new RawQRCode("this is a message");
        RawQRCode code2 = new RawQRCode("this is a message");
        RawQRCode code3 = new RawQRCode("this is another message");
        assertTrue(code1.isSameRawQRCode(code2));
        assertFalse(code1.isSameRawQRCode(code3));
    }

    /**
     * Test converting a digit in 0-15 to a hex character, and as well as converting back
     * @param digit
     * @param hexChar
     */
    public static void testTwoWaysHexConversion(byte digit, byte hexChar) {
        assertEquals(hexChar, RawQRCode.getHexCharacterOfHexDigit(digit));
        assertEquals(digit, RawQRCode.getHexDigitOfHexCharacter(hexChar));
    }

    @Test
    public void testHexDigitConversion() {
        for (int i = 0; i < 10; i++)
            testTwoWaysHexConversion((byte) i, (byte) ('0' + i));
        for (int i = 0; i < 6; i++)
            testTwoWaysHexConversion((byte) (10 + i), (byte) ('a' + i));
        assertThrows(IllegalArgumentException.class, () -> {
            RawQRCode.getHexCharacterOfHexDigit((byte) -1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            RawQRCode.getHexCharacterOfHexDigit((byte) 16);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            RawQRCode.getHexDigitOfHexCharacter((byte) 'g');
        });
        assertThrows(IllegalArgumentException.class, () -> {
            RawQRCode.getHexDigitOfHexCharacter((byte) ' ');
        });
    }
}
