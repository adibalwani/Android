package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

public class Tile {

    private final GameFragment mGame;
    private View mView;
    private String mText;
    private State mState;
    private Tile mGrid;

    public enum State {
        SELECTED,
        UNSELECTED,
        INACTIVE,
        LOCKED
    }

    public Tile(GameFragment game, Tile grid) {
        this.mGame = game;
        this.mState = State.UNSELECTED;
        this.mGrid = grid;
    }

    /**
     * Handle new update for the state change
     */
    public void notifyStateChanged() {
        if (!(mView instanceof Button)) {
            return;
        }

        Button button = (Button) mView;
        button.setBackgroundDrawable(getButtonBackgroundDrawable());
    }

    /**
     * Handle new update for the text change
     */
    public void notifyTextChanged() {
        if (!(mView instanceof Button)) {
            return;
        }

        Button button = (Button) mView;
        button.setText(mText);
    }

    /**
     * Remove background Drawable for button
     */
    public void removeBackgroundDrawable() {
        if (!(mView instanceof Button)) {
            return;
        }

        Button button = (Button) mView;
        Drawable background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_blank);
        button.setBackgroundDrawable(background);
    }

    /**
     * Get the current background drawable depending on the state
     *
     * @return Background Drawable
     */
    private Drawable getButtonBackgroundDrawable() {
        Drawable background = null;
        int phase = mGame.getPhase();
        switch (mState) {
            case SELECTED:
                if (phase == mGame.PHASE_1) {
                    background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_selected);
                } else if (phase == mGame.PHASE_2 && mGrid != null && mGrid.isInactive()) {
                    background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_inactive);
                } else if (phase == mGame.PHASE_2) {
                    background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_unselected);
                }
                break;
            case UNSELECTED:
                background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_unselected);
                break;
            case INACTIVE:
                background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_inactive);
                break;
            case LOCKED:
                background = mGame.getResources().getDrawable(R.drawable.twoplayerwordgame_tile_inactive);
                break;
            default:
                break;
        }
        return background;
    }

    /**
     * Check whether the given tile is modifiable or not
     *
     * @return true iff it is modifiable, false otherwise
     */
    public boolean isModifiable() {
        if (mState.equals(State.UNSELECTED)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether the given tile is selected or not
     *
     * @return true iff it is selected, false otherwise
     */
    public boolean isSelected() {
        if (mState.equals(State.SELECTED)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether the given tile is inactive or not
     *
     * @return true iff it is selected, false otherwise
     */
    public boolean isInactive() {
        if (mState.equals(State.INACTIVE)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether the given grid is locked or not
     *
     * @return true iff it is lcoked, false otherwise
     */
    public boolean isLocked() {
        if (mState.equals(State.LOCKED)) {
            return true;
        }

        return false;
    }

    /**
     * Return the current state of the Tile
     *
     * @return Tile state
     */
    public String getState() {
        StringBuilder builder = new StringBuilder();
        builder.append(mText);
        builder.append(":");
        builder.append(mState);
        return builder.toString();
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public void setState(State state) {
        this.mState = state;
    }

}
