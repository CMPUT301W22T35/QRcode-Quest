package com.qrcode_quest.entities;

import androidx.annotation.NonNull;

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
    @NonNull private final String qr;

    static private final Charset charset = StandardCharsets.US_ASCII; // encode method between String&byte[]

    /**
     * Create a RawQRCode from a text representation of QR code
     * @param qr text of the QR code
     */
    public RawQRCode(@NonNull String qr) { this.qr = qr; }

    /**
     * get the qr hash of this qr code
     * @return a qr hash string
     */
    public @NonNull String getQR() {
        return qr;
    }

    /**
     * Turns value of a hex digit between 0 and 15 into its readable byte representation
     * @param input An input value in the range between 0-15
     * @return the result value is in range '0' to '9' or 'a' to 'f'
     */
    static public byte getHexCharacterOfHexDigit(byte input) throws IllegalArgumentException {
        if (input >= 0 && input < 10) {
            return (byte) ('0' + input);
        } else if (input >= 10 && input < 16) {
            return (byte) ('a' + (input - 10));
        } else {
            throw new IllegalArgumentException(
                    "Input byte value " + Integer.toString(input) + " out of range [0, 16) and is not a hex digit!");
        }
    }

    /**
     * Turns a hex character to its corresponding numerical value
     * @param digit a hex character that is a byte in either '0' to '9' or 'a' to 'f'
     * @return the interpreted numerical hex value between 0 and 15
     */
    static public byte getHexDigitOfHexCharacter(byte digit) throws IllegalArgumentException {
        if (digit >= '0' && digit <= '9') {
            return (byte) (digit - '0');
        } else if (digit >= 'a' && digit <= 'f') {
            return (byte) (10 + (digit - 'a'));
        } else {
            throw new IllegalArgumentException(
                    "Input byte character " + digit + " is not in '0' to '9' or 'a' to 'f'!");
        }
    }

    /**
     * turns an array of input bytes to a byte array representing hex digits with the same literal
     * value; the resulting array will be about 2 times as long, but in case the most significant
     * digit is less than 16 it will have one digit less
     * @param input the input byte array
     * @return the hex representation of the input byte array
     */
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

        // each entry the byte array is currently within range 0-15, but we want them to be readable
        for (int i = 0; i < result.length; i++) {
            result[i] = getHexCharacterOfHexDigit(result[i]);
        }

        return result;
    }

    /**
     * calculate the score for a hex representation of hash depending on repeating digits in the bytes
     * note this function will cause overflow in case the score is too large
     * @param hash a hex representation of hash that the score will be calculated from
     * @return the score calculated
     */
    static public int getScoreFromHash(String hash) {
        byte[] bytes = hash.getBytes(charset);
        int score = 0;
        int i = 0;
        while (i < bytes.length) {
            byte digit = getHexDigitOfHexCharacter(bytes[i]);
            int baseScore = digit;
            if (digit == 0)
                baseScore = 20;

            // count continuous string of same digits as combo
            int comboScore = 1;
            int sequenceCount = 1;
            while (i + sequenceCount < bytes.length &&
                    getHexDigitOfHexCharacter(bytes[i + sequenceCount]) == digit) {
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
     * Gives SHA-256 hash of the QR code text, see <url href="https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html">MessageDigest</url>
     * @return the SHA-256 hash value in hexadecimal format, with each digit a byte value less than 16
     */
    public String getQRHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");  // use SHA-256 algorithm
        md.update(qr.getBytes(charset));  // encode to byte[] then apply SHA-256 hash
        return new String(getHexRepresentationOfByteArray(md.digest()), charset);  // write in hex format
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
