package com.cmp.voip.VoIPLayer.Compensation;

import java.util.ArrayList;
import java.util.Arrays;

public class RecieverCompensation {

    public static byte[][] applyRepetition(byte[][] block) {
        for (int i = 0; i < block.length; i++) {
            if (block[i] == null) {
                //If first element in array
                if (i == 0) {
                    //Finds next non null element
                    for (int j = i + 1; j < block.length; j++) {
                        if (block[j] != null) {
                            block[i] = block[j];
                            break;
                        }
                    }
                } else {
                    //Checks isnt the last block
                    if (i + 1 == block.length) {
                        block[i] = block[i - 1];
                    } else {
                        //Find the next non null as may have two or more a row
                        for (int j = i + 1; j < block.length; j++) {
                            if (block[j] != null) {
                                block[i] = block[j];
                                break;
                            }
                        }
                    }
                }
            }
        }
        return block;
    }

    public static int missing = 0;

    public static int getMissing(){
        return missing;
    }

    public static byte[][] applySplicing(byte[][] block) {
        int nullCount = 0;
        for (int i = 0; i < block.length; i++) {
            if (block[i] == null) {
                nullCount++;
            }
        }
        byte[][] splicedBlock = new byte[block.length - nullCount][];
        int j = 0;
        for (int i = 0; i < block.length; i++) {
            if (block[i] != null) {
                splicedBlock[j] = block[i];
                j++;
            }
        }

        return splicedBlock;
    }

    public static byte[][] applyInterpolation(byte[][] block) {
        for (int i = 0; i < block.length; i++) {
            if (block[i] == null) {
                //If first element in array
                if (i == 0) {
                    //Finds next non null element
                    for (int j = i + 1; j < block.length; j++) {
                        if (block[j] != null) {
                            block[i] = block[j];
                            break;
                        }
                    }
                } else {
                    //Checks isnt the last block
                    if (i + 1 == block.length) {
                        block[i] = block[i - 1];
                    } else {
                        //Find the next non null as may have two or more a row
                        for (int j = i + 1; j < block.length; j++) {
                            if (block[j] != null) {
                                byte[] newPacket = new byte[550];
                                //Gets half a packet from previous packet
                                for (int k = 0; k < (block[i - 1].length) / 2; k++) {
                                    newPacket[k] = block[i - 1][k];
                                }
                                //Gets half a packet from next
                                for (int k = (block[i - 1].length) / 2, l = 0; k < (block[j].length) / 2; k++, l++) {
                                    newPacket[k] = block[j][l];
                                }
                                block[i] = newPacket;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return block;
    }
}

