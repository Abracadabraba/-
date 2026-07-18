package com.skeeagle.dayin;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/** 用手机系统自带的语音合成朗读译文（免费，不需要任何 Key） */
public class OnDeviceTtsHelper implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ready = false;

    public OnDeviceTtsHelper(Context context) {
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        ready = (status == TextToSpeech.SUCCESS);
    }

    /** localeTag 形如 "zh-CN" / "vi-VN"；如果该语言没有装语音包，会静默失败 */
    public boolean speak(String text, String localeTag) {
        if (!ready || tts == null) return false;
        Locale locale = Locale.forLanguageTag(localeTag);
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return false;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dayin_utt");
        return true;
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
