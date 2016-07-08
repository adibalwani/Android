package edu.neu.madcourse.adibalwani.wordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class CustomDialogFragment extends DialogFragment {
    public static final String BUNDLE_LAYOUT_ID = "1";
    public static final String BUNDLE_SCORE_ID = "2";

    private Activity mActivity;
    private int mLayoutId;
    private String mScore;

    /**
     * Create new instance of CustomDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of CustomDialogFragment
     */
    static CustomDialogFragment newInstance(int layoutId) {
        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    /**
     * Create new instance of CustomDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @param score The device Id to print
     * @return Instance of CustomDialogFragment
     */
    static CustomDialogFragment newInstance(int layoutId, int score) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        args.putString(BUNDLE_SCORE_ID, score + "");

        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
        mScore = getArguments().getString(BUNDLE_SCORE_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);

        // Set score if provided
        if (mScore != null) {
            TextView textView = (TextView) view.findViewById(R.id.wordgame_dialog_score);
            textView.setText("Game ended. You scored: " + mScore);
            builder.setView(view)
                    .setPositiveButton(R.string.wordgame_end_game_label, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    });
        } else {
            builder.setView(view)
                    .setPositiveButton(R.string.wordgame_about_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CustomDialogFragment.this.getDialog().cancel();
                        }
                    });
        }


        return builder.create();
    }
}
