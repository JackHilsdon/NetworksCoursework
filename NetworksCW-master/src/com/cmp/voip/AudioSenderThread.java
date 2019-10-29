package com.cmp.voip;

import com.cmp.voip.VoIPLayer.IVoIPLayer;

public class AudioSenderThread implements Runnable {

    IVoIPLayer voipLayer;

    public AudioSenderThread(IVoIPLayer voipLayer) {
        this.voipLayer = voipLayer;
    }

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run (){
        voipLayer.sendAudio();
    }
}