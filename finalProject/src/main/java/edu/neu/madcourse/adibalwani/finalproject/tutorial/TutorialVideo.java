package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import edu.neu.madcourse.adibalwani.finalproject.R;

/**
 * @author rachit on 22-04-2016.
 */
public class TutorialVideo extends Fragment implements View.OnClickListener {

    private FragmentManager frag = null;
    private TextView skip;
    private VideoView videoView;
    private ImageButton playagain;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_video, container, false);
        skip = (TextView) view.findViewById(R.id.tutorial_video_skip_button);
        skip.setOnClickListener(this);
        playagain = (ImageButton) view.findViewById(R.id.play_button);
        playagain.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        videoView = (VideoView)view.findViewById(R.id.tutorial_video);
        float density = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        if (density >= 2.0) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() +"/"+R.raw.tutorial_video));
        } else {
            videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() +"/"+R.raw.tutorial_video_ldpi));
        }
        videoView.setMediaController(new MediaController(getActivity()));
        videoView.requestFocus();
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playagain.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.tutorial_video_skip_button) {
            frag = getFragmentManager();
            frag.beginTransaction().replace(R.id.tutorial_fragment, new TutorialHoldPhone()).commit();
        } else if (resourceId == R.id.play_button) {
            videoView.start();
            playagain.setVisibility(View.INVISIBLE);
        }
    }
}
