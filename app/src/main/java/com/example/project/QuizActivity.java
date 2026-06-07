package com.example.project;

import static org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve.q;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.network.GroqAPI;
import com.example.project.network.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    TextView tvQuestion, tvLoading;
    RadioGroup rgAnswers;
    Button btnNext, btnReview;

    ArrayList<Question> questions = new ArrayList<>();
    ArrayList<QuestionResult> reviewList = new ArrayList<>();

    int currentIndex = 0;
    int score = 0;

    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvLoading = findViewById(R.id.tvLoading);
        rgAnswers = findViewById(R.id.rgAnswers);
        btnNext = findViewById(R.id.btnNext);
        btnReview = findViewById(R.id.btnReview);

        content = getIntent().getStringExtra("content");
        if (content == null || content.trim().isEmpty()) {
            content = "Android Programming";
        }

        callAI(content);

        btnNext.setOnClickListener(v -> {

            if (questions.isEmpty()) return;

            int selectedId = rgAnswers.getCheckedRadioButtonId();
            if (selectedId == -1) return;

            int answerIndex = rgAnswers.indexOfChild(findViewById(selectedId));

            Question q = questions.get(currentIndex); // 🔥 ต้องมีบรรทัดนี้

            if (answerIndex == q.getCorrectAnswer()) {
                score++;
            }

            // 🔥 เก็บเฉลย
            reviewList.add(new QuestionResult(
                    q.getQuestion(),
                    q.getOptions(),
                    q.getCorrectAnswer(),
                    answerIndex
            ));

            currentIndex++;

            if (currentIndex < questions.size()) {
                showQuestion();
            } else {
                showScore();
            }
        });
    }

    // ================= SHOW SCORE =================
    private void showScore() {

        // 1. แปลงเฉลยเป็น JSON
        String detailsJson = new Gson().toJson(reviewList);

        // 2. บันทึกประวัติพร้อมรายละเอียด
        HistoryManager.saveHistory(this, content, score, questions.size(), detailsJson);

        // 3. ปรับ UI โชว์คะแนน
        tvQuestion.setText("🎉 เก่งมาก! ทำแบบทดสอบเรียบร้อยแล้ว\n\nคะแนนที่ได้: " + score + " / " + questions.size());
        rgAnswers.setVisibility(View.GONE);

        // 3. ปุ่มกลับหน้าหลัก
        btnNext.setText("กลับสู่หน้าหลัก");
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 4. ปุ่มดูเฉลย
        btnReview.setVisibility(View.VISIBLE);
        btnReview.setOnClickListener(v -> {
            // 🔥 ฝากข้อมูลก้อนใหญ่ไว้ในตัวแปร Static
            ReviewActivity.dataStore = reviewList;
            
            Intent intent = new Intent(this, ReviewActivity.class);
            startActivity(intent);
        });
    }


    // ================= AI CALL =================
    void callAI(String content) {

        tvLoading.setVisibility(View.VISIBLE);
        tvQuestion.setVisibility(View.GONE);
        rgAnswers.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);

        // 1. ทำความสะอาดข้อมูลเบื้องต้น (ลบตัวอักษรควบคุมที่อาจทำให้มีปัญหา)
        String safeContent = content.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ");

        // 2. จำกัดความยาว (2000 ตัวอักษร)
        if (safeContent.length() > 2000) {
            safeContent = safeContent.substring(0, 2000);
        }

        GroqAPI api = RetrofitClient.getClient().create(GroqAPI.class);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.1-8b-instant");
        body.put("temperature", 0.1);

        ArrayList<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a quiz creator. Response must be JSON object.");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "Create a 5-question Thai quiz in JSON format: " +
                "{\"questions\":[{\"question\":\"...\",\"options\":[\"...\",\"...\",\"...\",\"...\"],\"answer\":0}]}. " +
                "Content: " + safeContent);
        messages.add(userMsg);

        body.put("messages", messages);

        // 🔥 เปิด response_format กลับมาเพื่อบังคับ JSON
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        body.put("response_format", responseFormat);

        api.generateQuiz(body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                tvLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = new Gson().toJson(response.body());
                        parseAI(json);

                    } catch (Exception e) {
                        tvQuestion.setVisibility(View.VISIBLE);
                        tvQuestion.setText("Parse Error: " + e.getMessage());
                    }
                } else {
                    tvQuestion.setVisibility(View.VISIBLE);
                    tvQuestion.setText("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                tvLoading.setVisibility(View.GONE);
                tvQuestion.setVisibility(View.VISIBLE);
                tvQuestion.setText("Network Error: " + t.getMessage());
            }
        });
    }

    // ================= PARSE =================
    void parseAI(String json) {

        try {

            JSONObject obj = new JSONObject(json);

            String text = obj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 🔥 ล้าง Markdown เผื่อ AI ใส่มา (เช่น ```json ... ```)
            if (text.contains("```json")) {
                text = text.substring(text.indexOf("```json") + 7, text.lastIndexOf("```"));
            } else if (text.contains("```")) {
                text = text.substring(text.indexOf("```") + 3, text.lastIndexOf("```"));
            }

            JSONObject contentObj = new JSONObject(text.trim());
            JSONArray arr = contentObj.getJSONArray("questions");

            questions.clear();

            for (int i = 0; i < arr.length(); i++) {

                JSONObject q = arr.getJSONObject(i);

                String question = q.getString("question");
                JSONArray options = q.getJSONArray("options");

                String[] opt = new String[4];

                for (int j = 0; j < 4; j++) {
                    opt[j] = options.getString(j);
                }

                int answer = q.getInt("answer");

                questions.add(new Question(question, opt, answer));
            }

            currentIndex = 0;

            tvQuestion.setVisibility(View.VISIBLE);
            rgAnswers.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);

            showQuestion();

        } catch (Exception e) {
            tvQuestion.setVisibility(View.VISIBLE);
            tvQuestion.setText("JSON Error: " + e.getMessage());
        }
    }

    // ================= SHOW =================
    void showQuestion() {

        Question q = questions.get(currentIndex);

        tvQuestion.setText(q.getQuestion());

        ((RadioButton) findViewById(R.id.rb1)).setText(q.getOptions()[0]);
        ((RadioButton) findViewById(R.id.rb2)).setText(q.getOptions()[1]);
        ((RadioButton) findViewById(R.id.rb3)).setText(q.getOptions()[2]);
        ((RadioButton) findViewById(R.id.rb4)).setText(q.getOptions()[3]);

        rgAnswers.clearCheck();
    }
}