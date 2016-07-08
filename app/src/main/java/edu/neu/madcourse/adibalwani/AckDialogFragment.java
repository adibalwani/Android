package edu.neu.madcourse.adibalwani;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class AckDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;

    /**
     * Create new instance of AcknowledgementDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of AcknowledgementDialogFragment
     */
    static AckDialogFragment newInstance(int layoutId) {
        AckDialogFragment dialogFragment = new AckDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
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

        builder.setView(view)
                .setPositiveButton(edu.neu.madcourse.adibalwani.twoplayerwordgame.R.string.twoplayerwordgame_about_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AckDialogFragment.this.getDialog().cancel();
                            }
                        }
                );
        return builder.create();
    }
}
