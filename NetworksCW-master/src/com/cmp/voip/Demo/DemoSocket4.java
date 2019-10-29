package com.cmp.voip.Demo;

import com.cmp.voip.AudioRecieverThread;
import com.cmp.voip.AudioSenderThread;
import com.cmp.voip.SecurityLayer.BasicSecurityLayer;
import com.cmp.voip.SecurityLayer.ISecurityLayer;
import com.cmp.voip.VoIPLayer.VoIPLayer4;

public class DemoSocket4 {

    public static void main (String[] args){
        ISecurityLayer securityLayer = new BasicSecurityLayer();

        AudioSenderThread audioSenderThread = new AudioSenderThread(new VoIPLayer4(securityLayer));
        audioSenderThread.start();

        AudioRecieverThread audioRecieverThread = new AudioRecieverThread(new VoIPLayer4(securityLayer));
        audioRecieverThread.start();
    }
}
