package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.util.Log;

public class GamePlayManager {

    public static final String KEY_RESTORE = "key_restore_twoplayer";
    public static final String KEY_WAIT = "key_wait_twoplayer";
    public static final String KEY_MUSIC = "key_music_twoplayer";
    public static final String PREF_FILE = GameActivity.class.getSimpleName();

    private Activity mActivity;
    private String mGameData;
    private boolean mWait;
    private boolean mMusic;

    public GamePlayManager(Activity activity) {
        mActivity = activity;
        notifyGameChanged();
    }

    /**
     * Store the game data and the wait status in Shared Preference
     *
     * @param gameData The game state
     * @param wait Wait status
     */
    public void putGamePlay(String gameData, boolean wait) {
        storeGameData(gameData);
        storeWait(wait);
        notifyGameChanged();
    }

    /**
     * Store the game data in Shared Preference
     *
     * @param gameData The game state
     */
    public void putGameData(String gameData) {
        storeGameData(gameData);
        notifyGameChanged();
    }

    /**
     * Remove the game data and the wait status from Shared Preference
     */
    public void removeGamePlay() {
        removeGameData();
        removeWait();
        notifyGameChanged();
    }

    /**
     * Store the wait status in Shared Preference
     *
     * @param wait Wait status
     */
    public void putWait(boolean wait) {
        storeWait(wait);
        notifyGameChanged();
    }

    /**
     * Store the music status in Shared Preference
     *
     * @param music Music status
     */
    public void putMusic(boolean music) {
        storeMusic(music);
        notifyGameChanged();
    }

    /**
     * Check whether the player has his game turn
     *
     * @return true iff its player's turn. False, otherwise
     */
    public boolean hasGameTurn() {
        notifyGameChanged();
        /*StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.e("adib", stackTraceElements[i].getMethodName());
        }*/

        return !mWait;
    }

    /**
     * Check whether the music is ON/OFF
     *
     * @return true iff it is. False, otherwise
     */
    public boolean isMusicOn() {
        notifyGameChanged();
        return mMusic;
    }

    /**
     * Get the stored game data
     *
     * @return Game data
     */
    public String getGameData() {
        notifyGameChanged();
        return mGameData;
    }

    /**
     * Notify change in state
     */
    private void notifyGameChanged() {
        mWait = fetchWait();
        mGameData = fetchGameData();
        mMusic = fetchMusic();
    }

    /**
     * Store the state of the game in Shared Preference
     *
     * @param gameData The game state
     */
    private void storeGameData(String gameData) {
        mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                .putString(KEY_RESTORE, gameData)
                .commit();
    }

    /**
     * Store the turn of the game in Shared Preference
     *
     * @param wait The game turn
     */
    private void storeWait(boolean wait) {
        mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                .putBoolean(KEY_WAIT, wait)
                .commit();
    }

    /**
     * Store the music state in Shared Preference
     *
     * @param music Music state
     */
    private void storeMusic(boolean music) {
        mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                .putBoolean(KEY_MUSIC, music)
                .commit();
    }

    /**
     * Remove the game data from Shared Preference
     */
    private void removeGameData() {
        mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                .remove(KEY_RESTORE)
                .commit();
    }

    /**
     * Remove wait status from Shared Preference
     */
    private void removeWait() {
        mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE).edit()
                .remove(KEY_WAIT)
                .commit();
    }

    /**
     * Get the stored game data
     *
     * @return Game data if available. Null, otherwise
     */
    private String fetchGameData() {
        return mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE)
                .getString(KEY_RESTORE, null);
    }

    /**
     * Get the stored wait status
     *
     * @return Wait status if available. False, otherwise
     */
    private boolean fetchWait() {
        return mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE)
                .getBoolean(KEY_WAIT, false);
    }

    /**
     * Get the stored music status
     *
     * @return Music status if available. False, otherwise
     */
    private boolean fetchMusic() {
        return mActivity.getSharedPreferences(PREF_FILE, GameActivity.MODE_PRIVATE)
                .getBoolean(KEY_MUSIC, false);
    }
}
