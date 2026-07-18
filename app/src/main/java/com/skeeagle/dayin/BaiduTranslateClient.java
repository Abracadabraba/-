package com.skeeagle.dayin;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 百度翻译开放平台 - 通用文本翻译API
 * 注册只需要百度账号 + 实名认证（身份证），不需要银行卡；
 * 认证后每月100万字符免费。
 */
public class BaiduTranslateClient {

    public static String translate(String text, String appId, String secretKey,
                                     String fromCode, String toCode) throws Exception {
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = md5(appId + text + salt + secretKey);

        String query = "q=" + URLEncoder.encode(text, "UTF-8")
                + "&from=" + fromCode
                + "&to=" + toCode
                + "&appid=" + appId
                + "&salt=" + salt
                + "&sign=" + sign;

        URL url = new URL("https://fanyi-api.baidu.com/api/trans/vip/translate?" + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        }
        is.close();
        conn.disconnect();

        JSONObject json = new JSONObject(sb.toString());
        if (json.has("error_code")) {
            throw new Exception("百度翻译出错 " + json.optString("error_code") + "：" + json.optString("error_msg"));
        }
        JSONArray transResult = json.getJSONArray("trans_result");
        return transResult.getJSONObject(0).getString("dst");
    }

    private static String md5(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}
