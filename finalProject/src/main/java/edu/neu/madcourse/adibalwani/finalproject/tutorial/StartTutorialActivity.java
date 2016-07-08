package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import edu.neu.madcourse.adibalwani.finalproject.R;

public class StartTutorialActivity extends AppCompatActivity {

    private FragmentManager frag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tutorial);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        frag = getFragmentManager();
        frag.beginTransaction().replace(R.id.tutorial_fragment, new TutorialVideo()).commit();
    }
}
