package edu.neu.madcourse.adibalwani.finalproject.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import edu.neu.madcourse.adibalwani.finalproject.R;
import edu.neu.madcourse.adibalwani.finalproject.TextViewFont;

/**
 * @author rachit on 19-04-2016.
 */
public class ShootGuide extends Fragment implements View.OnClickListener {

    private Animation in;
    private Animation out;
    private Animation mediumcenter;
    private Animation slowcenter;
    private Animation fastcenter;

    private ImageView handmovement;
    private ImageButton rightNav;
    private ImageButton leftNav;
    private Button letsTry;
    private TextSwitcher textSwitcher;
    private LinearLayout rootView;
    private FragmentManager frag = null;

    private String[] shootTypes = {"MEDIUM SHOT", "SLOW SHOT", "FAST SHOT"};
    private int currentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_shoot, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        rootView = (LinearLayout) view.findViewById(R.id.root_view_fragment_tutorial_shoot);
        rightNav = (ImageButton) view.findViewById(R.id.right_nav);
        rightNav.setOnClickListener(this);
        leftNav = (ImageButton) view.findViewById(R.id.left_nav);
        leftNav.setOnClickListener(this);
        letsTry = (Button) view.findViewById(R.id.tutorial_shoot_lets_try);
        letsTry.setOnClickListener(this);
        textSwitcher = (TextSwitcher) view.findViewById(R.id.tutorial_shoot_types);
        handmovement = (ImageView) view.findViewById(R.id.tutorial_shoot_hand_movement);
        TextViewFont font = new TextViewFont();
        font.overrideFonts(getActivity().getApplicationContext(), rootView);
        loadAnimations();
    }

    private void loadAnimations() {
        in = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_in_left);
        out = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_out_right);
        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // create new textView and set the properties like color, size etc
                TextView myText = new TextView(getActivity());
                myText.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(20);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });
        textSwitcher.setText(shootTypes[0]);
        changeAnimation(0);

    }

    private void changeAnimation(int value) {
        handmovement.clearAnimation();
        mediumcenter = AnimationUtils.loadAnimation(getActivity(), R.anim.mediumcenter);
        slowcenter = AnimationUtils.loadAnimation(getActivity(), R.anim.slowcenter);
        fastcenter = AnimationUtils.loadAnimation(getActivity(), R.anim.fastcenter);
        //fastcenter.setStartOffset(3000);
        fastcenter.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.i("RACHIT",  "animation start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                long val = fastcenter.getStartOffset();
                Log.i("RACHIT",  "offset is :" +val);
                fastcenter.setRepeatCount(1);
                fastcenter.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                long val = fastcenter.getStartOffset();
                Log.i("RACHIT",  "value is :" +val);
            }
        });
        switch (value) {
            case 0:
                handmovement.setAnimation(mediumcenter);
                break;
            case 1:
                handmovement.setAnimation(slowcenter);
                break;
            case 2:
                handmovement.setAnimation(fastcenter);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();
        if (resourceId == R.id.tutorial_shoot_lets_try) {
            frag = getFragmentManager();
            frag.beginTransaction().replace(R.id.tutorial_fragment, new TutorialShoot()).commit();
        } else if (resourceId == R.id.right_nav) {
            currentIndex++;
            if (currentIndex == 3) {
                currentIndex = 0;
            }
            textSwitcher.setText(shootTypes[currentIndex]);
            changeAnimation(currentIndex);
        } else if (resourceId == R.id.left_nav) {
            currentIndex--;
            if (currentIndex == -1) {
                currentIndex = 2;
            }
            textSwitcher.setText(shootTypes[currentIndex]);
            changeAnimation(currentIndex);
        }
    }
}
