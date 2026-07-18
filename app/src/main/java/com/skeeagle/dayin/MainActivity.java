package com.skeeagle.dayin;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 大音 - 双语语音互译对讲
 * 顶部选择语言A / 语言B；分别按住"按住说 A / B"按钮说话，
 * 识别文字显示在聊天记录中，自动翻译成另一种语言并朗读给对方听。
 *
 * 三种可切换方案（设置里选）：
 * - 本地识别+朗读 + 百度翻译：不需要信用卡，用手机自带的语音识别/朗读 + 百度翻译API
 * - Google Cloud / Microsoft Azure：云端识别+翻译+朗读，效果更好但需要绑卡开通
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQ_RECORD_AUDIO = 1001;
    private static final String PREFS = ProviderFactory.PREFS;

    private Spinner spinnerLangA, spinnerLangB;
    private Button btnTalkA, btnTalkB;
    private ImageButton btnSettings;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private final AudioRecorder audioRecorder = new AudioRecorder();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean isRecording = false;
    private boolean recordingIsA = true;
    private MediaPlayer mediaPlayer;

    private OnDeviceSpeechHelper onDeviceSpeechHelper;
    private OnDeviceTtsHelper onDeviceTtsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerLangA = findViewById(R.id.spinnerLangA);
        spinnerLangB = findViewById(R.id.spinnerLangB);
        btnTalkA = findViewById(R.id.btnTalkA);
        btnTalkB = findViewById(R.id.btnTalkB);
        btnSettings = findViewById(R.id.btnSettings);
        recyclerChat = findViewById(R.id.recyclerChat);

        chatAdapter = new ChatAdapter(messages);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        ArrayAdapter<Language> langAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Language.ALL);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLangA.setAdapter(langAdapter);
        spinnerLangB.setAdapter(langAdapter);

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        spinnerLangA.setSelection(prefs.getInt("langA", 0)); // 默认：中文
        spinnerLangB.setSelection(prefs.getInt("langB", 2)); // 默认：越南语

        AdapterView.OnItemSelectedListener saveListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit()
                        .putInt("langA", spinnerLangA.getSelectedItemPosition())
                        .putInt("langB", spinnerLangB.getSelectedItemPosition())
                        .apply();
                updateTalkButtonLabels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerLangA.setOnItemSelectedListener(saveListener);
        spinnerLangB.setOnItemSelectedListener(saveListener);
        updateTalkButtonLabels();

        onDeviceSpeechHelper = new OnDeviceSpeechHelper(this);
        onDeviceTtsHelper = new OnDeviceTtsHelper(this);

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        btnTalkA.setOnTouchListener((v, event) -> handleTalkTouch(event, true));
        btnTalkB.setOnTouchListener((v, event) -> handleTalkTouch(event, false));
    }

    private void updateTalkButtonLabels() {
        Language a = (Language) spinnerLangA.getSelectedItem();
        Language b = (Language) spinnerLangB.getSelectedItem();
        if (a != null) btnTalkA.setText("按住说 " + a.displayName);
        if (b != null) btnTalkB.setText("按住说 " + b.displayName);
    }

    private boolean handleTalkTouch(MotionEvent event, boolean isA) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTalkPressed(isA);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTalkReleased(isA);
                return true;
        }
        return false;
    }

    private void onTalkPressed(boolean isA) {
        if (isRecording) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD_AUDIO);
            return;
        }
        if (!ProviderFactory.isConfigured(this)) {
            Toast.makeText(this, "请先在设置中完成当前方案的配置", Toast.LENGTH_LONG).show();
            return;
        }

        String provider = ProviderFactory.currentProvider(this);
        isRecording = true;
        recordingIsA = isA;
        (isA ? btnTalkA : btnTalkB).setText("正在聆听…");

        if (ProviderFactory.PROVIDER_ONDEVICE.equals(provider)) {
            if (!OnDeviceSpeechHelper.isAvailable(this)) {
                Toast.makeText(this, "当前设备不支持系统语音识别", Toast.LENGTH_LONG).show();
                isRecording = false;
                updateTalkButtonLabels();
                return;
            }
            Language langA = (Language) spinnerLangA.getSelectedItem();
            Language langB = (Language) spinnerLangB.getSelectedItem();
            Language source = isA ? langA : langB;
            Language target = isA ? langB : langA;
            onDeviceSpeechHelper.startListening(source.androidLocale, new OnDeviceSpeechHelper.Callback() {
                @Override
                public void onResult(String text) {
                    isRecording = false;
                    updateTalkButtonLabels();
                    processOnDeviceText(text, source, target, isA);
                }

                @Override
                public void onError(String message) {
                    isRecording = false;
                    updateTalkButtonLabels();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            boolean started = audioRecorder.start(this);
            if (!started) {
                Toast.makeText(this, "无法启动录音", Toast.LENGTH_SHORT).show();
                isRecording = false;
                updateTalkButtonLabels();
            }
        }
    }

    private void onTalkReleased(boolean isA) {
        if (!isRecording) return;
        String provider = ProviderFactory.currentProvider(this);

        if (ProviderFactory.PROVIDER_ONDEVICE.equals(provider)) {
            // 松手时让系统识别器立即结束并通过上面注册的回调返回结果
            onDeviceSpeechHelper.stopListening();
            return;
        }

        isRecording = false;
        updateTalkButtonLabels();
        byte[] pcm = audioRecorder.stop();
        if (pcm.length < 3200) { // 时长过短（约0.1秒以内），当作误触忽略
            return;
        }
        processCloudAudio(pcm, recordingIsA);
    }

    /** 本地识别 + 百度翻译 + 本地朗读 */
    private void processOnDeviceText(String transcript, Language source, Language target, boolean spokeA) {
        if (transcript == null || transcript.trim().isEmpty()) {
            Toast.makeText(this, "未识别到语音，请再说一次", Toast.LENGTH_SHORT).show();
            return;
        }
        String appId = ProviderFactory.getBaiduAppId(this);
        String secretKey = ProviderFactory.getBaiduSecretKey(this);

        executor.submit(() -> {
            try {
                String translated = BaiduTranslateClient.translate(
                        transcript, appId, secretKey, source.baiduCode, target.baiduCode);

                ChatMessage msg = new ChatMessage(
                        transcript, source.displayName,
                        translated, target.displayName,
                        spokeA);

                mainHandler.post(() -> {
                    messages.add(msg);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                    boolean spoke = onDeviceTtsHelper.speak(translated, target.androidLocale);
                    if (!spoke) {
                        Toast.makeText(this, "本机没有 " + target.displayName + " 的朗读语音包，仅显示文字", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "翻译出错: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    /** Google / Azure 云端识别 + 翻译 + 朗读 */
    private void processCloudAudio(byte[] pcm, boolean spokeA) {
        Language langA = (Language) spinnerLangA.getSelectedItem();
        Language langB = (Language) spinnerLangB.getSelectedItem();
        Language source = spokeA ? langA : langB;
        Language target = spokeA ? langB : langA;
        TranslationProvider provider = ProviderFactory.create(this);

        executor.submit(() -> {
            try {
                String transcript = provider.recognize(pcm, source);
                if (transcript == null || transcript.trim().isEmpty()) {
                    mainHandler.post(() -> Toast.makeText(this, "未识别到语音，请靠近麦克风重试", Toast.LENGTH_SHORT).show());
                    return;
                }

                String translated = provider.translate(transcript, source, target);

                ChatMessage msg = new ChatMessage(
                        transcript, source.displayName,
                        translated, target.displayName,
                        spokeA);

                mainHandler.post(() -> {
                    messages.add(msg);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    recyclerChat.scrollToPosition(messages.size() - 1);
                });

                byte[] mp3 = provider.synthesize(translated, target);
                playAudio(mp3);

            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "出错: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void playAudio(byte[] mp3) {
        try {
            File tmp = File.createTempFile("dayin_tts", ".mp3", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(mp3);
            }
            mainHandler.post(() -> {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(tmp.getAbsolutePath());
                    mediaPlayer.setOnCompletionListener(mp -> tmp.delete());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_RECORD_AUDIO
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "已获得录音权限，请再次按住说话", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        if (mediaPlayer != null) mediaPlayer.release();
        if (onDeviceSpeechHelper != null) onDeviceSpeechHelper.destroy();
        if (onDeviceTtsHelper != null) onDeviceTtsHelper.shutdown();
    }
}
