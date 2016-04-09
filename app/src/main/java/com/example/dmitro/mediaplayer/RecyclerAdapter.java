package com.example.dmitro.mediaplayer;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<String> mDataset;
    private ArrayList<String> mCleanCopyDataset;
    private ArrayList<String> numberArrayList;

    // Конструктор
    public RecyclerAdapter(ArrayList<String> dataset) {
        mDataset = dataset;
        mCleanCopyDataset = mDataset;
    }

    // Создает новые views (вызывается layout manager-ом)
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)  {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_folders, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Заменяет контент отдельного view (вызывается layout manager-ом)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String[] segment = mDataset.get(position).split(File.separator);
        String nameFolder = segment[segment.length - 1];
        holder.nameFolder.setText(nameFolder);
        holder.numberSongs.setText(numberArrayList.get(position));
        holder.imageFolder.setImageResource(R.drawable.ic_folder_open_black_24dp);
    }

    // Возвращает размер данных (вызывается layout manager-ом)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView nameFolder;
        public TextView numberSongs;
        public ImageView imageFolder;

        public ViewHolder(View v){
            super(v);
            nameFolder = (TextView) v.findViewById(R.id.name_folder);
            numberSongs = (TextView) v.findViewById(R.id.number_songs);
            imageFolder = (ImageView) v.findViewById(R.id.image_folder);
        }
        @Override
        public void onClick(final View view) {
//           String chosenPath = mDataset.get(getPosition());
//            Intent intent = new Intent(, ListOfSongs.class);
//            intent.putExtra("arrayPath", chosenPath);
//            startActivity(intent);

        }
    }

    public void getListPathFolders() {
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
        notifyDataSetChanged();
    }

}

