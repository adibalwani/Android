package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import edu.neu.madcourse.adibalwani.finalproject.Constants;
import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;
import edu.neu.madcourse.adibalwani.finalproject.network.NetworkManager;
import edu.neu.madcourse.adibalwani.finalproject.register.RegisterManager;

/**
 * @author rachit on 17-04-2016.
 */
public class LeaderBoardTopScore extends Fragment {

    private Firebase mFireRef;
    private ListView listView;
    private LeaderBoardAdapter leaderBoardAdapter;
    private ArrayList<User> leaderBoardList = new ArrayList<>();
    private TextView rank;
    private TextView name;
    private TextView score;
    private LinearLayout rootView;
    private LinearLayout rootViewList;
    private View mOwnRankView;
    private View mYourScoreText;
    private boolean networkFailed;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_two_player_leader_board, container, false);
        rootView = (LinearLayout) view.findViewById(R.id.root_view_activity_two_player_leader_board);
        rootViewList = (LinearLayout) view.findViewById(R.id.root_view_two_player_leaderboard_list);
        rank = (TextView) view.findViewById(R.id.leaderboard_current_user_rank);
        name = (TextView) view.findViewById(R.id.leaderboard_current_user_name);
        score = (TextView) view.findViewById(R.id.leaderboard_current_user_score);
        leaderBoardAdapter = new LeaderBoardAdapter(getActivity(), R.layout.two_player_leaderboard_list, leaderBoardList);
        View header = (View) inflater.inflate(R.layout.leaderboard_list_view_header, null);
        listView = (ListView)view.findViewById(R.id.two_player_leaderboad);
        listView.addHeaderView(header);
        listView.setAdapter(leaderBoardAdapter);
        Firebase.setAndroidContext(getActivity());
        mFireRef = new Firebase(Constants.FIREBASE_DB);
        mOwnRankView = view.findViewById(R.id.two_player_your_rank);
        mYourScoreText = view.findViewById(R.id.your_score);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
        font.overrideFonts(getActivity().getApplicationContext(), rootViewList);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /*RegisterManager registerManager = new RegisterManager(getActivity());
        String username = registerManager.getUsername();
        if (username == null) {
            mOwnRankView.setVisibility(View.GONE);
            mYourScoreText.setVisibility(View.GONE);
        } else {
            mOwnRankView.setVisibility(View.VISIBLE);
            mYourScoreText.setVisibility(View.VISIBLE);
        }
        Activity activity = getActivity();
        if (NetworkManager.isNetworkAvailable(activity)) {
            LeaderBoardManager leaderBoardManager = new LeaderBoardManager(activity);
            leaderBoardManager.syncWithFirebase();
            getTopFiveLeaderBoardUser();
            getCurrentUserRank(username);
        } else {
            NetworkManager.displayNetworkUnavailableToast(activity);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!networkFailed) {
            RegisterManager registerManager = new RegisterManager(getActivity());
            String username = registerManager.getUsername();
            if (username == null) {
                mOwnRankView.setVisibility(View.GONE);
                mYourScoreText.setVisibility(View.GONE);
            } else {
                mOwnRankView.setVisibility(View.VISIBLE);
                mYourScoreText.setVisibility(View.VISIBLE);
            }
            Activity activity = getActivity();
            if (NetworkManager.isNetworkAvailable(activity)) {
                LeaderBoardManager leaderBoardManager = new LeaderBoardManager(activity);
                leaderBoardManager.syncWithFirebase();
                getTopFiveLeaderBoardUser();
                getCurrentUserRank(username);
            } else {
                networkFailed = true;
                NetworkManager.displayNetworkUnavailableToast(activity);
            }
        }
    }

    public void getCurrentUserRank(final String username) {
        Firebase ref = mFireRef.child(Constants.USERS);
        Query query = ref.orderByChild("score");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                int rank = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    rank++;
                    User user = snapshot.getValue(User.class);
                    if (user.getName().equals(username)) {
                        name.setText(user.getName());
                        score.setText(String.valueOf(user.getScore()));
                        break;
                    }
                }
                LeaderBoardTopScore.this.rank.setText(String.valueOf(size - rank + 1));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(Constants.TAG, "Couldn't fetch users " + firebaseError.getMessage());
            }
        });
    }

    public void getTopFiveLeaderBoardUser() {
        leaderBoardList.clear();
        Firebase ref = mFireRef.child(Constants.USERS);
        Query query = ref.orderByValue().limitToFirst(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    leaderBoardList.add(user);
                }
                Collections.sort(leaderBoardList);
                leaderBoardAdapter.addAll(leaderBoardList);
                leaderBoardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(Constants.TAG, "Couldn't fetch users " + firebaseError.getMessage());
            }
        });
    }
}
