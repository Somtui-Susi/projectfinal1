package com.example.project;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<HistoryItem> historyList;
    HistoryAdapter adapter;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerHistory);

        dbHelper = new DBHelper(this);

        historyList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {

        historyList.clear(); // 🔥 สำคัญมาก

        Cursor cursor = dbHelper.getAllHistory();

        if (cursor.getCount() == 0) return;

        while (cursor.moveToNext()) {

            int id = cursor.getInt(0);
            String topic = cursor.getString(1);
            String score = cursor.getString(2);
            String date = cursor.getString(3);

            historyList.add(new HistoryItem(id, topic, score, date));
        }

        if (adapter == null) {
            adapter = new HistoryAdapter(historyList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}