package edu.neu.madcourse.adibalwani.finalproject.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;

public class EndGameDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface DismissListener {
        /**
         * Method to call on dismiss
         */
        void onDismiss();
    }

    private static final String BUNDLE_LAYOUT_ID = "1";
    private static final String BUNDLE_SCORE_ID = "2";

    private Activity mActivity;
    private DismissListener mDismissListener;
    private int mLayoutId;
    private int mScore;

    /**
     * Create new instance of EndGameDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of EndGameDialogFragment
     */
    static EndGameDialogFragment newInstance(int layoutId, int score, DismissListener listener) {
        EndGameDialogFragment dialogFragment = new EndGameDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        args.putInt(BUNDLE_SCORE_ID, score);
        dialogFragment.setArguments(args);
        dialogFragment.mDismissListener = listener;
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
        mScore = getArguments().getInt(BUNDLE_SCORE_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        TextView scoreView = (TextView) view.findViewById(R.id.finalproject_dialog_endgame_score);
        scoreView.setText(Constants.YOU_SCORED + mScore);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        /*if (resourceId == R.id.finalproject_dialog_pause_resume) {
            resumeGame();
        }*/
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mDismissListener.onDismiss();
        super.onDismiss(dialog);
    }
}
