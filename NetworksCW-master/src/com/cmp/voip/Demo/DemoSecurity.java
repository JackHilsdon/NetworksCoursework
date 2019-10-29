package com.cmp.voip.Demo;

import com.cmp.voip.AudioRecieverThread;
import com.cmp.voip.AudioSenderThread;
import com.cmp.voip.SecurityLayer.BasicSecurityLayer;
import com.cmp.voip.SecurityLayer.NoSecurityLayer;
import com.cmp.voip.VoIPLayer.VoIPLayer1;

public class DemoSecurity {

    public static void main (String[] args){
        AudioSenderThread audioSenderThread = new AudioSenderThread(new VoIPLayer1(new BasicSecurityLayer()));
        audioSenderThread.start();

        AudioRecieverThread audioRecieverThread = new AudioRecieverThread(new VoIPLayer1(new NoSecurityLayer()));
        audioRecieverThread.start();
    }
}
