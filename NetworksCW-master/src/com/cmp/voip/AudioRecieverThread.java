package com.cmp.voip;

import com.cmp.voip.VoIPLayer.IVoIPLayer;

public class AudioRecieverThread implements Runnable {

    IVoIPLayer voipLayer;

    public AudioRecieverThread(IVoIPLayer voipLayer) {
        this.voipLayer = voipLayer;
    }

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run (){
        voipLayer.receiveAudio();
    }
}