package edu.neu.madcourse.adibalwani.finalproject.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import edu.neu.madcourse.adibalwani.finalproject.R;

public class PauseDialogFragment extends DialogFragment implements View.OnClickListener {

    public interface DismissListener {
        /**
         * Method to call on game End
         */
        void onEndClicked();

        /**
         * Method to call on game resume
         */
        void onResumeClicked();
    }

    private static final String BUNDLE_LAYOUT_ID = "1";
    private static final String LOG_TAG = PauseDialogFragment.class.getSimpleName();

    private Activity mActivity;
    private DismissListener mDismissListener;
    private int mLayoutId;
    private Button mResumeGame;
    private Button mEndGame;

    /**
     * Create new instance of PauseDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of PauseDialogFragment
     */
    static PauseDialogFragment newInstance(int layoutId, DismissListener listener) {
        PauseDialogFragment dialogFragment = new PauseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
        dialogFragment.mDismissListener = listener;
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        initViews(view);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.finalproject_dialog_pause_resume) {
            resumeGame();
        } else if (resourceId == R.id.finalproject_dialog_pause_end) {
            endGame();
        }
    }

    /**
     * Initialize the view instances
     */
    private void initViews(View view) {
        mResumeGame = (Button) view.findViewById(R.id.finalproject_dialog_pause_resume);
        mEndGame = (Button) view.findViewById(R.id.finalproject_dialog_pause_end);
        mResumeGame.setOnClickListener(this);
        mEndGame.setOnClickListener(this);
    }

    /**
     * Resume the current game
     */
    private void resumeGame() {
        mDismissListener.onResumeClicked();
        PauseDialogFragment.this.dismiss();
    }

    /**
     * End the current game
     */
    private void endGame() {
        mDismissListener.onEndClicked();
        PauseDialogFragment.this.dismiss();
    }
}
