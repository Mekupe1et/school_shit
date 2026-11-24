package com.example.ollamatest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

/**
 * Minimal Ollama client on OkHttp. Works on Android and plain JVM.
 * Needs ThinkSplitter.java from the same package.
 *
 * Gradle:
 *   implementation("com.squareup.okhttp3:okhttp:4.12.0")
 *   implementation("com.google.code.gson:gson:2.11.0")
 *
 * Android notes:
 *  - Every method here blocks: call them from a background thread/coroutine, never the main thread.
 *  - Plain http:// needs cleartext allowed (network security config or usesCleartextTraffic).
 *  - Needs the INTERNET permission.
 *
 * Thinking vs. answer:
 *  Every reply is split into Reply.thinking (the reasoning) and Reply.content (the actual answer).
 *  Both of Ollama's formats are handled: the separate "thinking" field, and inline
 *  <think>...</think> tags inside the content. While streaming, onThinking() and onContent()
 *  are called separately, so a UI can show or hide the reasoning.
 *
 * Usage:
 *   OllamaOkHttpClient ollama = new OllamaOkHttpClient("http://MYPC:11434");
 *   OllamaOkHttpClient.Reply r = ollama.chat("lfm2.5:8b",
 *           Arrays.asList(OllamaOkHttpClient.Message.user("Hello!")));
 *   String answer = r.content;      // r.thinking holds the reasoning
 */
public class OllamaOkHttpClient {

    public static class Message {
        @SerializedName("role")    public final String role;
        @SerializedName("content") public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        public static Message system(String c)    { return new Message("system", c); }
        public static Message user(String c)      { return new Message("user", c); }
        public static Message assistant(String c) { return new Message("assistant", c); }
    }

    /** A finished reply: the reasoning and the answer, kept apart. */
    public static final class Reply {
        /** The model's reasoning; empty if it did not think. */
        public final String thinking;
        /** The actual answer. */
        public final String content;

        Reply(String thinking, String content) {
            this.thinking = thinking.trim();
            this.content = content.trim();
        }
    }

    /** Streaming callbacks. A lambda works if you only care about the answer. */
    public interface StreamListener {
        void onContent(String token);
        default void onThinking(String token) { }
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final String baseUrl;
    private final Gson gson = new Gson();
    private volatile Boolean think; // null = let Ollama/the model decide

    public OllamaOkHttpClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                // time between bytes: covers the first-load delay and pauses while a model thinks
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * false = ask the model not to think (much faster on a CPU-only server);
     * true = ask it to think; null = leave it to Ollama and the model.
     * Models without thinking support may reject true.
     */
    public void setThink(Boolean think) {
        this.think = think;
    }

    /** Connectivity check. Returns the Ollama server version. */
    public String version() throws IOException {
        Request req = new Request.Builder().url(baseUrl + "/api/version").get().build();
        try (Response resp = http.newCall(req).execute()) {
            checkOk(resp);
            return JsonParser.parseString(resp.body().string())
                    .getAsJsonObject().get("version").getAsString();
        }
    }

    /** One-shot chat. */
    public Reply chat(String model, List<Message> messages) throws IOException {
        JsonObject res = postJson("/api/chat", chatBody(model, messages, false));
        Collector c = new Collector(null);
        c.accept(res.getAsJsonObject("message"), "content");
        return c.finish();
    }

    /** Streaming chat: listener gets thinking and answer tokens separately; returns the full Reply. */
    public Reply chatStream(String model, List<Message> messages, StreamListener listener)
            throws IOException {
        Request req = new Request.Builder()
                .url(baseUrl + "/api/chat")
                .post(RequestBody.create(gson.toJson(chatBody(model, messages, true)), JSON))
                .build();

        Collector c = new Collector(listener);
        try (Response resp = http.newCall(req).execute()) {
            checkOk(resp);
            BufferedSource src = resp.body().source();
            while (!src.exhausted()) {
                String line = src.readUtf8Line();   // one JSON object per line
                if (line == null || line.isEmpty()) continue;
                JsonObject o = JsonParser.parseString(line).getAsJsonObject();
                if (o.has("error")) throw new IOException(o.get("error").getAsString());

                JsonObject m = o.getAsJsonObject("message");
                if (m != null) c.accept(m, "content");
                if (o.has("done") && o.get("done").getAsBoolean()) break;
            }
        }
        return c.finish();
    }

    /** One-shot completion without chat history. */
    public Reply generate(String model, String prompt) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("prompt", prompt);
        body.addProperty("stream", false);
        if (think != null) body.addProperty("think", think);
        JsonObject res = postJson("/api/generate", body);
        Collector c = new Collector(null);
        c.accept(res, "response");
        return c.finish();
    }

    /** Aborts any request in flight (a blocked chatStream then throws IOException). */
    public void cancel() {
        http.dispatcher().cancelAll();
    }

    // ---------------------------------------------------------------- helpers

    /** Collects thinking and answer text, and forwards each token to the listener. */
    private static final class Collector implements ThinkSplitter.Sink {
        private final StringBuilder thinking = new StringBuilder();
        private final StringBuilder content = new StringBuilder();
        private final StreamListener listener;
        private final ThinkSplitter splitter = new ThinkSplitter(this);

        Collector(StreamListener listener) {
            this.listener = listener;
        }

        /** Handles one JSON object: a stream chunk, or the whole non-streaming reply. */
        void accept(JsonObject obj, String contentKey) {
            if (obj == null) return;
            String th = str(obj, "thinking");     // Ollama's dedicated reasoning field
            if (th != null) emit(th, true);
            String text = str(obj, contentKey);   // may contain inline <think> tags
            if (text != null) splitter.feed(text);
        }

        @Override
        public void emit(String text, boolean isThinking) {
            if (text.isEmpty()) return;
            if (isThinking) {
                thinking.append(text);
                if (listener != null) listener.onThinking(text);
            } else {
                content.append(text);
                if (listener != null) listener.onContent(text);
            }
        }

        Reply finish() {
            splitter.flush();
            return new Reply(thinking.toString(), content.toString());
        }

        private static String str(JsonObject o, String key) {
            JsonElement e = o.get(key);
            return (e == null || e.isJsonNull()) ? null : e.getAsString();
        }
    }

    private JsonObject chatBody(String model, List<Message> messages, boolean stream) {
        JsonObject b = new JsonObject();
        b.addProperty("model", model);
        b.add("messages", gson.toJsonTree(messages));
        b.addProperty("stream", stream);
        if (think != null) b.addProperty("think", think);
        return b;
    }

    private JsonObject postJson(String path, JsonObject body) throws IOException {
        Request req = new Request.Builder()
                .url(baseUrl + path)
                .post(RequestBody.create(gson.toJson(body), JSON))
                .build();
        try (Response resp = http.newCall(req).execute()) {
            checkOk(resp);
            return JsonParser.parseString(resp.body().string()).getAsJsonObject();
        }
    }

    private static void checkOk(Response resp) throws IOException {
        if (!resp.isSuccessful()) {
            String msg = resp.body() != null ? resp.body().string() : "";
            throw new IOException("HTTP " + resp.code() + ": " + msg);
        }
    }
}
