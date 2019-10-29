package com.cmp.voip.TransportLayer;

import com.cmp.voip.SecurityLayer.ISecurityLayer;
import com.cmp.voip.SecurityLayer.NoSecurityLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BasicTransportPacket {

    public static final int SIZE = 576;

    public final byte[] audioData;
    private final byte[] checksum;

    protected BasicTransportPacket(byte[] audioData, byte[] checksum) {
        this.audioData = audioData;
        this.checksum = checksum;
    }

    public static BasicTransportPacket createPacket(byte[] audioData, ISecurityLayer securityLayer) {
        byte[] checksum = checksum(audioData);
        audioData = securityLayer.encryptAudio(audioData);

        return new BasicTransportPacket(audioData, checksum);
    }

    public static BasicTransportPacket readPacket(DatagramSocket socket, ISecurityLayer securityLayer) throws SecurityException, IOException {
        ByteBuffer byteBuffer = retrievePacketBuffer(socket);
        byte[] encryptedAudio = new byte[512];
        byteBuffer.get(encryptedAudio);
        byte[] receivedChecksum = new byte[64];
        byteBuffer.get(receivedChecksum);

        byte[] audioData = securityLayer.decryptAudio(encryptedAudio);
        byte[] actualChecksum = checksum(audioData);

        // If NoSecurityLayer is in use ignore checksum issues.
        // This will discard CRC, but it required for encryption demo for assessment.
        if(!(securityLayer instanceof NoSecurityLayer) && !Arrays.equals(receivedChecksum, actualChecksum)){
            throw new SecurityException("Invalid packet checksum.");
        }

        return new BasicTransportPacket(audioData, receivedChecksum);
    }

    protected static ByteBuffer retrievePacketBuffer(DatagramSocket socket) throws SecurityException, IOException {
        byte[] buffer = new byte[SIZE];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, SIZE);
        socket.receive(datagramPacket);

        if(datagramPacket.getLength() != SIZE){
            throw new SecurityException("Invalid packet length.");
        }

        return ByteBuffer.wrap(buffer);
    }

    public void send(DatagramSocket socket, InetAddress ip, int port) throws IOException {
        byte[] buffer = getBytes();

        //Make a DatagramPacket from it, with client address and port number
        DatagramPacket packet = new DatagramPacket(buffer, SIZE, ip, port);

        //Send it
        socket.send(packet);
    }

    protected static byte[] checksum(byte[] audioData) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-512");
            return mDigest.digest(audioData);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Could not create SHA-512 checksum.");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    protected byte[] getBytes() {
        byte[] bytes = new byte[SIZE];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.put(audioData);
        byteBuffer.put(checksum);
        return bytes;
    }
}