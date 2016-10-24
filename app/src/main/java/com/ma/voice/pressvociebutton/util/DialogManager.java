package com.ma.voice.pressvociebutton.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ma.voice.pressvociebutton.R;

/**
 * 管理Dialog的显示
 */
public class DialogManager {
    private Dialog mDialog;
    private View mRoot;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mLable;
    private Context mContext;
    private AudioManager mAudioManager;

    public DialogManager(Context context) {
        mContext = context;
    }

    //初始化并显示dialog
    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.pressvocie_dlg_theme);
        mRoot = LayoutInflater.from(mContext).inflate(R.layout.dlg_pressvoice, null);
        mDialog.setContentView(mRoot);
        mIcon = (ImageView) mDialog.findViewById(R.id.dialog_icon);
        mVoice = (ImageView) mDialog.findViewById(R.id.dialog_voice);
        mLable = (TextView) mDialog.findViewById(R.id.recorder_dialogtext);
        mDialog.show();
    }

    //设置正在录音时的dialog界面
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            mRoot.setBackgroundResource(R.drawable.dlg_pressvoice_record_bg);
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.dlg_pressvoice_record);
            mLable.setText(R.string.shouzhishanghua);
        }
    }

    //取消界面
    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mRoot.setBackgroundResource(R.drawable.dlg_pressvoice_cancel_bg);
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.dlg_pressvoice_cancel);
            mLable.setText(R.string.want_to_cancle);
        }

    }

    // 时间过短
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            mRoot.setBackgroundResource(R.drawable.dlg_pressvoice_record_bg);
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.dlg_pressvoice_tooshort);
            mLable.setText(R.string.tooshort);
        }

    }

    // 隐藏dialog
    public void dimissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

    }

    //显示振幅
    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            //通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
            int resId = mContext.getResources().getIdentifier("dlg_pressvoice_v" + level, "drawable", mContext.getPackageName());
            mVoice.setImageResource(resId);
        }
    }

    public void showLeftTime(int leftTime) {
        if (mDialog != null && mDialog.isShowing()) {
            //通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
            mLable.setVisibility(View.VISIBLE);
            mLable.setText("还可以说" + leftTime + "s");
        }

    }

}
