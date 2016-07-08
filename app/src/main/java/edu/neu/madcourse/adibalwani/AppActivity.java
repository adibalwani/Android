package edu.neu.madcourse.adibalwani;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.titlebar_name));
    }
}
