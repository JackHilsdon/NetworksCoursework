package com.cmp.voip.SecurityLayer;

public interface ISecurityLayer {
    byte[] encryptAudio(byte[] audioData);
    byte[] decryptAudio(byte[] encryptedAudio);
}
