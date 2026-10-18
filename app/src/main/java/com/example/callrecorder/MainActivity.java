package com.example.callrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("مسجل الصوت");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView message = new TextView(this);
        message.setText(
            "مرحباً بك\\n\\n" +
            "هذه نسخة تجريبية لإدارة التسجيلات الصوتية.\\n" +
            "لا تسجل المكالمات تلقائياً حالياً."
        );
        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);

        layout.addView(title);
        layout.addView(message);

        setContentView(layout);
    }
}
