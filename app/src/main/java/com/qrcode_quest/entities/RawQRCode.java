package com.qrcode_quest.entities;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a QR code; a RawQRCode is initialized from a text representation of the QR code. It
 * can be used to 1) reconstruct the original QR code in image format, as well a 2) providing the
 * hash information and 3) the score of the QR code
 * @author tianming
 * @version 1.0
 */
public class RawQRCode {
    private final String qr;

    // is ASCII the right choice?
    static private final Charset charset = StandardCharsets.US_ASCII; // encode method for String to byte[]

    /**
     * Create a RawQRCode from a text representation of QR code
     * @param qr text of the QR code
     */
    public RawQRCode(String qr) { this.qr = qr; }

    public String getQR() {
        return qr;
    }

    static public byte[] getHexRepresentationOfByteArray(byte[] input) {
        int outputLength = input.length * 2;  // length will double in hexadecimal format
        int finalByte = input[input.length - 1] & 0xFF;
        byte finalByteUpper = (byte) (finalByte / 16);
        byte finalByteLower = (byte) (finalByte - finalByteUpper * 16);
        if (finalByteLower == 0) {
            outputLength -= 1;
        }

        byte[] result = new byte[outputLength];  // zero initialize
        for (int i = 0; i < input.length; i++) {
            int inputDigit = input[i] & 0xFF;  // guarantees non-negative
            byte upper = (byte) (inputDigit / 16);
            byte lower = (byte) (inputDigit - upper * 16);

            result[i * 2] = upper;  // cutoff only happens at right, so only the upper part is needed
            if (lower != 0) {
                result[i * 2 + 1] = (byte) (inputDigit - upper * 16);
            }
        }
        return result;
    }

    /**
     * calculate the score for a hex representation of hash depending on repeating digits in the bytes
     * note this function will cause overflow in case the score is too large
     * @param hash a hex representation of hash that the score will be calculated from
     * @return the score calculated
     */
    static public int getScoreFromHash(byte[] hash) {
        int score = 0;
        int i = 0;
        while (i < hash.length) {
            byte digit = hash[i];
            int baseScore = digit;
            if (digit == 0)
                baseScore = 20;

            // count continuous string of same digits as combo
            int comboScore = 1;
            int sequenceCount = 1;
            while (i + sequenceCount < hash.length && hash[i + sequenceCount] == digit) {
                comboScore *= baseScore;
                sequenceCount += 1;
            }

            // only add score when there are more than one consecutive digits;
            // with the exception of one zero worth 1 point
            if (!(sequenceCount == 1 && digit != 0))
                score += comboScore;
            i += sequenceCount;
        }
        return score;
    }

    /**
     * translate a hex byte array to readable byte array
     * meaning the original value range from 0 to 15 will be mapped to '0' to '9' and 'a' to 'e'
     * @param qrHash the hash to translate
     * @return a new byte array of same length, but in readable format
     */
    static public byte[] translateHashToReadable(byte[] qrHash) {
        byte[] result = qrHash.clone();
        for(int i = 0; i < result.length; i++) {
            byte digit = result[i];
            if (digit >= 0 && digit < 10) {
                result[i] = (byte) ('0' + digit);
            } else if (digit >= 10 && digit < 16) {
                result[i] = (byte) ('a' + (digit - 10));
            } else {
                throw new IllegalArgumentException(
                        "hex representation of qrHash contains byte value out of range [0, 16) " +
                        "at index " + Integer.toString(i) + " with value " + Byte.toString(digit));
            }
        }
        return result;
    }

    /**
     * translate a readable hex byte array to hex byte array with each entry less than 16
     * meaning the original values '0' to '9' and 'a' to 'e' will be mapped to 0-15
     * @param bytes the hash to translate
     * @return a new byte array of same length, but with each entry less than 16
     */
    static public byte[] translateReadableToHash(byte[] bytes) {
        byte[] result = bytes.clone();
        for(int i = 0; i < result.length; i++) {
            byte digit = result[i];
            if (digit >= '0' && digit <= '9') {
                result[i] = (byte) (digit - '0');
            } else if (digit >= 'a' && digit <= 'f') {
                result[i] = (byte) (10 + (digit - 'a'));
            } else {
                throw new IllegalArgumentException(
                        "input is not a readable hexadecimal byte array " +
                        "at index " + Integer.toString(i) + " with value " + Byte.toString(digit));
            }
        }
        return result;
    }

    /**
     * Gives SHA-256 hash of the QR code text, see <url href="https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html">MessageDigest</url>
     * @return the SHA-256 hash value in hexadecimal format, with each digit a byte value less than 16
     */
    public byte[] getQRHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");  // use SHA-256 algorithm
        md.update(qr.getBytes(charset));  // encode to byte[] then apply SHA-256 hash
        return getHexRepresentationOfByteArray(md.digest());  // write in hex format
    }

    /**
     * calculate the score of the QR code
     * @return the score calculated
     */
    public int getScore() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return getScoreFromHash(getQRHash());
    }

    /**
     * compare if the two RawQRCode represents the same qr code
     * @param other the other raw qr code to compare
     * @return true if the two are the same; otherwise return false
     */
    public boolean isSameRawQRCode(RawQRCode other) {
        return getQR().equals(other.getQR());
    }
}
