package com.ma.voice.pressvociebutton.util;

/**
 * 按住说话录音的管理
 * Created by mapengtang on 2016/10/20.
 */
public class PressVoiceManager {
    private PressVoiceManager() {
        init();
    }

    public static PressVoiceManager getInstance() {
        return PressVoiceManagerHolder.ourInstance;
    }

    private void init() {
    }

    private static class PressVoiceManagerHolder {
        private static PressVoiceManager ourInstance = new PressVoiceManager();
    }
}