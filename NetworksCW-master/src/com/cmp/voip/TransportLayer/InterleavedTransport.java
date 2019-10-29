package com.cmp.voip.TransportLayer;

import CMPC3M06.AudioRecorder;
import com.cmp.voip.MainDemo;
import com.cmp.voip.SecurityLayer.ISecurityLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Arrays;

public class InterleavedTransport {

    private static final int SIZE = MainDemo.INTERLEAVER_BLOCK_SIZE;
    private static final int PORT = 55555;
    public static final int PACKET_SIZE = MainDemo.PACKET_SIZE;

    public static void sendBlock(byte[][] block, InetAddress receiverIP, DatagramSocket sending_socket) throws IOException {
        for(int i = 0; i<SIZE*SIZE;i++){
            //System.out.println(Arrays.toString(block[i]));
            DatagramPacket packet = new DatagramPacket(block[i], PACKET_SIZE, receiverIP, PORT);
            //Send it to the destination
            sending_socket.send(packet);
        }

    }

    public static byte[][] createBlock(AudioRecorder recorder, ISecurityLayer securityLayer) throws IOException {
        int blockSize = SIZE*SIZE;
        byte[][] block = new byte[blockSize][];
        //While still need to fill packets in block
        for(int i = 0; i<blockSize; i++){
            byte[] sendPacket = new byte[PACKET_SIZE];
            sendPacket = addHeader(sendPacket);
            byte[] buffer = recorder.getBlock();
            buffer = securityLayer.encryptAudio(buffer);
            System.arraycopy(buffer, 0, sendPacket,0,512);
            block[i] = sendPacket;
        }
        block = transposeBlock(block);
        RTPHeader.setBlockCount();
        //outputBlock(block);
        block = rotateBlock(block);
        return block;
    }

    //This method is used to output a block
    public static void outputBlock(byte[][] block){
        System.out.println(block.length);
        int blockSize = SIZE*SIZE;
        System.out.println("\n=================\n");
        for(int i = 0; i<blockSize; i++){
            System.out.println();
            System.out.print(i+ ": " + Arrays.toString(block[i]) +" | ");
        }

    }

    //This method will turn the rows to columns in the matrix (transpose)
    public static byte[][] transposeBlock(byte[][] block){
        byte[][] newBlock = new byte[SIZE*SIZE][];
        int tracker = 0;
        for(int i = 0; i < SIZE; i++){
            for(int j =0; j<SIZE; j++){
                newBlock[SIZE * j+i] = block[tracker];
                tracker++;
            }
        }
        return newBlock;
    }

    public static byte[][] rotateBlock(byte[][] arr){
        for (int i = 0; i < arr.length; i++)
            for (int j = 0, k = arr.length - 1;
                 j < k; j++, k--) {
                int temp = arr[j][i];
                arr[j][i] = arr[k][i];
                arr[k][i] = (byte)temp;
            }
        return arr;
    }

    public static byte[] addHeader(byte[] sendPacket){
        Timestamp testTime = new Timestamp(System.currentTimeMillis());
        RTPHeader header = new RTPHeader(testTime.getTime());
        byte[][] headerArr = header.constructRTPHeader();
        int positionTrack = 512;
        int[] byteSize = {1,1,1,1,1,1,1,6,1,4};
        for(int i =0; i < byteSize.length; i++){
            for(int j =0; j < byteSize[i]; j++){
                positionTrack++;
                sendPacket[positionTrack] = headerArr[i][j];
            }
        }
        return sendPacket;
    }

}
