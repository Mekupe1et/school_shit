package com.example.ollamatest;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String PREFERENCES = "ollama_chat";
    private static final String DEFAULT_SERVER = "http://10.0.2.2:11434";
    private static final String DEFAULT_MODEL = "lfm2.5:8b";
    private static final String LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<OllamaOkHttpClient.Message> history = new ArrayList<>();

    private EditText serverUrlInput;
    private EditText modelInput;
    private EditText messageInput;
    private Button connectButton;
    private Button sendButton;
    private TextView statusText;
    private LinearLayout messagesContainer;
    private ScrollView messagesScroll;
    private ActivityResultLauncher<String> localNetworkPermissionLauncher;
    private Runnable pendingNetworkAction;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localNetworkPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    Runnable action = pendingNetworkAction;
                    pendingNetworkAction = null;
                    if (granted && action != null) {
                        action.run();
                    } else {
                        setBusy(false);
                        showStatus(getString(R.string.error_local_network_permission), true);
                    }
                });
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        serverUrlInput = findViewById(R.id.serverUrlInput);
        modelInput = findViewById(R.id.modelInput);
        messageInput = findViewById(R.id.messageInput);
        connectButton = findViewById(R.id.connectButton);
        sendButton = findViewById(R.id.sendButton);
        statusText = findViewById(R.id.statusText);
        messagesContainer = findViewById(R.id.messagesContainer);
        messagesScroll = findViewById(R.id.messagesScroll);

        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        serverUrlInput.setText(preferences.getString("server", DEFAULT_SERVER));
        modelInput.setText(preferences.getString("model", DEFAULT_MODEL));

        addMessage(getString(R.string.welcome_message), false);
        connectButton.setOnClickListener(v -> testConnection());
        sendButton.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void testConnection() {
        final String server;
        try {
            server = getServerUrl();
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
            return;
        }

        runWithLocalNetworkPermission(() -> connectToServer(server));
    }

    private void connectToServer(String server) {
        saveSettings(server, modelInput.getText().toString().trim());
        setBusy(true);
        showStatus(getString(R.string.status_connecting), false);
        executor.execute(() -> {
            try {
                String version = new OllamaOkHttpClient(server).version();
                runOnUiThread(() -> {
                    setBusy(false);
                    showStatus(getString(R.string.status_connected, version), false);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setBusy(false);
                    showStatus(getString(R.string.status_connection_failed, errorMessage(e)), true);
                });
            }
        });
    }

    private void sendMessage() {
        if (busy) return;

        final String server;
        try {
            server = getServerUrl();
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), true);
            return;
        }

        String model = modelInput.getText().toString().trim();
        if (model.isEmpty()) {
            showStatus(getString(R.string.error_model_required), true);
            modelInput.requestFocus();
            return;
        }

        String prompt = messageInput.getText().toString().trim();
        if (prompt.isEmpty()) return;

        runWithLocalNetworkPermission(() -> sendPrompt(server, model, prompt));
    }

    private void sendPrompt(String server, String model, String prompt) {
        saveSettings(server, model);
        messageInput.setText("");
        addMessage(prompt, true);
        history.add(OllamaOkHttpClient.Message.user(prompt));
        setBusy(true);
        showStatus(getString(R.string.status_generating, model), false);

        List<OllamaOkHttpClient.Message> requestHistory = new ArrayList<>(history);
        StringBuilder answer = new StringBuilder();
        TextView answerView = addMessage("", false);
        executor.execute(() -> {
            try {
                OllamaOkHttpClient.Reply reply = new OllamaOkHttpClient(server).chatStream(
                        model,
                        requestHistory,
                        new OllamaOkHttpClient.StreamListener() {
                            @Override
                            public void onContent(String token) {
                                runOnUiThread(() -> {
                                    answer.append(token);
                                    answerView.setText(answer);
                                    scrollToBottom();
                                });
                            }
                        });
                runOnUiThread(() -> {
                    if (answer.length() == 0) {
                        answerView.setText(getString(R.string.empty_answer));
                    }
                    history.add(OllamaOkHttpClient.Message.assistant(reply.content));
                    setBusy(false);
                    showStatus(getString(R.string.status_ready), false);
                    scrollToBottom();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    if (history.size() > 0) history.remove(history.size() - 1);
                    String error = getString(R.string.error_request_failed, errorMessage(e));
                    if (answer.length() == 0) {
                        answerView.setText(error);
                        answerView.setTextColor(getColor(R.color.error));
                    } else {
                        answer.append("\n\n").append(error);
                        answerView.setText(answer);
                    }
                    setBusy(false);
                    showStatus(getString(R.string.status_request_failed), true);
                    scrollToBottom();
                });
            }
        });
    }

    private void runWithLocalNetworkPermission(Runnable action) {
        if (Build.VERSION.SDK_INT < 37
                || checkSelfPermission(LOCAL_NETWORK_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            action.run();
            return;
        }

        pendingNetworkAction = action;
        setBusy(true);
        showStatus(getString(R.string.status_requesting_local_network_permission), false);
        localNetworkPermissionLauncher.launch(LOCAL_NETWORK_PERMISSION);
    }

    private String getServerUrl() {
        String value = serverUrlInput.getText().toString().trim();
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        if (value.isEmpty() || uri.getHost() == null
                || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getQuery() != null || uri.getFragment() != null
                || (uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath()))) {
            throw new IllegalArgumentException(getString(R.string.error_invalid_server));
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private void saveSettings(String server, String model) {
        getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit()
                .putString("server", server)
                .putString("model", model)
                .apply();
    }

    private TextView addMessage(String text, boolean user) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(15);
        bubble.setTextColor(getColor(user ? R.color.user_message_text : R.color.text_primary));
        bubble.setBackgroundResource(user ? R.drawable.bg_user_bubble : R.drawable.bg_assistant_bubble);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.78f));
        bubble.setTextIsSelectable(true);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.gravity = user ? Gravity.END : Gravity.START;
        bubbleParams.topMargin = dp(5);
        bubbleParams.bottomMargin = dp(5);
        messagesContainer.addView(bubble, bubbleParams);
        scrollToBottom();
        return bubble;
    }

    private void setBusy(boolean value) {
        busy = value;
        connectButton.setEnabled(!value);
        sendButton.setEnabled(!value);
        sendButton.setAlpha(value ? 0.6f : 1f);
        connectButton.setAlpha(value ? 0.6f : 1f);
    }

    private void showStatus(String text, boolean error) {
        statusText.setText(text);
        statusText.setTextColor(getColor(error ? R.color.error : R.color.text_secondary));
    }

    private void scrollToBottom() {
        messagesScroll.post(() -> messagesScroll.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static String errorMessage(IOException e) {
        String message = e.getMessage();
        return message == null || message.trim().isEmpty() ? "Unknown error" : message;
    }
}
