package com.ma.voice.pressvociebutton.widget;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.ma.voice.pressvociebutton.BuildConfig;
import com.ma.voice.pressvociebutton.R;
import com.ma.voice.pressvociebutton.util.AudioManager;
import com.ma.voice.pressvociebutton.util.DialogManager;
import com.ma.voice.pressvociebutton.util.Global;

public class AudioRecordButton extends Button implements AudioManager.AudioStageListener {

    private static final float TIME_SHORT = 1;//最短录制时间为1s

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    private static final int DISTANCE_Y_CANCEL = 50;

    private static final int TIME_SPAN = 1000;

    private int mCurrentState = STATE_NORMAL;
    // 已经开始录音
    private boolean isRecording = false;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager;

    private int mTime = 0;
    // 是否触发了onlongclick，准备好了
    private boolean mReady;


    private boolean isVibrate = true;

    /**
     * 先实现两个参数的构造方法，布局会默认引用这个构造方法， 用一个 构造参数的构造方法来引用这个方法 * @param context
     */

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(getContext());
        mAudioManager = AudioManager.getInstance();
        mAudioManager.setDirString(Global.getFileRootDir());
        mAudioManager.setOnAudioStageListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //检测权限
                if (mAudioManager.prepareAudio()) {
                    mReady = true;
                    return false;
                } else {
                    mReady = false;
                    //权限申请 android 6.0
                    mListener.onPressVoiceRequestPermission(Manifest.permission.RECORD_AUDIO, "此功能需要" + BuildConfig.APP_NAME + "录音权限");
                    return true;
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAudioManager.removeOnAudioStageListener();
    }

    /**
     * 录音完成后的回调，回调给activiy，可以获得mtime和文件的路径
     *
     * @author nickming
     */
    public interface AudioFinishRecorderListener {

        void onPressVoiceRequestPermission(String permission, String hint);

        void onFinished(int seconds, String filePath);

        void vibrator();
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            mTime += 1;
            mhandler.sendEmptyMessage(MSG_VOICE_CHANGE);
            mhandler.postDelayed(this, TIME_SPAN);
        }
    };

    // 准备三个常量
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;

    private Handler mhandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 显示应该是在audio end prepare之后回调
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    isVibrate = true;
                    mhandler.postDelayed(mGetVoiceLevelRunnable, TIME_SPAN);
                    // 需要开启一个线程来变换音量
                    break;
                case MSG_VOICE_CHANGE:
                    if (mTime >= 60 && mListener != null) {// 并且callbackActivity，保存录音
                        mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                        mDialogManager.dimissDialog();
                        mAudioManager.release();// release释放一个mediarecorder
                        reset();
                    }
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(6));
                    if (60 - mTime <= 10) {
                        if (mListener != null && isVibrate) {
                            isVibrate = false;
                            mListener.vibrator();
                        }
                        mDialogManager.showLeftTime(60 - mTime);
                    }
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;

            }
        }

        ;
    };

    // 在这里面发送一个handler的消息
    @Override
    public void wellPrepared() {
        mhandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 直接复写这个监听函数
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTime = 0;
                        mhandler.removeCallbacks(mGetVoiceLevelRunnable);
                        mDialogManager.tooShort();
                        mAudioManager.cancel();
                        reset();
                        mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 持续1.3s
                    }
                });
                break;
            case MotionEvent.ACTION_UP:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if (!isRecording || mTime < TIME_SHORT) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mhandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);// 持续1.3s
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
                    mDialogManager.dimissDialog();
                    mAudioManager.release();// release释放一个mediarecorder
                    if (mListener != null) {// 并且callbackActivity，保存录音
                        mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                    }
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mAudioManager.cancel();
                    mDialogManager.dimissDialog();
                }
                reset();// 恢复标志位
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        isRecording = false;
        mhandler.removeCallbacks(mGetVoiceLevelRunnable);
        changeState(STATE_NORMAL);
        mReady = false;
        mTime = 0;
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_preseevoice_normal);
                    setText(R.string.normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_preseevoice_pressed);
                    setText(R.string.recording);
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;

                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_preseevoice_pressed);
                    setText(R.string.want_to_cancle);
                    mDialogManager.wantToCancel();
                    break;

            }
        }

    }

   /* @Override
    public boolean onPreDraw() {
        // TODO Auto-generated method stub
        return false;
    }*/

}
