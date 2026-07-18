package com.skeeagle.dayin;

public class ChatMessage {
    public final String originalText;
    public final String originalLangName;
    public final String translatedText;
    public final String translatedLangName;
    public final boolean isLeft; // true = 由“语言A”一方说出，气泡靠左显示

    public ChatMessage(String originalText, String originalLangName,
                        String translatedText, String translatedLangName,
                        boolean isLeft) {
        this.originalText = originalText;
        this.originalLangName = originalLangName;
        this.translatedText = translatedText;
        this.translatedLangName = translatedLangName;
        this.isLeft = isLeft;
    }
}
