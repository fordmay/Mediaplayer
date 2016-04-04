package com.example.dmitro.mediaplayer;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ListOfSongs extends AppCompatActivity {

    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;
    //адаптер
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    //контроллер
    private TextView selelctedFile;
    private SeekBar seekbar;
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton stopButton;
    //дааные музыки
    private Cursor cursor;
    private String[] dataTitle;
    private String[] dataArtist;
    private String[] dataAlbum;
    private String[] dataDuration;
    private ImageButton temporaryMusikImage;
    private int numberPosition;

    private boolean isStarted = true;
    private String currentFile;
    private boolean isMoveingSeekBar = false;

    private final Handler handler = new Handler();
    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);

        selelctedFile = (TextView) findViewById(R.id.selected_file);
        seekbar = (SeekBar) findViewById(R.id.seek_bar);
        playButton = (ImageButton) findViewById(R.id.play);
        prevButton = (ImageButton) findViewById(R.id.previous);
        nextButton = (ImageButton) findViewById(R.id.next);
        stopButton = (ImageButton) findViewById(R.id.stop);

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(onCompletion);
        mediaPlayer.setOnErrorListener(onError);
        seekbar.setOnSeekBarChangeListener(seekBarChanged);

        //массивы для данных
        ArrayList<String> arrayListTitle = new ArrayList<>();
        ArrayList<String> arrayListArtist = new ArrayList<>();
        ArrayList<String> arrayListAlbum = new ArrayList<>();
        ArrayList<String> arrayListDuration = new ArrayList<>();
        //берем все аудио файлы в системе
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor == null) {
        } else if (!cursor.moveToFirst()) {
        } else {
            //берем параметры для адаптера
            while (cursor.isAfterLast() == false) {
                arrayListArtist.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
                arrayListAlbum.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
                arrayListTitle.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));

                long durationInMs = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
                double durationInMin = ((double) durationInMs / 1000.0) / 60.0;
                durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();
                arrayListDuration.add("" + durationInMin);

                cursor.moveToNext();
            }
        }
        //переделываем в простой массив для скорости и простоты работы с адаптером
        dataAlbum = arrayListAlbum.toArray(new String[arrayListAlbum.size()]);
        dataArtist = arrayListArtist.toArray(new String[arrayListArtist.size()]);
        dataDuration = arrayListDuration.toArray(new String[arrayListDuration.size()]);
        dataTitle = arrayListTitle.toArray(new String[arrayListTitle.size()]);

        // adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(dataTitle, dataArtist, dataAlbum, dataDuration, R.drawable.ic_play_arrow_black_24dp);
        mRecyclerView.setAdapter(mAdapter);
//        Cursor cursor = getContentResolver().query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
//
//        if (null != cursor) {
//            cursor.moveToFirst();
//
//            mediaAdapter = new MediaCursorAdapter(this, R.layout.list_of_item, cursor);
//
//            setListAdapter(mediaAdapter);
//        }
            playButton.setOnClickListener(onButtonClick);
            nextButton.setOnClickListener(onButtonClick);
            prevButton.setOnClickListener(onButtonClick);
            stopButton.setOnClickListener(onButtonClick);


    }

