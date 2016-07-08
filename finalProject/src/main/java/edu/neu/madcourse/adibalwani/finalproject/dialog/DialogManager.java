package edu.neu.madcourse.adibalwani.finalproject.dialog;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;

import edu.neu.madcourse.adibalwani.finalproject.Constants;

/**
 * Class used to manage dialog fragments
 */
public class DialogManager {

    private Activity mActivity;

    public DialogManager(Activity activity) {
        mActivity = activity;
    }

    /**
     * Display registeration dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    public void displayRegisterDialog(final int layoutId,
                                      RegisterDialogFragment.DismissListener listener) {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = RegisterDialogFragment.newInstance(layoutId, listener);
        dialogFragment.show(fragmentTransaction, Constants.REGISTER_DIALOG_TAG);
    }

    /**
     * Display pause dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    public void displayPauseDialog(final int layoutId,
                                   PauseDialogFragment.DismissListener listener) {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = PauseDialogFragment.newInstance(layoutId, listener);
        dialogFragment.setCancelable(false);
        dialogFragment.show(fragmentTransaction, Constants.PAUSE_DIALOG_TAG);
    }

    /**
     * Display End Game dialog box containing the given layout and score
     *
     * @param layoutId Layout to be displayed
     */
    public void displayEndGameDialog(final int layoutId, int score,
                                     EndGameDialogFragment.DismissListener listener) {
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = EndGameDialogFragment.newInstance(layoutId, score, listener);
        dialogFragment.show(fragmentTransaction, Constants.END_GAME_DIALOG_TAG);
    }
}
