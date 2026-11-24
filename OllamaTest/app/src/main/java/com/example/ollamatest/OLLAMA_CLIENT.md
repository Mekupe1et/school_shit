# OllamaOkHttpClient

A small Ollama client for Android and plain Java, built on OkHttp. It supports chat, streaming chat, and one-shot generation, and it keeps the model's **thinking** separate from its **answer**.

## Files

| File | Purpose |
|------|---------|
| `OllamaOkHttpClient.java` | The client |
| `ThinkSplitter.java` | Helper that separates inline `<think>...</think>` text. Keep it in the same package |

Both use the package `com.example.ollama`. Change it to your own if you like.

## Setup

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")   // 2.8.6 or newer
}
```

Android also needs:

- `<uses-permission android:name="android.permission.INTERNET" />` in the manifest
- Cleartext HTTP allowed for your server (`android:usesCleartextTraffic="true"`, or a network security config limited to your server)
- Java 8 or newer language level (the code uses an interface default method)
- For release builds, if replies arrive empty, add `-keep class com.example.ollama.OllamaOkHttpClient$Message { *; }` to your R8/ProGuard rules

## Quick start

```java
OllamaOkHttpClient ollama = new OllamaOkHttpClient("http://MYPC:11434");

OllamaOkHttpClient.Reply r = ollama.chat("lfm2.5:8b",
        Arrays.asList(OllamaOkHttpClient.Message.user("What is 2+2?")));

String answer = r.content;     // the actual answer
String reasoning = r.thinking; // the model's reasoning ("" if it did not think)
```

### Streaming

```java
ollama.chatStream("lfm2.5:8b", messages, new OllamaOkHttpClient.StreamListener() {
    @Override public void onThinking(String token) { /* show or hide the reasoning */ }
    @Override public void onContent(String token)  { /* append to the answer */ }
});
```

If you only want the answer, pass a lambda. Thinking tokens are then skipped:

```java
ollama.chatStream("lfm2.5:8b", messages, token -> append(token));
```

### Conversation history

Send the whole conversation each time. The server keeps no state.

```java
List<Message> history = new ArrayList<>();
history.add(Message.system("You are concise."));
history.add(Message.user("Hi!"));
Reply r = ollama.chat(model, history);
history.add(Message.assistant(r.content));   // keep the answer, not the thinking
```

### Kotlin (coroutines)

Every call blocks, so run it off the main thread:

```kotlin
val reply = withContext(Dispatchers.IO) { ollama.chat(model, messages) }
```

## API

| Method | Returns | Description |
|--------|---------|-------------|
| `new OllamaOkHttpClient(baseUrl)` | | Base URL such as `http://MYPC:11434`. A trailing `/` is accepted |
| `version()` | `String` | Server version. Use it as a connectivity check |
| `chat(model, messages)` | `Reply` | One-shot chat |
| `chatStream(model, messages, listener)` | `Reply` | Streaming chat. Returns the full reply when finished |
| `generate(model, prompt)` | `Reply` | One-shot completion without chat history |
| `setThink(Boolean)` | `void` | `false` = ask the model not to think, `true` = ask it to think, `null` = let Ollama decide (default) |
| `cancel()` | `void` | Aborts requests in flight. A blocked call then throws `IOException` |

### Types

- **`Message`**: `role` and `content`. Build one with `Message.system(...)`, `Message.user(...)` or `Message.assistant(...)`.
- **`Reply`**: `thinking` and `content`, both trimmed.
- **`StreamListener`**: `onContent(String)` is required. `onThinking(String)` is optional and does nothing by default.

## Thinking vs. answer

Ollama can deliver reasoning in two ways, and the client handles both:

1. **A separate `thinking` field.** It goes straight into `Reply.thinking`.
2. **Inline tags**, where the content contains `<think>...</think>`. `ThinkSplitter` removes them and routes the text correctly, even when a tag is split across stream chunks.

If the model does not think, `Reply.thinking` is an empty string.

**Speed tip:** reasoning costs extra tokens. On a CPU-only server, a long reasoning pass can add minutes before the answer starts. `setThink(false)` is usually the biggest speed-up for simple questions.

## Errors and threading

- All methods throw `IOException` for network problems, non-2xx responses (for example `HTTP 404: {"error":"model not found"}`), and errors reported inside a stream.
- Asking for `think = true` on a model without thinking support may be rejected by the server.
- **Never call these on the Android main thread.** Streaming callbacks run on the calling (background) thread, so post UI updates back to the main thread yourself.
- Timeouts: 10 seconds to connect, 5 minutes of silence while reading. The long read timeout covers the first model load and slow thinking on a CPU.

## Not included

Tool calling, embeddings, image input, model management (pull, delete, list), and request options such as temperature or context size. The client only covers `/api/version`, `/api/chat` and `/api/generate`. Adding more means building another JSON body and calling `postJson`.

## Testing status

The code was checked against stand-in OkHttp classes and a fake Ollama server, covering streaming, non-streaming, both thinking formats, plain replies, and error handling. It has not been compiled against the real OkHttp library or run on a device, so do a test build before relying on it.
