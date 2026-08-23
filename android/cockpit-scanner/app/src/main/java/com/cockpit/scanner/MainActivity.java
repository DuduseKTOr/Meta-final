package com.cockpit.scanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
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

        TextView title = text("Cockpit Scanner", 24, Color.WHITE);
        column.addView(title);
        TextView subtitle = text("Laboratório somente leitura — Uber, 99 e inDrive", 14, Color.LTGRAY);
        column.addView(subtitle);
        TextView warning = text("Este app apenas observa textos acessíveis. Ele não toca, aceita, recusa ou controla corridas.", 14, Color.rgb(129, 199, 132));
        warning.setPadding(0, dp(12), 0, dp(12));
        column.addView(warning);

        Button settings = new Button(this);
        settings.setText("ATIVAR SERVIÇO DE LEITURA");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        column.addView(settings);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        countView = text("0 capturas", 14, Color.LTGRAY);
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
            if (countView != null) countView.setText(CaptureStore.count() + " captura(s) nesta sessão");
            if (captureView != null) captureView.setText(CaptureStore.snapshot());
        });
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
