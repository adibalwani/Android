package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class SessionQuitDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;
    private DismissListener mDismissListener;

    public interface DismissListener {
        /**
         * Method to call on positive dismissal
         */
        void onPositiveDismiss();

        /**
         * Method to call on negative dismissal
         */
        void onNegativeDismiss();
    }

    /**
     * Create new instance of SessionQuitDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of SessionQuitDialogFragment
     */
    static SessionQuitDialogFragment newInstance(int layoutId, DismissListener listener) {
        SessionQuitDialogFragment dialogFragment = new SessionQuitDialogFragment();
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

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_session_quit_dialog_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDismissListener.onPositiveDismiss();
                                SessionQuitDialogFragment.this.getDialog().cancel();
                            }
                        }
                )
                .setNegativeButton(R.string.twoplayerwordgame_session_quit_dialog_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDismissListener.onNegativeDismiss();
                                SessionQuitDialogFragment.this.getDialog().cancel();
                            }
                        }
                );
        return builder.create();
    }
}
