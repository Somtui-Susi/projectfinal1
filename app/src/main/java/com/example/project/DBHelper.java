package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "exam.db", null, 2); // 🔥 อัปเกรดเป็นเวอร์ชัน 2 เพื่อให้โครงสร้างตารางใหม่ทำงาน
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "topic TEXT," +
                "score TEXT," +
                "date TEXT," +
                "details TEXT)"); // 🔥 เพิ่มคอลัมน์เก็บข้อมูลเฉลย
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    // บันทึกข้อมูล
    public void insertHistory(String topic, String score, String date, String details) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("topic", topic);
        values.put("score", score);
        values.put("date", date);
        values.put("details", details); // 🔥 บันทึก JSON เฉลยลงไปด้วย

        db.insert("history", null, values);
    }
    public void deleteHistory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("history", "id=?", new String[]{String.valueOf(id)});
    }

    //ดึงข้อมูล
    public Cursor getAllHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM history ORDER BY id DESC", null);
    }
}