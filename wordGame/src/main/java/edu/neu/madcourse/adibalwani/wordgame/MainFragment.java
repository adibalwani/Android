package edu.neu.madcourse.adibalwani.wordgame;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wordgame_fragment_main, container, false);
        mActivity = getActivity();
        initButtonListener(rootView);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.wordgame_new_game_button) {
            startNewGame();
        } else if (resourceId == R.id.wordgame_continue_button) {
            continueGame();
        } else if (resourceId == R.id.wordgame_acknowledgements_button) {
            displayDialog(R.layout.wordgame_dialog_acknowledgement);
        } else if (resourceId == R.id.wordgame_instructions_button) {
            displayDialog(R.layout.wordgame_dialog_instruction);
        } else if (resourceId == R.id.wordgame_music_button) {
            toggleMusic();
        } else if (resourceId == R.id.wordgame_quit_button) {
            endActivity();
        }
    }

    /**
     * Turn ON/OFF background music
     */
    private void toggleMusic() {
        boolean music = mActivity.getSharedPreferences(MainActivity.PREF_FILE, MainActivity.MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_RESTORE, false);
        if (music) {
            ((MainActivity) mActivity).stopMusic();
        } else {
            ((MainActivity) mActivity).startMusic();
        }
    }

    /**
     * Display dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    private void displayDialog(int layoutId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = CustomDialogFragment.newInstance(layoutId);
        dialogFragment.show(fragmentTransaction, "dialog");
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.wordgame_new_game_button).setOnClickListener(this);
        view.findViewById(R.id.wordgame_continue_button).setOnClickListener(this);
        view.findViewById(R.id.wordgame_instructions_button).setOnClickListener(this);
        view.findViewById(R.id.wordgame_music_button).setOnClickListener(this);
        view.findViewById(R.id.wordgame_acknowledgements_button).setOnClickListener(this);
        view.findViewById(R.id.wordgame_quit_button).setOnClickListener(this);
    }

    /**
     * Continue Word Game - Scraggle
     */
    private void continueGame() {
        Intent intent = new Intent(mActivity, GameActivity.class);
        intent.putExtra(GameActivity.KEY_RESTORE, true);
        mActivity.startActivity(intent);
    }

    /**
     * Start a new Word Game - Scraggle
     */
    private void startNewGame() {
        Intent intent = new Intent(mActivity, GameActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Handle Return press by killing the current Activity
     */
    private void endActivity() {
        getActivity().finish();
    }
}
