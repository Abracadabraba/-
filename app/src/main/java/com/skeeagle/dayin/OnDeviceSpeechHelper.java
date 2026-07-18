package com.skeeagle.dayin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;

/** 用手机系统自带的语音识别（免费，不需要任何 Key），按住说话时调用 */
public class OnDeviceSpeechHelper {

    public interface Callback {
        void onResult(String text);
        void onError(String message);
    }

    private final SpeechRecognizer recognizer;
    private Callback callback;

    public OnDeviceSpeechHelper(Context context) {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (callback == null) return;
                if (matches != null && !matches.isEmpty()) {
                    callback.onResult(matches.get(0));
                } else {
                    callback.onError("未识别到内容");
                }
            }

            @Override
            public void onError(int error) {
                if (callback != null) callback.onError("识别出错（代码 " + error + "）");
            }

            @Override public void onReadyForSpeech(Bundle params) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { }
            @Override public void onPartialResults(Bundle partialResults) { }
            @Override public void onEvent(int eventType, Bundle params) { }
        });
    }

    public static boolean isAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    /** 开始监听：localeTag 形如 "zh-CN" / "vi-VN" */
    public void startListening(String localeTag, Callback cb) {
        this.callback = cb;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizer.startListening(intent);
    }

    /** 松开按钮时调用，让识别器立刻结束并返回结果（而不是等待自动检测静音） */
    public void stopListening() {
        recognizer.stopListening();
    }

    public void destroy() {
        recognizer.destroy();
    }
}