//    @Override
//    protected void onListItemClick(ListView list, View view, int position, long id) {
//        super.onListItemClick(list, view, position, id);
//
//        currentFile = (String) view.getTag();
//
//        startPlay(currentFile);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(updatePositionRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();

        mediaPlayer = null;
    }

    private void startPlay(String file) {
        Log.i("Selected: ", file);

        selelctedFile.setText(file);
        seekbar.setProgress(0);

        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(file);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekbar.setMax(mediaPlayer.getDuration());
        playButton.setImageResource(R.drawable.ic_pause_black_24dp);

        updatePosition();

        isStarted = true;
    }

    private void stopPlay() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(0);

        isStarted = false;
    }

    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);

        seekbar.setProgress(mediaPlayer.getCurrentPosition());

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    //Адаптер
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        String[] dataTitle;
        String[] dataArtist;
        String[] dataAlbum;
        String[] dataDuration;
        int image_for_list;

        public MyAdapter(String[] dataTitle, String[] dataArtist, String[] dataAlbum, String[] dataDuration, int image_for_list) {
            this.dataTitle = dataTitle;
            this.dataArtist = dataArtist;
            this.dataAlbum = dataAlbum;
            this.dataDuration = dataDuration;
            this.image_for_list = image_for_list;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_of_item, parent, false);
            ViewHolder VH = new ViewHolder(view);
            return VH;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.title.setText(this.dataTitle[position]);
            holder.album.setText(this.dataAlbum[position]);
            holder.artist.setText(this.dataArtist[position]);
            holder.duration.setText(this.dataDuration[position]);

            holder.image_for_list.setImageResource(image_for_list);
            holder.image_for_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (temporaryMusikImage != null) {
                        temporaryMusikImage.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    }
                    Toast.makeText(ListOfSongs.this, "click:" + position, Toast.LENGTH_SHORT).show();
                    holder.image_for_list.setImageResource(R.drawable.ic_pause_black_24dp);
                    temporaryMusikImage = holder.image_for_list;
                    numberPosition = position;
                    cursor.moveToPosition(position);
                    
                    startPlay(cursor.getString(
                            cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataTitle.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView album;
            TextView artist;
            TextView duration;
            ImageButton image_for_list;

            public ViewHolder(View vItem) {
                super(vItem);
                album = (TextView) vItem.findViewById(R.id.album);
                artist = (TextView) vItem.findViewById(R.id.artist);
                duration = (TextView) vItem.findViewById(R.id.duration);
                title = (TextView) vItem.findViewById(R.id.title);
                image_for_list = (ImageButton) vItem.findViewById(R.id.image_for_list);
            }
        }
    }

//    private class MediaCursorAdapter extends SimpleCursorAdapter {
//
//        public MediaCursorAdapter(Context context, int layout, Cursor c) {
//            super(context, layout, c,
//                    new String[]{MediaStore.Audio.AudioColumns.ARTIST,
//                            MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE,
//                            MediaStore.Audio.AudioColumns.DURATION},
//                    new int[]{R.id.title, R.id.album, R.id.artist, R.id.duration}
//            );
//        }
//
//        @Override
//        public void bindView(View view, Context context, Cursor cursor) {
//            TextView title = (TextView) view.findViewById(R.id.title);
//            TextView album = (TextView) view.findViewById(R.id.album);
//            TextView artist = (TextView) view.findViewById(R.id.artist);
//            TextView duration = (TextView) view.findViewById(R.id.duration);
//
//            artist.setText(cursor.getString(
//                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
//
//            album.setText(cursor.getString(
//                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
//
//            title.setText(cursor.getString(
//                    cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
//
//            long durationInMs = Long.parseLong(cursor.getString(
//                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
//
//            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;
//
//            durationInMin = new BigDecimal(Double.toString(durationInMin))
//                    .setScale(2, BigDecimal.ROUND_UP).doubleValue();
//
//            duration.setText("" + durationInMin);
//
//            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
//        }
//
//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            LayoutInflater inflater = LayoutInflater.from(context);
//            View v = inflater.inflate(R.layout.list_of_item, parent, false);
//
//            bindView(v, context, cursor);
//
//            return v;
//        }
//    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (mediaPlayer.isPlaying()) {
                        handler.removeCallbacks(updatePositionRunnable);
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    } else {
                        if (isStarted) {
                            mediaPlayer.start();
                            playButton.setImageResource(R.drawable.ic_pause_black_24dp);

                            updatePosition();
                        } else {
                            startPlay(currentFile);
                        }
                    }

                    break;
                }
                case R.id.stop: {
                    mediaPlayer.stop();
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mediaPlayer.seekTo(0);
                    isMoveingSeekBar = false;
                    break;
                }
                case R.id.next: {

                    int seekto = mediaPlayer.getCurrentPosition() + STEP_VALUE;

                    if (seekto > mediaPlayer.getDuration())
                        seekto = mediaPlayer.getDuration();

                    mediaPlayer.pause();
                    mediaPlayer.seekTo(seekto);
                    mediaPlayer.start();

                    break;
                }
                case R.id.previous: {
                    int seekto = mediaPlayer.getCurrentPosition() - STEP_VALUE;

                    if (seekto < 0)
                        seekto = 0;

                    mediaPlayer.pause();
                    mediaPlayer.seekTo(seekto);
                    mediaPlayer.start();

                    break;
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };

    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isMoveingSeekBar) {
                mediaPlayer.seekTo(progress);

                Log.i("OnSeekBarChangeListener", "onProgressChanged");
            }
        }
    };
}
