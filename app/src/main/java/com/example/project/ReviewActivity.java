package com.example.project;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    TextView tvReview;
    ArrayList<QuestionResult> list;

    // 🔥 เพิ่มตัวแปร Static สำหรับรับข้อมูลขนาดใหญ่
    public static ArrayList<QuestionResult> dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        tvReview = findViewById(R.id.tvReview);

        // 1. ลองดึงจาก Static ก่อน (วิธีใหม่ - ปลอดภัย)
        if (dataStore != null) {
            list = dataStore;
            dataStore = null; // เคลียร์ทิ้งหลังใช้เสร็จเพื่อประหยัด RAM
        } 
        // 2. ถ้าไม่มี ค่อยดึงจาก Intent (วิธีเก่า - สำรอง)
        else {
            String json = getIntent().getStringExtra("data");
            if (json != null) {
                Type type = new TypeToken<ArrayList<QuestionResult>>(){}.getType();
                list = new Gson().fromJson(json, type);
            }
        }

        if (list == null || list.isEmpty()) {
            tvReview.setText("ไม่พบข้อมูลเฉลย");
            return;
        }

        showReview();
    }

    void showReview() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            QuestionResult q = list.get(i);
            if (q == null) continue;

            sb.append("ข้อที่ ").append(i + 1).append(": ").append(q.getQuestion()).append("\n\n");

            String[] options = q.getOptions();
            if (options != null) {
                for (int j = 0; j < options.length; j++) {
                    String prefix = (char) ('A' + j) + ") ";
                    sb.append(prefix).append(options[j]).append("\n");
                }
                sb.append("\n");

                // ตรวจสอบ Index ป้องกันแอปค้าง
                int correctIdx = q.getCorrectAnswer();
                int userIdx = q.getUserAnswer();

                if (correctIdx >= 0 && correctIdx < options.length) {
                    sb.append("✔ คำตอบที่ถูก: ").append(options[correctIdx]).append("\n");
                } else {
                    sb.append("✔ คำตอบที่ถูก: [ข้อมูลไม่ถูกต้อง]\n");
                }

                if (userIdx >= 0 && userIdx < options.length) {
                    sb.append("👤 คำตอบของคุณ: ").append(options[userIdx]);
                    if (userIdx == correctIdx) {
                        sb.append(" ✅ (ถูกต้อง)\n");
                    } else {
                        sb.append(" ❌ (ผิด)\n");
                    }
                } else {
                    sb.append("👤 คำตอบของคุณ: [ไม่ได้เลือกตอบ]\n");
                }
            } else {
                sb.append("[ไม่พบตัวเลือกคำตอบ]\n");
            }

            sb.append("\n----------------------------------\n\n");
        }

        tvReview.setText(sb.toString());
    }
}