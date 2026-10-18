package com.example.callrecorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import java.io.IOException;

public class MainActivity extends Activity {
    private TextView status;
    private MediaPlayer player;

    @Override public void onCreate(Bundle savedInstanceState) {
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
            startActivityForResult(intent, 101);
        });
        root.addView(choose, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("لم يتم اختيار ملف");
        status.setTextSize(15);
        status.setPadding(0, 24, 0, 24);
        root.addView(status);

        setContentView(root);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                if (player != null) { player.release(); player = null; }
                player = new MediaPlayer();
                player.setDataSource(this, uri);
                player.setOnPreparedListener(mp -> {
                    mp.start();
                    status.setText("يتم تشغيل الملف الصوتي المحدد.");
                });
                player.setOnCompletionListener(mp -> status.setText("انتهى تشغيل الملف."));
                player.setOnErrorListener((mp, what, extra) -> {
                    status.setText("تعذر تشغيل هذا الملف.");
                    return true;
                });
                player.prepareAsync();
            } catch (IOException e) {
                status.setText("تعذر فتح الملف الصوتي.");
                Toast.makeText(this, "تعذر تشغيل الملف", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override protected void onDestroy() {
        if (player != null) { player.release(); player = null; }
        super.onDestroy();
    }
}
