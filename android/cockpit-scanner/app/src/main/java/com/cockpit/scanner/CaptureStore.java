package com.cockpit.scanner;

import java.text.DateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** In-memory technical diagnostics. Raw UI text is never written to disk or sent over a network. */
public final class CaptureStore {
    public interface Listener { void onCapturesChanged(); }

    private static final int MAX_ENTRIES = 80;
    private static final ArrayDeque<String> entries = new ArrayDeque<>();
    private static final Map<String, Integer> eventsByPackage = new LinkedHashMap<>();
    private static Listener listener;
    private static int eventCount;
    private static int supportedObservationCount;
    private static String lastForegroundPackage = "nenhum ainda";

    private CaptureStore() { }

    public static synchronized void recordEvent(String packageName, String event, String platform,
            boolean rootAvailable, int visibleTextItems) {
        eventCount++;
        eventsByPackage.put(packageName, eventsFor(packageName) + 1);

        boolean windowChanged = event.contains("WINDOW_STATE_CHANGED");
        if (windowChanged) lastForegroundPackage = packageName;

        if (platform != null) {
            supportedObservationCount++;
            addEntry(timestamp() + "  •  " + platform + "  •  " + event
                    + "\nDiagnóstico: raiz " + (rootAvailable ? "disponível" : "indisponível")
                    + "; " + visibleTextItems
                    + " nó(s) visível(is) com texto. Conteúdo não é armazenado.");
        } else if (windowChanged) {
            addEntry(timestamp() + "  •  pacote em primeiro plano"
                    + "\n" + packageName + "  •  " + event
                    + "\nDiagnóstico: pacote ainda não reconhecido; nenhum texto foi lido.");
        }
        notifyListener();
    }

    public static synchronized String summary() {
        StringBuilder result = new StringBuilder()
                .append(eventCount).append(" evento(s) recebido(s) • ")
                .append(supportedObservationCount).append(" observação(ões) de app reconhecido\n")
                .append("Último pacote em primeiro plano: ").append(lastForegroundPackage);
        if (!eventsByPackage.isEmpty()) {
            result.append("\nEventos por pacote:");
            for (Map.Entry<String, Integer> item : eventsByPackage.entrySet()) {
                result.append("\n• ").append(item.getKey()).append(": ").append(item.getValue());
            }
        }
        return result.toString();
    }

    public static synchronized String snapshot() {
        if (entries.isEmpty()) {
            return "Ainda não há diagnósticos exibíveis. Ative o serviço e abra qualquer app; "
                    + "a tela mostrará o pacote em primeiro plano mesmo se ele não for reconhecido.";
        }
        StringBuilder result = new StringBuilder();
        for (String entry : entries) result.append(entry).append("\n\n────────────\n\n");
        return result.toString();
    }

    public static synchronized int eventCount() { return eventCount; }
    public static synchronized int observationCount() { return supportedObservationCount; }

    public static synchronized void clear() {
        entries.clear();
        eventsByPackage.clear();
        eventCount = 0;
        supportedObservationCount = 0;
        lastForegroundPackage = "nenhum ainda";
        notifyListener();
    }

    public static synchronized void setListener(Listener value) { listener = value; }

    private static int eventsFor(String packageName) {
        Integer value = eventsByPackage.get(packageName);
        return value == null ? 0 : value;
    }

    private static String timestamp() {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM, new Locale("pt", "BR"))
                .format(new Date());
    }

    private static void addEntry(String entry) {
        while (entries.size() >= MAX_ENTRIES) entries.removeLast();
        entries.addFirst(entry);
    }

    private static void notifyListener() {
        if (listener != null) listener.onCapturesChanged();
    }
}
