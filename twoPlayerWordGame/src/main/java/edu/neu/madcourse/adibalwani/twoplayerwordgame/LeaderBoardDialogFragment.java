package edu.neu.madcourse.adibalwani.twoplayerwordgame;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class LeaderBoardDialogFragment extends DialogFragment {
    private static final String BUNDLE_LAYOUT_ID = "1";
    public static final String LEADERBOARD = "LEADERBOARD";

    private Activity mActivity;
    private int mLayoutId;

    /**
     * Create new instance of LeaderBoardDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of LeaderBoardDialogFragment
     */
    static LeaderBoardDialogFragment newInstance(int layoutId) {
        LeaderBoardDialogFragment dialogFragment = new LeaderBoardDialogFragment();
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
        populateList(view);

        builder.setView(view)
                .setPositiveButton(R.string.twoplayerwordgame_leaderboard_dialog_end,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LeaderBoardDialogFragment.this.getDialog().cancel();
                            }
                        }
                );
        return builder.create();
    }

    /**
     * Initialize the View Adapter and data
     *
     * @param view Fragment View
     */
    private void populateList(final View view) {
        new FirebaseClient(mActivity).get(LEADERBOARD, new FirebaseClient.ResponseListener() {
            @Override
            public void onSuccess(String value) {
                if (value == null) {
                    displayToast("Leaderboard is Empty");
                } else {
                    String[] leaders = value.split("\\s+");
                    LeaderBoard[] leaderBoards = new LeaderBoard[leaders.length];
                    for (int i = 0; i < leaders.length; i++) {
                        String[] data = leaders[i].split(":");
                        leaderBoards[i] = new LeaderBoard(data[0], data[1], data[2], data[3]);
                    }
                    Arrays.sort(leaderBoards);
                    ListView listView = (ListView) view.findViewById(
                            R.id.twoplayerwordgame_leaderboard_dialog_list_view);
                    LeaderBoardArrayAdapter adapter = new LeaderBoardArrayAdapter(
                            mActivity.getApplicationContext(),
                            leaderBoards);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(String value) {
                displayToast("Retry: Failed to contact server");
                dismiss();
            }
        });
    }

    /**
     * Display a toast message with the given message for 1 second
     *
     * @param message The message to display
     */
    private void displayToast(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
    }

    /**
     * LeaderboardAdapter class
     */
    private static class LeaderBoardArrayAdapter extends ArrayAdapter<LeaderBoard> {

        private final Context mContext;
        private final LeaderBoard[] mLeaderBoards;

        public LeaderBoardArrayAdapter(Context context, LeaderBoard[] leaderBoards) {
            super(context, R.layout.twoplayerwordgame_dialog_leaderboard_list_view, leaderBoards);
            mContext = context;
            mLeaderBoards = leaderBoards;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.twoplayerwordgame_dialog_leaderboard_list_view,
                    parent, false);
            TextView player = (TextView)
                    rowView.findViewById(R.id.twoplayerwordgame_leaderboard_dialog_player);
            TextView date = (TextView)
                    rowView.findViewById(R.id.twoplayerwordgame_leaderboard_dialog_date);
            TextView score = (TextView)
                    rowView.findViewById(R.id.twoplayerwordgame_leaderboard_dialog_score);
            TextView partner = (TextView)
                    rowView.findViewById(R.id.twoplayerwordgame_leaderboard_dialog_partner);

            player.setText(mLeaderBoards[position].getPlayer());
            date.setText(mLeaderBoards[position].getDate());
            score.setText(mLeaderBoards[position].getScore());
            partner.setText(mLeaderBoards[position].getPartner());

            return rowView;
        }
    }

    /**
     * Class representing leaderboard details
     */
    private static class LeaderBoard implements Comparable<LeaderBoard> {

        private final String player;
        private final String date;
        private final String score;
        private final String partner;

        public LeaderBoard(String player, String date, String score, String partner) {
            this.player = player;
            this.date = date;
            this.score = score;
            this.partner = partner;
        }

        @Override
        public int compareTo(LeaderBoard another) {
            int scoreInt = Integer.parseInt(score);
            int anotherScoreInt = Integer.parseInt(another.getScore());
            if (scoreInt > anotherScoreInt) {
                return -1;
            } else if (scoreInt < anotherScoreInt) {
                return 1;
            } else {
                return 0;
            }
        }

        public String getPlayer() {
            return player;
        }

        public String getDate() {
            return date;
        }

        public String getScore() {
            return score;
        }

        public String getPartner() {
            return partner;
        }
    }
}
