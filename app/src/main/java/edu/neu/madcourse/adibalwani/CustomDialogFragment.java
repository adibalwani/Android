package edu.neu.madcourse.adibalwani;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class CustomDialogFragment extends DialogFragment {
    public static final String BUNDLE_LAYOUT_ID = "1";
    public static final String BUNDLE_DEVICE_ID = "2";

    private Activity mActivity;
    private int mLayoutId;
    private String mDeviceId;

    /**
     * Create new instance of EndGameDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @return Instance of EndGameDialogFragment
     */
    static CustomDialogFragment newInstance(int layoutId) {
        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    /**
     * Create new instance of EndGameDialogFragment, providing layoutId
     * as an argument
     *
     * @param layoutId The id of Layout to display in dialog
     * @param deviceId The device Id to print
     * @return Instance of EndGameDialogFragment
     */
    static CustomDialogFragment newInstance(int layoutId, String deviceId) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_LAYOUT_ID, layoutId);
        args.putString(BUNDLE_DEVICE_ID, deviceId);

        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mLayoutId = getArguments().getInt(BUNDLE_LAYOUT_ID);
        mDeviceId = getArguments().getString(BUNDLE_DEVICE_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = inflater.inflate(mLayoutId, null);

        // Set Device Id if provided
        if (mDeviceId != null) {
            TextView textView = (TextView) view.findViewById(R.id.about_device_id);
            textView.setText(mDeviceId);
        }

        builder.setView(view)
                .setPositiveButton(R.string.about_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
