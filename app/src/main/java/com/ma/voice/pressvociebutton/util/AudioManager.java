package com.ma.voice.pressvociebutton.util;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

public class AudioManager {

    private static final String VOCIE_FILENAME_PREFIX = "press_voice";
    private static final String VOCIE_SUFFIX = ".amr";

    private MediaRecorder mRecorder;
    private String mDirString;
    private String mCurrentFilePathString;

    private boolean isPrepared;// 是否准备好了

    private AudioManager(String dir) {
        mDirString = dir;
    }

    public void setDirString(String mDirString) {
        this.mDirString = mDirString;
    }

    private AudioManager() {
        init();
    }

    public static AudioManager getInstance() {
        return AudioManager.AudioManagerHolder.ourInstance;
    }

    private void init() {
    }

    private static class AudioManagerHolder {
        private static AudioManager ourInstance = new AudioManager();
    }

    /**
     * 回调函数，准备完毕，准备好后，button才会开始显示录音框
     *
     * @author nickming
     */
    public interface AudioStageListener {
        void wellPrepared();
    }

    public AudioStageListener mListener;

    public void setOnAudioStageListener(AudioStageListener listener) {
        mListener = listener;
    }

    public void removeOnAudioStageListener() {
        mListener = null;
    }

    // 准备方法
    public boolean prepareAudio() {
        try {
            // 一开始应该是false的
            isPrepared = false;

            File dir = new File(mDirString);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileNameString = generalFileName();
            File file = new File(dir, fileNameString);

            mCurrentFilePathString = file.getAbsolutePath();

            mRecorder = new MediaRecorder();
            // 设置输出文件
            mRecorder.setOutputFile(file.getAbsolutePath());
            // 设置meidaRecorder的音频源是麦克风
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置文件音频的输出格式为amr
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            // 设置音频的编码格式为amr
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // 严格遵守google官方api给出的mediaRecorder的状态流程图
            mRecorder.prepare();

            mRecorder.start();
            // 准备结束
            isPrepared = true;
            // 已经准备好了，可以录制了
            if (mListener != null) {
                mListener.wellPrepared();
            }
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            isPrepared = false;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            isPrepared = false;
            return false;
        } catch (RuntimeException e) {
            e.printStackTrace();
            isPrepared = false;
            return false;
        }

    }

    /**
     * 随机生成文件的名称
     *
     * @return 文件名称
     */
    private String generalFileName() {
        return VOCIE_FILENAME_PREFIX + System.currentTimeMillis() + VOCIE_SUFFIX;
    }

    // 获得声音的level
    public int getVoiceLevel(int maxLevel) {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
        if (isPrepared) {
            try {
                // 取证+1，否则去不到6
                int maxAmplitude = 32768;
                int amplitude = mRecorder.getMaxAmplitude();
                int limit = maxAmplitude / 8;
                if (amplitude >= limit) {
                    return maxLevel;
                } else {
                    return maxLevel * amplitude / limit + 1;
                }
            } catch (Exception e) {
            }
        }
        return 1;
    }

    // 释放资源
    public void release() {
        // 严格按照api流程进行
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mRecorder = null;
        }
    }

    // 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
    // 这是与release的方法的区别
    public void cancel() {
        release();
        if (mCurrentFilePathString != null) {
            File file = new File(mCurrentFilePathString);
            file.delete();
            mCurrentFilePathString = null;
        }

    }

    public String getCurrentFilePath() {
        return mCurrentFilePathString;
    }

}
