package com.cmp.voip.VoIPLayer;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import com.cmp.voip.AudioLayer.AudioHandler;
import com.cmp.voip.MainDemo;
import com.cmp.voip.SecurityLayer.ISecurityLayer;
import com.cmp.voip.TransportLayer.InterleavedTransport;
import com.cmp.voip.TransportLayer.RTPHeader;
import com.cmp.voip.TransportLayer.TransportHandler;
import com.cmp.voip.VoIPLayer.Compensation.RecieverCompensation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.sun.deploy.trace.Trace.flush;

public class VoIPLayer2 implements IVoIPLayer {
    public boolean enableRepetition = true;
    public boolean enableInterpolation = false;
    public boolean enableSplicing = false;

    public class TimedExit {
        Timer timer = new Timer();
        TimerTask exitApp = new TimerTask() {
            public void run() {
                flush();
                System.out.println("Total meant to received count: " + packetCount);
                System.out.println("Actual received count: " + receievedPackets);
                System.out.println("Missing count: " + RecieverCompensation.getMissing());
                System.exit(0);
            }
        };

        public TimedExit() {
            timer.schedule(exitApp, new Date(System.currentTimeMillis() + 30 * 1000));
        }
    }


    private int receievedPackets = 0;

    protected DatagramSocket datagramSocket = null;
    private final ISecurityLayer securityLayer;
//    private final int INTERLEAVER_BLOCK_SIZE = 4;

    public VoIPLayer2(ISecurityLayer securityLayer) {
        this.securityLayer = securityLayer;
    }

    private static int packetCount = 0;
    @Override
    public void receiveAudio(){
//        new TimedExit();
        datagramSocket = TransportHandler.getDatagramSocket2(false);
        AudioPlayer player = AudioHandler.getAudioPlayer();

        byte[][] block = new byte[MainDemo.INTERLEAVER_BLOCK_SIZE*MainDemo.INTERLEAVER_BLOCK_SIZE][];
        long previousBlockId = 1;
        long currBlockId = 0;
        boolean sameBlock = true;
        long lastAddedBlock = 0;
        while(sameBlock) {
            try {
                //Creates buffer to store packet of the size the packet will be
                byte[] buffer = new byte[InterleavedTransport.PACKET_SIZE];
                //Creates a packet to store the data in
                DatagramPacket packet = new DatagramPacket(buffer, 0, InterleavedTransport.PACKET_SIZE);
                //If the packet is too big or small, skip.
                if (packet.getLength() != InterleavedTransport.PACKET_SIZE)
                    continue;
                //Receives the audio in the packet created
                datagramSocket.receive(packet);
                //Gets the header
                RTPHeader rtpheader = RTPHeader.createFromHeader(buffer);

                //New block is being received so send previous
                currBlockId = rtpheader.getLongBlockNumber();
                //If different block send previous
                if (currBlockId != previousBlockId) {
                    if (((previousBlockId > 64 && currBlockId < 64) || previousBlockId < currBlockId)) {
                        if (enableRepetition) {
                            block = RecieverCompensation.applyRepetition(block);
                        }
                        if (enableInterpolation) {
                            block = RecieverCompensation.applyInterpolation(block);
                        }
                        if (enableSplicing) {
                            block = RecieverCompensation.applySplicing(block);
                        }
                        byte[] playBack = new byte[512];
                        byte[] curr = new byte[InterleavedTransport.PACKET_SIZE];
                        //System.out.println("BLOCK: " + currBlockId);
                        boolean[] trueArr = new boolean[MainDemo.INTERLEAVER_BLOCK_SIZE * MainDemo.INTERLEAVER_BLOCK_SIZE];
                        for (int i = 0; i < block.length; i++) {
                            playBack = new byte[512];
                            curr = block[i];
                            if (block[i] != null) {
                                trueArr[i] = false;
                                System.arraycopy(curr, 0, playBack, 0, 512);
                                player.playBlock(playBack);
                                //System.out.println("End: " + System.currentTimeMillis());
                            } else {
                                trueArr[i] = true;
//                            System.out.println("Missing " + (packetCount-i ));
                            }
                            //System.out.println(trueArr[i]);
                        }
                        lastAddedBlock = previousBlockId;
                        block = new byte[MainDemo.INTERLEAVER_BLOCK_SIZE * MainDemo.INTERLEAVER_BLOCK_SIZE][];
                    }
                }
                //System.out.println(rtpheader.getLongSequenceNumber());
                receievedPackets++;
                int position = (int) rtpheader.getLongSequenceNumber() - 1;
                //System.out.println(position);
                block[position] = securityLayer.decryptAudio(buffer);
                if(lastAddedBlock < currBlockId) { previousBlockId = currBlockId;}
            } catch (IOException e) {
                System.out.println("ERROR: VoIPLayer1: IO error occured!");
                e.printStackTrace();
            }
        }
        datagramSocket.close();
    }

    private int test = 0;
    @Override
    public void sendAudio(){

        //Gets the IP for the machine to receive
        InetAddress receiverIP = TransportHandler.getDestination();

        //Creates a new audio recorder
        AudioRecorder recorder = AudioHandler.getAudioRecorder();

        datagramSocket = TransportHandler.getDatagramSocket2(true);

        //Sets sending to true until condition met
        boolean running = true;
        while (running) {
            try {
                byte[][] block = InterleavedTransport.createBlock(recorder, securityLayer);
                InterleavedTransport.sendBlock(block, receiverIP, datagramSocket);
                //System.out.println("Start: " + System.currentTimeMillis());

            } catch (IOException e) {
                System.out.println("ERROR: AudioSender: IO error occurred!");
                e.printStackTrace();
            }
        }
        //Close the socket
        datagramSocket.close();
    }
}
