package com.skeeagle.dayin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 通用 HTTP POST 工具，供各 Provider 复用 */
public class HttpUtil {

    /** POST，body 和响应都当作字节数组处理（用于上传音频 / 下载音频） */
    public static byte[] postRaw(String urlStr, Map<String, String> headers, byte[] body) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            conn.setRequestProperty(e.getKey(), e.getValue());
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        is.close();
        conn.disconnect();

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + ": " + new String(baos.toByteArray(), StandardCharsets.UTF_8));
        }
        return baos.toByteArray();
    }

    /** POST JSON字符串，返回JSON字符串（Google 系列接口走这个，鉴权在URL里带 key） */
    public static String postJson(String urlStr, String jsonBody) throws IOException {
        java.util.HashMap<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        byte[] resp = postRaw(urlStr, headers, jsonBody.getBytes(StandardCharsets.UTF_8));
        return new String(resp, StandardCharsets.UTF_8);
    }

    public static String xmlEscape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
