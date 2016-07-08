package edu.neu.madcourse.adibalwani.dictionary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictionary_activity_main);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.dictionary_titlebar_name));
    }

}
