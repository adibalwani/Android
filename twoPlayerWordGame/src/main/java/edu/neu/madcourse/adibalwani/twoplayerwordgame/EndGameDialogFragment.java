package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EndGameDialogFragment extends DialogFragment {
    public static final String BUNDLE_LAYOUT_ID = "1";
    public static final String BUNDLE_SCORE_ID = "2";

    private Activity mActivity;
    private int mLayoutId;
    private String mScore;

    /**
     * Create new instance of EndGameDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @param score The device Id to print
     * @return Instance of EndGameDialogFragment
     */
    static EndGameDialogFragment newInstance(int layoutId, int score) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        args.putString(BUNDLE_SCORE_ID, score + "");

        EndGameDialogFragment dialogFragment = new EndGameDialogFragment();
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

        TextView textView = (TextView) view.findViewById(R.id.twoplayerwordgame_dialog_score);
        textView.setText("Game ended. You scored: " + mScore);
        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_end_game_label,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mActivity.finish();
                            }
                        }
                );


        return builder.create();
    }
}
