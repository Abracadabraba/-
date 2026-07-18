package com.skeeagle.dayin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.core.app.ActivityCompat;
import java.io.ByteArrayOutputStream;

/** 采集 16kHz / 单声道 / 16bit PCM 原始音频，供 Google Speech-to-Text (LINEAR16) 使用 */
public class AudioRecorder {
    public static final int SAMPLE_RATE = 16000;

    private AudioRecord audioRecord;
    private Thread recordThread;
    private volatile boolean recording = false;
    private ByteArrayOutputStream buffer;

    public boolean start(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        int minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBuf <= 0) minBuf = 4096;
        final int bufSize = minBuf * 2;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return false;
        }

        buffer = new ByteArrayOutputStream();
        recording = true;
        audioRecord.startRecording();

        recordThread = new Thread(() -> {
            byte[] tmp = new byte[bufSize];
            while (recording) {
                int read = audioRecord.read(tmp, 0, tmp.length);
                if (read > 0) {
                    buffer.write(tmp, 0, read);
                }
            }
        });
        recordThread.start();
        return true;
    }

    /** 停止录音，返回原始 16bit PCM 采样数据（16kHz 单声道） */
    public byte[] stop() {
        recording = false;
        try {
            if (recordThread != null) recordThread.join();
        } catch (InterruptedException ignored) {
        }
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException ignored) {
            }
            audioRecord.release();
            audioRecord = null;
        }
        return buffer != null ? buffer.toByteArray() : new byte[0];
    }
}
