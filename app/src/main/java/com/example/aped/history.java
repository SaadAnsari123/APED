package com.example.aped;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.List;

public class history extends AppCompatActivity {
private ImageView backIv;
private RecyclerView hist_recycler;
int counter = 0;
DBhelper DB = new DBhelper(this);
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent rec_intent = getIntent();
      String value = rec_intent.getStringExtra("KEY_SENDER");

        List<String> items = new LinkedList<>();
        items.add("Hello");
        RecyclerView hist_recycler = findViewById(R.id.hist_recycler);
        hist_recycler.setLayoutManager(new LinearLayoutManager(this));
        histAdapter adapter = new histAdapter(items);
        hist_recycler.setAdapter(adapter);


        findViewById(R.id.refresh).setOnClickListener(view -> {
            //Cursor res = DB.getdata();
            items.add(value);
            counter++;
            adapter.notifyItemInserted(items.size()-1);

        });


        backIv = findViewById(R.id.backIv);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(history.this, home.class);
                startActivity(intent);

            }
        });


    }
}