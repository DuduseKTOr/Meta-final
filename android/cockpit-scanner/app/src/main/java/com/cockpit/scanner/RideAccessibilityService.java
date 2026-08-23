package com.cockpit.scanner;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Observation-only service. It reads accessible UI text from the selected apps and never calls
 * performAction(), dispatchGesture(), launches intents, or changes another application's UI.
 */
public final class RideAccessibilityService extends AccessibilityService {
    private static final int MAX_TEXT_ITEMS = 40;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String platform = platformFor(event.getPackageName().toString());
        if (platform == null) return;

        Set<String> text = new LinkedHashSet<>();
        for (CharSequence value : event.getText()) addText(text, value);

        // Reading the active window's node tree is passive; no action is performed on nodes.
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            collectVisibleText(root, text);
            root.recycle();
        }
        CaptureStore.add(platform, AccessibilityEvent.eventTypeToString(event.getEventType()),
                new ArrayList<>(text));
    }

    @Override public void onInterrupt() { /* No feedback channel to stop. */ }

    private static String platformFor(String packageName) {
        if (packageName.equals("com.ubercab")) return "Uber";
        if (packageName.equals("com.taxis99")) return "99";
        if (packageName.equals("sinet.startup.inDriver")) return "inDrive";
        return null;
    }

    private static void collectVisibleText(AccessibilityNodeInfo node, Set<String> out) {
        if (out.size() >= MAX_TEXT_ITEMS || node == null) return;
        if (node.isVisibleToUser()) {
            addText(out, node.getText());
            addText(out, node.getContentDescription());
        }
        for (int i = 0; i < node.getChildCount() && out.size() < MAX_TEXT_ITEMS; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectVisibleText(child, out);
                child.recycle();
            }
        }
    }

    private static void addText(Set<String> out, CharSequence value) {
        if (value == null) return;
        String normalized = value.toString().trim().replaceAll("\\s+", " ");
        if (!normalized.isEmpty()) out.add(normalized);
    }
}
