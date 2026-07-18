package com.skeeagle.dayin;

import android.content.Context;
import android.content.SharedPreferences;

/** 根据设置里选的服务商，构造对应的 TranslationProvider 实现 */
public class ProviderFactory {
    public static final String PREFS = "dayin_prefs";
    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String PROVIDER_AZURE = "AZURE";
    public static final String PROVIDER_ONDEVICE = "ONDEVICE";

    public static String currentProvider(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("provider", PROVIDER_ONDEVICE);
    }

    /** 仅 Google / Azure 走这个统一接口；ONDEVICE 模式在 MainActivity 里单独处理（系统识别是异步回调式的） */
    public static TranslationProvider create(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String provider = currentProvider(context);

        if (PROVIDER_AZURE.equals(provider)) {
            String speechKey = prefs.getString("azure_speech_key", "");
            String speechRegion = prefs.getString("azure_speech_region", "");
            String translatorKey = prefs.getString("azure_translator_key", "");
            String translatorRegion = prefs.getString("azure_translator_region", "");
            return new AzureProvider(speechKey, speechRegion, translatorKey, translatorRegion);
        } else {
            String apiKey = prefs.getString("google_api_key", "");
            return new GoogleProvider(apiKey);
        }
    }

    /** 检查当前所选服务商的必填项是否已经填好 */
    public static boolean isConfigured(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String provider = currentProvider(context);
        switch (provider) {
            case PROVIDER_AZURE:
                return !empty(prefs.getString("azure_speech_key", ""))
                        && !empty(prefs.getString("azure_speech_region", ""))
                        && !empty(prefs.getString("azure_translator_key", ""))
                        && !empty(prefs.getString("azure_translator_region", ""));
            case PROVIDER_ONDEVICE:
                return !empty(prefs.getString("baidu_app_id", ""))
                        && !empty(prefs.getString("baidu_secret_key", ""));
            default:
                return !empty(prefs.getString("google_api_key", ""));
        }
    }

    public static String getBaiduAppId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("baidu_app_id", "");
    }

    public static String getBaiduSecretKey(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("baidu_secret_key", "");
    }

    private static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
