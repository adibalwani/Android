package edu.neu.madcourse.adibalwani.wordgame;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    public static final String KEY_RESTORE = "key_restore";
    public static final String PREF_RESTORE = "pref_restore";
    public static final String PREF_FILE = GameActivity.class.getSimpleName();
    private GameFragment mGameFragment;
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordgame_activity_game);
        mGameFragment = (GameFragment) getFragmentManager()
                .findFragmentById(R.id.wordgame_fragment_game);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean music = getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_RESTORE, false);
        if (music) {
            startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String gameData = mGameFragment.saveState();
        getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit()
                .putString(PREF_RESTORE, gameData)
                .commit();

        boolean music = getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_RESTORE, false);
        if (music) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }

    /**
     * Restore the state
     */
    public void restoreState() {
        boolean restore = getIntent().getBooleanExtra(KEY_RESTORE, false);
        if (restore) {
            String gameData = getSharedPreferences(PREF_FILE, MODE_PRIVATE)
                    .getString(PREF_RESTORE, null);
            if (gameData != null) {
                mGameFragment.restoreState(gameData);
            }
        }
        getIntent().putExtra(GameActivity.KEY_RESTORE, true);
    }

    /**
     * Start the Music
     */
    public void startMusic() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mMediaPlayer.setVolume(0.5f, 0.5f);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    /**
     * Stop the music
     */
    public void stopMusic() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }
}
