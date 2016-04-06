//package com.example.dmitro.mediaplayer;
//
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Locale;
//
//public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
//    private List<Information> mData = Collections.emptyList();
//    private List<Information> cleanCopyData = Collections.emptyList();
//    private ImageButton temporaryMusikImage;
//    public int savedNumberForCursor;
//
//    public MyRecyclerAdapter(List<Information> data) {
//        mData = data;
//        cleanCopyData = mData;
//    }
//
//    @Override
//    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.list_of_item, parent, false);
//        ViewHolder VH = new ViewHolder(view);
//        return VH;
//    }
//
//    @Override
//    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        final Information current = mData.get(position);
//        holder.title.setText(current.title);
//        holder.album.setText(current.album);
//        holder.artist.setText(current.artist);
//        holder.duration.setText(current.duration);
//
//        if (current.numberForCursor == savedNumberForCursor && temporaryMusikImage != null) {
//            holder.image_for_list.setImageResource(R.drawable.ic_pause_black_24dp);
//        } else {
//            holder.image_for_list.setImageResource(current.image);
//        }
//        holder.image_for_list.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (temporaryMusikImage != null) {
//                    temporaryMusikImage.setImageResource(R.drawable.ic_play_arrow_black_24dp);
//                }
//                holder.image_for_list.setImageResource(R.drawable.ic_pause_black_24dp);
//                temporaryMusikImage = holder.image_for_list;
//                savedNumberForCursor = current.numberForCursor;
//
//                ListOfSongs listOfSongs = new ListOfSongs();
//                listOfSongs.startPlay();
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mData.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        TextView title;
//        TextView album;
//        TextView artist;
//        TextView duration;
//        ImageButton image_for_list;
//
//        public ViewHolder(View vItem) {
//            super(vItem);
//            album = (TextView) vItem.findViewById(R.id.album);
//            artist = (TextView) vItem.findViewById(R.id.artist);
//            duration = (TextView) vItem.findViewById(R.id.duration);
//            title = (TextView) vItem.findViewById(R.id.title);
//            image_for_list = (ImageButton) vItem.findViewById(R.id.image_for_list);
//
//        }
//    }
//
//    public void filter(String charText) {
//
//        charText = charText.toLowerCase(Locale.getDefault());
//        mData = new ArrayList<Information>();
//
//        if (charText.length() == 0) {
//            mData.addAll(cleanCopyData);
//        } else {
//            for (int i = 0; i < cleanCopyData.size(); i++) {
//                final Information cleanCopyCurrent = cleanCopyData.get(i);
//
//                if (cleanCopyCurrent.title.toLowerCase(Locale.getDefault()).contains(charText) ||
//                        cleanCopyCurrent.artist.toLowerCase(Locale.getDefault()).contains(charText) ||
//                        cleanCopyCurrent.album.toLowerCase(Locale.getDefault()).contains(charText)) {
//                    mData.add(cleanCopyData.get(i));
//                }
//            }
//        }
//        notifyDataSetChanged();
//    }
//}