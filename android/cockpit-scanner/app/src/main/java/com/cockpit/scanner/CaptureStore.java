package com.cockpit.scanner;

import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Process-memory debug buffer. No capture is written to disk or sent over the network. */
public final class CaptureStore {
    public interface Listener { void onCapturesChanged(); }
    private static final int MAX_ENTRIES = 80;
    private static final ArrayDeque<String> entries = new ArrayDeque<>();
    private static Listener listener;

    private CaptureStore() { }

    public static synchronized void add(String platform, String event, List<String> text) {
        if (text.isEmpty()) return;
        String timestamp = DateFormat.getTimeInstance(DateFormat.MEDIUM, new Locale("pt", "BR"))
                .format(new Date());
        StringBuilder entry = new StringBuilder(timestamp)
                .append("  •  ").append(platform).append("  •  ").append(event).append("\n");
        for (String line : text) entry.append(line).append("\n");
        while (entries.size() >= MAX_ENTRIES) entries.removeLast();
        entries.addFirst(entry.toString().trim());
        notifyListener();
    }

    public static synchronized String snapshot() {
        if (entries.isEmpty()) return "Ainda não há capturas. Ative o serviço e abra Uber, 99 ou inDrive.";
        StringBuilder result = new StringBuilder();
        for (String entry : entries) result.append(entry).append("\n\n────────────\n\n");
        return result.toString();
    }

    public static synchronized int count() { return entries.size(); }
    public static synchronized void clear() { entries.clear(); notifyListener(); }
    public static synchronized void setListener(Listener value) { listener = value; }
    private static void notifyListener() { if (listener != null) listener.onCapturesChanged(); }
}
