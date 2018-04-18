package com.droid.sxbot;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.droid.sxbot.util.Util;

/**
 * Created by lisongting on 2018/4/10.
 */

public class AnimateDialog extends DialogFragment {

    private TextView tvBigText,tvSmallText;
    private ProgressBar progressBar;
    private RadioButton rbAlwaysSelect,rbAlwaysSelectDelay;
    private TextView btCancel,btOk;
    private OnButtonClickListener listener;
    private int mode;
    private String largeContentText;
    private boolean isShowingTwoButtons;

    public static final int FILE_SELECT_MODE_ALWAYS = 1;
    public static final int FILE_SELECT_MODE_ALWAYS_DELAY = 2;

    public static final int DIALOG_STYLE_SELECT_FILE = 11;
    public static final int DIALOG_STYLE_LOADING = 22;
    public static final int DIALOG_STYLE_SHOW_CONTENT = 33;

    public AnimateDialog() {

    }

    public interface OnButtonClickListener{
        void onCancel();

        void onConfirm();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_layout, container, false);
        tvBigText = v.findViewById(R.id.dialog_text);
        tvSmallText = v.findViewById(R.id.dialog_text2);
        progressBar = v.findViewById(R.id.dialog_progress);
        rbAlwaysSelect = v.findViewById(R.id.dialog_rb1);
        rbAlwaysSelectDelay = v.findViewById(R.id.dialog_rb2);
        btCancel = v.findViewById(R.id.dialog_bt_cancel);
        btOk = v.findViewById(R.id.dialog_bt_ok);

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (listener != null) {
                    listener.onCancel();
                    listener = null;
                }
            }
        });
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rbAlwaysSelect.isChecked()) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putInt(getString(R.string.pref_key_file_set_mode), FILE_SELECT_MODE_ALWAYS);
                    editor.apply();
                    Config.AUDIO_FILE_SELECT_MODE = FILE_SELECT_MODE_ALWAYS;
                } else if (rbAlwaysSelectDelay.isChecked()) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putInt(getString(R.string.pref_key_file_set_mode),FILE_SELECT_MODE_ALWAYS_DELAY);
                    editor.apply();
                    Config.AUDIO_FILE_SELECT_MODE = FILE_SELECT_MODE_ALWAYS_DELAY;
                }
                if (listener != null) {
                    listener.onConfirm();
                    listener = null;
                }
                dismiss();

            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        setCancelable(false);
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics metrics = Util.getScreenInfo(getContext());
            if (Util.isPortrait(getContext())) {
                dialog.getWindow().setLayout((int) (metrics.widthPixels * 0.75), ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                dialog.getWindow().setLayout((int) (metrics.heightPixels * 0.75), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        switch (mode) {
            case DIALOG_STYLE_SELECT_FILE:
                tvBigText.setVisibility(View.VISIBLE);
                tvBigText.setText("是否立即设置对应位置的音频？");
                tvSmallText.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                btOk.setVisibility(View.VISIBLE);
                btCancel.setVisibility(View.VISIBLE);
                if (Config.AUDIO_FILE_SELECT_MODE == -1) {
                    rbAlwaysSelect.setVisibility(View.VISIBLE);
                    rbAlwaysSelectDelay.setVisibility(View.VISIBLE);
                } else {
                    rbAlwaysSelect.setVisibility(View.INVISIBLE);
                    rbAlwaysSelectDelay.setVisibility(View.INVISIBLE);
                }
                break;
            case DIALOG_STYLE_LOADING:
                tvBigText.setVisibility(View.INVISIBLE);
                tvSmallText.setText("文件上传中...");
                tvSmallText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                rbAlwaysSelect.setVisibility(View.GONE);
                rbAlwaysSelectDelay.setVisibility(View.GONE);
                btOk.setVisibility(View.VISIBLE);
                btCancel.setVisibility(View.INVISIBLE);
                break;
            case DIALOG_STYLE_SHOW_CONTENT:
                tvBigText.setVisibility(View.VISIBLE);
                tvBigText.setText(largeContentText);
                tvSmallText.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                rbAlwaysSelect.setVisibility(View.GONE);
                rbAlwaysSelectDelay.setVisibility(View.GONE);
                btOk.setVisibility(View.VISIBLE);
                if (isShowingTwoButtons) {
                    btCancel.setVisibility(View.VISIBLE);
                } else {
                    btCancel.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                break;
        }

    }

    public void setModeAndContent(int mode, String largeContentText, boolean isShowingTwoButtons, OnButtonClickListener listener) {
        this.listener = listener;
        this.mode = mode;
        this.largeContentText = largeContentText;
        this.isShowingTwoButtons = isShowingTwoButtons;
    }

    //更新进度，表示当前完成的百分比
    public void updateProgress(int percent){
        if (isVisible() && mode == DIALOG_STYLE_LOADING) {
            progressBar.setProgress(percent);
        }
    }
}
