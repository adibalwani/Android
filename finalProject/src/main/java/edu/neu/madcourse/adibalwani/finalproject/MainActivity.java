package edu.neu.madcourse.adibalwani.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;

import edu.neu.madcourse.adibalwani.finalproject.tutorial.StartTutorialActivity;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private Animation scale;
    private Animation fadeIn;
    private Animation dribble;
    private MediaPlayer music;

    private LinearLayout fullScreen;
    private TextView increaseVolume;
    private TextView tapToStart;
    private ImageView basketBall;

    private GameSharedPreference sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalproject_activity_main);
        getSupportActionBar().hide();
        initViews();
        scale = AnimationUtils.loadAnimation(this, R.anim.scale);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        dribble = AnimationUtils.loadAnimation(this, R.anim.dribble);
    }

    private void initViews() {
        fullScreen = (LinearLayout) findViewById(R.id.main_screen_full_layout);
        fullScreen.setOnTouchListener(this);
        increaseVolume = (TextView) findViewById(R.id.main_screen_increase_volume);
        tapToStart = (TextView) findViewById(R.id.main_screen_tap_to_start);
        basketBall = (ImageView) findViewById(R.id.main_screen_basket_ball);
        sharedPref = new GameSharedPreference(this);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getApplicationContext(), fullScreen);
    }

    private void startMusic() {
        music = MediaPlayer.create(getApplicationContext(), R.raw.backgroundmusic);
        music.setLooping(true); // Set looping
        music.setVolume(0.6f, 0.6f);
        music.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (music != null) {
            music.stop();
            music.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        increaseVolume.setAnimation(scale);
        tapToStart.setAnimation(fadeIn);
        basketBall.setAnimation(dribble);
        startMusic();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Intent intent;
        String tutorial = sharedPref.getString(Constants.TUTORIAL_COMPLETED);
        //intent = new Intent(MainActivity.this, HomeScreenActivity.class);
        if (tutorial.isEmpty()) {
            intent = new Intent(MainActivity.this, StartTutorialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        } else {
            intent = new Intent(MainActivity.this, HomeScreenActivity.class);
        }
        startActivity(intent);
        return false;
    }
}
