package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnStart;
    Button btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btnStart);
        btnHistory = findViewById(R.id.btnHistory);

        btnStart.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            InputActivity.class
                    );

            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HistoryActivity.class
                    );

            startActivity(intent);
        });
    }
}
