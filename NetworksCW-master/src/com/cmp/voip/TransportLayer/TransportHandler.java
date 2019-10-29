package com.cmp.voip.TransportLayer;

import com.cmp.voip.MainDemo;
import uk.ac.uea.cmp.voip.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TransportHandler {

    public static final int PORT = MainDemo.PORT;
    public static final String DESTINATION = MainDemo.DESTINATION;

    public static InetAddress getDestination(){
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName(DESTINATION);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(1);
        }
        return clientIP;
    }

    private static DatagramSocket createSocket(Class<? extends DatagramSocket> clazz, boolean sending) {
        DatagramSocket datagramSocket = null;
        try {
            if(sending)
                datagramSocket = clazz.getConstructor().newInstance();
            else
                datagramSocket = clazz.getConstructor(Integer.TYPE).newInstance(PORT);
        } catch (Exception e) {
            System.out.println("Could not open UDP socket.");
            e.printStackTrace();
            System.exit(1);
        }

        return datagramSocket;
    }

    public static DatagramSocket getDatagramSocket1(boolean sending) {
        return createSocket(DatagramSocket3.class, sending);
    }

    public static DatagramSocket getDatagramSocket2(boolean sending) {
        return createSocket(DatagramSocket2.class, sending);
    }

    public static DatagramSocket getDatagramSocket3(boolean sending) {
        return createSocket(DatagramSocket3.class, sending);
    }

    public static DatagramSocket getDatagramSocket4(boolean sending) {
        return createSocket(DatagramSocket4.class, sending);
    }
}
