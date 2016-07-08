package edu.neu.madcourse.adibalwani;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;


public class FinalProjectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String appDescription =
            "<h1><center>Virtual Basketball</center></h1>" +
            "                                                                        <p>\n" +
            "Grab your phone and enjoy the real-life basketball experience like never before!\n" +
            "</p>\n" +
            "                                                                        <p>\n" +
            "    Packed with awesome features, Virtual Basketball keeps your innate sportsmen alive. Score as many points as possible without missing a shot. Check out the leaderboard to see how you rank.\n" +
            "</p>\n" +
            "                                                                        <p>How to Play:</p>\n" +
            "                                                                        <ol>\n" +
            "                                                                            <li>Move the phone in upward and downward motion as one would dribble a basketball as directed by the game</li>\n" +
            "                                                                            <li>Vibration on the phone would notify when to take the shot</li>\n" +
            "                                                                            <li>Take the shot with appropriate delay as directed by the game</li>\n" +
            "                                                                        </ol>\n" +
            "                                                                        <p>Features:</p>\n" +
            "                                                                        <ul>\n" +
            "                                                                            <li>Leaderboard to see how you rank</li>\n" +
            "                                                                            <li>Measure your game performance by checking out your scores</li>\n" +
            "                                                                            <li>Realistic feel with increased challenges</li>\n" +
            "                                                                        </ul>\n" +
            "                                                                        <p>Tap and download now!</p>\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_project);

        // Set ActionBar label
        this.setTitle(getResources().getString(R.string.final_project_activity));

        WebView desc = (WebView) findViewById(R.id.app_description);
        desc.loadData(appDescription, "text/html", "UTF-8");

        Button ackn = (Button) findViewById(R.id.acknowledgement_button);
        ackn.setOnClickListener(this);

        Button finalProject = (Button) findViewById(R.id.start_final_project_button);
        finalProject.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acknowledgement_button:
                displayAcknowledgementDialog();
                break;
            case R.id.start_final_project_button:
                Intent intent = new Intent(this,
                        edu.neu.madcourse.adibalwani.finalproject.MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * Display acknowledgement dialog box containing the layout
     */
    private void displayAcknowledgementDialog() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment =
                AckDialogFragment.newInstance(R.layout.dialog_acknowledgement);
        dialogFragment.show(fragmentTransaction, "ACKNOWLEDGEMENT_DIALOG");
    }

}
