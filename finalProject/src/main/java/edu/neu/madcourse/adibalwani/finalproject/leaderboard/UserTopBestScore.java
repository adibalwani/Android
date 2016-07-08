package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;
import edu.neu.madcourse.adibalwani.finalproject.register.RegisterManager;

/**
 * Fragment for User's Top Best Score
 */
public class UserTopBestScore extends Fragment {

    private ListView listView;
    private LinearLayout rootView;
    private LeaderBoardAdapter leaderBoardAdapter;
    private ArrayList<User> leaderBoardList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaderboard_user_best_score, container, false);
        rootView = (LinearLayout) view.findViewById(R.id.root_view_two_player_leaderboard_list);
        leaderBoardAdapter = new LeaderBoardAdapter(getActivity(), R.layout.two_player_leaderboard_list, leaderBoardList);
        View header = inflater.inflate(R.layout.leaderboard_list_view_header, null);
        listView = (ListView)view.findViewById(R.id.leaderboad_user_score);
        listView.addHeaderView(header);
        listView.setAdapter(leaderBoardAdapter);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fetchUserSavedScore();
    }

    private void fetchUserSavedScore() {
        Activity activity = getActivity();
        LeaderBoardManager leaderBoardManager = new LeaderBoardManager(activity);
        if (leaderBoardManager.doesHistoryExist()) {
            List<Integer> historyList = leaderBoardManager.getHistory();
            RegisterManager registerManager = new RegisterManager(activity);
            String username = registerManager.getUsername();
            for (int score : historyList) {
                leaderBoardList.add(new User(username, score));
            }
        }
        leaderBoardAdapter.addAll(leaderBoardList);
        leaderBoardAdapter.notifyDataSetChanged();
    }
}
