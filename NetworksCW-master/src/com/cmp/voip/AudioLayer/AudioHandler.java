package com.cmp.voip.AudioLayer;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;

public class AudioHandler {

    public static AudioPlayer getAudioPlayer(){
        try {
            return new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("Failed to create audio player..");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static AudioRecorder getAudioRecorder(){
        try {
            return new AudioRecorder();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

}
