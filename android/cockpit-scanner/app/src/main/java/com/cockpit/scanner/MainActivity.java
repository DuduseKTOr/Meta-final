package com.cockpit.scanner;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public final class MainActivity extends Activity {
    private TextView statusView;
    private TextView summaryView;
    private TextView countView;
    private TextView captureView;
    private final CaptureStore.Listener listener = this::render;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(createContent());
    }

    @Override protected void onResume() { super.onResume(); CaptureStore.setListener(listener); render(); }
    @Override protected void onPause() { CaptureStore.setListener(null); super.onPause(); }

    private View createContent() {
        int pad = dp(16);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(pad, pad, pad, pad);
        column.setBackgroundColor(Color.rgb(16, 17, 20));

        column.addView(text("Cockpit Scanner", 24, Color.WHITE));
        column.addView(text("Diagnóstico somente leitura — Uber Driver, 99 Motorista e inDrive",
                14, Color.LTGRAY));
        TextView warning = text("Não toca, aceita, recusa ou controla outros apps. "
                + "Textos da interface não são armazenados.", 14, Color.rgb(129, 199, 132));
        warning.setPadding(0, dp(12), 0, dp(12));
        column.addView(warning);

        statusView = text("Serviço: verificando…", 15, Color.WHITE);
        statusView.setPadding(0, 0, 0, dp(8));
        column.addView(statusView);

        Button settings = new Button(this);
        settings.setText("ABRIR CONFIGURAÇÕES DE ACESSIBILIDADE");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        column.addView(settings);

        summaryView = text("", 13, Color.LTGRAY);
        summaryView.setPadding(0, dp(8), 0, dp(8));
        summaryView.setTextIsSelectable(true);
        column.addView(summaryView);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        countView = text("", 14, Color.LTGRAY);
        actions.addView(countView, new LinearLayout.LayoutParams(0, -2, 1));
        Button clear = new Button(this);
        clear.setText("LIMPAR");
        clear.setOnClickListener(v -> CaptureStore.clear());
        actions.addView(clear);
        column.addView(actions);

        captureView = text("", 14, Color.WHITE);
        captureView.setTextIsSelectable(true);
        captureView.setPadding(0, dp(8), 0, dp(8));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(captureView);
        column.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return column;
    }

    private void render() {
        runOnUiThread(() -> {
            if (statusView != null) {
                boolean enabled = isScannerServiceEnabled();
                statusView.setText(enabled ? "Serviço: ATIVO ✓" : "Serviço: DESATIVADO");
                statusView.setTextColor(enabled ? Color.rgb(129, 199, 132) : Color.rgb(255, 193, 7));
            }
            if (summaryView != null) summaryView.setText(CaptureStore.summary());
            if (countView != null) countView.setText(CaptureStore.eventCount()
                    + " evento(s) • " + CaptureStore.observationCount() + " reconhecido(s)");
            if (captureView != null) captureView.setText(CaptureStore.snapshot());
        });
    }

    private boolean isScannerServiceEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> services = manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : services) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null
                    && getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)
                    && RideAccessibilityService.class.getName().equals(
                    info.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
