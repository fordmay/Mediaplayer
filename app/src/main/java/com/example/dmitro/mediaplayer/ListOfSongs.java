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
import android.widget.ImageView;
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

    //data for music
    private Cursor cursor;
    private ArrayList<String> arrayListPath;
    private ArrayList<String> arrayListPathFolder;
    private ArrayList<String> arrayListTitle;
    private ArrayList<String> arrayListArtist;
    private ArrayList<String> arrayListAlbum;
    private ArrayList<String> arrayListDuration;

    //TextView for player
    private TextView titleIsPlay;
    private TextView artistIsPlay;
    private TextView albumIsPlay;
    private TextView totalRunningTime;
    private TextView timePlayed;
    //control for player
    private SeekBar seekbar;
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton stopButton;

    private String pathIsPlaying;
    private int positionItemAdapter;
    private List<Information> dataAftrerAdapt;
    private boolean isStarted = true;
    private boolean firstStart = true;
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
        //player
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
        arrayListPathFolder = new ArrayList<>();
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

        //onClickListener for player
        playButton.setOnClickListener(onButtonClick);
        nextButton.setOnClickListener(onButtonClick);
        prevButton.setOnClickListener(onButtonClick);
        stopButton.setOnClickListener(onButtonClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_of_songs, menu);
        //add the search in ActionBar
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
        if (id == R.id.open_directory) {
            Intent intent = new Intent(this, DirectoryActivity.class);
            intent.putExtra("arrayPath", getArrayListPathFolder());
            startActivityForResult(intent, RequestCode.REQUESTCODE);
            return true;
        }
        if (id == R.id.sort_title) {
            mAdapter.sortTitle();
            return true;
        }
        if (id == R.id.sort_album) {
            mAdapter.sortAlbum();
            return true;
        }
        if (id == R.id.sort_artist) {
            mAdapter.sortArtist();
            return true;
        }
        if (id == R.id.sort_duration) {
            mAdapter.sortDuration();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RequestCode.REQUESTCODE) {
                String path = data.getStringExtra("path");
                mAdapter.getAllTrackFromDirectory(path);
                Toast.makeText(ListOfSongs.this, "" + path, Toast.LENGTH_SHORT).show();
            }
        }
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

    //find a new location track in the list after sorting or filter or...,
    // if there is no track positionItemAdapter = -1
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

        mediaPlayer.stop();
        mediaPlayer.reset();
        pathIsPlaying = curentAfterAdapt.path;
        try {
            mediaPlayer.setDataSource(pathIsPlaying);
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

        mAdapter.setImage();
        updatePosition();
        isStarted = true;
        firstStart = false;
    }
    //stop player
    private void stopPlay() {
        mediaPlayer.stop();
        mediaPlayer.reset();

        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(0);
        timePlayed.setText("00.00");
        isStarted = false;
    }
    //update position SeekBar and play time
    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(mediaPlayer.getCurrentPosition());

        long durationInMs = mediaPlayer.getCurrentPosition();
        double durationInMin = ((double) durationInMs / 1000.0) / 60.0;
        durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        timePlayed.setText("" + durationInMin);

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
        //next track
        if (mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()) {
            updatePlayerPosition();
            if (positionItemAdapter == -1) {
                positionItemAdapter = 0;
                startPlay();
            }else if (positionItemAdapter == (dataAftrerAdapt.size() - 1)) {
                stopPlay();
            } else {
                positionItemAdapter = positionItemAdapter+1;
                startPlay();
            }
        }
    }
    //get arrayListPathFolder
    public ArrayList<String> getArrayListPathFolder() {
        return arrayListPathFolder;
    }
    //get data for adapter
    private List<Information> getData() {
        List<Information> data = new ArrayList<>();
        for (int i = 0; i < arrayListTitle.size(); i++) {
            Information current = new Information();

            current.title = arrayListTitle.get(i);
            current.artist = arrayListArtist.get(i);
            current.album = arrayListAlbum.get(i);
            current.duration = arrayListDuration.get(i);
            current.path = arrayListPath.get(i);
            current.image = R.drawable.ic_play_arrow_black_24dp;
            current.pathFolder = arrayListPathFolder.get(i);
            data.add(current);
        }
        return data;
    }

    //class Adapter
    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
        List<Information> mData = Collections.emptyList();
        List<Information> copyDataForFilterSort = Collections.emptyList();
        List<Information> cleanData = Collections.emptyList();

        public MyRecyclerAdapter(List<Information> data) {
            mData = data;
            copyDataForFilterSort = mData;
            cleanData = mData;
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
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            TextView album;
            TextView artist;
            TextView duration;
            ImageView image_for_list;

            public ViewHolder(View vItem) {
                super(vItem);
                vItem.setOnClickListener(this);
                album = (TextView) vItem.findViewById(R.id.album);
                artist = (TextView) vItem.findViewById(R.id.artist);
                duration = (TextView) vItem.findViewById(R.id.duration);
                title = (TextView) vItem.findViewById(R.id.title);
                image_for_list = (ImageView) vItem.findViewById(R.id.image_for_list);

            }

            @Override
            public void onClick(View v) {
                positionItemAdapter = getPosition();
                startPlay();
            }
        }

        public void sortTitle() {
            Collections.sort(copyDataForFilterSort, new Comparator<Information>() {
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
            notifyDataSetChanged();
        }

        public void sortAlbum() {
            Collections.sort(copyDataForFilterSort, new Comparator<Information>() {
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
            notifyDataSetChanged();
        }

        public void sortArtist() {
            Collections.sort(copyDataForFilterSort, new Comparator<Information>() {
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
            notifyDataSetChanged();
        }

        public void sortDuration() {
            Collections.sort(copyDataForFilterSort, new Comparator<Information>() {
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
            notifyDataSetChanged();
        }

        public void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            mData = new ArrayList<>();

            if (charText.length() == 0) {
                mData.addAll(copyDataForFilterSort);
            } else {
                for (int i = 0; i < copyDataForFilterSort.size(); i++) {
                    final Information copyCurrent = copyDataForFilterSort.get(i);

                    if (copyCurrent.title.toLowerCase(Locale.getDefault()).contains(charText) ||
                            copyCurrent.artist.toLowerCase(Locale.getDefault()).contains(charText) ||
                            copyCurrent.album.toLowerCase(Locale.getDefault()).contains(charText)) {
                        mData.add(copyDataForFilterSort.get(i));
                    }
                }
            }
            setImage();
        }

        public void getAllTrackFromDirectory(String path) {
            if ("All Phone Tracks".equals(path)) {
                mData = cleanData;
                copyDataForFilterSort = cleanData;
            } else {
                mData = new ArrayList<>();
                for (int i = 0; i < cleanData.size(); i++) {
                    final Information cleanCopyCurrent = cleanData.get(i);
                    if (cleanCopyCurrent.pathFolder.equals(path)) {
                        mData.add(cleanData.get(i));
                    }
                }
                copyDataForFilterSort = mData;
            }
            setImage();
        }

        public List<Information> updateAdapterList() {
            notifyDataSetChanged();
            return mData;
        }

        //set correct image and update adapter
        public void setImage() {
            for (int i = 0; i < mData.size(); i++) {
                final Information current = mData.get(i);
                current.image = R.drawable.ic_play_arrow_black_24dp;
                if ((current.path).equals(pathIsPlaying)) {
                    current.image = R.drawable.ic_pause_black_24dp;
                }
            }
            notifyDataSetChanged();
        }
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (mediaPlayer.isPlaying()) {
                        //pause
                        handler.removeCallbacks(updatePositionRunnable);
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    } else {
                        if (firstStart) {
                            positionItemAdapter = 0;
                            startPlay();
                        } else if (isStarted) {
                            //after pause
                            mediaPlayer.start();
                            playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            updatePosition();
                        } else {
                            //after stop
                            try {
                                mediaPlayer.setDataSource(pathIsPlaying);
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                            updatePosition();
                            isStarted = true;
                        }
                    }
                }
                break;

                case R.id.stop: {
                    stopPlay();
                }
                break;

                case R.id.next: {
                    updatePlayerPosition();
                    //check for playing the songs in the adapter list
                    if (positionItemAdapter == -1) {
                        Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
                    } else if (positionItemAdapter == (dataAftrerAdapt.size() - 1)) {
                        Toast.makeText(ListOfSongs.this, "The last track", Toast.LENGTH_SHORT).show();
                    } else {
                        positionItemAdapter = positionItemAdapter + 1;
                        startPlay();
                    }
                }
                break;

                case R.id.previous: {
                    updatePlayerPosition();
                    //check for playing the songs in the adapter list
                    if (positionItemAdapter == -1) {
                        Toast.makeText(ListOfSongs.this, "Select the song", Toast.LENGTH_SHORT).show();
                    } else if (positionItemAdapter == 0) {
                        Toast.makeText(ListOfSongs.this, "The first track", Toast.LENGTH_SHORT).show();
                    } else {
                        positionItemAdapter = positionItemAdapter - 1;
                        startPlay();
                    }
                }
                break;
            }
        }
    };

    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    };
//use SeekBar
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
