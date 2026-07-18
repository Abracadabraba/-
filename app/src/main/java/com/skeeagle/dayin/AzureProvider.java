package com.skeeagle.dayin;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Microsoft Azure 版本：Speech（STT+TTS）+ Translator */
public class AzureProvider implements TranslationProvider {

    private final String speechKey;
    private final String speechRegion;
    private final String translatorKey;
    private final String translatorRegion;

    public AzureProvider(String speechKey, String speechRegion, String translatorKey, String translatorRegion) {
        this.speechKey = speechKey;
        this.speechRegion = speechRegion;
        this.translatorKey = translatorKey;
        this.translatorRegion = translatorRegion;
    }

    @Override
    public String recognize(byte[] pcm16, Language language) throws Exception {
        String url = "https://" + speechRegion + ".stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1"
                + "?language=" + language.azureLocale + "&format=simple";

        byte[] wav = WavUtils.pcmToWav(pcm16, AudioRecorder.SAMPLE_RATE, 1, 16);

        Map<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", speechKey);
        headers.put("Content-Type", "audio/wav; codecs=audio/pcm; samplerate=16000");
        headers.put("Accept", "application/json");

        byte[] resp = HttpUtil.postRaw(url, headers, wav);
        JSONObject json = new JSONObject(new String(resp, StandardCharsets.UTF_8));
        String status = json.optString("RecognitionStatus", "");
        if (!"Success".equalsIgnoreCase(status)) {
            return null;
        }
        return json.optString("DisplayText", null);
    }

    @Override
    public String translate(String text, Language source, Language target) throws Exception {
        String url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0"
                + "&from=" + source.azureTranslateCode + "&to=" + target.azureTranslateCode;

        JSONArray body = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("Text", text);
        body.put(item);

        Map<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", translatorKey);
        headers.put("Ocp-Apim-Subscription-Region", translatorRegion);
        headers.put("Content-Type", "application/json; charset=utf-8");

        byte[] resp = HttpUtil.postRaw(url, headers, body.toString().getBytes(StandardCharsets.UTF_8));
        JSONArray response = new JSONArray(new String(resp, StandardCharsets.UTF_8));
        JSONArray translations = response.getJSONObject(0).getJSONArray("translations");
        return translations.getJSONObject(0).getString("text");
    }

    @Override
    public byte[] synthesize(String text, Language language) throws Exception {
        String url = "https://" + speechRegion + ".tts.speech.microsoft.com/cognitiveservices/v1";

        String ssml = "<speak version='1.0' xml:lang='" + language.azureLocale + "'>"
                + "<voice xml:lang='" + language.azureLocale + "' name='" + language.azureVoiceName + "'>"
                + HttpUtil.xmlEscape(text)
                + "</voice></speak>";

        Map<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", speechKey);
        headers.put("Content-Type", "application/ssml+xml");
        headers.put("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3");
        headers.put("User-Agent", "DaYinApp");

        return HttpUtil.postRaw(url, headers, ssml.getBytes(StandardCharsets.UTF_8));
    }
}
