package edu.neu.madcourse.adibalwani;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainScreenFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_screen, container, false);
        initButtonListener(rootView);
        mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tic_tac_toe_button:
                startTicTacToe();
                break;
            case R.id.dictionary_button:
                startDictionary();
                break;
            case R.id.word_game_button:
                startWordGame();
                break;
            case R.id.communication_button:
                startCommunicationTest();
                break;
            case R.id.two_player_word_game_button:
                startTwoPlayerWordGame();
                break;
            case R.id.two_player_trickiest_part_button:
                startTrickiestPart();
                break;
            case R.id.final_project_button:
                startFinalProject();
                break;
            case R.id.generate_error_button:
                generateError();
                break;
            case R.id.about_button:
                displayDialogWithDeviceId(R.layout.dialog_about);
                break;
            case R.id.quit_button:
                endActivity();
                break;
            default:
                break;
        }
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.tic_tac_toe_button).setOnClickListener(this);
        view.findViewById(R.id.dictionary_button).setOnClickListener(this);
        view.findViewById(R.id.word_game_button).setOnClickListener(this);
        view.findViewById(R.id.communication_button).setOnClickListener(this);
        view.findViewById(R.id.two_player_word_game_button).setOnClickListener(this);
        view.findViewById(R.id.two_player_trickiest_part_button).setOnClickListener(this);
        view.findViewById(R.id.final_project_button).setOnClickListener(this);
        view.findViewById(R.id.generate_error_button).setOnClickListener(this);
        view.findViewById(R.id.about_button).setOnClickListener(this);
        view.findViewById(R.id.quit_button).setOnClickListener(this);
    }

    /**
     * Start the Ultimate TicTacToev6 Game
     */
    private void startTicTacToe() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.tictactoe.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the Dictionary
     */
    private void startDictionary() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.dictionary.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the Word Game
     */
    private void startWordGame() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.wordgame.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the test code activity for communication
     */
    private void startCommunicationTest() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.communication.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the two player Word Game
     */
    private void startTwoPlayerWordGame() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.twoplayerwordgame.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the trickiest part of the project
     */
    private void startTrickiestPart() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.trickiestpart.MainActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the Final Project - Virtual BasketBall
     */
    private void startFinalProject() {
        /*Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.finalproject.MainActivity.class);*/
        Intent intent = new Intent(mActivity, FinalProjectActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Generate an error for program to crash
     */
    private void generateError() {
        String error = null;
        error.isEmpty();
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
     * Display dialog box containing the given layout along with device ID
     *
     * @param layoutId Layout to be displayed
     */
    private void displayDialogWithDeviceId(int layoutId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        TelephonyManager telephonyManager = (TelephonyManager) mActivity.getSystemService(
                Context.TELEPHONY_SERVICE
        );
        String deviceId = telephonyManager.getDeviceId();
        if (deviceId == null) {
            getResources().getString(R.string.about_default_device_id);
        }
        DialogFragment dialogFragment = CustomDialogFragment.newInstance(layoutId, deviceId);
        dialogFragment.show(fragmentTransaction, "dialog");
    }

    /**
     * Handle Quit by killing the current Activity
     */
    private void endActivity() {
        mActivity.finish();
    }

}
