package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class PrivateMatchDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;
    private ClickListener mClickListener;

    public interface ClickListener {
        /**
         * Method to when Join Game button is clicked
         */
        void onJoinGameClick();

        /**
         * Method to when Host Game button is clicked
         */
        void onHostGameClick();
    }

    /**
     * Create new instance of PrivateMatchDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of PrivateMatchDialogFragment
     */
    static PrivateMatchDialogFragment newInstance(int layoutId, ClickListener listener) {
        PrivateMatchDialogFragment dialogFragment = new PrivateMatchDialogFragment();
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
        View view = inflater.inflate(mLayoutId, null);
        initButtonListener(view);

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_private_match_dialog_end,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PrivateMatchDialogFragment.this.getDialog().cancel();
                            }
                        }
                );
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.twoplayerwordgame_private_match_dialog_host_button) {
            mClickListener.onHostGameClick();
            PrivateMatchDialogFragment.this.getDialog().cancel();
        } else if (resourceId == R.id.twoplayerwordgame_private_match_dialog_join_button) {
            mClickListener.onJoinGameClick();
            PrivateMatchDialogFragment.this.getDialog().cancel();
        }
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.twoplayerwordgame_private_match_dialog_join_button).setOnClickListener(this);
        view.findViewById(R.id.twoplayerwordgame_private_match_dialog_host_button).setOnClickListener(this);
    }

    private void setDismissListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }
}
