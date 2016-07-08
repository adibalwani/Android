package edu.neu.madcourse.adibalwani.twoplayerwordgame;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GameFragment mGameFragment;
    private GamePlayManager mGamePlayManager;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twoplayerwordgame_activity_game);
        mGameFragment = (GameFragment) getFragmentManager()
                .findFragmentById(R.id.twoplayerwordgame_fragment_game);
        mGamePlayManager = new GamePlayManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean music = mGamePlayManager.isMusicOn();
        if (music) {
            startMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String gameData = mGameFragment.saveState();
        mGamePlayManager.putGameData(gameData);

        boolean music = mGamePlayManager.isMusicOn();
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
        boolean restore = getIntent().getBooleanExtra(Constants.INTENT_KEY_RESTORE, false);
        if (restore) {
            String gameData = mGamePlayManager.getGameData();
            if (gameData != null) {
                mGameFragment.restoreState(gameData);
            }
        }
        getIntent().putExtra(Constants.INTENT_KEY_RESTORE, true);
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
