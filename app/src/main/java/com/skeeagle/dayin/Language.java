package com.skeeagle.dayin;

/** 一种语言在 Google / Azure 两套服务里各自对应的代码 */
public class Language {
    public final String displayName;

    // Google Cloud
    public final String googleSttCode;       // Speech-to-Text 语言代码 (BCP-47)
    public final String googleTranslateCode; // Cloud Translation 语言代码 (ISO)
    public final String googleTtsCode;       // Text-to-Speech 语言代码 (BCP-47)

    // Microsoft Azure
    public final String azureLocale;         // STT 使用的 locale (BCP-47)，TTS SSML 里的 xml:lang 也用它
    public final String azureTranslateCode;  // Azure Translator 语言代码（部分和 ISO 不同，如中文是 zh-Hans）
    public final String azureVoiceName;      // Azure 神经网络语音名称
    public final String baiduCode;           // 百度翻译开放平台语种代码（部分和ISO不同，如日语是jp、韩语是kor）
    public final String androidLocale;       // Android 系统自带语音识别/朗读用的 locale（和 azureLocale 相同格式）

    public Language(String displayName,
                     String googleSttCode, String googleTranslateCode, String googleTtsCode,
                     String azureLocale, String azureTranslateCode, String azureVoiceName,
                     String baiduCode) {
        this.displayName = displayName;
        this.googleSttCode = googleSttCode;
        this.googleTranslateCode = googleTranslateCode;
        this.googleTtsCode = googleTtsCode;
        this.azureLocale = azureLocale;
        this.azureTranslateCode = azureTranslateCode;
        this.azureVoiceName = azureVoiceName;
        this.baiduCode = baiduCode;
        this.androidLocale = azureLocale; // 两者格式一致，直接复用
    }

    @Override
    public String toString() {
        return displayName;
    }

    // 注：id / may / hi / tr 这几个百度翻译代码是按通用语种表填的，出发前建议先在
    // App 里对该语言测一下翻译，如果报错，去百度翻译开放平台的语种列表核对一下实际代码。
    public static final Language[] ALL = new Language[]{
            new Language("中文（普通话）", "cmn-Hans-CN", "zh-CN", "cmn-CN", "zh-CN", "zh-Hans", "zh-CN-XiaoxiaoNeural", "zh"),
            new Language("English", "en-US", "en", "en-US", "en-US", "en", "en-US-JennyNeural", "en"),
            new Language("Tiếng Việt", "vi-VN", "vi", "vi-VN", "vi-VN", "vi", "vi-VN-HoaiMyNeural", "vie"),
            new Language("ไทย", "th-TH", "th", "th-TH", "th-TH", "th", "th-TH-PremwadeeNeural", "th"),
            new Language("Bahasa Indonesia", "id-ID", "id", "id-ID", "id-ID", "id", "id-ID-GadisNeural", "id"),
            new Language("Bahasa Melayu", "ms-MY", "ms", "ms-MY", "ms-MY", "ms", "ms-MY-YasminNeural", "may"),
            new Language("Русский", "ru-RU", "ru", "ru-RU", "ru-RU", "ru", "ru-RU-SvetlanaNeural", "ru"),
            new Language("العربية", "ar-SA", "ar", "ar-XA", "ar-SA", "ar", "ar-SA-ZariyahNeural", "ara"),
            new Language("Español", "es-ES", "es", "es-ES", "es-ES", "es", "es-ES-ElviraNeural", "spa"),
            new Language("Français", "fr-FR", "fr", "fr-FR", "fr-FR", "fr", "fr-FR-DeniseNeural", "fra"),
            new Language("日本語", "ja-JP", "ja", "ja-JP", "ja-JP", "ja", "ja-JP-NanamiNeural", "jp"),
            new Language("한국어", "ko-KR", "ko", "ko-KR", "ko-KR", "ko", "ko-KR-SunHiNeural", "kor"),
            new Language("हिन्दी", "hi-IN", "hi", "hi-IN", "hi-IN", "hi", "hi-IN-SwaraNeural", "hi"),
            new Language("Português", "pt-PT", "pt", "pt-PT", "pt-PT", "pt", "pt-PT-RaquelNeural", "pt"),
            new Language("Türkçe", "tr-TR", "tr", "tr-TR", "tr-TR", "tr", "tr-TR-EmelNeural", "tr"),
    };
}
