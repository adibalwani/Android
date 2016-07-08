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

public class WaitForTurnDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";

    private Activity mActivity;
    private int mLayoutId;
    private DismissListener mDismissListener;
    private BroadcastReceiver mBroadcastReceiver;
    private GamePlayManager mGamePlayManager;
    private boolean registered;

    public interface DismissListener {
        /**
         * Method to call on Dismiss
         */
        void onDismiss(boolean hasGameTurn);

        /**
         * Method to call on GameEnd
         *
         * @param score Game score
         */
        void onGameEnd(int score);
    }

    /**
     * Create new instance of WaitDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of WaitDialogFragment
     */
    static WaitForTurnDialogFragment newInstance(int layoutId, DismissListener listener) {
        WaitForTurnDialogFragment dialogFragment = new WaitForTurnDialogFragment();
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
        mGamePlayManager = new GamePlayManager(mActivity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_wait_dialog_end,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDismissListener.onDismiss(false);
                                registered = false;
                                mActivity.unregisterReceiver(mBroadcastReceiver);
                                mActivity.finish();
                            }
                        }
                );
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGamePlayManager.hasGameTurn()) {
            mDismissListener.onDismiss(mGamePlayManager.hasGameTurn());
            WaitForTurnDialogFragment.this.getDialog().cancel();
        }
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (registered) {
            mActivity.unregisterReceiver(mBroadcastReceiver);
        }
    }

    /**
     * Register a broadcast receiver to handle player found
     */
    private void registerBroadcastReceiver() {
        registered = true;
        final IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory(mActivity.getPackageName());
        filter.setPriority(2);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (!extras.isEmpty()) {
                    String gameData = extras.getString("gamestate");
                    String wait = extras.getString("wait");
                    if (gameData != null && wait != null) {
                        mActivity.unregisterReceiver(mBroadcastReceiver);
                        registered = false;
                        abortBroadcast();
                        if (gameData.equals("null")) {
                            String score = extras.getString("score");
                            mGamePlayManager.removeGamePlay();
                            mDismissListener.onGameEnd(Integer.parseInt(score));
                        } else {
                            mGamePlayManager.putGamePlay(gameData, Boolean.valueOf(wait));
                            mDismissListener.onDismiss(mGamePlayManager.hasGameTurn());
                        }
                        WaitForTurnDialogFragment.this.getDialog().cancel();
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
