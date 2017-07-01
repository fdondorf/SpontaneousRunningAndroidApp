package org.spontaneous.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.spontaneous.R;

/**
 * Created by fdondorf on 01.04.2017.
 */

public class MyProfileFragment extends PreferenceFragment {

    public static final int NESTED_SCREEN_1_KEY = 1; // My Profile

    private static final String TAG_KEY = "NESTED_KEY";

    public static MyProfileFragment newInstance(int key) {
        MyProfileFragment fragment = new MyProfileFragment();

        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPreferenceResource();
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        switch (key) {
            case NESTED_SCREEN_1_KEY:
                addPreferencesFromResource(R.xml.myprofile);
                break;
            default:
                break;
        }
    }
}
