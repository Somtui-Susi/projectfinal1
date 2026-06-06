package com.example.project;

public class HistoryItem {

    private int id;
    private String topic;
    private String score;
    private String date;

    public HistoryItem(int id, String topic, String score, String date) {
        this.id = id;
        this.topic = topic;
        this.score = score;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getScore() {
        return score;
    }

    public String getDate() {
        return date;
    }
}