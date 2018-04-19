package com.droid.sxbot.mvp.map.tts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.sxbot.R;

/**
 * Created by lisongting on 2018/4/18.
 */

public class TTSFragment extends Fragment {

    private Button btPlay, btMake;
    private EditText editText;
    private TTSModel ttsModel;
    private String text,speaker;
    private Spinner spinner;
    private EditText etFile;
    //显示语速，语调的TextView
    private TextView tvSpeed,tvTone;
    //调节语速，语调的SeekBar
    private SeekBar sbSpeed,sbTone;

    //每个发音人对应的引擎参数
    private String[] cloudSpeakerValues;
    private Handler handler;

    public TTSFragment(){
        handler = new Handler(Looper.getMainLooper());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tts_fragment, viewGroup, false);
        btPlay = v.findViewById(R.id.tts_play);
        btMake = v.findViewById(R.id.tts_make);
        editText = v.findViewById(R.id.tts_edittext);
        spinner = v.findViewById(R.id.tts_spinner);
        etFile = v.findViewById(R.id.tts_et_file_name);
        tvSpeed = v.findViewById(R.id.tts_tv_speed);
        tvTone = v.findViewById(R.id.tts_tv_tone);
        sbSpeed = v.findViewById(R.id.tts_sb_speed);
        sbTone = v.findViewById(R.id.tts_sb_tone);
        Resources resources = getResources();
        cloudSpeakerValues = resources.getStringArray(R.array.cloud_speaker_values);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ttsModel = new TTSModel(getContext());
        TTSModel.TTSListener ttsListener = new TTSModel.TTSListener() {
            @Override
            public void onComplete(final String path) {
                log("onComplete:" + path);
                showOnMainThread("音频生成成功："+path);
            }
            @Override
            public void onError(String msg) {
                log("onError");
                showOnMainThread(msg);
            }
        };
        ttsModel.setTtsListener(ttsListener);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speaker = cloudSpeakerValues[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString();
                String speed = tvSpeed.getText().toString().split(" ")[1];
                String tone = tvTone.getText().toString().split(" ")[1];
                if (s.trim().length() == 0) {
                    Toast.makeText(getContext(), "请输入要合成为音频的文字", Toast.LENGTH_SHORT).show();
                }
                ttsModel.play(s,speaker,speed,tone);
            }
        });

        btMake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = editText.getText().toString();
                int i = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (i == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    String fileName = etFile.getText().toString();
                    if (fileName.trim().length() == 0) {
                        Toast.makeText(getContext(), "请输入音频文件名", Toast.LENGTH_SHORT).show();
                    }else  if (text.trim().length() == 0) {
                        Toast.makeText(getContext(), "请输入要合成为音频的文字", Toast.LENGTH_SHORT).show();
                    }else {
                        String speed = tvSpeed.getText().toString().split(" ")[1];
                        String tone = tvTone.getText().toString().split(" ")[1];
                        ttsModel.textToSpeech(text,speaker,fileName,speed,tone);
                    }
                }
            }
        });

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSpeed.setText("语速: "+progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sbTone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTone.setText("语调: " + progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String fileName = etFile.getText().toString();
                if (fileName.trim().length() == 0) {
                    Toast.makeText(getContext(), "请输入音频文件名", Toast.LENGTH_SHORT).show();
                }else if (text.trim().length() == 0) {
                    Toast.makeText(getContext(), "请输入要合成为音频的文字", Toast.LENGTH_SHORT).show();
                }else {
                    String speed = tvSpeed.getText().toString().split(" ")[1];
                    String tone = tvTone.getText().toString().split(" ")[1];
                    ttsModel.textToSpeech(text, speaker,fileName,speed,tone);
                }
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                    startActivity(intent);
                    Toast.makeText(getContext(), "无法获取文件读写权限，请到手机设置中进行授权", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "无法获取文件读写权限,请进行授权" , Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showOnMainThread(final String tip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("提示")
                            .setMessage(tip)
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ttsModel != null) {
            ttsModel.destroy();
        }
    }

    private void log(String s) {
        Log.i("TTSFragment", s);
    }
}
