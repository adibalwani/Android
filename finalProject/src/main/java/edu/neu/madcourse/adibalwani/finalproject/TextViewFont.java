package edu.neu.madcourse.adibalwani.finalproject;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by rachit on 23-04-2016.
 */
public class TextViewFont {

    /*private Context context;
    private View mView;

    TextViewFont(Context c, View v) {
        context = c;
        mView = v;
    }*/

    public void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/comicSans.ttf"));
            }
        } catch (Exception e) {
        }
    }
}
