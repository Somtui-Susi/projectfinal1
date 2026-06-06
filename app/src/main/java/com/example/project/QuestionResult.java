package com.example.project;

public class QuestionResult {

    private String question;
    private String[] options;
    private int correctAnswer;
    private int userAnswer;

    public QuestionResult(String question, String[] options, int correctAnswer, int userAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
    }

    public String getQuestion() { return question; }

    public String[] getOptions() { return options; }

    public int getCorrectAnswer() { return correctAnswer; }

    public int getUserAnswer() { return userAnswer; }
}