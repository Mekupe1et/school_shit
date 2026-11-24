package com.example.ollamatest;

/**
 * Splits streamed text into "thinking" and "answer" parts when a model writes its reasoning inline
 * as <think>...</think> instead of using Ollama's separate "thinking" field.
 * Handles tags that arrive split across chunks (e.g. "<thi" + "nk>").
 */
final class ThinkSplitter {

    interface Sink {
        void emit(String text, boolean thinking);
    }

    private static final String OPEN = "<think>";
    private static final String CLOSE = "</think>";

    private final Sink sink;
    private final StringBuilder pending = new StringBuilder(); // possible start of a tag, held back
    private boolean inThink;

    ThinkSplitter(Sink sink) {
        this.sink = sink;
    }

    void feed(String chunk) {
        String buf = pending.append(chunk).toString();
        pending.setLength(0);
        while (true) {
            String tag = inThink ? CLOSE : OPEN;
            int idx = buf.indexOf(tag);
            if (idx >= 0) {
                emit(buf.substring(0, idx));
                buf = buf.substring(idx + tag.length());
                inThink = !inThink;
                continue;
            }
            int keep = partialSuffix(buf, tag);
            emit(buf.substring(0, buf.length() - keep));
            pending.append(buf, buf.length() - keep, buf.length());
            return;
        }
    }

    /** Call once at the end of the stream to release anything held back. */
    void flush() {
        if (pending.length() > 0) {
            emit(pending.toString());
            pending.setLength(0);
        }
    }

    private void emit(String s) {
        if (!s.isEmpty()) sink.emit(s, inThink);
    }

    /** Length of the longest suffix of buf that is a proper prefix of tag. */
    private static int partialSuffix(String buf, String tag) {
        int max = Math.min(buf.length(), tag.length() - 1);
        for (int n = max; n > 0; n--) {
            if (buf.endsWith(tag.substring(0, n))) return n;
        }
        return 0;
    }
}
