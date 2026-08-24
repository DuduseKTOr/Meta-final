package com.cockpit.scanner;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Observation-only diagnostics service. It never performs actions, gestures, clicks, intents,
 * or UI changes in another app. Raw UI text is intentionally not retained.
 */
public final class RideAccessibilityService extends AccessibilityService {
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;

        String packageName = event.getPackageName().toString();
        String platform = platformFor(packageName);
        boolean supported = platform != null;
        int visibleTextItems = 0;
        boolean rootAvailable = false;

        // Inspect only known driver apps, only long enough to count accessible text nodes.
        // Unknown foreground apps are logged by package/event metadata only.
        if (supported) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                rootAvailable = true;
                visibleTextItems = countVisibleTextItems(root);
                root.recycle();
            }
        }

        CaptureStore.recordEvent(
                packageName,
                AccessibilityEvent.eventTypeToString(event.getEventType()),
                platform,
                rootAvailable,
                visibleTextItems);
    }

    @Override public void onInterrupt() { /* No feedback channel to stop. */ }

    private static String platformFor(String packageName) {
        if (packageName.equals("com.ubercab.driver")) return "Uber Driver";
        if (packageName.equals("com.app99.driver")) return "99 Motorista";
        // inDrive uses one package for rider and driver modes.
        if (packageName.equals("sinet.startup.inDriver")) return "inDrive";
        return null;
    }

    private static int countVisibleTextItems(AccessibilityNodeInfo node) {
        if (node == null) return 0;
        int count = 0;
        if (node.isVisibleToUser() && (hasText(node.getText())
                || hasText(node.getContentDescription()))) {
            count++;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                count += countVisibleTextItems(child);
                child.recycle();
            }
        }
        return count;
    }

    private static boolean hasText(CharSequence value) {
        return value != null && value.toString().trim().length() > 0;
    }
}
