package com.example.project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryManager {

    public static void saveHistory(QuizActivity activity, String content, int score, int total) {

        // 🔥 กำหนด Topic (ย่อถ้าเกิน 30 ตัวอักษร)
        String topic = content.length() > 30
                ? content.substring(0, 30)
                : content;

        // 🔥 วันที่ปัจจุบัน
        String date = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // 🔥 บันทึกลง SQLite
        DBHelper db = new DBHelper(activity);
        db.insertHistory(
                topic,
                score + "/" + total,
                date
        );
    }
}
