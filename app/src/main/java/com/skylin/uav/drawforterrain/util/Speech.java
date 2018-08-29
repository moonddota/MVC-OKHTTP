package com.skylin.uav.drawforterrain.util;

import android.speech.tts.TextToSpeech;

import com.skylin.uav.drawforterrain.APP;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by wh on 2017/3/24.
 */

public class Speech implements TextToSpeech.OnInitListener {

    private final TextToSpeech mTextToSpeech;//TTS对象
    private final ConcurrentLinkedQueue<String> mBufferedMessages;//消息对垒
    private boolean misReady;

    public Speech() {
        this.mTextToSpeech = new TextToSpeech(APP.getContext(), this);
        this.mBufferedMessages = new ConcurrentLinkedQueue<String>();//实例化对象
    }

    //初始化TTS引擎
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = this.mTextToSpeech.setLanguage(Locale.CHINA);//设置识别语音为中文
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ToastUtil.show("语言不可用");
            }
            synchronized (this) {
                this.misReady = true;
                for (String bufferedMessage : this.mBufferedMessages) {
                    speakText(bufferedMessage);//读语音
                }
                this.mBufferedMessages.clear();//读完后清空队列

            }
        }
    }

    //释放资源
    public void release() {
        synchronized (this) {
            this.mTextToSpeech.shutdown();
            this.misReady = false;
        }
    }

    //更新消息队列，或者读语音
    public void notifyNewMessage(String lanaugh) {
        String message = lanaugh;
        synchronized (this) {
            if (this.misReady) {
                speakText(message);
            } else {
                this.mBufferedMessages.add(message);
            }
        }
    }

    //读语音处理
    private void speakText(String message) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, "STREAM_NOTIFICATION");//设置播放类型（音频流类型）
        this.mTextToSpeech.speak(message, TextToSpeech.QUEUE_ADD, params);//将这个发音任务添加当前任务之后
        this.mTextToSpeech.playSilence(100, TextToSpeech.QUEUE_ADD, params);//间隔多长时间
    }


}
