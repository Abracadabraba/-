package com.skeeagle.dayin;

/** 语音识别 + 翻译 + 语音合成 三件套的统一接口，不同云厂商各自实现 */
public interface TranslationProvider {
    /** 语音识别：传入原始 16bit/16kHz PCM 音频和明确的语言，返回识别出的文字 */
    String recognize(byte[] pcm16, Language language) throws Exception;

    /** 文本翻译 */
    String translate(String text, Language source, Language target) throws Exception;

    /** 文本转语音，返回 MP3 音频字节 */
    byte[] synthesize(String text, Language language) throws Exception;
}
