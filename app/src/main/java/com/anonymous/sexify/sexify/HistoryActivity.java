package com.anonymous.sexify.sexify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyclerView = findViewById(R.id.historyRecylerView);
        historyRecyclerView.setNestedScrollingEnabled(true);
        historyRecyclerView.setHasFixedSize(true);

    }
}
