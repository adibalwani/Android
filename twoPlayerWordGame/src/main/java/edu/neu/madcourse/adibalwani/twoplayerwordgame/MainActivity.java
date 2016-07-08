package edu.neu.madcourse.adibalwani.twoplayerwordgame;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private GamePlayManager mGamePlayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twoplayerwordgame_activity_main);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.twoplayerwordgame_titlebar_name));

        mGamePlayManager = new GamePlayManager(this);

        mMediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mMediaPlayer.setVolume(0.5f, 0.5f);
        mMediaPlayer.setLooping(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume button
        String gameData = mGamePlayManager.getGameData();
        if (gameData != null) {
            findViewById(R.id.twoplayerwordgame_continue_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.twoplayerwordgame_continue_button).setVisibility(View.GONE);
        }

        // Music button
        boolean music = mGamePlayManager.isMusicOn();
        if (music) {
            startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        boolean music = mGamePlayManager.isMusicOn();
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
        mGamePlayManager.putMusic(true);
        ((Button) findViewById(R.id.twoplayerwordgame_music_button))
                .setText(getResources().getString(R.string.twoplayerwordgame_music_off_label));
        mMediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mMediaPlayer.setVolume(0.5f, 0.5f);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    /**
     * Stop the music
     */
    public void stopMusic() {
        mGamePlayManager.putMusic(false);
        ((Button) findViewById(R.id.twoplayerwordgame_music_button))
                .setText(getResources().getString(R.string.twoplayerwordgame_music_on_label));
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }
}
