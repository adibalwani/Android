package edu.neu.madcourse.adibalwani.wordgame;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mMediaPlayer;
    public static final String PREF_RESTORE = "pref_restore";
    public static final String PREF_FILE = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordgame_activity_main);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.wordgame_app_name));

        mMediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mMediaPlayer.setVolume(0.5f, 0.5f);
        mMediaPlayer.setLooping(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume button
        String gameData = getSharedPreferences(GameActivity.PREF_FILE, MODE_PRIVATE)
                .getString(GameActivity.PREF_RESTORE, null);
        if (gameData != null) {
            findViewById(R.id.wordgame_continue_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.wordgame_continue_button).setVisibility(View.GONE);
        }

        // Music button
        boolean music = getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_RESTORE, false);
        if (music) {
            startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        boolean music = getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_RESTORE, false);
        if (music) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }

    /**
     * Start the music
     */
    public void startMusic() {
        getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit()
                .putBoolean(PREF_RESTORE, true)
                .commit();
        ((Button) findViewById(R.id.wordgame_music_button))
                .setText(getResources().getString(R.string.wordgame_music_off_label));
        mMediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mMediaPlayer.setVolume(0.5f, 0.5f);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    /**
     * Stop the music
     */
    public void stopMusic() {
        getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit()
                .putBoolean(PREF_RESTORE, false)
                .commit();
        ((Button) findViewById(R.id.wordgame_music_button))
                .setText(getResources().getString(R.string.wordgame_music_on_label));
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }
}
