package com.skeeagle.dayin;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

/** Google Cloud 版本：Speech-to-Text + Cloud Translation + Text-to-Speech */
public class GoogleProvider implements TranslationProvider {

    private final String apiKey;

    public GoogleProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String recognize(byte[] pcm16, Language language) throws Exception {
        String url = "https://speech.googleapis.com/v1/speech:recognize?key=" + apiKey;

        JSONObject config = new JSONObject();
        config.put("encoding", "LINEAR16");
        config.put("sampleRateHertz", AudioRecorder.SAMPLE_RATE);
        config.put("languageCode", language.googleSttCode);
        config.put("enableAutomaticPunctuation", true);

        JSONObject audio = new JSONObject();
        audio.put("content", Base64.encodeToString(pcm16, Base64.NO_WRAP));

        JSONObject body = new JSONObject();
        body.put("config", config);
        body.put("audio", audio);

        JSONObject response = new JSONObject(HttpUtil.postJson(url, body.toString()));
        JSONArray results = response.optJSONArray("results");
        if (results == null || results.length() == 0) {
            return null;
        }
        JSONArray alternatives = results.getJSONObject(0).getJSONArray("alternatives");
        return alternatives.getJSONObject(0).getString("transcript");
    }

    @Override
    public String translate(String text, Language source, Language target) throws Exception {
        String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;
        JSONObject body = new JSONObject();
        body.put("q", text);
        body.put("source", source.googleTranslateCode);
        body.put("target", target.googleTranslateCode);
        body.put("format", "text");

        JSONObject response = new JSONObject(HttpUtil.postJson(url, body.toString()));
        JSONObject data = response.getJSONObject("data");
        JSONArray translations = data.getJSONArray("translations");
        return translations.getJSONObject(0).getString("translatedText");
    }

    @Override
    public byte[] synthesize(String text, Language language) throws Exception {
        String url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey;

        JSONObject input = new JSONObject();
        input.put("text", text);

        JSONObject voice = new JSONObject();
        voice.put("languageCode", language.googleTtsCode);
        voice.put("ssmlGender", "NEUTRAL");

        JSONObject audioConfig = new JSONObject();
        audioConfig.put("audioEncoding", "MP3");

        JSONObject body = new JSONObject();
        body.put("input", input);
        body.put("voice", voice);
        body.put("audioConfig", audioConfig);

        JSONObject response = new JSONObject(HttpUtil.postJson(url, body.toString()));
        String audioContentB64 = response.getString("audioContent");
        return Base64.decode(audioContentB64, Base64.DEFAULT);
    }
}
