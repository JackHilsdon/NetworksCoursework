package com.cmp.voip.SecurityLayer;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class BasicSecurityLayer implements ISecurityLayer {

    private static final int SHIFT_COUNT = 117;
    private Integer privateKey = null;

    public BasicSecurityLayer() {
        Scanner scanner = new Scanner(System.in);
        while (privateKey == null) {
            System.out.print("Private Key: ");
            String input = scanner.nextLine();
            try {
                privateKey = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, try again.");
            }
        }
    }

    public byte[] encryptAudio(byte[] audioData) {
        byte[] encrypted = new byte[512];

        ByteBuffer encryptedBuffer = ByteBuffer.wrap(encrypted);
        ByteBuffer audioBuffer = ByteBuffer.wrap(audioData);

        for(int i = 0; i < audioData.length / 4; i++){
            encryptedBuffer.putInt(audioBuffer.getInt() ^ privateKey);
        }

        return shiftArray(encrypted, SHIFT_COUNT);
    }

    public byte[] decryptAudio(byte[] encryptedAudio) {
        byte[] audioData = new byte[512];

        ByteBuffer audioBuffer = ByteBuffer.wrap(audioData);
        ByteBuffer encryptedBuffer = ByteBuffer.wrap(encryptedAudio);

        for(int i = 0; i < audioData.length / 4; i++){
            audioBuffer.putInt(encryptedBuffer.getInt() ^ privateKey);
        }

        return shiftArray(audioData, -SHIFT_COUNT);
    }

    private byte[] shiftArray(byte[] unshifted, int shiftAmount) {
        byte[] shifted = new byte[unshifted.length];
        ByteBuffer shiftedBuffer = ByteBuffer.wrap(shifted);

        if(shiftAmount < 0) {
            shiftAmount = unshifted.length + shiftAmount;
        }

        shiftedBuffer.put(unshifted, 0, shiftAmount);
        shiftedBuffer.put(unshifted, shiftAmount, unshifted.length - shiftAmount);


        return shifted;
    }
}
