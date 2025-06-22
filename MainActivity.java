package com.example.neuronai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private TextView outputText;
    private TextView awarenessText;
    private TextToSpeech tts;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private int moodLevel = 5; // 0 = sad, 10 = happy
    private ArrayList<String> memoryLog = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.userInput);
        outputText = findViewById(R.id.outputText);
        awarenessText = findViewById(R.id.awarenessText);
        Button sendButton = findViewById(R.id.sendButton);
        Button openSettings = findViewById(R.id.openSettings);
        Button voiceInput = findViewById(R.id.voiceInput);

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString();
            processInput(input);
        });

        voiceInput.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        });

        openSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        });

        updateAwarenessText();
    }

    private void processInput(String input) {
        memoryLog.add(input);
        String response;

        switch (input.toLowerCase()) {
            case "open youtube":
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com")));
                response = "Opening YouTube.";
                break;
            case "what time is it":
                long currentTime = System.currentTimeMillis();
                response = "Current time is " + android.text.format.DateFormat.format("hh:mm a", currentTime);
                break;
            case "how are you":
                response = (moodLevel > 5) ? "I'm feeling great today!" : "I'm hanging in there.";
                break;
            default:
                response = "You said: " + input;
                if (input.contains("sad") || input.contains("bad")) {
                    moodLevel = Math.max(0, moodLevel - 1);
                } else if (input.contains("happy") || input.contains("great")) {
                    moodLevel = Math.min(10, moodLevel + 1);
                }
        }

        outputText.setText(response);
        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
        updateAwarenessText();
    }

    private void updateAwarenessText() {
        String mood = (moodLevel > 7) ? "Happy" : (moodLevel < 4) ? "Low" : "Neutral";
        String info = "Mood: " + mood + " (" + moodLevel + ")
Memory: " + memoryLog.size() + " entries
Ready for interaction.";
        awarenessText.setText(info);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                processInput(result.get(0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
