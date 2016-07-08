package edu.neu.madcourse.adibalwani.trickiestpart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trickiest_part_activity_main);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.trickiest_part_titlebar_name));
    }
}
