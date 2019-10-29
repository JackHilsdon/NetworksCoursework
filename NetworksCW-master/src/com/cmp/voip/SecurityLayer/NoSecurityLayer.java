package com.cmp.voip.SecurityLayer;

public class NoSecurityLayer implements ISecurityLayer{
    @Override
    public byte[] encryptAudio(byte[] audioData) {
        return audioData;
    }

    @Override
    public byte[] decryptAudio(byte[] encryptedAudio) {
        return encryptedAudio;
    }
}
