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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        tvReview = findViewById(R.id.tvReview);

        String json = getIntent().getStringExtra("data");

        if (json == null) {
            tvReview.setText("ไม่พบข้อมูลเฉลย");
            return;
        }

        Type type = new TypeToken<ArrayList<QuestionResult>>(){}.getType();

        list = new Gson().fromJson(json, type);

        if (list == null || list.isEmpty()) {
            tvReview.setText("ไม่มีข้อมูลเฉลย");
            return;
        }

        showReview();
    }

    void showReview() {

        StringBuilder sb = new StringBuilder();

        for (QuestionResult q : list) {

            sb.append("❓ ").append(q.getQuestion()).append("\n");

            sb.append("A) ").append(q.getOptions()[0]).append("\n");
            sb.append("B) ").append(q.getOptions()[1]).append("\n");
            sb.append("C) ").append(q.getOptions()[2]).append("\n");
            sb.append("D) ").append(q.getOptions()[3]).append("\n\n");

            sb.append("✔ คำตอบที่ถูก: ")
                    .append(q.getOptions()[q.getCorrectAnswer()])
                    .append("\n");

            sb.append("👤 คำตอบคุณ: ")
                    .append(q.getOptions()[q.getUserAnswer()]);

            if (q.getUserAnswer() == q.getCorrectAnswer()) {
                sb.append(" ✅ ถูก\n\n");
            } else {
                sb.append(" ❌ ผิด\n\n");
            }

            sb.append("----------------------\n\n");
        }

        tvReview.setText(sb.toString());
    }
}