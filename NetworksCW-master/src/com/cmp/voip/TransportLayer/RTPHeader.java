package com.cmp.voip.TransportLayer;

import com.cmp.voip.MainDemo;

import java.math.BigInteger;
import java.util.BitSet;

public class RTPHeader {

    private static int maxSize = (MainDemo.INTERLEAVER_BLOCK_SIZE*MainDemo.INTERLEAVER_BLOCK_SIZE);

    private static long identifierRanVal = 21391230;

    private BitSet randomNum = new BitSet();

    //Version field which indicates the version of the protocol. Current version is 2
    private static BitSet version = new BitSet(2);

    //Counter tracking how many packets have been made
    private static int sequenceCount = 1;

    //Used to track how many blocks made
    private static int blockCount = 1;

    //Padding.  If the padding bit is set, the packet contains one or more additional padding octets at the end which
    // are not part of the payload.
    private BitSet padding = new BitSet(1);

    //If the extension bit is set, the fixed header is followed by exactly one header extension
    private BitSet extension = new BitSet(1);

    //How many contributing sources are present (from 0-15)
    private BitSet cSRCcount = new BitSet(4);

    //Marker is used to mark the start of a word in an audio channel
    private BitSet marker = new BitSet(1);

    //Payload type tells which encoding algorithm has been used
    private BitSet payloadType = new BitSet(7);

    //Sequence number is the counter thant is incremented on each RTPHeader packet sent
    private BitSet sequenceNumber = new BitSet(8);

    //Timestamp is produce by the streams source to note when the first sample in the packet was made. This is u
    // sed by the receiver to play back the received samples at appropriate time and interval reducing jitter
    private BitSet timeStamp = new BitSet(32);

    //SSRC tells which stream the packet belongs to.
    private BitSet synchronizationSourceIdentifier = new BitSet(32);

    static {
        version = BitSet.valueOf(new long[]{2});;
    }

    /*
    Description: Default consturctor
     */
    public RTPHeader(){
        sequenceNumber = BitSet.valueOf(new long[]{sequenceCount});
        synchronizationSourceIdentifier = BitSet.valueOf(new long[]{blockCount});
        randomNum = BitSet.valueOf(new long[]{identifierRanVal});
        if(sequenceCount >= maxSize){
            sequenceCount = 1;
        }
        else {
            sequenceCount++;
        }
    }


    /*
    Description: This is the main constructor that will be used when sending packets as only 3 of the fields are really
    relevant.
    @param: BitSet - payloadType will define the which encoding algorithm for the payload has been used
    @param: BitSet - timeStamp will define the time from source to recording packet
    @param: BitSet - synchronizationSourceIdentifier is the block that this packet will be placed into
     */
    public RTPHeader(long timeStamp) {
        this.timeStamp = BitSet.valueOf(new long[]{timeStamp});
        synchronizationSourceIdentifier = BitSet.valueOf(new long[]{blockCount});
        randomNum = BitSet.valueOf(new long[]{identifierRanVal});
        sequenceNumber = BitSet.valueOf(new long[]{sequenceCount});
        if(sequenceCount >= maxSize){
            sequenceCount = 1;
        }
        else {
            sequenceCount++;
        }
    }

    public RTPHeader(long padding, long extension, long cSRCcount, long marker, long payloadType, long sequenceNumber,
                     long timeStamp, long synchronizationSourceIdentifier, long randomNum) {
        this.padding = convertLongToBit(padding);
        this.extension = convertLongToBit(extension);
        this.cSRCcount = convertLongToBit(cSRCcount);
        this.marker = convertLongToBit(marker);
        this.payloadType = convertLongToBit(payloadType);
        this.sequenceNumber = convertLongToBit(sequenceNumber);
        this.timeStamp = convertLongToBit(timeStamp);
        this.synchronizationSourceIdentifier = convertLongToBit(synchronizationSourceIdentifier);
        this.randomNum = convertLongToBit(randomNum);
    }

    public byte[][] constructRTPHeader() {

        byte[][] header = new byte[16][];
        header[0] = new BigInteger(String.valueOf(convertBitToLong(version))).toByteArray(); //1 byte
        header[1] = new BigInteger(String.valueOf(convertBitToLong(padding))).toByteArray(); //1 byte
        header[2] = new BigInteger(String.valueOf(convertBitToLong(extension))).toByteArray(); //1 byte
        header[3] = new BigInteger(String.valueOf(convertBitToLong(cSRCcount))).toByteArray(); //1 byte
        header[4] = new BigInteger(String.valueOf(convertBitToLong(marker))).toByteArray(); // 1 byte
        header[5] = new BigInteger(String.valueOf(convertBitToLong(payloadType))).toByteArray(); //1 byte
        header[6] = new BigInteger(String.valueOf(convertBitToLong(sequenceNumber))).toByteArray(); //1 byte
        header[7] = new BigInteger(String.valueOf(convertBitToLong(timeStamp))).toByteArray(); //6 byte
        header[8] = new BigInteger(String.valueOf(convertBitToLong(synchronizationSourceIdentifier))).toByteArray(); //1 byte
        header[9] = new BigInteger(String.valueOf(convertBitToLong(randomNum))).toByteArray(); //4 bytes
        return header;
    }

    private BitSet convertLongToBit(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    //Code from: https://stackoverflow.com/questions/2473597/bitset-to-and-from-integer-long
    public static long convertBitToLong(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    public long getLongSequenceNumber(){
        return RTPHeader.convertBitToLong(sequenceNumber);
    }

    public long getLongRandomNum(){
        return RTPHeader.convertBitToLong(randomNum);
    }

    public long getLongBlockNumber(){
        return RTPHeader.convertBitToLong(synchronizationSourceIdentifier);
    }

    public static long getIdentifierRanVal() {
        return identifierRanVal;
    }

    public static RTPHeader createFromHeader(byte[] header){
        long[] headerVals = new long[10];
        int positionTrack = 512;
        int[] byteSize = {1,1,1,1,1,1,1,6,1,4};
        for(int i =0; i < byteSize.length; i++){
            byte[] curr = new byte[byteSize[i]];
            for(int j =0; j < byteSize[i]; j++){
                positionTrack++;
                curr[j] = header[positionTrack];
            }
            headerVals[i] = (new BigInteger(curr).longValue());
        }
        //System.out.println("PacketId: " + headerVals[6]);
        return new RTPHeader(headerVals[1],headerVals[2],headerVals[3],headerVals[4],headerVals[5],headerVals[6],headerVals[7],
                headerVals[8], headerVals[9]);
    }

    public static void setBlockCount(){
        if(blockCount >= 127){
            blockCount = 1;
        }
        else {
            blockCount++;
        }
    }

    public long getTimeStamp() {
        return RTPHeader.convertBitToLong(timeStamp);
    }

    public BitSet getRandomNum() {
        return randomNum;
    }
}
