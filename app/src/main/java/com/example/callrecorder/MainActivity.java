package com.example.callrecorder;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final int REQ_PICK_AUDIO = 101;

    private TextView status;
    private Button pauseBtn;
    private Button stopBtn;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setGravity(Gravity.TOP);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("مسجل الصوت");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView note = new TextView(this);
        note.setText("نسخة تجريبية: اختر ملفاً صوتياً محفوظاً لتشغيله. هذه النسخة لا تسجل المكالمات الهاتفية تلقائياً.");
        note.setTextSize(16);
        note.setPadding(0, 28, 0, 24);
        root.addView(note);

        Button choose = new Button(this);
        choose.setText("اختيار تسجيل صوتي");
        choose.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            startActivityForResult(intent, REQ_PICK_AUDIO);
        });
        root.addView(choose, new LinearLayout.LayoutParams(-1, -2));

        pauseBtn = new Button(this);
        pauseBtn.setText("إيقاف مؤقت");
        pauseBtn.setEnabled(false);
        pauseBtn.setOnClickListener(v -> togglePause());
        root.addView(pauseBtn, new LinearLayout.LayoutParams(-1, -2));

        stopBtn = new Button(this);
        stopBtn.setText("إيقاف");
        stopBtn.setEnabled(false);
        stopBtn.setOnClickListener(v -> {
            releasePlayer();
            status.setText("تم إيقاف التشغيل.");
        });
        root.addView(stopBtn, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("لم يتم اختيار ملف");
        status.setTextSize(15);
        status.setPadding(0, 24, 0, 24);
        root.addView(status);

        setContentView(root);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_AUDIO || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        releasePlayer();
        final String name = displayName(uri);
        try {
            player = new MediaPlayer();
            player.setDataSource(this, uri);
            player.setOnPreparedListener(mp -> {
                mp.start();
                setControls(true);
                status.setText("يتم تشغيل: " + name);
            });
            player.setOnCompletionListener(mp -> {
                setControls(false);
                status.setText("انتهى تشغيل الملف.");
            });
            player.setOnErrorListener((mp, what, extra) -> {
                releasePlayer();
                status.setText("تعذر تشغيل هذا الملف.");
                return true;
            });
            status.setText("جاري التحميل...");
            player.prepareAsync();
        } catch (IOException | RuntimeException e) {
            releasePlayer();
            status.setText("تعذر فتح الملف الصوتي.");
            Toast.makeText(this, "تعذر تشغيل الملف", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePause() {
        if (player == null) return;
        try {
            if (player.isPlaying()) {
                player.pause();
                pauseBtn.setText("متابعة");
                status.setText("متوقف مؤقتاً.");
            } else {
                player.start();
                pauseBtn.setText("إيقاف مؤقت");
                status.setText("يتم التشغيل...");
            }
        } catch (IllegalStateException ignored) {
            releasePlayer();
        }
    }

    private void setControls(boolean active) {
        pauseBtn.setEnabled(active);
        stopBtn.setEnabled(active);
        pauseBtn.setText("إيقاف مؤقت");
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (pauseBtn != null) setControls(false);
    }

    private String displayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) return c.getString(i);
            }
        } catch (RuntimeException ignored) { }
        return "الملف المحدد";
    }

    @Override
    protected void onStop() {
        if (player != null && player.isPlaying()) {
            player.pause();
            pauseBtn.setText("متابعة");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
