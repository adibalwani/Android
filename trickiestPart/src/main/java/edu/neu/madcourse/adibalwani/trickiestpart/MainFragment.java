package edu.neu.madcourse.adibalwani.trickiestpart;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trickiest_part_fragment_main, container, false);
        initButtonListener(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initInstances();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.trickiest_part_detect_dribble_button) {
            detectDribble();
        } else if (resourceId == R.id.trickiest_part_quit_button) {
            endActivity();
        } else if (resourceId == R.id.trickiest_part_detect_shoot_button) {
            detectShoot();
        }
    }

    /**
     * Initialize the global instances
     */
    private void initInstances() {
        mActivity = getActivity();
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.trickiest_part_detect_dribble_button).setOnClickListener(this);
        view.findViewById(R.id.trickiest_part_quit_button).setOnClickListener(this);
        view.findViewById(R.id.trickiest_part_detect_shoot_button).setOnClickListener(this);
    }

    /**
     * Start the detect dribble activity
     */
    private void detectDribble() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.trickiestpart.DribbleActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Start the detect dribble activity
     */
    private void detectShoot() {
        Intent intent = new Intent(mActivity,
                edu.neu.madcourse.adibalwani.trickiestpart.ShootActivity.class);
        mActivity.startActivity(intent);
    }

    /**
     * Handle Quit by killing the current Activity
     */
    private void endActivity() {
        mActivity.finish();
    }
}
