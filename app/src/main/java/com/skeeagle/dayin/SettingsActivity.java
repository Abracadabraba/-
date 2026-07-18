package com.skeeagle.dayin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = ProviderFactory.PREFS;

    private RadioGroup providerGroup;
    private EditText baiduAppIdInput, baiduSecretKeyInput;
    private EditText googleKeyInput;
    private EditText azureSpeechKeyInput, azureSpeechRegionInput;
    private EditText azureTranslatorKeyInput, azureTranslatorRegionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        providerGroup = findViewById(R.id.providerGroup);
        baiduAppIdInput = findViewById(R.id.baiduAppIdInput);
        baiduSecretKeyInput = findViewById(R.id.baiduSecretKeyInput);
        googleKeyInput = findViewById(R.id.googleKeyInput);
        azureSpeechKeyInput = findViewById(R.id.azureSpeechKeyInput);
        azureSpeechRegionInput = findViewById(R.id.azureSpeechRegionInput);
        azureTranslatorKeyInput = findViewById(R.id.azureTranslatorKeyInput);
        azureTranslatorRegionInput = findViewById(R.id.azureTranslatorRegionInput);
        Button saveButton = findViewById(R.id.saveButton);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String provider = ProviderFactory.currentProvider(this);
        int checkedId;
        if (ProviderFactory.PROVIDER_AZURE.equals(provider)) {
            checkedId = R.id.radioAzure;
        } else if (ProviderFactory.PROVIDER_GOOGLE.equals(provider)) {
            checkedId = R.id.radioGoogle;
        } else {
            checkedId = R.id.radioOnDevice;
        }
        ((RadioButton) findViewById(checkedId)).setChecked(true);

        baiduAppIdInput.setText(prefs.getString("baidu_app_id", ""));
        baiduSecretKeyInput.setText(prefs.getString("baidu_secret_key", ""));
        googleKeyInput.setText(prefs.getString("google_api_key", ""));
        azureSpeechKeyInput.setText(prefs.getString("azure_speech_key", ""));
        azureSpeechRegionInput.setText(prefs.getString("azure_speech_region", ""));
        azureTranslatorKeyInput.setText(prefs.getString("azure_translator_key", ""));
        azureTranslatorRegionInput.setText(prefs.getString("azure_translator_region", ""));

        saveButton.setOnClickListener(v -> {
            String selectedProvider;
            int checked = providerGroup.getCheckedRadioButtonId();
            if (checked == R.id.radioAzure) {
                selectedProvider = ProviderFactory.PROVIDER_AZURE;
            } else if (checked == R.id.radioGoogle) {
                selectedProvider = ProviderFactory.PROVIDER_GOOGLE;
            } else {
                selectedProvider = ProviderFactory.PROVIDER_ONDEVICE;
            }

            prefs.edit()
                    .putString("provider", selectedProvider)
                    .putString("baidu_app_id", baiduAppIdInput.getText().toString().trim())
                    .putString("baidu_secret_key", baiduSecretKeyInput.getText().toString().trim())
                    .putString("google_api_key", googleKeyInput.getText().toString().trim())
                    .putString("azure_speech_key", azureSpeechKeyInput.getText().toString().trim())
                    .putString("azure_speech_region", azureSpeechRegionInput.getText().toString().trim())
                    .putString("azure_translator_key", azureTranslatorKeyInput.getText().toString().trim())
                    .putString("azure_translator_region", azureTranslatorRegionInput.getText().toString().trim())
                    .apply();
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
