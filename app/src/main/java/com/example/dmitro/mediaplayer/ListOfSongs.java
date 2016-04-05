package com.example.dmitro.mediaplayer;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListOfSongs extends AppCompatActivity {

    private static final int UPDATE_FREQUENCY = 500;
    //    private static final int STEP_VALUE = 4000;
    //adapter
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter mAdapter;
    //control
    private SeekBar seekbar;
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton stopButton;
    //data for music
    private Cursor cursor;
    private ArrayList<String> arrayListTitle;
    private ArrayList<String> arrayListArtist;
    private ArrayList<String> arrayListAlbum;
    private ArrayList<String> arrayListDuration;
    private ImageButton temporaryMusikImage;
    private int savedNumberForCursor;
    //TextView for player
    private TextView titleIsPlay;
    private TextView artistIsPlay;
    private TextView albumIsPlay;
    private TextView totalRunningTime;
    private TextView timePlayed;

    private boolean isStarted = true;
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

        totalRunningTime = (TextView) findViewById(R.id.total_running_time);
        timePlayed = (TextView) findViewById(R.id.time_played);
        titleIsPlay = (TextView) findViewById(R.id.title_is_play);
        artistIsPlay = (TextView) findViewById(R.id.artist_is_play);
        albumIsPlay = (TextView) findViewById(R.id.album_is_play);
        seekbar = (SeekBar) findViewById(R.id.seek_bar);
        playButton = (ImageButton) findViewById(R.id.play);
        prevButton = (ImageButton) findViewById(R.id.previous);
        nextButton = (ImageButton) findViewById(R.id.next);
        stopButton = (ImageButton) findViewById(R.id.stop);

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnErrorListener(onError);
        seekbar.setOnSeekBarChangeListener(seekBarChanged);

        //ArrayList for music's data
        arrayListTitle = new ArrayList<>();
        arrayListArtist = new ArrayList<>();
        arrayListAlbum = new ArrayList<>();
        arrayListDuration = new ArrayList<>();
        //take all the audio files on the system
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor == null) {
        } else if (!cursor.moveToFirst()) {
        } else {
            //take parameters for the adapter
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

        // adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new MyRecyclerAdapter(getData());
        mRecyclerView.setAdapter(mAdapter);

        //player's buttons
        playButton.setOnClickListener(onButtonClick);
        nextButton.setOnClickListener(onButtonClick);
        prevButton.setOnClickListener(onButtonClick);
        stopButton.setOnClickListener(onButtonClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_of_songs, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                mAdapter.filter(query);

                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(updatePositionRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();

        mediaPlayer = null;
    }

    private void startPlay() {
        titleIsPlay.setText(arrayListTitle.get(savedNumberForCursor));
        artistIsPlay.setText(arrayListArtist.get(savedNumberForCursor));
        albumIsPlay.setText(arrayListAlbum.get(savedNumberForCursor));
        totalRunningTime.setText(arrayListDuration.get(savedNumberForCursor));
        seekbar.setProgress(0);
        cursor.moveToPosition(savedNumberForCursor);
        mediaPlayer.stop();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
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
        mAdapter.notifyDataSetChanged();

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

    public List<Information> getData() {
        List<Information> data = new ArrayList<>();
        for (int i = 0; i < arrayListTitle.size(); i++) {
            Information current = new Information();
            current.title = arrayListTitle.get(i);
            current.artist = arrayListArtist.get(i);
            current.album = arrayListAlbum.get(i);
            current.duration = arrayListDuration.get(i);
            current.numberForCursor = i;
            current.image = R.drawable.ic_play_arrow_black_24dp;
            data.add(current);
        }
        return data;
    }

    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
        List<Information> data = Collections.emptyList();
        List<Information> cleanCopyData = Collections.emptyList();

        public MyRecyclerAdapter(List<Information> data) {
            this.data = data;
            cleanCopyData = this.data;
        }

        @Override
        public MyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_of_item, parent, false);
            ViewHolder VH = new ViewHolder(view);
            return VH;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Information current = data.get(position);
            holder.title.setText(current.title);
            holder.album.setText(current.album);
            holder.artist.setText(current.artist);
            holder.duration.setText(current.duration);

            if (current.numberForCursor == savedNumberForCursor && temporaryMusikImage != null) {
                holder.image_for_list.setImageResource(R.drawable.ic_pause_black_24dp);
            } else {
                holder.image_for_list.setImageResource(current.image);
            }
            holder.image_for_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (temporaryMusikImage != null) {
                        temporaryMusikImage.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    }
                    Toast.makeText(ListOfSongs.this, "click:" + position, Toast.LENGTH_SHORT).show();
                    holder.image_for_list.setImageResource(R.drawable.ic_pause_black_24dp);
                    temporaryMusikImage = holder.image_for_list;
                    savedNumberForCursor = current.numberForCursor;

                    startPlay();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
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

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            data = new ArrayList<Information>();
            if (charText.length() == 0) {
                data.addAll(cleanCopyData);
            } else {
                for (int i = 0; i < cleanCopyData.size(); i++) {
                    if (arrayListTitle.get(i).toLowerCase(Locale.getDefault()).contains(charText) ||
                            arrayListAlbum.get(i).toLowerCase(Locale.getDefault()).contains(charText) ||
                            arrayListArtist.get(i).toLowerCase(Locale.getDefault()).contains(charText)) {
                        data.add(cleanCopyData.get(i));
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (temporaryMusikImage != null) {
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
                                startPlay();
                            }
                        }
                    }
                    break;

                    case R.id.stop: {
                        stopPlay();
                    }
                    break;
                    case R.id.next: {
                        savedNumberForCursor = savedNumberForCursor + 1;
                        startPlay();
                    }
                    break;

                    case R.id.previous: {
                        savedNumberForCursor = savedNumberForCursor - 1;
                        startPlay();
                    }
                    break;
                }
            } else {
                Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
            }

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
