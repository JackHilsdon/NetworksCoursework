package com.cmp.voip.TransportLayer;

import com.cmp.voip.SecurityLayer.ISecurityLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

public class NumberedVoIPPacket extends BasicTransportPacket {
    public static final int SIZE = BasicTransportPacket.SIZE + 4;
    private static int lastPacketNumber = 0;

    public final int number;

    protected NumberedVoIPPacket(byte[] audioData, byte[] checksum, int number) {
        super(audioData, checksum);
        this.number = number;
    }

    public static NumberedVoIPPacket createPacket(byte[] audioData, ISecurityLayer securityLayer) {
        byte[] checksum = checksum(audioData);

        audioData = securityLayer != null
                ? securityLayer.encryptAudio(audioData)
                : audioData;

        lastPacketNumber++;

        return new NumberedVoIPPacket(audioData, checksum, lastPacketNumber);
    }

    public static NumberedVoIPPacket readPacket(DatagramSocket socket, ISecurityLayer securityLayer) throws SecurityException, IOException {
        ByteBuffer byteBuffer = retrievePacketBuffer(socket);
        byte[] encryptedAudio = new byte[512];
        byteBuffer.get(encryptedAudio);
        byte[] receivedChecksum = new byte[64];
        byteBuffer.get(receivedChecksum);
        int number = byteBuffer.getInt();

        byte[] audioData = securityLayer != null
                ? securityLayer.decryptAudio(encryptedAudio)
                : encryptedAudio;

        byte[] actualChecksum = checksum(audioData);

        if(!Arrays.equals(receivedChecksum, actualChecksum)){
            throw new SecurityException("Invalid packet.");
        }

        return new NumberedVoIPPacket(audioData, receivedChecksum, number);
    }

    public void send(DatagramSocket socket, InetAddress ip, int port) throws IOException {
        byte[] buffer = getBytes();

        //Make a DatagramPacket from it, with client address and port number
        DatagramPacket packet = new DatagramPacket(buffer, SIZE, ip, port);

        //Send it
        socket.send(packet);
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

    protected byte[] getBytes() {
        byte[] bytes = new byte[SIZE];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.put(super.getBytes());
        byteBuffer.putInt(number);
        return bytes;
    }

    public static class NumberComparator implements Comparator<NumberedVoIPPacket> {
        @Override
        public int compare(NumberedVoIPPacket o1, NumberedVoIPPacket o2) {
            return ((Integer)o1.number).compareTo(o2.number);
        }
    }
}