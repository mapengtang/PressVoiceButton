package com.ma.voice.pressvociebutton.view;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ma.voice.pressvociebutton.R;
import com.ma.voice.pressvociebutton.widget.AudioRecordButton;

public class TestVoiceActivity extends BasePermissionActivity implements AudioRecordButton.AudioFinishRecorderListener {

    private final int REQUEST_PERMISSIONN_ID = 0x001;

    private AudioRecordButton mRecordBtn;
    private TextView mShowVoicePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_vociepress_test);
        findView();
        init();
    }

    private void findView() {
        mRecordBtn = (AudioRecordButton) findViewById(R.id.audio_record_btn);
        mShowVoicePath = (TextView) findViewById(R.id.voice_file_path);
    }

    private void init() {
        mRecordBtn.setAudioFinishRecorderListener(this);
    }

    @Override
    public void onPressVoiceRequestPermission(String permission, String hint) {
        //请求成功后不做任何处理
        baseRequestPermission(permission, hint, REQUEST_PERMISSIONN_ID);
    }

    @Override
    public void onFinished(int seconds, String filePath) {
        mShowVoicePath.setText("文件路径:" + filePath);
    }

    @Override
    public void vibrator() {
        //录制剩10s时振动提醒
        Vibrator vibator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibator.vibrate(300);
    }
}
