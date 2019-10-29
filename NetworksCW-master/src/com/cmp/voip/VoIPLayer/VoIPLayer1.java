package com.cmp.voip.VoIPLayer;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import com.cmp.voip.AudioLayer.AudioHandler;
import com.cmp.voip.SecurityLayer.ISecurityLayer;
import com.cmp.voip.TransportLayer.TransportHandler;
import com.cmp.voip.TransportLayer.BasicTransportPacket;
import com.cmp.voip.VoIPLayer.Compensation.RecieverCompensation;

import java.io.IOException;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.sun.deploy.trace.Trace.flush;

public class VoIPLayer1 implements IVoIPLayer {

    private int packetCount = 0;
    private int receievedPackets = 0;

    public class TimedExit {
        Timer timer = new Timer();
        TimerTask exitApp = new TimerTask() {
            public void run() {
                flush();
                System.out.println("Total meant to received count: " + packetCount);
                System.out.println("Actual received count: " + receievedPackets);
                System.exit(0);
            }
        };

        public TimedExit() {
            timer.schedule(exitApp, new Date(System.currentTimeMillis() + 30 * 1000));
        }
    }

    protected DatagramSocket datagramSocket = null;
    private final ISecurityLayer securityLayer;

    public VoIPLayer1(ISecurityLayer securityLayer) {
        this.securityLayer = securityLayer;
    }

    @Override
    public void receiveAudio(){
        new TimedExit();
        datagramSocket = TransportHandler.getDatagramSocket1(false);
        boolean running = true;
        AudioPlayer player = AudioHandler.getAudioPlayer();

        byte[] previousAudio = null;
        while (running){

            try {
                BasicTransportPacket packet = BasicTransportPacket.readPacket(datagramSocket, securityLayer);
                receievedPackets++;
                player.playBlock(packet.audioData);
            } catch (SecurityException e) {
                continue;
            } catch (IOException e){
                System.out.println("ERROR: VoIPLayer1: IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        datagramSocket.close();
    }

    @Override
    public void sendAudio(){
        InetAddress clientIP = TransportHandler.getDestination();
        System.out.println(clientIP.toString());
        datagramSocket = TransportHandler.getDatagramSocket1(true);

        AudioRecorder recorder = AudioHandler.getAudioRecorder();

        boolean running = true;

        while (running){

            try{
                System.out.println(packetCount);
                packetCount++;
                BasicTransportPacket.createPacket(recorder.getBlock(), securityLayer)
                        .send(datagramSocket, clientIP, TransportHandler.PORT);

            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        datagramSocket.close();


    }
}
