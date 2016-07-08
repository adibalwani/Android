package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class WaitDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;
    private DismissListener mDismissListener;
    private BroadcastReceiver mBroadcastReceiver;
    private RegisterManager mRegisterManager;
    private GamePlayManager mGamePlayManager;

    public interface DismissListener {
        /**
         * Method to call on Dismiss
         */
        void onDismiss(boolean gameFound);
    }

    /**
     * Create new instance of WaitDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of WaitDialogFragment
     */
    static WaitDialogFragment newInstance(int layoutId, DismissListener listener) {
        WaitDialogFragment dialogFragment = new WaitDialogFragment();
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
        mRegisterManager = new RegisterManager(mActivity);
        mGamePlayManager = new GamePlayManager(mActivity);
        registerBroadcastReceiver();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);
        ((TextView) view.findViewById(R.id.twoplayergame_dialog_waiting_username)).setText(
                mRegisterManager.getUsername()
        );

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_wait_dialog_end,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDismissListener.onDismiss(false);
                                mActivity.unregisterReceiver(mBroadcastReceiver);
                            }
                        }
                );
        return builder.create();
    }



    /**
     * Register a broadcast receiver to handle player found
     */
    private void registerBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory(mActivity.getPackageName());
        filter.setPriority(2);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Wierdly GCM is sending multiple broadcast for the first time
                // TODO : A hack though works like a charm
                if (WaitDialogFragment.this.getDialog() == null) {
                    mActivity.unregisterReceiver(mBroadcastReceiver);
                    return;
                }

                Bundle extras = intent.getExtras();
                if (!extras.isEmpty()) {
                    String gameData = extras.getString("gamestate");
                    String opponent = extras.getString("opponent");
                    String wait = extras.getString("wait");
                    if (gameData != null && opponent != null && wait != null) {
                        abortBroadcast();
                        mGamePlayManager.putGamePlay(gameData, Boolean.valueOf(wait));
                        mRegisterManager.registerOpponent(opponent);
                        //mActivity.unregisterReceiver(mBroadcastReceiver);
                        mDismissListener.onDismiss(true);
                        WaitDialogFragment.this.getDialog().cancel();
                    }
                }
            }
        };
        mActivity.registerReceiver(
                mBroadcastReceiver,
                filter,
                "com.google.android.c2dm.permission.SEND",
                null
        );
    }

    private void setDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }
}
