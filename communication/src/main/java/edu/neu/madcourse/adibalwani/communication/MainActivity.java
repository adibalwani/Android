package edu.neu.madcourse.adibalwani.communication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communication_activity_main);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.communication_titlebar_name));
    }
}
