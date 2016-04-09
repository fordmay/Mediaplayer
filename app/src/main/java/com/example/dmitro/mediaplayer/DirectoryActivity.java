package com.example.dmitro.mediaplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class DirectoryActivity extends AppCompatActivity {
    //adapter
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        // adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.list_directory);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Intent intent = getIntent();
        ArrayList<String> arrayPath = intent.getStringArrayListExtra("arrayPath");

        mAdapter = new RecyclerAdapter(arrayPath);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.getListPathFolders();


    }
}
