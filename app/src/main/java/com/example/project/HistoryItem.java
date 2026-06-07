package com.example.project;

public class HistoryItem {

    private int id;
    private String topic;
    private String score;
    private String date;
    private String details; // 🔥 เพิ่มฟิลด์เก็บ JSON เฉลย

    public HistoryItem(int id, String topic, String score, String date, String details) {
        this.id = id;
        this.topic = topic;
        this.score = score;
        this.date = date;
        this.details = details;
    }

    public int getId() { return id; }
    public String getTopic() { return topic; }
    public String getScore() { return score; }
    public String getDate() { return date; }
    public String getDetails() { return details; } // 🔥 Getter สำหรับเฉลย
}