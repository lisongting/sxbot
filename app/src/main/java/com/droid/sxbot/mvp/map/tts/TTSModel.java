package com.droid.sxbot.mvp.map.tts;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by lisongting on 2018/4/18.
 * 文字转音频文件
 */

public class TTSModel {
    private static final String TAG = "tts";

    private Context context;
    private SpeechSynthesizer speechSynthesizer;
    private SynthesizerListener synthesizerListener;
    private TTSListener ttsListener;
    private boolean isInitSuccess = false;
    private String audioPath ;
    private String audioFile;
    private int mode = -1;
    //播放模式
    private static final int TTS_MODE_SPEAK = 1;
    //合成
    private static final int TTS_MODE_SYNTHETISE = 2;

    public interface TTSListener{
        void onComplete(String path);

        void onError(String msg);
    }

    public TTSModel(Context context) {
        this.context = context;
        init();
        audioPath = Environment.getExternalStorageDirectory() + "/audio_tts/";
    }


    public void init() {

        final InitListener initListener = new InitListener() {
            @Override
            public void onInit(int i) {
                if (i == ErrorCode.SUCCESS) {
                    log("init success");
                    isInitSuccess = true;
                } else {
                    isInitSuccess = false;
                    if (ttsListener != null) {
                        ttsListener.onError("初始化失败，错误码:" + i);
                    }
                }
            }
        };

        synthesizerListener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                log("SynthesizerListener -- onSpeakBegin");
            }
            //缓冲进度
            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos,
                                         String info) {
                if (percent >= 98 && ttsListener != null&&audioFile!=null&&mode==TTS_MODE_SYNTHETISE) {
                    ttsListener.onComplete(audioFile);
                }
                log("SynthesizerListener -- onBufferProgress: percent:" +percent+",beginPos:"+beginPos+",endPos:"+endPos);
            }

            @Override
            public void onSpeakPaused() {
                log("SynthesizerListener -- onSpeakPaused");
            }

            @Override
            public void onSpeakResumed() {
                log("SynthesizerListener -- onSpeakResumed");
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
                log("SynthesizerListener -- onSpeakProgress: percent:"+percent+",beginPos:"+beginPos+",endPos:"+endPos);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                log("SynthesizerListener -- onCompleted:"+speechError.getErrorCode());
                if (ttsListener != null) {
                    if (speechError.getErrorCode() == 10202) {
                        ttsListener.onError("网络连接超时");
                    }
                }
                mode = -1;
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
                log("onEvent:" + i + "," + i1 + "," + i2);
            }
        };
        speechSynthesizer = SpeechSynthesizer.createSynthesizer(context, initListener);

        setParameters();
    }

    private void setParameters() {
        speechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        //设置为在线工作模式
        speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);

        //设置合成语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        speechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        speechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    }

    //将文字合成为音频
    public void textToSpeech(String text,String speaker,String fileName,String speed,String tone) {
        // 设置在线合成发音人
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, speaker);
        //设置合成语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED,speed);
        //设置合成音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH, tone);
        audioFile = audioPath + fileName + ".wav";
        speechSynthesizer.synthesizeToUri(text, audioFile, synthesizerListener);
        mode = TTS_MODE_SYNTHETISE;
    }

    //播放指定音频
    public void play(String text,String speaker,String speed,String tone) {
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, speaker);
        //设置合成语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED,speed);
        //设置合成音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH, tone);
        int code = speechSynthesizer.startSpeaking(text, synthesizerListener);
        mode = TTS_MODE_SPEAK;
    }

    public void destroy(){
        if (null != speechSynthesizer) {
            speechSynthesizer.stopSpeaking();
            speechSynthesizer.destroy();
        }
    }

    public void setTtsListener(TTSListener listener) {
        ttsListener = listener;
    }

    private void log(String s) {
        Log.i(TAG, s);
    }





}
