package com.skeeagle.dayin;

import java.io.ByteArrayOutputStream;

/** 把原始 PCM16 采样数据包装成标准 WAV 容器（供 Azure 语音识别接口使用） */
public class WavUtils {

    public static byte[] pcmToWav(byte[] pcm, int sampleRate, int channels, int bitsPerSample) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int dataLen = pcm.length;
        int chunkSize = 36 + dataLen;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeAscii(out, "RIFF");
        writeIntLE(out, chunkSize);
        writeAscii(out, "WAVE");
        writeAscii(out, "fmt ");
        writeIntLE(out, 16); // fmt chunk size
        writeShortLE(out, (short) 1); // PCM
        writeShortLE(out, (short) channels);
        writeIntLE(out, sampleRate);
        writeIntLE(out, byteRate);
        writeShortLE(out, (short) blockAlign);
        writeShortLE(out, (short) bitsPerSample);
        writeAscii(out, "data");
        writeIntLE(out, dataLen);
        out.write(pcm, 0, pcm.length);
        return out.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) {
        out.write(s.getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, s.length());
    }

    private static void writeIntLE(ByteArrayOutputStream out, int v) {
        out.write(v & 0xff);
        out.write((v >> 8) & 0xff);
        out.write((v >> 16) & 0xff);
        out.write((v >> 24) & 0xff);
    }

    private static void writeShortLE(ByteArrayOutputStream out, short v) {
        out.write(v & 0xff);
        out.write((v >> 8) & 0xff);
    }
}
