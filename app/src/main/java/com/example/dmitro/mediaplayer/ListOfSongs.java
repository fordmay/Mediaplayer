package com.example.dmitro.mediaplayer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ListOfSongs extends AppCompatActivity {

    private static final int UPDATE_FREQUENCY = 500;
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
    private ArrayList<String> arrayListPath;
    private static ArrayList<String> arrayListPathFolder;
    private ArrayList<String> arrayListTitle;
    private ArrayList<String> arrayListArtist;
    private ArrayList<String> arrayListAlbum;
    private ArrayList<String> arrayListDuration;

    private String pathIsPlaying;
    private int positionItemAdapter;
    private List<Information> dataAftrerAdapt;
    //TextView for player
    private TextView titleIsPlay;
    private TextView artistIsPlay;
    private TextView albumIsPlay;
    private TextView totalRunningTime;
    private TextView timePlayed;

    //    private boolean isStarted = true;
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
        arrayListPath = new ArrayList<>();
        arrayListPathFolder  = new ArrayList<>();
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

                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                arrayListPath.add(path);
                String[] segment = path.split(File.separator);
                String pathFolder = path.substring(0, path.length() - segment[segment.length - 1].length());
                arrayListPathFolder.add(pathFolder);

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
                mAdapter.updateAdapterList();
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
        if (id == R.id.open_directory) {
            Intent intent = new Intent(this, DirectoryActivity.class);
            intent.putExtra("arrayPath", getArrayListPathFolder());
            startActivity(intent);
            return true;
        }
        if (id == R.id.sort_title) {
            mAdapter.sortTitle();
            mAdapter.updateAdapterList();
            return true;
        }
        if (id == R.id.sort_album) {
            mAdapter.sortAlbum();
            mAdapter.updateAdapterList();
            return true;
        }
        if (id == R.id.sort_artist) {
            mAdapter.sortArtist();
            mAdapter.updateAdapterList();
            return true;
        }
        if (id == R.id.sort_duration) {
            mAdapter.sortDuration();
            mAdapter.updateAdapterList();
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

    //
    private void updatePlayerPosition() {
        dataAftrerAdapt = mAdapter.updateAdapterList();
        boolean check = false;
        for (int i = 0; i < dataAftrerAdapt.size(); i++) {
            Information curentAfterAdapt = dataAftrerAdapt.get(i);
            String path = curentAfterAdapt.path;
            if ((path).equals(pathIsPlaying)) {
                positionItemAdapter = i;
                check = true;
            }
        }
        if (!check) {
            positionItemAdapter = -1;
        }
    }

    private void startPlay() {
        dataAftrerAdapt = mAdapter.updateAdapterList();
        Information curentAfterAdapt = dataAftrerAdapt.get(positionItemAdapter);

        titleIsPlay.setText(curentAfterAdapt.title);
        artistIsPlay.setText(curentAfterAdapt.artist);
        albumIsPlay.setText(curentAfterAdapt.album);
        totalRunningTime.setText(curentAfterAdapt.duration);

        seekbar.setProgress(0);
        mediaPlayer.stop();
        mediaPlayer.reset();
        pathIsPlaying = curentAfterAdapt.path;
        try {
            mediaPlayer.setDataSource(curentAfterAdapt.path);
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
        //change adapter's images
        for (int i = 0; i < dataAftrerAdapt.size(); i++) {
            curentAfterAdapt = dataAftrerAdapt.get(i);
            curentAfterAdapt.image = R.drawable.ic_play_arrow_black_24dp;
            if ((curentAfterAdapt.path).equals(pathIsPlaying)) {
                curentAfterAdapt.image = R.drawable.ic_pause_black_24dp;
            }
        }

        updatePosition();
        mAdapter.notifyDataSetChanged();

//        isStarted = true;
    }

    private void stopPlay() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(0);
        titleIsPlay.setText("Not file selected");
        artistIsPlay.setText("");
        albumIsPlay.setText("");
        totalRunningTime.setText("00.00");
//        isStarted = false;
    }

    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);

        seekbar.setProgress(mediaPlayer.getCurrentPosition());

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    public static ArrayList<String> getArrayListPathFolder(){
        return arrayListPathFolder;
    }
    private List<Information> getData() {
        List<Information> data = new ArrayList<>();
        for (int i = 0; i < arrayListTitle.size(); i++) {
            Information current = new Information();

            current.title = arrayListTitle.get(i);
            current.artist = arrayListArtist.get(i);
            current.album = arrayListAlbum.get(i);
            current.duration = arrayListDuration.get(i);
            current.path = arrayListPath.get(i);
            current.pathFolder = arrayListPathFolder.get(i);
            current.image = R.drawable.ic_play_arrow_black_24dp;
            data.add(current);
        }
        return data;
    }

    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
        List<Information> mData = Collections.emptyList();
        List<Information> cleanCopyData = Collections.emptyList();

        public MyRecyclerAdapter(List<Information> data) {
            mData = data;
            cleanCopyData = mData;
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
            final Information current = mData.get(position);
            holder.title.setText(current.title);
            holder.album.setText(current.album);
            holder.artist.setText(current.artist);
            holder.duration.setText(current.duration);
            holder.image_for_list.setImageResource(current.image);
            holder.image_for_list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    positionItemAdapter = position;
                    startPlay();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
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

        public void sortTitle() {
            Collections.sort(cleanCopyData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.title.compareTo(i2.title);
                }
            });
            Collections.sort(mData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.title.compareTo(i2.title);
                }
            });
        }

        public void sortAlbum() {
            Collections.sort(cleanCopyData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.album.compareTo(i2.album);
                }
            });
            Collections.sort(mData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.album.compareTo(i2.album);
                }
            });
        }

        public void sortArtist() {
            Collections.sort(cleanCopyData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.artist.compareTo(i2.artist);
                }
            });
            Collections.sort(mData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.artist.compareTo(i2.artist);
                }
            });
        }

        public void sortDuration() {
            Collections.sort(cleanCopyData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.duration.compareTo(i2.duration);
                }
            });
            Collections.sort(mData, new Comparator<Information>() {
                @Override
                public int compare(Information i1, Information i2) {
                    return i1.duration.compareTo(i2.duration);
                }
            });
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            mData = new ArrayList<>();

            if (charText.length() == 0) {
                mData.addAll(cleanCopyData);
            } else {
                for (int i = 0; i < cleanCopyData.size(); i++) {
                    final Information cleanCopyCurrent = cleanCopyData.get(i);

                    if (cleanCopyCurrent.title.toLowerCase(Locale.getDefault()).contains(charText) ||
                            cleanCopyCurrent.artist.toLowerCase(Locale.getDefault()).contains(charText) ||
                            cleanCopyCurrent.album.toLowerCase(Locale.getDefault()).contains(charText)) {
                        mData.add(cleanCopyData.get(i));
                    }
                }
            }
            notifyDataSetChanged();
        }

        public void getFromDirectory(String path){
            mData = new ArrayList<>();
            for(int i=0;i<cleanCopyData.size();i++){
                final Information cleanCopyCurrent = cleanCopyData.get(i);
                if (cleanCopyCurrent.path.equals(path)){
                    mData.add(cleanCopyData.get(i));
                }
            }
            notifyDataSetChanged();
        }

        public List<Information> updateAdapterList() {
            notifyDataSetChanged();
            return mData;
        }
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (titleIsPlay.equals("Not file selected")) {
                Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
            } else {
                switch (v.getId()) {
                    case R.id.play: {
                        if (mediaPlayer.isPlaying()) {
                            handler.removeCallbacks(updatePositionRunnable);
                            mediaPlayer.pause();
                            playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        } else {
//                            if (isStarted) {
                            mediaPlayer.start();
                            playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            updatePosition();
//                            } else {
//                                startPlay();
//                            }
                        }
                    }
                    break;

                    case R.id.stop: {
                        stopPlay();
                    }
                    break;

                    case R.id.next: {
                        updatePlayerPosition();
                        if (positionItemAdapter == -1) {
                            Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
                        } else if (positionItemAdapter == (dataAftrerAdapt.size() - 1)) {
                            Toast.makeText(ListOfSongs.this, "єто последняя", Toast.LENGTH_SHORT).show();
                        } else {
                            positionItemAdapter = positionItemAdapter + 1;
                            startPlay();
                        }
                    }
                    break;

                    case R.id.previous: {
                        updatePlayerPosition();
                        if (positionItemAdapter == -1) {
                            Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
                        } else if (positionItemAdapter == 0) {
                            Toast.makeText(ListOfSongs.this, "єто первая", Toast.LENGTH_SHORT).show();
                        } else {
                            positionItemAdapter = positionItemAdapter - 1;
                            startPlay();
                        }
                    }
                    break;

                }
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
