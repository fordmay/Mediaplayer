package com.example.dmitro.mediaplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
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
        // data from ListOfSongs.class
        Intent intent = getIntent();
        ArrayList<String> arrayPath = intent.getStringArrayListExtra("arrayPath");
        // adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.list_directory);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RecyclerAdapter(arrayPath);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.getListPathFolders();

    }
    //class Adapter for file browser.
    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private ArrayList<String> mDataset;
        private ArrayList<String> mCleanCopyDataset;
        //array for quantity tracks in folder
        private ArrayList<String> numberArrayList;

        public RecyclerAdapter(ArrayList<String> dataset) {
            mDataset = dataset;
            mCleanCopyDataset = mDataset;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_folders, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //get name folder from path
            String[] segment = mDataset.get(position).split(File.separator);
            String nameFolder = segment[segment.length - 1];

            holder.nameFolder.setText(nameFolder);
            holder.numberSongs.setText(numberArrayList.get(position));
            holder.imageFolder.setImageResource(R.drawable.ic_folder_open_black_24dp);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView nameFolder;
            public TextView numberSongs;
            public ImageView imageFolder;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                nameFolder = (TextView) v.findViewById(R.id.name_folder);
                numberSongs = (TextView) v.findViewById(R.id.number_songs);
                imageFolder = (ImageView) v.findViewById(R.id.image_folder);
            }

            @Override
            public void onClick(final View view) {
                String chosenPath = mDataset.get(getPosition());
                //sendback chosen path
                Intent intent = new Intent();
                intent.putExtra("path", chosenPath);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        public void getListPathFolders() {
            //delete duplicates paths
            mDataset = new ArrayList<>();
            numberArrayList = new ArrayList<>();
            boolean check = false;
            for (int i = 0; i < mCleanCopyDataset.size(); i++) {
                mCleanCopyDataset.get(i);
                if (mDataset.size() == 0) {
                    mDataset.add(mCleanCopyDataset.get(i));
                } else {
                    for (int j = 0; j < mDataset.size(); j++) {
                        if (mDataset.get(j).equals(mCleanCopyDataset.get(i))) {
                            check = true;
                        }
                    }
                    if (check == false) {
                        mDataset.add(mCleanCopyDataset.get(i));

                    }
                    check = false;
                }
            }
            //calculate quantity tracks in folder
            for (int i = 0; i < mDataset.size(); i++) {
                mDataset.get(i);
                int number = 0;
                for (int j = 0; j < mCleanCopyDataset.size(); j++) {
                    if (mDataset.get(i).equals(mCleanCopyDataset.get(j))) {
                        number++;
                    }
                }
                numberArrayList.add("" + number);
            }
            //add folder's name for all phone tracks
            mDataset.add("All Phone Tracks");
            numberArrayList.add("" + mCleanCopyDataset.size());
            //updated adapter
            notifyDataSetChanged();
        }

    }
}
