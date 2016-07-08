package edu.neu.madcourse.adibalwani.finalproject.leaderboard;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import edu.neu.madcourse.adibalwani.finalproject.R;

public class LeaderBoardActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton topRun;
    private ImageButton highScores;
    private FragmentManager frag = null;
    private enum ScoreType {
        TOPSCORE, HIGHSCORE
    }
    private ImageView title;
    private ScoreType mScoreType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        getSupportActionBar().hide();
        frag = getFragmentManager();
        frag.beginTransaction().replace(R.id.leaderboard_fragment, new LeaderBoardTopScore()).commit();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LeaderBoardManager leaderBoardManager = new LeaderBoardManager(this);
        Log.e("adib", "History " + leaderBoardManager.doesHistoryExist());
        if (!leaderBoardManager.doesHistoryExist()) {
            highScores.setVisibility(View.GONE);
        } else {
            highScores.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        title = (ImageView) findViewById(R.id.leaderboard_title);
        topRun = (ImageButton) findViewById(R.id.leaderboard_top_run);
        topRun.setOnClickListener(this);
        highScores = (ImageButton) findViewById(R.id.leaderboard_my_high_scores);
        highScores.setOnClickListener(this);
        topRun.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonselected));
        mScoreType = ScoreType.TOPSCORE;
    }

    private void changeFragment(ScoreType type) {
        switch (type) {
            case HIGHSCORE:
                topRun.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonshape));
                highScores.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonselected));
                frag.beginTransaction().replace(R.id.leaderboard_fragment, new UserTopBestScore()).commit();
                title.setImageDrawable(getResources().getDrawable(R.drawable.yourtopscores));
                break;
            case TOPSCORE:
                topRun.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonselected));
                highScores.setBackgroundDrawable(getResources().getDrawable(R.drawable.buttonshape));
                frag.beginTransaction().replace(R.id.leaderboard_fragment, new LeaderBoardTopScore()).commit();
                title.setImageDrawable(getResources().getDrawable(R.drawable.leaderboard));
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.leaderboard_top_run && mScoreType != ScoreType.TOPSCORE) {
            mScoreType = ScoreType.TOPSCORE;
            changeFragment(ScoreType.TOPSCORE);
        } else if (resourceId == R.id.leaderboard_my_high_scores && mScoreType != ScoreType.HIGHSCORE) {
            mScoreType = ScoreType.HIGHSCORE;
            changeFragment(ScoreType.HIGHSCORE);
        }
    }
}
