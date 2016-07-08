package edu.neu.madcourse.adibalwani.finalproject;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.neu.madcourse.adibalwani.finalproject.dialog.RegisterDialogFragment;
import edu.neu.madcourse.adibalwani.finalproject.leaderboard.LeaderBoardActivity;
import edu.neu.madcourse.adibalwani.finalproject.tutorial.StartTutorialActivity;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private Animation slide;
    private Button resumeGame;
    private Button newGame;
    private Button leaderboard;
    private TextView tutorial;
    private MediaPlayer music;
    private LinearLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        getSupportActionBar().hide();
        initViews();
    }

    private void initViews() {
        rootView = (LinearLayout) findViewById(R.id.root_view_activity_home_screen);
        resumeGame = (Button) findViewById(R.id.home_screen_resume_game);
        resumeGame.setOnClickListener(this);
        newGame = (Button) findViewById(R.id.home_screen_new_game);
        newGame.setOnClickListener(this);
        leaderboard = (Button) findViewById(R.id.home_screen_leaderboard);
        leaderboard.setOnClickListener(this);
        tutorial = (TextView) findViewById(R.id.home_screen_play_tutorial);
        tutorial.setOnClickListener(this);
        slide = AnimationUtils.loadAnimation(this, R.anim.slide);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getApplicationContext(), rootView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GameSharedPreference gameSharedPreference = new GameSharedPreference(this);
        if (gameSharedPreference.getString(Constants.PROPERTY_GAME_CONFIG).isEmpty()) {
            resumeGame.setVisibility(View.GONE);
        } else {
            resumeGame.setVisibility(View.VISIBLE);
        }
        resumeGame.setAnimation(slide);
        newGame.setAnimation(slide);
        leaderboard.setAnimation(slide);
        startMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (music != null) {
            music.stop();
            music.release();
        }
    }

    private void startMusic() {
        music = MediaPlayer.create(getApplicationContext(), R.raw.backgroundmusic);
        music.setLooping(true); // Set looping
        music.setVolume(0.6f, 0.6f);
        music.start();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.home_screen_resume_game) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(Constants.INTENT_KEY_RESTORE, true);
            startActivity(intent);
        } else if (resourceId == R.id.home_screen_new_game) {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        } else if (resourceId == R.id.home_screen_leaderboard) {
            Intent intent = new Intent(this, LeaderBoardActivity.class);
            startActivity(intent);
        } else if (resourceId == R.id.home_screen_play_tutorial) {
            Intent intent = new Intent(this, StartTutorialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
}
