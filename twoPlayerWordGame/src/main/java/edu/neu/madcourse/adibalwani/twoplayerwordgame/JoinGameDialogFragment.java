package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class JoinGameDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;
    private DismissListener mDismissListener;

    public interface DismissListener {
        /**
         * Method to call on Dismiss
         */
        void onDismiss(String username);
    }

    /**
     * Create new instance of JoinGameDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of JoinGameDialogFragment
     */
    static JoinGameDialogFragment newInstance(int layoutId, DismissListener listener) {
        JoinGameDialogFragment dialogFragment = new JoinGameDialogFragment();
        dialogFragment.setDismissListener(listener);
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
        final View view = inflater.inflate(mLayoutId, null);

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_join_game_dialog_search,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText username = (EditText) view.findViewById(
                                        R.id.twoplayerwordgame_join_game_dialog_username
                                );
                                mDismissListener.onDismiss(username.getText().toString());
                                JoinGameDialogFragment.this.getDialog().cancel();
                            }
                        }
                );
        return builder.create();
    }

    private void setDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
